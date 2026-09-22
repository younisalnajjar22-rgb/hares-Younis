package com.harasyounis

import android.content.Context
import android.net.*
import android.os.ParcelFileDescriptor
import java.io.FileInputStream

class YounisVpnService : VpnService() {
    private var vpn: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var lastMode = NetworkMode.OTHER

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        registerNetworkCallback()
        rebuildForCurrentNetwork(force = true)
        return START_STICKY
    }

    private fun registerNetworkCallback() {
        if (callback != null) return
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                rebuildForCurrentNetwork()
            }
            override fun onLost(network: Network) {
                rebuildForCurrentNetwork()
            }
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                rebuildForCurrentNetwork()
            }
        }

        cm.registerDefaultNetworkCallback(callback!!)
    }

    @Synchronized
    private fun rebuildForCurrentNetwork(force: Boolean = false) {
        val mode = NetworkModeDetector.current(this)
        if (!force && mode == lastMode && vpn != null) return
        lastMode = mode

        stopVpnOnly()

        val prefs = getSharedPreferences("rules", MODE_PRIVATE)

        val blockedPackages = packageManager.getInstalledApplications(0)
            .filter { app ->
                when (mode) {
                    NetworkMode.WIFI ->
                        prefs.getBoolean("${app.packageName}_wifi", false)
                    NetworkMode.MOBILE ->
                        prefs.getBoolean("${app.packageName}_mobile", false)
                    NetworkMode.OTHER -> false
                }
            }
            .map { it.packageName }

        // Important design:
        // Only apps blocked for the CURRENT network are put into the VPN.
        // Every other app bypasses the VPN and keeps its normal Android route.
        // The VPN simply discards packets from the selected apps.
        if (blockedPackages.isEmpty()) return

        val builder = Builder()
            .setSession("حارس يونس")
            .setMtu(1500)
            .addAddress("10.8.0.2", 32)
            .addRoute("0.0.0.0", 0)

        for (pkg in blockedPackages) {
            try {
                builder.addAllowedApplication(pkg)
            } catch (_: Exception) {
                // Package may disappear between query and VPN setup.
            }
        }

        vpn = builder.establish() ?: return

        worker = Thread {
            try {
                val input = FileInputStream(vpn!!.fileDescriptor)
                val buffer = ByteArray(32767)
                while (!Thread.currentThread().isInterrupted) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    // Deliberately do not write packets back.
                    // These are the apps blocked for the active network.
                }
            } catch (_: Exception) {
            }
        }
        worker?.start()
    }

    private fun stopVpnOnly() {
        worker?.interrupt()
        worker = null
        vpn?.close()
        vpn = null
    }

    override fun onDestroy() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        callback?.let {
            try { cm.unregisterNetworkCallback(it) } catch (_: Exception) {}
        }
        callback = null
        stopVpnOnly()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpnOnly()
        super.onRevoke()
    }
}
