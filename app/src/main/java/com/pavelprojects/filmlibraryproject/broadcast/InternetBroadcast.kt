package com.pavelprojects.filmlibraryproject.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

class InternetBroadcast(private val listener: OnBroadcastReceiver) : BroadcastReceiver() {
    companion object {
        const val TAG = "InternetBroadcast"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        var isOnline = false
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = cm.activeNetwork
            val actNw = cm.getNetworkCapabilities(networkCapabilities)
            actNw?.let {
                isOnline = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        } else {
            cm.run {
                cm.activeNetworkInfo?.run {
                    isOnline = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
        Log.d(TAG, "isOnline = $isOnline")
        listener.onOnlineStatus(isOnline)
    }

    interface OnBroadcastReceiver {
        fun onOnlineStatus(isOnline: Boolean)
    }
}