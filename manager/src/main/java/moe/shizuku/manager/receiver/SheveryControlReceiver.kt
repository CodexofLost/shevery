package moe.shizuku.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ktx.logw
import moe.shizuku.manager.service.SheveryNotificationManager
import moe.shizuku.manager.service.WatchdogManager

class SheveryControlReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_START_SERVER = "moe.shizuku.manager.action.START_SERVER"
        const val ACTION_STOP_SERVER = "moe.shizuku.manager.action.STOP_SERVER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START_SERVER -> {
                WatchdogManager.clearUserStopRequest(context.applicationContext)
                // Re-enqueue the constrained worker too: the error
                // notification's tap previously only attempted an immediate
                // mDNS start (which fails on cellular) and left the boot work
                // in FAILED state, so returning to Wi-Fi never resumed it.
                // The worker carries the UNMETERED constraint and now retries
                // transient failures instead of terminally failing.
                moe.shizuku.manager.worker.AdbStartWorker.enqueueIfIdle(context.applicationContext)
                // Refresh the worker banner to match reality: enqueueIfIdle with
                // KEEP is a silent no-op when work is already pending, so without
                // this the tap looks dead and a stale "awaiting Wi-Fi" survives
                // even on Wi-Fi.
                val appCtx = context.applicationContext
                val state = if (moe.shizuku.manager.utils.EnvironmentUtils.isWifiRequired() &&
                    !moe.shizuku.manager.worker.AdbStartWorker.isUnmeteredNetworkAvailable(appCtx)
                ) {
                    ShizukuReceiverStarter.WorkerState.AWAITING_WIFI
                } else {
                    ShizukuReceiverStarter.WorkerState.RUNNING
                }
                ShizukuReceiverStarter.updateNotification(appCtx, state)
                WatchdogManager.attemptRestart(context.applicationContext)
            }
            ACTION_STOP_SERVER -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        WatchdogManager.stopServerAndWait(context.applicationContext, userInitiated = true)
                        SheveryNotificationManager.updateNotification(context.applicationContext)
                    } catch (e: Exception) {
                        logw("Stop server failed", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
