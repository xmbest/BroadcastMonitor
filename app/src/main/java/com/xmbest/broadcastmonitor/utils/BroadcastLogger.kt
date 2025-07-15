package com.xmbest.broadcastmonitor.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.xmbest.broadcastmonitor.constants.AppConstants
import com.xmbest.broadcastmonitor.data.BroadcastData
import com.xmbest.broadcastmonitor.data.BroadcastDataManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Broadcast logger
 * Responsible for recording and managing broadcast data
 */
object BroadcastLogger {
    private const val TAG = AppConstants.Log.TAG_LOGGER
    const val BROADCAST_DATA_ACTION = "com.xmbest.broadcastmonitor.BROADCAST_DATA"
    
    // Date formatter, using thread-safe approach
    private val dateFormatter = ThreadLocal.withInitial {
        SimpleDateFormat(AppConstants.File.DATE_FORMAT_PATTERN, Locale.getDefault())
    }


    /**
     * Record broadcast data
     */
    fun logBroadcast(
        context: Context?,
        timestamp: String,
        action: String,
        extras: String,
        categories: String,
        data: String,
        type: String,
        component: String,
        flags: String,
        packageName: String,
        category: String = AppConstants.Broadcast.DEFAULT_CATEGORY,
        priority: Int = AppConstants.Broadcast.DEFAULT_PRIORITY
    ) {
        try {
            val logMessage = buildLogMessage(
                timestamp, category, priority, action, packageName,
                component, categories, data, type, flags, extras
            )

            Log.d(TAG, logMessage)

            // Extract and clean action information
            val (source, cleanAction) = extractSourceAndCleanAction(action)

            // Create broadcast data
            val broadcastData = createBroadcastData(
                timestamp, cleanAction, source, packageName,
                extras, categories, data, type, component, flags,
                category, priority
            )

            // Send data via broadcast to avoid cross-process singleton issues
            sendBroadcastData(context, broadcastData)
            
        } catch (e: Exception) {
            logError("Failed to record broadcast: ${e.message}")
        }
    }
    
    /**
     * Build log message
     */
    private fun buildLogMessage(
        timestamp: String,
        category: String,
        priority: Int,
        action: String,
        packageName: String,
        component: String,
        categories: String,
        data: String,
        type: String,
        flags: String,
        extras: String
    ): String {
        return buildString {
            append("========== Broadcast Record ==========\n")
            append("Time: $timestamp\n")
            append("Category: $category (Priority: $priority)\n")
            append("Action: $action\n")
            append("Package: $packageName\n")
            append("Component: $component\n")
            append("Categories: $categories\n")
            append("Data: $data\n")
            append("Type: $type\n")
            append("Flags: $flags\n")
            append("Extras:\n$extras\n")
            append("==============================\n")
        }
    }
    
    /**
     * Extract source information and clean action
     */
    private fun extractSourceAndCleanAction(action: String): Pair<String, String> {
        val source = when {
            action.contains("[") && action.contains("]") -> {
                val start = action.indexOf("[")
                val end = action.indexOf("]")
                if (start >= 0 && end > start) {
                    action.substring(start + 1, end)
                } else {
                    AppConstants.Broadcast.UNKNOWN_SOURCE
                }
            }
            else -> AppConstants.Broadcast.UNKNOWN_SOURCE
        }

        val cleanAction = if (action.contains("] ")) {
            action.substring(action.indexOf("] ") + 2)
        } else {
            action
        }
        
        return Pair(source, cleanAction)
    }
    
    /**
     * Create broadcast data object
     */
    private fun createBroadcastData(
        timestamp: String,
        action: String,
        source: String,
        packageName: String,
        extras: String,
        categories: String,
        data: String,
        type: String,
        component: String,
        flags: String,
        category: String,
        priority: Int
    ): BroadcastData {
        val extrasInfo = buildExtrasInfo(extras, categories, data, type, component, flags)
        
        return BroadcastData(
            timestamp = BroadcastDataManager.getCurrentTimestamp(),
            action = action,
            source = source,
            packageName = packageName,
            extras = extrasInfo,
            category = category,
            priority = priority
        )
    }
    
    /**
     * Build extras information string
     */
    private fun buildExtrasInfo(
        extras: String,
        categories: String,
        data: String,
        type: String,
        component: String,
        flags: String
    ): String {
        return buildString {
            addInfoIfNotEmpty("Extras", extras)
            addInfoIfNotEmpty("Categories", categories)
            addInfoIfNotEmpty("Data", data)
            addInfoIfNotEmpty("Type", type)
            addInfoIfNotEmpty("Component", component)
            if (flags.isNotEmpty()) {
                appendLine("Flags: $flags")
            }
        }.trim().ifEmpty { "No additional information" }
    }
    
    /**
     * Add non-empty information to string builder
     */
    private fun StringBuilder.addInfoIfNotEmpty(label: String, value: String) {
        if (value.isNotEmpty() && !value.startsWith("No")) {
            appendLine("$label: $value")
        }
    }

    fun logError(message: String) {
        try {
            val timestamp = dateFormatter.get()?.format(Date()) ?: "Unknown"
            val logMessage = "[$timestamp] ERROR: $message"
            Log.e(TAG, logMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting log message: ${e.message}")
        }
    }

    fun logD(message: String) {
        try {
            val timestamp = dateFormatter.get()?.format(Date()) ?: "Unknown"
            val logMessage = "[$timestamp] DEBUG: $message"
            Log.d(TAG, logMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting debug message: ${e.message}")
        }
    }

    /**
     * Send data via broadcast
     */
    private fun sendBroadcastData(context: Context?, broadcastData: BroadcastData) {
        if (context == null) {
            logError("Context not initialized, unable to send broadcast data")
            return
        }

        try {
            val intent = createBroadcastIntent(broadcastData)
            context.sendBroadcast(intent)
            logD("Broadcast data sent: ${broadcastData.action}")
        } catch (e: Exception) {
            logError("Failed to send broadcast data: ${e.message}")
        }
    }
    
    /**
     * Create broadcast Intent
     */
    private fun createBroadcastIntent(broadcastData: BroadcastData): Intent {
        return Intent(BROADCAST_DATA_ACTION).apply {
            putExtra(AppConstants.Intent.EXTRA_TIMESTAMP, broadcastData.timestamp)
            putExtra(AppConstants.Intent.EXTRA_ACTION, broadcastData.action)
            putExtra(AppConstants.Intent.EXTRA_SOURCE, broadcastData.source)
            putExtra(AppConstants.Intent.EXTRA_PACKAGE_NAME, broadcastData.packageName)
            putExtra(AppConstants.Intent.EXTRA_EXTRAS, broadcastData.extras)
            putExtra(AppConstants.Intent.EXTRA_CATEGORY, broadcastData.category)
            putExtra(AppConstants.Intent.EXTRA_PRIORITY, broadcastData.priority)
            // Add package name restriction to ensure only this app receives
            setPackage(AppConstants.App.PACKAGE_NAME)
        }
    }
}