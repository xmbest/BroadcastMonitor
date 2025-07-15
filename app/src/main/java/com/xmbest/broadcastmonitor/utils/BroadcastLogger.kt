package com.xmbest.broadcastmonitor.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.xmbest.broadcastmonitor.data.BroadcastData
import com.xmbest.broadcastmonitor.data.BroadcastDataManager
import java.text.SimpleDateFormat
import java.util.*

object BroadcastLogger {
    private const val TAG = "BroadcastMonitor"
    const val BROADCAST_DATA_ACTION = "com.xmbest.broadcastmonitor.BROADCAST_DATA"


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
        category: String = "其他广播",
        priority: Int = 5
    ) {
        val logMessage = buildString {
            append("========== 广播记录 ==========\n")
            append("时间: $timestamp\n")
            append("分类: $category (优先级: $priority)\n")
            append("动作: $action\n")
            append("包名: $packageName\n")
            append("组件: $component\n")
            append("分类: $categories\n")
            append("数据: $data\n")
            append("类型: $type\n")
            append("标志: $flags\n")
            append("附加数据:\n$extras\n")
            append("==============================\n")
        }

        Log.d(TAG, logMessage)

        // 提取source信息
        val source = when {
            action.contains("[") && action.contains("]") -> {
                val start = action.indexOf("[")
                val end = action.indexOf("]")
                if (start >= 0 && end > start) {
                    action.substring(start + 1, end)
                } else {
                    "Unknown"
                }
            }

            else -> "Unknown"
        }

        // 清理action，移除source标记
        val cleanAction = if (action.contains("] ")) {
            action.substring(action.indexOf("] ") + 2)
        } else {
            action
        }

        // 创建广播数据
        val broadcastData = BroadcastData(
            timestamp = BroadcastDataManager.getCurrentTimestamp(),
            action = cleanAction,
            source = source,
            packageName = packageName,
            extras = extras,
            category = category,
            priority = priority
        )

        // 通过广播发送数据，避免跨进程单例问题
        sendBroadcastData(context, broadcastData)
    }

    fun logError(message: String) {
        val timestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] ERROR: $message"
        Log.e(TAG, logMessage)
    }

    fun logD(message: String) {
        val timestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] DEBUG: $message"
        Log.d(TAG, logMessage)
    }

    /**
     * 通过广播发送数据
     */
    private fun sendBroadcastData(context: Context?, broadcastData: BroadcastData) {

        if (context == null){
            Log.e(TAG,"Context 未初始化，无法发送广播数据")
            return
        }

        try {
            val intent = Intent(BROADCAST_DATA_ACTION).apply {
                putExtra("timestamp", broadcastData.timestamp)
                putExtra("action", broadcastData.action)
                putExtra("source", broadcastData.source)
                putExtra("packageName", broadcastData.packageName)
                putExtra("extras", broadcastData.extras)
                putExtra("category", broadcastData.category)
                putExtra("priority", broadcastData.priority)
                // 添加包名限制，确保只有本应用接收
                setPackage("com.xmbest.broadcastmonitor")
            }

            context.sendBroadcast(intent)
            logD("广播数据已发送: ${broadcastData.action}")
        } catch (e: Exception) {
            logError("发送广播数据失败: ${e.message}")
        }
    }
}