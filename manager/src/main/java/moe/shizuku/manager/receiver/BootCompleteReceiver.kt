package moe.shizuku.manager.receiver

import android.Manifest.permission.ACCESS_LOCAL_NETWORK
import android.Manifest.permission.NEARBY_WIFI_DEVICES
import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import moe.shizuku.manager.adb.AdbStarter
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.service.StartupNotificationManager
import moe.shizuku.manager.utils.UserHandleCompat
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class BootCompleteReceiver : BroadcastReceiver() {

    companion object {
        // Re-entrancy guard: LOCKED_BOOT_COMPLETED and BOOT_COMPLETED both fire.
        // Prevent two concurrent adbStart() calls fighting over adb_wifi_enabled.
        private val adbStarting = AtomicBoolean(false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED != intent.action
            && Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }

        if (UserHandleCompat.myUserId() > 0 || Shizuku.pingBinder()) return

        if (ShizukuSettings.getStartOnBootAdb()
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && context.checkSelfPermission(WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        ) {
            if (hasLocalNetworkPermission(context)) {
                adbStart(context)
            } else {
                // Can't request runtime permissions from a boot receiver.
                // Notify the user so the failure isn't silent.
                Log.w(
                    AppConstants.TAG,
                    "Start-on-boot ADB skipped: missing local network permission " +
                            "(NEARBY_WIFI_DEVICES on 33+, ACCESS_LOCAL_NETWORK on 37+)"
                )
                StartupNotificationManager.showFailed(
                    context,
                    context.getString(R.string.notification_startup_no_permission)
                )
            }
        } else if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            rootStart(context)
        } else {
            Log.w(AppConstants.TAG, "No support start on boot")
        }
    }

    /**
     * mDNS discovery (NsdManager) needs NEARBY_WIFI_DEVICES on API 33+ and
     * ACCESS_LOCAL_NETWORK on API 37+. Without them, discovery silently
     * produces no results and boot start fails with a confusing timeout.
     */
    private fun hasLocalNetworkPermission(context: Context): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM -> // API 37
            context.checkSelfPermission(ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> // API 33
            context.checkSelfPermission(NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        else -> true
    }

    private fun rootStart(context: Context) {
        if (!Shell.getShell().isRoot) {
            //NotificationHelper.notify(context, AppConstants.NOTIFICATION_ID_STATUS, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_no_root)
            Shell.getCachedShell()?.close()
            return
        }

        Shell.cmd(Starter.internalCommand).exec()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun adbStart(context: Context) {
        // Re-entrancy guard: if LOCKED_BOOT_COMPLETED already started the ADB
        // boot flow, don't start a second one from BOOT_COMPLETED.
        if (!adbStarting.compareAndSet(false, true)) {
            Log.i(AppConstants.TAG, "adbStart already in progress, skipping")
            return
        }

        val pending: BroadcastReceiver.PendingResult
        val cr = context.contentResolver
        try {
            StartupNotificationManager.showProgress(
                context,
                context.getString(R.string.notification_startup_enabling_wifi)
            )
            Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
            Settings.Global.putInt(cr, Settings.Global.ADB_ENABLED, 1)
            pending = goAsync()
        } catch (e: Exception) {
            // Synchronous failure before coroutine launches — reset guard.
            Log.w(AppConstants.TAG, "adbStart synchronous failure", e)
            adbStarting.set(false)
            StartupNotificationManager.showFailed(
                context,
                context.getString(R.string.notification_startup_failed)
            )
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                suspend fun waitForServer(maxMs: Long): Boolean {
                    var running = Shizuku.pingBinder()
                    var waited = 0L
                    while (!running && waited < maxMs) {
                        delay(250)
                        waited += 250
                        running = Shizuku.pingBinder()
                    }
                    return running
                }

                val portFound = CountDownLatch(1)
                val startDone = CountDownLatch(1)
                val alreadyConnecting = AtomicBoolean(false)
                var lastError: String? = null
                val adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { port ->
                    if (port <= 0) return@AdbMdns
                    // NsdManager can fire onServiceResolved multiple times for
                    // cached mDNS records. Only start the connection sequence once.
                    if (!alreadyConnecting.compareAndSet(false, true)) return@AdbMdns
                    portFound.countDown()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            StartupNotificationManager.showProgress(
                                context,
                                context.getString(R.string.notification_startup_connecting)
                            )
                            AdbStarter.start(
                                port = port,
                                context = context.applicationContext,
                                listener = { errorMsg -> lastError = String(errorMsg) }
                            )
                            if (waitForServer(3000)) {
                                StartupNotificationManager.showStarted(
                                    context,
                                    context.getString(R.string.notification_startup_started)
                                )
                            } else {
                                val detail = lastError?.takeIf { it.isNotBlank() }
                                StartupNotificationManager.showFailed(
                                    context,
                                    context.getString(R.string.notification_startup_failed) +
                                            if (detail != null) "\n$detail" else ""
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(AppConstants.TAG, "ADB boot start failed", e)
                            if (waitForServer(5000)) {
                                StartupNotificationManager.showStarted(
                                    context,
                                    context.getString(R.string.notification_startup_started)
                                )
                            } else {
                                val detail = lastError?.takeIf { it.isNotBlank() }
                                        ?: e.message?.takeIf { it.isNotBlank() }
                                StartupNotificationManager.showFailed(
                                    context,
                                    context.getString(R.string.notification_startup_failed) +
                                            if (detail != null) "\n$detail" else ""
                                )
                            }
                        } finally {
                            startDone.countDown()
                        }
                    }
                }
                adbMdns.start()
                var waited = 0L
                val step = 3_000L
                val deadline = 12_000L
                while (!portFound.await(step, TimeUnit.MILLISECONDS) && waited < deadline) {
                    waited += step
                    // Force adbd to re-announce the wireless debugging port over mDNS.
                    // Toggle off, wait 1.5s for adbd to tear down, then re-enable.
                    Settings.Global.putInt(cr, "adb_wifi_enabled", 0)
                    delay(1500)
                    Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
                }
                adbMdns.stop()
                if (portFound.await(0, TimeUnit.MILLISECONDS)) {
                    startDone.await(10, TimeUnit.SECONDS)
                } else {
                    StartupNotificationManager.showFailed(
                        context,
                        context.getString(R.string.notification_startup_no_port)
                    )
                }
            } finally {
                // Reset re-entrancy guard AFTER async work completes.
                adbStarting.set(false)
                try {
                    pending.finish()
                } catch (e: IllegalStateException) {
                    // Broadcast was already recycled (timeout) — ignore.
                }
            }
        }
    }
}
