package moe.shizuku.manager.adb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket

@RequiresApi(Build.VERSION_CODES.R)
class AdbMdns(
    context: Context, private val serviceType: String,
    private val observer: Observer<Int>
) {

    private var registered = false
    private var running = false
    private var serviceName: String? = null
    private val listener = DiscoveryListener(this)
    private val nsdManager: NsdManager = context.getSystemService(NsdManager::class.java)
    // On API 30-33, NsdManager can only resolve one service at a time.
    // Queue discovered services and resolve them one by one.
    private var pendingResolve: NsdServiceInfo? = null
    private val resolveQueue = ArrayDeque<NsdServiceInfo>()

    fun start() {
        if (running) return
        running = true
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service discovery", e)
        }
    }

    fun stop() {
        if (!running) return
        running = false
        try {
            nsdManager.stopServiceDiscovery(listener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop service discovery: ${e.message}")
        }
    }

    private fun onDiscoveryStart() {
        registered = true
    }

    private fun onDiscoveryStop() {
        registered = false
    }

    private fun onServiceFound(info: NsdServiceInfo) {
        // On API 30-33, NsdManager can only resolve one service at a time.
        // Queue the service and resolve sequentially.
        if (pendingResolve == null) {
            pendingResolve = info
            nsdManager.resolveService(info, ResolveListener(this))
        } else {
            resolveQueue.add(info)
        }
    }

    private fun onServiceLost(info: NsdServiceInfo) {
        if (info.serviceName == serviceName) observer.onChanged(-1)
    }

    private fun onServiceResolved(resolvedService: NsdServiceInfo) {
        if (running && isPortAvailable(resolvedService.port)) {
            serviceName = resolvedService.serviceName
            observer.onChanged(resolvedService.port)
        }
        // Resolve the next queued service after this one completes.
        pendingResolve = null
        val next = resolveQueue.pollFirst()
        if (next != null && running) {
            pendingResolve = next
            nsdManager.resolveService(next, ResolveListener(this))
        }
    }

    private fun isPortAvailable(port: Int) = runBlocking(Dispatchers.IO) {
        try {
            ServerSocket().use {
                it.bind(InetSocketAddress("127.0.0.1", port), 1)
                false
            }
        } catch (e: IOException) {
            true
        }
    }

    internal class DiscoveryListener(private val adbMdns: AdbMdns) : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String) {
            Log.v(TAG, "onDiscoveryStarted: $serviceType")

            adbMdns.onDiscoveryStart()
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.v(TAG, "onStartDiscoveryFailed: $serviceType, $errorCode")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.v(TAG, "onDiscoveryStopped: $serviceType")

            adbMdns.onDiscoveryStop()
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.v(TAG, "onStopDiscoveryFailed: $serviceType, $errorCode")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.v(TAG, "onServiceFound: ${serviceInfo.serviceName}")

            adbMdns.onServiceFound(serviceInfo)
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.v(TAG, "onServiceLost: ${serviceInfo.serviceName}")

            adbMdns.onServiceLost(serviceInfo)
        }
    }

    internal class ResolveListener(private val adbMdns: AdbMdns) : NsdManager.ResolveListener {
        override fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, i: Int) {}

        override fun onServiceResolved(nsdServiceInfo: NsdServiceInfo) {
            adbMdns.onServiceResolved(nsdServiceInfo)
        }

    }

    companion object {
        const val TLS_CONNECT = "_adb-tls-connect._tcp"
        const val TLS_PAIRING = "_adb-tls-pairing._tcp"
        const val TAG = "AdbMdns"
    }
}
