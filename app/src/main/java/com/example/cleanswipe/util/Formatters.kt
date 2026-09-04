package com.example.cleanswipe.util

import com.example.cleanswipe.data.model.MediaItem
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        val symbols = java.text.DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.#", symbols)
        return "${df.format(value)} ${units[index]}"
    }

    fun formatDuration(durationMs: Long?): String {
        if (durationMs == null || durationMs <= 0) return ""
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun formatDateHeader(dateModifiedSeconds: Long): String {
        val itemTimeMillis = dateModifiedSeconds * 1000L
        val itemCal = Calendar.getInstance().apply { timeInMillis = itemTimeMillis }
        val nowCal = Calendar.getInstance()

        val isSameYear = itemCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        val isSameDay = isSameYear && itemCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterdayCal.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                yesterdayCal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Hari Ini"
            isYesterday -> "Kemarin"
            isSameYear -> {
                val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                sdf.format(Date(itemTimeMillis))
            }
            else -> {
                val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                sdf.format(Date(itemTimeMillis))
            }
        }
    }

    fun groupMediaByDate(items: List<MediaItem>): Map<String, List<MediaItem>> {
        val groups = LinkedHashMap<String, MutableList<MediaItem>>()
        for (item in items) {
            val header = formatDateHeader(item.dateModified)
            val list = groups.getOrPut(header) { mutableListOf() }
            list.add(item)
        }
        return groups
    }
}
