package moe.shizuku.manager.worker

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import moe.shizuku.manager.ShizukuSettings
import rikka.shizuku.Shizuku

/**
 * Safety net for the "boot on cellular, no auto-restart on Wi-Fi" bug.
 *
 * The boot worker carries an UNMETERED constraint so WorkManager resumes it
 * when Wi-Fi returns — but only while the work is still pending. Work that
 * already terminally failed (older app versions, user-cancelled retries)
 * stays dead. This observer re-enqueues an idle start worker whenever an
 * unmetered network becomes available, so returning to Wi-Fi restarts
 * Shevery even in those stranded states.
 */
object AdbNetworkObserver {

    @Volatile
    private var registered = false

    /** Suppresses back-to-back onAvailable flaps racing the constraint-unblock. */
    @Volatile
    private var lastTriggerMs = 0L
    private const val DEBOUNCE_MS = 5_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Strong ref: ConnectivityManager does not retain the callback; an anonymous instance would be GC'd and silently kill the observer. */
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun register(app: Application) {
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
            } catch (_: Exception) {
                // Observing is best-effort; the constrained worker covers the
                // normal path on its own.
            }
        }
    }

    private fun onUnmeteredAvailable(app: Application) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        // Wireless debugging needs API 30+; the gate stays.
        synchronized(this) {
            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastTriggerMs < DEBOUNCE_MS) return
            lastTriggerMs = now
        }
        scope.launch {
            try {
                if (!ShizukuSettings.getStartOnBootAdb()) return@launch
                if (Shizuku.pingBinder()) return@launch
                AdbStartWorker.enqueueIfIdle(app.applicationContext)
            } catch (_: Exception) {
            }
        }
    }
}
