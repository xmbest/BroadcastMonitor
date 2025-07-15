package com.xmbest.broadcastmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xmbest.broadcastmonitor.constants.AppConstants
import com.xmbest.broadcastmonitor.data.BroadcastData
import com.xmbest.broadcastmonitor.data.BroadcastDataManager

/**
 * Broadcast data receiver
 * Used to receive cross-process broadcast data and add to data manager
 */
class BroadcastDataReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = AppConstants.Log.TAG_DATA_RECEIVER
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        // Validate Intent and Action
        if (!isValidIntent(intent)) {
            return
        }
        
        try {
            // Extract and validate broadcast data
            val broadcastData = extractBroadcastData(intent!!) ?: return
            
            // Add to data manager
            BroadcastDataManager.addBroadcast(broadcastData)
            
            Log.d(TAG, "Received broadcast data: ${broadcastData.action}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process broadcast data: ${e.message}")
        }
    }
    
    /**
     * Validate if Intent is valid
     * Optimization: Extract validation logic, enhance security
     */
    private fun isValidIntent(intent: Intent?): Boolean {
        return intent?.action == AppConstants.Broadcast.DATA_ACTION
    }
    
    /**
     * Extract broadcast data from Intent
     * Optimization: Use constant management, enhance data validation
     */
    private fun extractBroadcastData(intent: Intent): BroadcastData? {
        return try {
            // Extract required data, return null if missing
            val timestamp = intent.getStringExtra(AppConstants.Intent.EXTRA_TIMESTAMP) 
                ?: return logAndReturnNull("Missing timestamp")
            val action = intent.getStringExtra(AppConstants.Intent.EXTRA_ACTION) 
                ?: return logAndReturnNull("Missing action")
            val source = intent.getStringExtra(AppConstants.Intent.EXTRA_SOURCE) 
                ?: return logAndReturnNull("Missing source")
            val packageName = intent.getStringExtra(AppConstants.Intent.EXTRA_PACKAGE_NAME) 
                ?: return logAndReturnNull("Missing packageName")
            
            // Extract optional data, use default values
            val extras = intent.getStringExtra(AppConstants.Intent.EXTRA_EXTRAS) ?: ""
            val category = intent.getStringExtra(AppConstants.Intent.EXTRA_CATEGORY) 
                ?: AppConstants.Broadcast.DEFAULT_CATEGORY
            val priority = intent.getIntExtra(AppConstants.Intent.EXTRA_PRIORITY, 
                AppConstants.Broadcast.DEFAULT_PRIORITY)
            
            // Create and return broadcast data object
            BroadcastData(
                timestamp = timestamp,
                action = action,
                source = source,
                packageName = packageName,
                extras = extras,
                category = category,
                priority = priority
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract broadcast data: ${e.message}")
            null
        }
    }
    
    /**
     * Log error and return null
     */
    private fun logAndReturnNull(message: String): BroadcastData? {
        Log.w(TAG, message)
        return null
    }
}