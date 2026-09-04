package moe.shizuku.manager.worker

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
                // Unmetered Wi-Fi is back: refresh the banner immediately so a
                // frozen "awaiting Wi-Fi" can't survive the transition while the
                // worker (re)starts — enqueueIfIdle with KEEP is a silent no-op

                // when work is already pending,and a pending retry may sit in
                // backoff for minutes without posting anything itself.

                ShizukuReceiverStarter.updateNotification(
                    app.applicationContext,
                    ShizukuReceiverStarter.WorkerState.RUNNING
                )
                AdbStartWorker.enqueueIfIdle(app.applicationContext)

            } catch (e: Exception) {
                Log.w(TAG, "onUnmeteredAvailable enqueue failed", e)
            }
        }
    }
}
