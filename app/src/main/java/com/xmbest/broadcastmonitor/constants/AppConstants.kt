package com.xmbest.broadcastmonitor.constants

/**
 * Application constants definition
 * Centralized management of constants used in the application to improve code maintainability
 */
object AppConstants {
    
    // Application related constants
    object App {
        const val PACKAGE_NAME = "com.xmbest.broadcastmonitor"
    }
    
    // Data management related constants
    const val MAX_BROADCAST_LIST_SIZE = 1000
    
    // Broadcast related constants
    object Broadcast {
        const val DATA_ACTION = "com.xmbest.broadcastmonitor.BROADCAST_DATA"
        const val MAX_LIST_SIZE = 1000
        const val DEFAULT_PRIORITY = 5
        const val DEFAULT_CATEGORY = "Other Broadcast"
        const val UNKNOWN_SOURCE = "Unknown"
    }
    
    // Intent related constants
    object Intent {
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_ACTION = "action"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_EXTRAS = "extras"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_PRIORITY = "priority"
    }
    
    // Log related constants
    object Log {
        const val TAG_APP = "BroadcastMonitorApp"
        const val TAG_MAIN = "MainActivity"
        const val TAG_HOOK = "BroadcastHook"
        const val TAG_LOGGER = "BroadcastLogger"
        const val TAG_DATA_MANAGER = "BroadcastDataManager"
        const val TAG_DATA_RECEIVER = "BroadcastDataReceiver"
        const val TAG_FILE_MANAGER = "BroadcastFileManager"
        const val TAG_FILE_WATCHER = "BroadcastFileWatcher"
        const val TAG_MAIN_ACTIVITY = "MainActivity"
    }
    
    // File related constants
    object File {
        const val BROADCAST_FILE_NAME = "broadcast_data.json"
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
        const val TIME_FORMAT_PATTERN = "HH:mm:ss.SSS"
    }
    
    // Permission related constants
    object Permission {
        const val REQUEST_CODE_STORAGE = 1001
    }
    
    // Broadcast category constants
    object Category {
        const val SECURITY = "Security Related"
        const val MEDIA = "Media Related"
        const val SYSTEM = "System General"
        const val BLUETOOTH = "Bluetooth Related"
        const val WIFI = "WiFi Related"
        const val PACKAGE = "Package Management"
        const val USER = "User Related"
        const val BATTERY = "Battery Related"
        const val SCREEN = "Screen Related"
        const val SYSTEM_SERVICE = "System Service"
        const val OTHER = "Other Broadcast"
    }
    
    // High frequency broadcast filter list
    object HighFrequency {
        val ACTIONS = setOf(
            "android.intent.action.TIME_TICK",
            "android.intent.action.BATTERY_CHANGED"
        )
    }
}