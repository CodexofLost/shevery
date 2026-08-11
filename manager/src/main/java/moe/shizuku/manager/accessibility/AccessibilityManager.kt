package moe.shizuku.manager.accessibility

import android.content.Context
import android.os.ParcelFileDescriptor
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import moe.shizuku.manager.ktx.logd
import moe.shizuku.manager.ktx.logw
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

/**
 * Core controller for accessibility service management.
 *
 * All reads and writes go through the Shizuku shell:
 *  - `settings get secure enabled_accessibility_services` — ground truth, never
 *    filtered by Android 11+ package visibility (an app-process read of
 *    [Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES] IS filtered, which is why
 *    states appeared wrong for most services).
 *  - `settings put secure ...` — shell owns WRITE_SECURE_SETTINGS.
 *
 * Every read-modify-write cycle is serialized by [lock] so rapid toggles can
 * never clobber each other (a previous toggle must not be reverted by a
 * concurrent one).
 */
object AccessibilityManager {

    private const val TAG = "AccessibilityManager"
    private const val SETTINGS_KEY = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    private const val SHELL_TIMEOUT_SECONDS = 10L

    /** Serializes every read-modify-write so toggles can't race each other. */
    private val lock = Mutex()

    /**
     * Set just before a shell write and cleared by the daemon's ContentObserver
     * when it fires for that write, so the daemon never re-restores the services
     * it (or the user, via this object) just wrote. Mirrors the reference app's
     * isSelfModification flag.
     */
    @Volatile
    private var selfWritePending = false

    fun isSelfWrite(): Boolean = selfWritePending

    fun clearSelfWrite() {
        selfWritePending = false
    }

    /** Current enabled services, read through the Shizuku shell (unfiltered). */
    suspend fun getEnabledServices(context: Context): Set<String> = lock.withLock {
        readEnabledServices(context)
    }

    /** Enable a service, atomically: read fresh → add → write. */
    suspend fun enableService(context: Context, serviceId: String): Boolean = lock.withLock {
        val enabled = readEnabledServices(context).toMutableSet()
        if (!enabled.add(serviceId)) return@withLock true
        writeEnabledServices(context, enabled)
    }

    /** Disable a service, atomically: read fresh → remove → write. */
    suspend fun disableService(context: Context, serviceId: String): Boolean = lock.withLock {
        val enabled = readEnabledServices(context).toMutableSet()
        if (!enabled.remove(serviceId)) return@withLock true
        writeEnabledServices(context, enabled)
    }

    /**
     * Re-enable every pinned-but-missing service. Returns the list that was
     * restored (empty if nothing needed restoring or the write failed).
     */
    suspend fun restorePinnedServices(context: Context): List<String> = lock.withLock {
        val pinned = AccessibilityKeepAliveStore.getKeepAliveIds()
        if (pinned.isEmpty()) return@withLock emptyList()

        val enabled = readEnabledServices(context).toMutableSet()
        val missing = pinned.filter { it !in enabled }
        if (missing.isEmpty()) return@withLock emptyList()

        enabled.addAll(missing)
        if (writeEnabledServices(context, enabled)) {
            logd("Restored pinned accessibility services: $missing")
            return@withLock missing
        }
        logw("Failed to restore pinned accessibility services: $missing")
        emptyList()
    }

    // ---------------------------------------------------------------------
    // Shell plumbing
    // ---------------------------------------------------------------------

    /** Reads the raw enabled list via `settings get secure` (never filtered). */
    private suspend fun readEnabledServices(context: Context): Set<String> {
        val result = runShizukuShell("settings get secure $SETTINGS_KEY") ?: return emptySet()
        val value = result.trim()
        if (value.isEmpty() || value == "null") return emptySet()
        return value.split(':').filter { it.isNotEmpty() }.toSet()
    }

    /** Writes the full enabled list via `settings put secure`. */
    private suspend fun writeEnabledServices(context: Context, services: Set<String>): Boolean {
        if (!Shizuku.pingBinder()) {
            logw("Cannot write accessibility services: Shizuku not running")
            return false
        }
        val value = services.sorted().joinToString(":")
        val cmd = "settings put secure $SETTINGS_KEY ${shellQuote(value)}"
        selfWritePending = true
        val result = runShizukuShell(cmd)
        if (result == null) {
            selfWritePending = false
            return false
        }
        logd("Wrote enabled accessibility services (${services.size}): $cmd")
        return true
    }

    /**
     * Runs a shell command through Shizuku and returns its stdout, or null on
     * failure. Command is executed via `sh -c` so quoting works as expected.
     */
    private suspend fun runShizukuShell(cmd: String): String? = withContext(Dispatchers.IO) {
        if (!Shizuku.pingBinder()) {
            logw("Cannot run shell command: Shizuku not running")
            return@withContext null
        }
        try {
            val binder = Shizuku.getBinder() ?: return@withContext null
            val service = IShizukuService.Stub.asInterface(binder)
            val remote = service.newProcess(arrayOf("sh", "-c", cmd), null, null)

            val stdoutPfd = remote.getInputStream()
            val stderrPfd = remote.getErrorStream()

            var stdoutText = ""
            var stderrText = ""
            val stdoutThread = Thread {
                try {
                    stdoutText = readStream(stdoutPfd)
                } catch (ignore: Exception) {
                }
            }
            val stderrThread = Thread {
                try {
                    stderrText = readStream(stderrPfd)
                } catch (ignore: Exception) {
                }
            }
            stdoutThread.start()
            stderrThread.start()

            val finished = remote.waitForTimeout(SHELL_TIMEOUT_SECONDS, TimeUnit.SECONDS.name)
            val exitCode = if (finished) {
                remote.exitValue()
            } else {
                logw("Shell command timed out: $cmd")
                remote.destroy()
                -1
            }
            stdoutThread.join(2_000)
            stderrThread.join(2_000)

            if (exitCode != 0) {
                logw("Shell command failed (exit $exitCode): $cmd ${stderrText.take(300)}")
                return@withContext null
            }
            stdoutText.trim()
        } catch (e: Exception) {
            logw("Shell command exception: ${e.message}")
            null
        }
    }

    /** Reads the full stdout of a ParcelFileDescriptor. */
    private fun readStream(pfd: ParcelFileDescriptor): String {
        return ParcelFileDescriptor.AutoCloseInputStream(pfd).reader(Charsets.UTF_8).use { reader ->
            val buffer = CharArray(8192)
            val sb = StringBuilder()
            while (true) {
                val read = reader.read(buffer)
                if (read <= 0) break
                sb.append(buffer, 0, read)
                if (sb.length > 64 * 1024) break
            }
            sb.toString()
        }
    }

    /** Single-quotes a value for safe use inside `sh -c`. */
    private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"
}
