package moe.shizuku.manager.worker

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.AdbStarter
import moe.shizuku.manager.receiver.SheveryControlReceiver
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.service.SheveryNotificationManager
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.ShizukuStateMachine
import java.io.EOFException
import java.util.concurrent.TimeoutException

class AdbStartWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            ShizukuReceiverStarter.updateNotification(
                applicationContext,
                ShizukuReceiverStarter.WorkerState.RUNNING
            )

            val cr = applicationContext.contentResolver

            Settings.Global.putInt(cr, Settings.Global.ADB_ENABLED, 1)
            Settings.Global.putLong(cr, "adb_allowed_connection_time", 0L)

            val tcpPort = EnvironmentUtils.getAdbTcpPort()
            // Note: thedjchi calls stopTcp() here to clear stale TCP connections,
            // but shevery doesn't have that method. AdbStarter.start() will handle
            // TCP mode switching internally via connectWithRetry.

            val port = if (!EnvironmentUtils.isWifiRequired()) {
                tcpPort
            } else {
                // mDNS discovery + content observer pattern from thedjchi
                callbackFlow {
                    val adbMdns = AdbMdns(applicationContext, AdbMdns.TLS_CONNECT) { port ->
                        if (port > 0) trySend(port)
                    }

                    var awaitingAuth = false
                    var timeoutJob: Job? = null
                    var unlockReceiver: BroadcastReceiver? = null

                    fun startDiscoveryWithTimeout() {
                        adbMdns.start()
                        timeoutJob?.cancel()
                        timeoutJob = this.launch {
                            delay(15_000)
                            close(TimeoutException("Timed out during mDNS port discovery"))
                        }
                    }

                    fun handleAuth() {
                        val km = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if (km.isKeyguardLocked) {
                            val notification = ShizukuReceiverStarter.buildNotification(
                                applicationContext,
                                null
                            )
                            val foregroundInfo = ForegroundInfo(
                                ShizukuReceiverStarter.NOTIFICATION_ID,
                                notification
                            )
                            setForegroundAsync(foregroundInfo)

                            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
                            unlockReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    if (intent.action == Intent.ACTION_USER_PRESENT) {
                                        context.unregisterReceiver(this)
                                        unlockReceiver = null
                                        Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
                                    }
                                }
                            }
                            applicationContext.registerReceiver(unlockReceiver, filter)
                        } else {
                            awaitingAuth = true
                        }
                        timeoutJob?.cancel()
                        adbMdns.stop()
                    }

                    val observer = object : ContentObserver(null) {
                        override fun onChange(selfChange: Boolean) {
                            when (Settings.Global.getInt(cr, "adb_wifi_enabled", 0)) {
                                0 -> if (awaitingAuth) {
                                    close(SecurityException("Network is not authorized for wireless debugging"))
                                } else {
                                    handleAuth()
                                }
                                1 -> startDiscoveryWithTimeout()
                            }
                        }
                    }

                    Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
                    cr.registerContentObserver(
                        Settings.Global.getUriFor("adb_wifi_enabled"),
                        false,
                        observer
                    )
                    startDiscoveryWithTimeout()

                    awaitClose {
                        adbMdns.stop()
                        timeoutJob?.cancel()
                        cr.unregisterContentObserver(observer)
                        unlockReceiver?.let {
                            try {
                                applicationContext.unregisterReceiver(it)
                            } catch (_: Exception) {}
                        }
                    }
                }.first()
            }

            AdbStarter.start("127.0.0.1", port, applicationContext)
            Starter.waitForBinder()

            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(ShizukuReceiverStarter.NOTIFICATION_ID)

            return Result.success()
        } catch (e: CancellationException) {
            val state = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                ShizukuReceiverStarter.WorkerState.AWAITING_RETRY
            } else {
                when (stopReason) {
                    WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY -> ShizukuReceiverStarter.WorkerState.AWAITING_WIFI
                    WorkInfo.STOP_REASON_CANCELLED_BY_APP -> ShizukuReceiverStarter.WorkerState.STOPPED
                    else -> ShizukuReceiverStarter.WorkerState.AWAITING_RETRY
                }
            }
            ShizukuReceiverStarter.updateNotification(applicationContext, state)
            throw e
        } catch (e: Exception) {
            val ignored = listOf(
                EOFException::class,
                SecurityException::class,
                TimeoutException::class
            )
            if (ignored.none { it.isInstance(e) }) {
                showErrorNotification(applicationContext, e)
            }

            if (ShizukuStateMachine.update() == ShizukuStateMachine.State.RUNNING) {
                return Result.success()
            } else {
                ShizukuReceiverStarter.updateNotification(
                    applicationContext,
                    ShizukuReceiverStarter.WorkerState.AWAITING_RETRY
                )
                return Result.retry()
            }
        }
    }

    private fun showErrorNotification(context: Context, e: Exception) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ShizukuReceiverStarter.CHANNEL_ID,
                context.getString(R.string.wadb_notification_title),
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val msgNotif = "$e. ${context.getString(R.string.wadb_error_notify_dev)}"

        val intent = Intent(context, SheveryControlReceiver::class.java).apply {
            action = SheveryControlReceiver.ACTION_START_SERVER
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ShizukuReceiverStarter.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_system_icon)
            .setContentTitle(context.getString(R.string.wadb_error_title))
            .setContentText(msgNotif)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msgNotif))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ShizukuReceiverStarter.NOTIFICATION_ID, notification)
    }

    companion object {
        fun enqueue(context: Context) {
            val cb = Constraints.Builder()
            if (EnvironmentUtils.isWifiRequired()) {
                cb.setRequiredNetworkType(NetworkType.UNMETERED)
            }
            val constraints = cb.build()

            val request = OneTimeWorkRequestBuilder<AdbStartWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "adb_start_worker",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        const val CHANNEL_ID = "AdbStartWorker"
        const val NOTIFICATION_ID = 1448
    }
}
