package com.example.myapplication.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Helper for network connectivity checks.
 * Uses ConnectivityManager with NetworkCapabilities (API 23+).
 */
object NetworkUtil {

    /** Returns true when an active internet connection exists (WiFi, Cellular, Ethernet) */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
