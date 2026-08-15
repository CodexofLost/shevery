package moe.shizuku.manager.receiver

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

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED != intent.action
            && Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }

        if (UserHandleCompat.myUserId() > 0 || Shizuku.pingBinder()) return

        if (ShizukuSettings.getStartOnBootAdb() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && context.checkSelfPermission(WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            adbStart(context)
        } else if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            rootStart(context)
        } else {
            Log.w(AppConstants.TAG, "No support start on boot")
        }
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
        StartupNotificationManager.showProgress(
            context,
            context.getString(R.string.notification_startup_enabling_wifi)
        )
        val cr = context.contentResolver
        Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
        Settings.Global.putInt(cr, Settings.Global.ADB_ENABLED, 1)
        Settings.Global.putLong(cr, "adb_allowed_connection_time", 0L)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
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
            var lastError: String? = null
            val adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { port ->
                if (port <= 0) return@AdbMdns
                portFound.countDown()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        StartupNotificationManager.showProgress(
                            context,
                            context.getString(R.string.notification_startup_connecting)
                        )
                        AdbStarter.start(port = port, context = context.applicationContext) {
                            lastError = it
                        }
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
            val deadline = 20_000L
            while (!portFound.await(step, TimeUnit.MILLISECONDS) && waited < deadline) {
                waited += step
                // Force adbd to re-announce the wireless debugging port over mDNS.
                Settings.Global.putInt(cr, "adb_wifi_enabled", 0)
                Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
            }
            adbMdns.stop()
            if (portFound.await(0, TimeUnit.MILLISECONDS)) {
                startDone.await(20, TimeUnit.SECONDS)
            } else {
                StartupNotificationManager.showFailed(
                    context,
                    context.getString(R.string.notification_startup_no_port)
                )
            }
            pending.finish()
        }
    }
}
