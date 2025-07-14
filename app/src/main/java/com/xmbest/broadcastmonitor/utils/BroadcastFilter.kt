package com.xmbest.broadcastmonitor.utils

object BroadcastFilter {
    
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
    
    private val securityActions = setOf(
        "android.intent.action.NEW_OUTGOING_CALL",
        "android.provider.Telephony.SMS_RECEIVED",
        "android.intent.action.PHONE_STATE",
        "android.location.PROVIDERS_CHANGED",
        "android.intent.action.LOCALE_CHANGED"
    )
    
    private val mediaActions = setOf(
        "android.intent.action.MEDIA_MOUNTED",
        "android.intent.action.MEDIA_UNMOUNTED",
        "android.intent.action.MEDIA_EJECT",
        "android.intent.action.MEDIA_SCANNER_STARTED",
        "android.intent.action.MEDIA_SCANNER_FINISHED"
    )
    
    fun categorize(action: String): String {
        return when {
            securityActions.contains(action) -> "安全相关"
            mediaActions.contains(action) -> "媒体相关"
            commonSystemActions.contains(action) -> "系统常规"
            action.startsWith("android.bluetooth") -> "蓝牙相关"
            action.startsWith("android.net.wifi") -> "WiFi相关"
            action.startsWith("android.intent.action.PACKAGE") -> "应用包管理"
            action.startsWith("android.intent.action.USER") -> "用户相关"
            action.startsWith("android.intent.action.BATTERY") -> "电池相关"
            action.startsWith("android.intent.action.SCREEN") -> "屏幕相关"
            action.contains("com.android.server") -> "系统服务"
            else -> "其他广播"
        }
    }
    
    fun shouldLog(action: String, packageName: String): Boolean {
        return when {
            action.isBlank() -> false
            isHighFrequencyBroadcast(action) -> false
            else -> true
        }
    }
    
    private fun isHighFrequencyBroadcast(action: String): Boolean {
        return action in setOf(
            "android.intent.action.TIME_TICK",
            "android.intent.action.BATTERY_CHANGED"
        )
    }
    
    fun getPriority(action: String): Int {
        return when {
            securityActions.contains(action) -> 1
            action.startsWith("android.intent.action.PACKAGE") -> 2
            action.startsWith("android.intent.action.USER") -> 2
            mediaActions.contains(action) -> 3
            commonSystemActions.contains(action) -> 4
            else -> 5
        }
    }
}