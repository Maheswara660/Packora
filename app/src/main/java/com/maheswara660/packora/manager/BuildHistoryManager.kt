package com.maheswara660.packora.manager

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val targetUrl: String,
    val versionCode: Int,
    val versionName: String,
    val isDesktopMode: Boolean,
    val browserEngine: String,
    val allowCopying: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val apkPath: String? = null,
    val iconPath: String? = null
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

class BuildHistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("packora_build_history_prefs", Context.MODE_PRIVATE)

    fun addHistoryItem(item: HistoryItem) {
        val items = getHistoryItems().toMutableList()
        items.removeAll { it.packageName == item.packageName || it.id == item.id }
        items.add(0, item)
        saveHistoryItems(items)
    }

    fun getHistoryItems(): List<HistoryItem> {
        val jsonStr = prefs.getString("history_items_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        appName = obj.optString("appName", "Web App"),
                        packageName = obj.optString("packageName", ""),
                        targetUrl = obj.optString("targetUrl", ""),
                        versionCode = obj.optInt("versionCode", 1),
                        versionName = obj.optString("versionName", "1.0.0"),
                        isDesktopMode = obj.optBoolean("isDesktopMode", false),
                        browserEngine = obj.optString("browserEngine", "SYSTEM_DEFAULT"),
                        allowCopying = obj.optBoolean("allowCopying", false),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        apkPath = if (obj.has("apkPath")) obj.getString("apkPath") else null,
                        iconPath = if (obj.has("iconPath")) obj.getString("iconPath") else null
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteHistoryItem(id: String) {
        val items = getHistoryItems().toMutableList()
        items.removeAll { it.id == id }
        saveHistoryItems(items)
    }

    fun clearAllHistory() {
        prefs.edit().remove("history_items_json").apply()
    }

    private fun saveHistoryItems(items: List<HistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("appName", item.appName)
                put("packageName", item.packageName)
                put("targetUrl", item.targetUrl)
                put("versionCode", item.versionCode)
                put("versionName", item.versionName)
                put("isDesktopMode", item.isDesktopMode)
                put("browserEngine", item.browserEngine)
                put("allowCopying", item.allowCopying)
                put("timestamp", item.timestamp)
                if (item.apkPath != null) put("apkPath", item.apkPath)
                if (item.iconPath != null) put("iconPath", item.iconPath)
            }
            array.put(obj)
        }
        prefs.edit().putString("history_items_json", array.toString()).apply()
    }
}
