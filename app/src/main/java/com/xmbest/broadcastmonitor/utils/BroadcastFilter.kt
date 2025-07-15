package com.xmbest.broadcastmonitor.utils

import com.xmbest.broadcastmonitor.constants.AppConstants

/**
 * Broadcast filter
 * Responsible for broadcast classification, filtering and priority management
 */
object BroadcastFilter {
    
    // System general broadcast action set
    private val commonSystemActions = setOf(
        "android.intent.action.BATTERY_CHANGED",
        "android.intent.action.TIME_TICK",
        "android.intent.action.SCREEN_ON",
        "android.intent.action.SCREEN_OFF",
        "android.net.conn.CONNECTIVITY_CHANGE",
        "android.intent.action.USER_PRESENT",
        "android.intent.action.BOOT_COMPLETED",
        "android.intent.action.PACKAGE_ADDED",
        "android.intent.action.PACKAGE_REMOVED",
        "android.intent.action.PACKAGE_REPLACED",
        "android.intent.action.PACKAGE_CHANGED",
        "android.intent.action.APPLICATION_RESTRICTIONS_CHANGED"
    )
    
    // Security related broadcast action set
    private val securityActions = setOf(
        "android.intent.action.NEW_OUTGOING_CALL",
        "android.provider.Telephony.SMS_RECEIVED",
        "android.intent.action.PHONE_STATE",
        "android.location.PROVIDERS_CHANGED",
        "android.intent.action.LOCALE_CHANGED"
    )
    
    // Media related broadcast action set
    private val mediaActions = setOf(
        "android.intent.action.MEDIA_MOUNTED",
        "android.intent.action.MEDIA_UNMOUNTED",
        "android.intent.action.MEDIA_EJECT",
        "android.intent.action.MEDIA_SCANNER_STARTED",
        "android.intent.action.MEDIA_SCANNER_FINISHED"
    )
    
    // Broadcast prefix to category mapping, improve classification performance
    private val prefixCategoryMap = mapOf(
        "android.bluetooth" to AppConstants.Category.BLUETOOTH,
        "android.net.wifi" to AppConstants.Category.WIFI,
        "android.intent.action.PACKAGE" to AppConstants.Category.PACKAGE,
        "android.intent.action.USER" to AppConstants.Category.USER,
        "android.intent.action.BATTERY" to AppConstants.Category.BATTERY,
        "android.intent.action.SCREEN" to AppConstants.Category.SCREEN,
        "com.android.server" to AppConstants.Category.SYSTEM_SERVICE
    )
    
    /**
     * Classify broadcast actions
     */
    fun categorize(action: String): String {
        // First check exact match sets
        return when {
            securityActions.contains(action) -> AppConstants.Category.SECURITY
            mediaActions.contains(action) -> AppConstants.Category.MEDIA
            commonSystemActions.contains(action) -> AppConstants.Category.SYSTEM
            else -> {
                // Use prefix mapping for fast lookup
                prefixCategoryMap.entries.find { (prefix, _) -> 
                    action.startsWith(prefix) 
                }?.value ?: AppConstants.Category.OTHER
            }
        }
    }
    
    /**
     * Determine whether this broadcast should be logged
     */
    fun shouldLog(action: String, packageName: String): Boolean {
        return when {
            action.isBlank() -> false
            isHighFrequencyBroadcast(action) -> false
//            isSystemInternalBroadcast(action) -> false
            else -> true
        }
    }
    
    /**
     * Check if it's a high frequency broadcast
     */
    private fun isHighFrequencyBroadcast(action: String): Boolean {
        return action in AppConstants.HighFrequency.ACTIONS
    }
    
    /**
     * Check if it's a system internal broadcast (optional filtering)
     */
    private fun isSystemInternalBroadcast(action: String): Boolean {
        return action.startsWith("com.android.internal") ||
               action.startsWith("android.intent.action.SIG_STR") ||
               action.startsWith("android.intent.action.SERVICE_STATE")
    }
    
    /**
     * Get broadcast priority
     */
    fun getPriority(action: String): Int {
        return when {
            securityActions.contains(action) -> 1 // Highest priority
            action.startsWith("android.intent.action.PACKAGE") -> 2
            action.startsWith("android.intent.action.USER") -> 2
            mediaActions.contains(action) -> 3
            commonSystemActions.contains(action) -> 4
            else -> AppConstants.Broadcast.DEFAULT_PRIORITY // Default priority
        }
    }
}