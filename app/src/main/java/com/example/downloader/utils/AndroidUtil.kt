package com.example.downloader.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.RequiresPermission

object AndroidUtil {

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context
            .applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = manager.activeNetworkInfo

        return !(networkInfo == null || !networkInfo.isAvailable)
    }
}