package com.xmbest.broadcastmonitor.hook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.xmbest.broadcastmonitor.constants.AppConstants
import com.xmbest.broadcastmonitor.utils.BroadcastFilter
import com.xmbest.broadcastmonitor.utils.BroadcastLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Broadcast Hook class
 * Responsible for hooking system and application broadcast sending methods
 */
class BroadcastHook(private val packageParam: PackageParam) {

    /**
     * Hook system-level broadcasts
     * Monitor ActivityManagerService.broadcastIntentLocked
     */
    fun hookSystemBroadcast() {
        BroadcastLogger.logD("Setting up system broadcast hooks...")
        try {
            packageParam.apply {
                "com.android.server.am.ActivityManagerService".toClass().method {
                    name = "broadcastIntentLocked"
                }.hook {
                    before {
                        handleBroadcastIntent(packageParam.appContext, args, "System-AMS")
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("ActivityManagerService hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("System broadcast hook failed: ${e.message}")
        }
    }

    /**
     * Hook user-level broadcasts
     * Monitor Context-related broadcast sending methods
     */
    fun hookUserBroadcast() {
        BroadcastLogger.logD("Setting up user broadcast hooks...")

        // Hook ContextImpl
        hookContextImpl()

        // Hook ContextWrapper
//        hookContextWrapper()

        // Hook ActivityManagerProxy
        hookActivityManagerProxy()
    }

    /**
     * Hook ContextImpl broadcast methods
     */
    private fun hookContextImpl() {
        val className = "android.app.ContextImpl"

        // Hook sendBroadcast(Intent)
        hookSendBroadcast(className, "ContextImpl-sendBroadcast")

        // Hook sendStickyBroadcast(Intent)
        hookSendStickyBroadcast(className, "ContextImpl-sendStickyBroadcast")

        // Hook sendOrderedBroadcast methods
        hookSendOrderedBroadcast(className, "ContextImpl-sendOrderedBroadcast")
    }

    /**
     * Hook ContextWrapper broadcast methods
     */
    private fun hookContextWrapper() {
        val className = "android.content.ContextWrapper"
        hookSendBroadcast(className, "ContextWrapper-sendBroadcast")
    }

    /**
     * Hook ActivityManagerProxy broadcast methods
     */
    private fun hookActivityManagerProxy() {
        try {
            packageParam.apply {
                "android.app.ActivityManagerProxy".toClass().method {
                    name = "broadcastIntent"
                }.hook {
                    before {
                        handleBroadcastIntent(
                            packageParam.appContext,
                            args,
                            "ActivityManagerProxy-broadcastIntent"
                        )
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("ActivityManagerProxy hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("ActivityManagerProxy hook failed: ${e.message}")
        }
    }

    /**
     * Hook sendBroadcast method
     */
    private fun hookSendBroadcast(className: String, source: String) {
        try {
            packageParam.apply {
                className.toClass().method {
                    name = "sendBroadcast"
                    paramCount = 1
                }.hook {
                    before {
                        handleBroadcastIntent(packageParam.appContext, args, source)
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source hook failed: ${e.message}")
        }
    }

    /**
     * Hook sendStickyBroadcast method
     */
    private fun hookSendStickyBroadcast(className: String, source: String) {
        try {
            packageParam.apply {
                className.toClass().method {
                    name = "sendStickyBroadcast"
                    paramCount = 1
                }.hook {
                    before {
                        handleBroadcastIntent(packageParam.appContext, args, source)
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source hook failed: ${e.message}")
        }
    }

    /**
     * Hook sendOrderedBroadcast method
     */
    private fun hookSendOrderedBroadcast(className: String, source: String) {
        // Hook sendOrderedBroadcast(Intent, String)
        try {
            packageParam.apply {
                className.toClass().method {
                    name = "sendOrderedBroadcast"
                    paramCount = 2
                }.hook {
                    before {
                        handleBroadcastIntent(packageParam.appContext, args, "$source-2")
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source-2 hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source-2 hook failed: ${e.message}")
        }

        // Hook sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)
        try {
            packageParam.apply {
                className.toClass().method {
                    name = "sendOrderedBroadcast"
                    paramCount = 7
                }.hook {
                    before {
                        handleBroadcastIntent(packageParam.appContext, args, "$source-7")
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source-7 hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source-7 hook failed: ${e.message}")
        }
    }

    /**
     * Handle broadcast Intent
     */
    private fun handleBroadcastIntent(context: Context?, args: Array<Any?>, source: String) {
        try {
            val intentIndex = args.indexOfFirst { it is Intent }
            if (intentIndex >= 0) {
                val intent = args[intentIndex] as Intent
                processIntent(context, intent, source)
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("Handle broadcast intent error: ${e.message}")
        }
    }

    /**
     * Process Intent data
     */
    private fun processIntent(context: Context?, intent: Intent, source: String) {
        try {
            val intentData = extractIntentData(intent)
            if (shouldLogBroadcast(intentData)) {
                logBroadcast(context, intentData, source)
            } else {
                BroadcastLogger.logD("Broadcast filtered out by BroadcastFilter")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("Process intent error: ${e.message}")
        }
    }

    /**
     * Determine whether the broadcast should be logged
     */
    private fun shouldLogBroadcast(intentData: IntentData): Boolean {
        // Filter out data transmission broadcasts sent by ourselves to avoid circular logging
        if (intentData.action == AppConstants.Broadcast.DATA_ACTION) {
            return false
        }
        return BroadcastFilter.shouldLog(intentData.action, intentData.packageName)
    }

    /**
     * Log broadcast
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun logBroadcast(context: Context?, intentData: IntentData, source: String) {
        val timestamp = getCurrentTimestamp()

        BroadcastLogger.logBroadcast(
            context,
            timestamp = timestamp,
            action = "[$source] ${intentData.action}",
            extras = intentData.extras,
            categories = intentData.categories,
            data = intentData.data,
            type = intentData.type,
            component = intentData.component,
            flags = intentData.flags,
            packageName = intentData.packageName
        )
    }
}

/**
 * Extract Intent data
 */
private fun extractIntentData(intent: Intent): IntentData {
    return IntentData(
        action = intent.action ?: "No Action",
        packageName = intent.`package` ?: "No Package",
        extras = extractExtras(intent.extras),
        categories = intent.categories?.joinToString(", ") ?: "No Category",
        data = intent.dataString ?: "No Data",
        type = intent.type ?: "No Type",
        component = intent.component?.flattenToString() ?: "No Component",
        flags = intent.flags.toString()
    )
}

/**
 * Extract Bundle data
 */
private fun extractExtras(extras: Bundle?): String {
    if (extras == null || extras.isEmpty) return "No Extras"

    return buildString {
        for (key in extras.keySet()) {
            try {
                val value = when {
                    extras.containsKey(key) -> extras[key]
                    else -> null
                }
                appendLine("$key: ${value?.toString() ?: "null"}")
            } catch (e: Exception) {
                appendLine("$key: [Read failed: ${e.message}]")
            }
        }
    }.trim()
}

/**
 * Get current timestamp
 */
private fun getCurrentTimestamp(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS",
        Locale.getDefault()
    ).format(Date())
}

/**
 * Intent data model
 */
private data class IntentData(
    val action: String,
    val packageName: String,
    val extras: String,
    val categories: String,
    val data: String,
    val type: String,
    val component: String,
    val flags: String
)
