package moe.shizuku.manager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import moe.shizuku.manager.MainActivity
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ktx.logd
import moe.shizuku.manager.ktx.logi
import moe.shizuku.manager.ktx.logw
import moe.shizuku.manager.module.ModuleSettings
import moe.shizuku.manager.utils.ShizukuStateMachine
import moe.shizuku.server.IShizukuService

class WatchdogService : Service() {

    private var zombieProtectJob: Job? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    // Infrequent check (60s) solely to detect "zombie" binders where the process
    // is running and answers pingBinder(), but IPC transactions fail/hang.
    // Process death is detected immediately (0ms) by ShizukuStateMachine listener.
    private val zombieCheckIntervalMs = 60_000L

    private val stateListener: (ShizukuStateMachine.State) -> Unit = { state ->
        if (state == ShizukuStateMachine.State.CRASHED) {
            logw("WatchdogService: observed CRASHED state from ShizukuStateMachine")
            serviceScope.launch {
                handleCrashState()
            }
        }
    }

    private val binderReceivedListener = object : rikka.shizuku.Shizuku.OnBinderReceivedListener {
        override fun onBinderReceived() {
            startAsForeground()
        }
    }

    private val binderDeadListener = object : rikka.shizuku.Shizuku.OnBinderDeadListener {
        override fun onBinderDead() {
            startAsForeground()
        }
    }

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true
        WatchdogManager.init(applicationContext)
        ShizukuStateMachine.addListener(stateListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            logi("WatchdogService: received ACTION_STOP_SERVICE from notification action")
            ModuleSettings.setWatchdogEnabled(false)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!WatchdogManager.shouldRunService()) {
            stopSelf()
            return START_NOT_STICKY
        }

        _isRunning.value = true
        startAsForeground()
        startZombieProtectLoop()

        rikka.shizuku.Shizuku.removeBinderReceivedListener(binderReceivedListener)
        rikka.shizuku.Shizuku.removeBinderDeadListener(binderDeadListener)
        rikka.shizuku.Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        rikka.shizuku.Shizuku.addBinderDeadListener(binderDeadListener)
        return START_STICKY
    }

    override fun onDestroy() {
        ShizukuStateMachine.removeListener(stateListener)
        rikka.shizuku.Shizuku.removeBinderReceivedListener(binderReceivedListener)
        rikka.shizuku.Shizuku.removeBinderDeadListener(binderDeadListener)
        stopZombieProtectLoop()
        serviceJob.cancel()
        _isRunning.value = false
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Foreground service persists independently of task recents.
    }

    private suspend fun handleCrashState() {
        if (!WatchdogManager.shouldRunService()) {
            logd("WatchdogService: watchdog disabled, skipping crash restart")
            return
        }
        if (WatchdogManager.isExpectingDeathActive()) {
            logd("WatchdogService: death is expected or starter active, skipping crash restart")
            return
        }
        if (WatchdogManager.isUserStopRequested()) {
            logi("WatchdogService: stop was user-initiated, skipping crash restart")
            return
        }
        if (ShizukuSettings.getLastLaunchMode() == ShizukuSettings.LaunchMethod.UNKNOWN) {
            logd("WatchdogService: server never started (UNKNOWN mode), skipping crash restart")
            return
        }

        withContext(Dispatchers.IO) {
            logw("WatchdogService: reactive restart triggered following daemon crash")
            WatchdogManager.attemptRestart(applicationContext)
        }
    }

    private data class HealthResult(
        val healthy: Boolean,
        val reason: String,
        val binderAlive: Boolean
    )

    private fun checkHealth(): HealthResult {
        try {
            val binder = rikka.shizuku.Shizuku.getBinder()
                ?: return HealthResult(false, "binder is null", false)
            val ping = try {
                rikka.shizuku.Shizuku.pingBinder() && binder.pingBinder()
            } catch (e: Throwable) {
                false
            }
            if (!ping) {
                return HealthResult(false, "pingBinder() failed", false)
            }
            // Real remote transaction to verify the daemon process is responsive
            val service = IShizukuService.Stub.asInterface(binder)
            val version = try {
                service.version
            } catch (e: Throwable) {
                return HealthResult(false, "binder transaction failed: ${e.javaClass.simpleName}", true)
            }
            if (version <= 0) {
                return HealthResult(false, "bad remote version=$version", true)
            }
            return HealthResult(true, "ok version=$version", true)
        } catch (e: Throwable) {
            return HealthResult(false, "check threw ${e.javaClass.simpleName}: ${e.message}", rikka.shizuku.Shizuku.pingBinder())
        }
    }

    private fun startZombieProtectLoop() {
        zombieProtectJob?.cancel()
        zombieProtectJob = null
        if (!ModuleSettings.isWatchdogEnabled()) return

        zombieProtectJob = serviceScope.launch {
            var consecutiveFailures = 0
            logi("WatchdogService: zombie protection monitoring started (interval=${zombieCheckIntervalMs}ms)")
            while (isActive) {
                delay(zombieCheckIntervalMs)
                if (!ModuleSettings.isWatchdogEnabled()) break

                val result = checkHealth()
                if (result.healthy) {
                    if (consecutiveFailures > 0) {
                        logi("WatchdogService: service healthy (${result.reason})")
                    }
                    consecutiveFailures = 0
                } else {
                    consecutiveFailures++
                    logw("WatchdogService: unhealthy [$consecutiveFailures/$FAILURES_TO_RESTART]: ${result.reason}")
                    if (consecutiveFailures >= FAILURES_TO_RESTART) {
                        consecutiveFailures = 0
                        handleUnhealthy(result)
                        if (!isActive) break
                    }
                }
            }
            logi("WatchdogService: zombie protection monitoring stopped")
        }
    }

    private suspend fun handleUnhealthy(result: HealthResult) {
        if (ShizukuSettings.getLastLaunchMode() == ShizukuSettings.LaunchMethod.UNKNOWN) {
            logd("WatchdogService: server never started (UNKNOWN mode), skipping restart")
            return
        }
        if (WatchdogManager.isExpectingDeathActive()) {
            logd("WatchdogService: death is expected or starter active, skipping restart")
            return
        }
        if (WatchdogManager.isUserStopRequested()) {
            logi("WatchdogService: last stop was user-initiated, skipping restart")
            return
        }
        if (!WatchdogManager.shouldRunService()) {
            logd("WatchdogService: watchdog disabled, skipping restart")
            return
        }

        withContext(Dispatchers.IO) {
            if (result.binderAlive) {
                logw("WatchdogService: zombie binder detected (${result.reason}). Stopping before restart...")
                WatchdogManager.requestStopServer(applicationContext, userInitiated = false)
                ShizukuStateMachine.awaitStopped(3_000L)
            } else {
                logw("WatchdogService: binder dead (${result.reason}). Restarting...")
            }
            WatchdogManager.attemptRestart(applicationContext)
        }
    }

    private fun stopZombieProtectLoop() {
        zombieProtectJob?.cancel()
        zombieProtectJob = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        } catch (e: Throwable) {
            logw("WatchdogService: startForeground failed: ${e.message}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                e is android.app.ForegroundServiceStartNotAllowedException
            ) {
                stopSelf()
            }
        }
    }

    private fun buildNotification(): Notification {
        val notificationManager = getSystemService(NotificationManager::class.java)
        ensureChannel(notificationManager)

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val launchPendingIntent = PendingIntent.getActivity(
            this,
            0x7F030001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WatchdogService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0x7F030002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_server_ok_24dp)
            .setContentTitle(getString(R.string.watchdog_service_title))
            .setContentText(getString(R.string.watchdog_service_text))
            .setContentIntent(launchPendingIntent)
            .addAction(
                R.drawable.ic_close_24,
                getString(R.string.watchdog_action_turn_off),
                stopPendingIntent
            )
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun ensureChannel(notificationManager: NotificationManager) {
        if (channelCreated) return
        channelCreated = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_watchdog),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    companion object {
        const val ACTION_STOP_SERVICE = "moe.shizuku.manager.action.STOP_WATCHDOG_SERVICE"
        private const val CHANNEL_ID = "service_watchdog"
        private const val NOTIFICATION_ID = 1004
        private const val FAILURES_TO_RESTART = 2
        private var channelCreated = false

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun reconcile(context: Context) {
            val appContext = context.applicationContext
            if (WatchdogManager.shouldRunService()) {
                start(appContext)
            } else {
                stop(appContext)
            }
        }

        private fun start(context: Context) {
            try {
                val intent = Intent(context, WatchdogService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                logw("Failed to start watchdog service: ${e.message}")
            }
        }

        private fun stop(context: Context) {
            try {
                context.stopService(Intent(context, WatchdogService::class.java))
            } catch (e: Throwable) {
                logw("Failed to stop watchdog service: ${e.message}")
            }
        }
    }
}
