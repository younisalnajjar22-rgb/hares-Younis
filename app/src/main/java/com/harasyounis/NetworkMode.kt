package com.harasyounis

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

enum class NetworkMode { WIFI, MOBILE, OTHER }

object NetworkModeDetector {
    fun current(context: Context): NetworkMode {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return NetworkMode.OTHER
        val caps = cm.getNetworkCapabilities(network) ?: return NetworkMode.OTHER

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkMode.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkMode.MOBILE
            else -> NetworkMode.OTHER
        }
    }
}
