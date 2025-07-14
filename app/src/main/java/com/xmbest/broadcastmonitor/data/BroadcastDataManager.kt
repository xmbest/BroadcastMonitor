package com.xmbest.broadcastmonitor.data

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 广播数据管理器
 * 负责管理广播数据流和存储
 */
object BroadcastDataManager {

    private const val TAG = "BroadcastDataManager"

    val coroutineScope = CoroutineScope(Dispatchers.Main + CoroutineName(TAG))

    // 广播数据流
    private val _broadcastFlow = MutableSharedFlow<List<BroadcastData>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val broadcastFlow = _broadcastFlow.asSharedFlow()

    // 广播数据列表
    private val _broadcastList = mutableListOf<BroadcastData>()

    /**
     * 添加广播数据
     */
    fun addBroadcast(broadcastData: BroadcastData) {
        _broadcastList.add(0, broadcastData) // 添加到列表顶部

        // 限制列表大小，避免内存过多占用
        if (_broadcastList.size > 1000) {
            _broadcastList.removeAt(_broadcastList.size - 1)
        }
        val list = _broadcastList.toList()
        // 发送整个列表到Flow
        coroutineScope.launch {
            _broadcastFlow.emit(list)
        }
    }

    /**
     * 清空广播数据
     */
    fun clearBroadcasts() {
        _broadcastList.clear()
        coroutineScope.launch {
            _broadcastFlow.emit(emptyList())
        }
    }

    /**
     * 获取格式化的时间戳
     */
    fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    }
}

/**
 * 广播数据模型
 */
data class BroadcastData(
    val timestamp: String,
    val action: String,
    val source: String,
    val packageName: String,
    val extras: String,
    val category: String,
    val priority: Int
) {
    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        return "[$timestamp] $action"
    }

    /**
     * 获取显示详情
     */
    fun getDisplayDetails(): String {
        return buildString {
            appendLine("来源: $source")
            appendLine("包名: $packageName")
            appendLine("分类: $category")
            if (extras.isNotEmpty() && extras != "无Extras") {
                appendLine("附加数据:")
                appendLine(extras)
            }
        }.trim()
    }
}