package com.xmbest.broadcastmonitor.data

import android.annotation.SuppressLint
import com.xmbest.broadcastmonitor.constants.AppConstants
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Broadcast data manager
 * Responsible for managing broadcast data flow and storage
 */
object BroadcastDataManager {

    private const val TAG = "BroadcastDataManager"

    val coroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName(TAG))

    // Broadcast data flow
    private val _broadcastFlow = MutableStateFlow<List<BroadcastData>>(emptyList())
    val broadcastFlow = _broadcastFlow.asStateFlow()

    // Broadcast data list - using thread-safe queue
    private val _broadcastList = ConcurrentLinkedQueue<BroadcastData>()

    /**
     * Add broadcast data
     */
    fun addBroadcast(broadcastData: BroadcastData) {
        _broadcastList.offer(broadcastData) // Add to queue

        // Limit list size to avoid excessive memory usage
        while (_broadcastList.size > AppConstants.MAX_BROADCAST_LIST_SIZE) {
            _broadcastList.poll()
        }
        val list = _broadcastList.toList().reversed() // Reverse to show newest first
        // Send entire list to Flow
        coroutineScope.launch {
            _broadcastFlow.emit(list)
        }
    }

    /**
     * Clear broadcast data
     */
    fun clearBroadcasts() {
        _broadcastList.clear()
        coroutineScope.launch {
            _broadcastFlow.emit(emptyList())
        }
    }

    /**
     * Get current broadcast data list
     */
    fun getBroadcastList(): List<BroadcastData> {
        return _broadcastList.toList().reversed() // Newest first
    }

    /**
     * Get broadcast data count
     */
    fun getBroadcastCount(): Int {
        return _broadcastList.size
    }

    /**
     * Get current timestamp
     * Optimization: Use thread-safe formatter
     */
    fun getCurrentTimestamp(): String {
        return synchronized(dateFormatter) {
            dateFormatter.format(Date())
        }
    }

    /**
     * Thread-safe date formatter
     */
    @SuppressLint("ConstantLocale")
    private val dateFormatter = SimpleDateFormat(
        AppConstants.File.DATE_FORMAT_PATTERN,
        Locale.getDefault()
    )
}

/**
 * Broadcast data model
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
     * Get display title
     */
    fun getDisplayTitle(): String {
        return "[$timestamp] $action"
    }

    /**
     * Get display details
     */
    fun getDisplayDetails(): String {
        return buildString {
            appendLine("Source: $source")
            appendLine("Package: $packageName")
            appendLine("Category: $category")
            if (extras.isNotEmpty() && extras != "No Extras") {
                appendLine("Extras:")
                appendLine(extras)
            }
        }.trim()
    }
}