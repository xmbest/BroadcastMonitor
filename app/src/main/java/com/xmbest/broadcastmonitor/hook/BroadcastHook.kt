package com.xmbest.broadcastmonitor.hook

import android.content.Intent
import android.os.Bundle
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.xmbest.broadcastmonitor.utils.BroadcastFilter
import com.xmbest.broadcastmonitor.utils.BroadcastLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 广播监听Hook实现
 * 负责拦截系统和应用进程的广播发送
 */
class BroadcastHook(private val packageParam: PackageParam) {

    /**
     * 启动所有Hook
     */
    fun hook() {
        BroadcastLogger.logD("BroadcastHook initialization started")
        hookSystemBroadcast()
        hookUserBroadcast()
    }

    /**
     * Hook系统级广播
     * 监听ActivityManagerService.broadcastIntentLocked
     */
    fun hookSystemBroadcast() {
        BroadcastLogger.logD("Setting up system broadcast hooks...")
        try {
            packageParam.apply {
                "com.android.server.am.ActivityManagerService".toClass().method {
                    name = "broadcastIntentLocked"
                }.hook {
                    before {
                        handleBroadcastIntent(args, "System-AMS")
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("ActivityManagerService hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("System broadcast hook failed: ${e.message}")
        }
    }

    /**
     * Hook用户级广播
     * 监听Context相关的广播发送方法
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
     * Hook ContextImpl 的广播方法
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
     * Hook ContextWrapper 的广播方法
     */
    private fun hookContextWrapper() {
        val className = "android.content.ContextWrapper"
        hookSendBroadcast(className, "ContextWrapper-sendBroadcast")
    }

    /**
     * Hook ActivityManagerProxy 的广播方法
     */
    private fun hookActivityManagerProxy() {
        try {
            packageParam.apply {
                "android.app.ActivityManagerProxy".toClass().method {
                    name = "broadcastIntent"
                }.hook {
                    before {
                        handleBroadcastIntent(args, "ActivityManagerProxy-broadcastIntent")
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("ActivityManagerProxy hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("ActivityManagerProxy hook failed: ${e.message}")
        }
    }

    /**
     * Hook sendBroadcast 方法
     */
    private fun hookSendBroadcast(className: String, source: String) {
        try {
            packageParam.apply {
                className.toClass().method {
                    name = "sendBroadcast"
                    paramCount = 1
                }.hook {
                    before {
                        handleBroadcastIntent(args, source)
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source hook failed: ${e.message}")
        }
    }

    /**
     * Hook sendStickyBroadcast 方法
     */
    private fun hookSendStickyBroadcast(className: String, source: String) {
        try {
            packageParam.apply {
                className.toClass().method {
                    name = "sendStickyBroadcast"
                    paramCount = 1
                }.hook {
                    before {
                        handleBroadcastIntent(args, source)
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source hook failed: ${e.message}")
        }
    }

    /**
     * Hook sendOrderedBroadcast 方法
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
                        handleBroadcastIntent(args, "$source-2")
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
                        handleBroadcastIntent(args, "$source-7")
                    }
                }.ignoredAllFailure()
                BroadcastLogger.logD("$source-7 hook setup completed")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("$source-7 hook failed: ${e.message}")
        }
    }

    /**
     * 处理广播Intent
     */
    private fun handleBroadcastIntent(args: Array<Any?>, source: String) {
        try {
            val intentIndex = args.indexOfFirst { it is Intent }
            if (intentIndex >= 0) {
                val intent = args[intentIndex] as Intent
                processIntent(intent, source)
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("Handle broadcast intent error: ${e.message}")
        }
    }

    /**
     * 处理Intent数据
     */
    private fun processIntent(intent: Intent, source: String) {
        try {
            val intentData = extractIntentData(intent)
            if (shouldLogBroadcast(intentData)) {
                logBroadcast(intentData, source)
            } else {
                BroadcastLogger.logD("Broadcast filtered out by BroadcastFilter")
            }
        } catch (e: Exception) {
            BroadcastLogger.logError("Process intent error: ${e.message}")
        }
    }

    /**
     * 判断是否应该记录广播
     */
    private fun shouldLogBroadcast(intentData: IntentData): Boolean {
        return BroadcastFilter.shouldLog(intentData.action, intentData.packageName)
    }

    /**
     * 记录广播
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun logBroadcast(intentData: IntentData, source: String) {
        val timestamp = getCurrentTimestamp()
        val category = BroadcastFilter.categorize(intentData.action)
        val priority = BroadcastFilter.getPriority(intentData.action)

        BroadcastLogger.logBroadcast(
            timestamp = timestamp,
            action = "[$source] ${intentData.action}",
            extras = intentData.extras,
            categories = intentData.categories,
            data = intentData.data,
            type = intentData.type,
            component = intentData.component,
            flags = intentData.flags,
            packageName = intentData.packageName,
            category = category,
            priority = priority
        )
    }
}

/**
 * 提取Intent数据
 */
private fun extractIntentData(intent: Intent): IntentData {
    return IntentData(
        action = intent.action ?: "无Action",
        packageName = intent.`package` ?: "无Package",
        extras = extractExtras(intent.extras),
        categories = intent.categories?.joinToString(", ") ?: "无Category",
        data = intent.dataString ?: "无Data",
        type = intent.type ?: "无Type",
        component = intent.component?.flattenToString() ?: "无Component",
        flags = intent.flags.toString()
    )
}

/**
 * 提取Bundle数据
 */
private fun extractExtras(extras: Bundle?): String {
    if (extras == null || extras.isEmpty) return "无Extras"

    return buildString {
        for (key in extras.keySet()) {
            try {
                val value = when {
                    extras.containsKey(key) -> extras[key]
                    else -> null
                }
                appendLine("$key: ${value?.toString() ?: "null"}")
            } catch (e: Exception) {
                appendLine("$key: [读取失败: ${e.message}]")
            }
        }
    }.trim()
}

/**
 * 获取当前时间戳
 */
private fun getCurrentTimestamp(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS",
        Locale.getDefault()
    ).format(Date())
}

/**
 * Intent数据模型
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
