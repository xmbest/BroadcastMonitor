package com.xmbest.broadcastmonitor

import android.app.Application
import android.content.Context
import android.util.Log
import com.highcapable.yukihookapi.YukiHookAPI
import com.xmbest.broadcastmonitor.constants.AppConstants

/**
 * Application class
 */
class App : Application() {

    companion object {
        private const val TAG = AppConstants.Log.TAG_APP
        private lateinit var instance: App

        /**
     * Get application context
     * Optimization: Add null check
     */
        fun getContext(): Context {
            return if (::instance.isInitialized) {
                instance.applicationContext
            } else {
                throw IllegalStateException("App instance not initialized")
            }
        }

        /**
     * Check if application is initialized
     */
        fun isInitialized(): Boolean {
            return ::instance.isInitialized
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            instance = this
            Log.d(TAG, "Application initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Application initialization failed: ${e.message}")
        }
    }

    override fun attachBaseContext(base: Context?) {
        try {
            // Load YukiHookAPI
            YukiHookAPI.encase(base) {
            }
            super.attachBaseContext(base)
            Log.d(TAG, "Hook framework loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Hook framework loading failed: ${e.message}")
            super.attachBaseContext(base)
        }
    }
}