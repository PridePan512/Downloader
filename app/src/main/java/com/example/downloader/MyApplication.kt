package com.example.downloader

import android.app.Application
import com.example.library.LibHelper
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 开启动态配色
        DynamicColors.applyToActivitiesIfAvailable(this)
        //初始化资源
        LibHelper.init(applicationContext)
    }
}