package com.xmbest.broadcastmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xmbest.broadcastmonitor.data.BroadcastData
import com.xmbest.broadcastmonitor.data.BroadcastDataManager

/**
 * 广播数据接收器
 * 用于接收跨进程的广播数据并添加到数据管理器
 */
class BroadcastDataReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BroadcastDataReceiver"
        const val BROADCAST_DATA_ACTION = "com.xmbest.broadcastmonitor.BROADCAST_DATA"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != BROADCAST_DATA_ACTION) {
            return
        }
        
        try {
            // 提取广播数据
            val timestamp = intent.getStringExtra("timestamp") ?: return
            val action = intent.getStringExtra("action") ?: return
            val source = intent.getStringExtra("source") ?: return
            val packageName = intent.getStringExtra("packageName") ?: return
            val extras = intent.getStringExtra("extras") ?: ""
            val category = intent.getStringExtra("category") ?: "其他广播"
            val priority = intent.getIntExtra("priority", 5)
            
            // 创建广播数据对象
            val broadcastData = BroadcastData(
                timestamp = timestamp,
                action = action,
                source = source,
                packageName = packageName,
                extras = extras,
                category = category,
                priority = priority
            )
            
            // 添加到数据管理器
            BroadcastDataManager.addBroadcast(broadcastData)
            
            Log.d(TAG, "接收到广播数据: $action")
            
        } catch (e: Exception) {
            Log.e(TAG, "处理广播数据失败: ${e.message}")
        }
    }
}