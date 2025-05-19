package com.example.downloader

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 开启动态配色
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}