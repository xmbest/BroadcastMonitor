package com.xmbest.broadcastmonitor

import android.app.Application
import android.content.Context
import com.highcapable.yukihookapi.YukiHookAPI

class App : Application() {
    
    companion object {
        private lateinit var instance: App
        
        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context?) {
        // 装载 Hook Framework
        //
        // Your code here.
        //
        // 装载 YukiHookAPI
        YukiHookAPI.encase(base) {
            // Your code here.

        }
        super.attachBaseContext(base)
    }
}