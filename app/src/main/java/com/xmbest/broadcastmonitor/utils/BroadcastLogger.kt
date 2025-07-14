package com.xmbest.broadcastmonitor.utils

import android.util.Log
import com.xmbest.broadcastmonitor.data.BroadcastData
import com.xmbest.broadcastmonitor.data.BroadcastDataManager
import java.text.SimpleDateFormat
import java.util.*

object BroadcastLogger {
    private const val TAG = "BroadcastMonitor"
    
    fun logBroadcast(
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
        
        // 创建广播数据并添加到数据管理器
        val broadcastData = BroadcastData(
            timestamp = BroadcastDataManager.getCurrentTimestamp(),
            action = cleanAction,
            source = source,
            packageName = packageName,
            extras = extras,
            category = category,
            priority = priority
        )
        
        BroadcastDataManager.addBroadcast(broadcastData)
    }
    
    fun logError(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] ERROR: $message"
        Log.e(TAG, logMessage)
    }

    fun logD(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] DEBUG: $message"
        Log.d(TAG, logMessage)
    }
}