package moe.shizuku.manager.worker

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import rikka.shizuku.Shizuku

/**
 * Safety net for the "boot on cellular, no auto-restart on Wi-Fi" bug.
 *
 * The boot worker carries an UNMETERED constraint so WorkManager resumes it
 * when Wi-Fi returns — but only while the work is still pending. Work that
 * already terminally failed (older app versions, user-cancelled retries) stays
 * dead. This observer re-enqueues an idle start worker whenever an unmetered
 * network becomes available, so returning to Wi-Fi restarts Shevery even in
 * those stranded states.

 */
object AdbNetworkObserver {

    private const val TAG = "AdbNetworkObserver"

    @Volatile
    private var registered = false

    /** Suppresses back-to-back onAvailable flaps racing the constraint-unblock. */
    @Volatile
    private var lastTriggerMs = 0L
    private const val DEBOUNCE_MS =  5_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Strong ref: ConnectivityManager does not retain the callback; an
     *  anonymous instance would be GC'd and silently kill the observer. */
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /** Never unregistered: process-scoped singleton, registered once at
     *  Application.onCreate; process death cleans it up,and the
     *  registered flag guards double-observation on re-entry. */
    fun register(app: Application) {
        // Wireless debugging needs API 30+; on older devices there is
        // nothing to observe, so skip installing the callback entirely.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        if (registered) return
        synchronized(this) {
            if (registered) return
            try {
                val cm = app.getSystemService(ConnectivityManager::class.java) ?: return
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                    .build()
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        onUnmeteredAvailable(app)
                    }
                }
                networkCallback = callback
                cm.registerNetworkCallback(request, callback)
                registered = true
            } catch (e: Exception) {
                // Observing is best-effort; the constrained worker covers the
                Log.w(TAG, "registerNetworkCallback failed", e)
                // normal path on its own.

            }
        }
    }

    private fun onUnmeteredAvailable(app: Application) {
        // Binder already up (started by another path): any lingering
        // "awaiting Wi-Fi" banner is definitively stale — clear it. This
        // check runs before the debounce so a no-op binder-up flap doesn't
        // burn the 5s window a real unstarted case would then wait out.

        if (Shizuku.pingBinder()) {

            ShizukuReceiverStarter.updateNotification(
                app.applicationContext,
                ShizukuReceiverStarter.WorkerState.STOPPED
            )
            return
        }
        if (!ShizukuSettings.getStartOnBootAdb()) return

        synchronized(this) {
            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastTriggerMs < DEBOUNCE_MS) return

            lastTriggerMs = now
        }
        scope.launch {
            try {
                // Unmetered Wi-Fi is back: re-enqueue the start worker, then post a
                // banner that reflects the actual WorkManager state instead of an
                // optimistic RUNNING. KEEP no-ops when work is already pending,so
                // the old unconditional RUNNING lied when a backoff-parked worker
                // would sit "awaiting Wi-Fi" on a now-present network for minutes.

                AdbStartWorker.enqueueIfIdle(app.applicationContext)

                val state = withTimeout(5_000L) {
                    val info = WorkManager.getInstance(app.applicationContext)
                        .getWorkInfosForUniqueWork(AdbStartWorker.UNIQUE_WORK_NAME)
                        .get()
                        .firstOrNull()
                    when (info?.state) {
                        WorkInfo.State.RUNNING -> ShizukuReceiverStarter.WorkerState.RUNNING
                        // Constraint still unmet:don't claim RUNNING yet.
                        WorkInfo.State.BLOCKED -> ShizukuReceiverStarter.WorkerState.AWAITING_WIFI

                        // Still enqueued (backoff or constraint wait(:the worker owns its
                        // banner,and will post the accurate state when it wakes — show
                        // "awaiting" meanwhile (AWAITING_WIFI iff Wi-Fi is missing).
                        WorkInfo.State.ENQUEUED ->
                            AdbStartWorker.bannerStateFor(app.applicationContext, retrying = true)

                        // Enqueue race:(the row above is not in the DB yet, or the chain was
                        // just re-enqueued after being finished(:use the same
                        // environment-based guess the worker itself uses.
                        else -> AdbStartWorker.bannerStateFor(app.applicationContext)
                    }
                }
                ShizukuReceiverStarter.updateNotification(app.applicationContext, state)
            } catch (e: Exception) {
                Log.w(TAG, "onUnmeteredAvailable enqueue failed", e)
            }
        }
    }
}
