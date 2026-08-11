package moe.shizuku.manager.accessibility

import android.content.ComponentName
import android.content.Context
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import moe.shizuku.manager.ktx.logd
import moe.shizuku.manager.ktx.logi
import moe.shizuku.manager.ktx.logw
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.util.concurrent.TimeUnit

/**
 * Core controller for the Accessibility Manager feature.
 *
 * The enabled-services list lives in
 * [Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES] as a colon-separated
 * string of flattened [ComponentName]s. Reading it requires no permission;
 * writing it requires WRITE_SECURE_SETTINGS. Since Shevery already owns the
 * Shizuku shell (which runs with that permission), writes go through a
 * Shizuku `settings put secure` command instead of requesting the permission
 * for the app process itself.
 */
object AccessibilityManager {

    private const val TAG = "AccessibilityManager"
    private const val SETTINGS_KEY = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    private const val SHELL_TIMEOUT_SECONDS = 10L

    /**
     * Set when this app writes the enabled-services list, so the daemon's
     * ContentObserver can ignore its own change and avoid a restore loop.
     * Mirrors the reference app's isSelfModification flag.
     */
    @Volatile
    private var selfWritePending = false

    fun wasSelfWrite(): Boolean = selfWritePending

    fun clearSelfWrite() {
        selfWritePending = false
    }

    /**
     * Read the set of currently enabled accessibility services.
     * Safe to call from the app process (readable without permission).
     */
    fun getEnabledServices(context: Context): Set<String> {
        val raw = Settings.Secure.getString(context.contentResolver, SETTINGS_KEY) ?: return emptySet()
        return raw.split(":").filter { it.isNotBlank() }.toSet()
    }

    /**
     * Whether a specific service (flattened ComponentName string) is enabled.
     */
    fun isServiceEnabled(context: Context, serviceId: String): Boolean {
        return getEnabledServices(context).contains(serviceId)
    }

    /**
     * Enable a service, preserving all currently enabled services.
     *
     * @return true if the write succeeded
     */
    suspend fun enableService(context: Context, serviceId: String): Boolean {
        val enabled = getEnabledServices(context).toMutableSet()
        if (!enabled.add(serviceId)) return true
        return writeEnabledServices(context, enabled)
    }

    /**
     * Disable a service, preserving all other currently enabled services.
     *
     * @return true if the write succeeded
     */
    suspend fun disableService(context: Context, serviceId: String): Boolean {
        val enabled = getEnabledServices(context).toMutableSet()
        if (!enabled.remove(serviceId)) return true
        return writeEnabledServices(context, enabled)
    }

    /**
     * Overwrite the enabled-services list with exactly [services].
     *
     * @return true if the write succeeded
     */
    suspend fun writeEnabledServices(context: Context, services: Set<String>): Boolean {
        if (!Shizuku.pingBinder()) {
            logw("Cannot write accessibility services: Shizuku not running")
            return false
        }
        val newValue = services.joinToString(":")
        val cmd = "settings put secure $SETTINGS_KEY ${shellQuote(newValue)}"
        selfWritePending = true
        val (exitCode, stderr) = runShizukuShell(cmd)
        if (exitCode == 0) {
            logi("Enabled accessibility services updated: $newValue")
            return true
        }
        selfWritePending = false
        Log.e(TAG, "Failed to write accessibility services (exit $exitCode): $stderr")
        return false
    }

    /**
     * Ensure all pinned services are present in the enabled list.
     * Returns the list of services that were re-enabled, if any.
     */
    suspend fun restorePinnedServices(context: Context): List<String> {
        val pinned = AccessibilityKeepAliveStore.getKeepAliveIds()
        if (pinned.isEmpty()) return emptyList()

        val enabled = getEnabledServices(context).toMutableSet()
        val missing = pinned.filter { it !in enabled }
        if (missing.isEmpty()) return emptyList()

        enabled.addAll(missing)
        if (writeEnabledServices(context, enabled)) {
            logd("Restored pinned accessibility services: $missing")
            return missing
        }
        return emptyList()
    }

    private suspend fun runShizukuShell(cmd: String): Pair<Int, String> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val binder = Shizuku.getBinder()
                    ?: return@withContext -1 to "No binder"
                val service = IShizukuService.Stub.asInterface(binder)
                val process = service.newProcess(arrayOf("sh", "-c", cmd), null, null)

                val stderrPfd = process.getErrorStream()
                val stderrThread = Thread {
                    try {
                        readStream(stderrPfd)
                    } catch (ignore: Exception) {
                        ""
                    }
                }
                stderrThread.start()

                val finished = process.waitForTimeout(SHELL_TIMEOUT_SECONDS, TimeUnit.SECONDS.name)
                val exitCode = if (finished) {
                    process.exitValue()
                } else {
                    process.destroy()
                    124
                }
                stderrThread.join(1000)
                exitCode to ""
            } catch (e: Exception) {
                Log.e(TAG, "Shizuku shell failed", e)
                -1 to (e.message ?: e.toString())
            }
        }
    }

    private fun readStream(pfd: ParcelFileDescriptor): String {
        return ParcelFileDescriptor.AutoCloseInputStream(pfd).reader(Charsets.UTF_8).use { reader ->
            reader.readText().trim()
        }
    }

    private fun shellQuote(value: String): String {
        return "'" + value.replace("'", "'\\''") + "'"
    }
}
