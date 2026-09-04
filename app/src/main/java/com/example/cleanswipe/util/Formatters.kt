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

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))

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
            else -> monthYearFormat.format(Date(itemTimeMillis))
        }
    }

    fun groupMediaByDate(items: List<MediaItem>): Map<String, List<MediaItem>> {
        val groups = LinkedHashMap<String, MutableList<MediaItem>>()
        if (items.isEmpty()) return groups

        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDay = now.get(Calendar.DAY_OF_YEAR)

        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayYear = yesterday.get(Calendar.YEAR)
        val yesterdayDay = yesterday.get(Calendar.DAY_OF_YEAR)

        val itemCal = Calendar.getInstance()
        val monthCache = HashMap<Long, String>()

        for (item in items) {
            val timeMillis = item.dateModified * 1000L
            itemCal.timeInMillis = timeMillis
            val itemYear = itemCal.get(Calendar.YEAR)
            val itemDay = itemCal.get(Calendar.DAY_OF_YEAR)

            val header = when {
                itemYear == todayYear && itemDay == todayDay -> "Hari Ini"
                itemYear == yesterdayYear && itemDay == yesterdayDay -> "Kemarin"
                else -> {
                    val monthKey = itemYear * 100L + itemCal.get(Calendar.MONTH)
                    monthCache.getOrPut(monthKey) {
                        monthYearFormat.format(Date(timeMillis))
                    }
                }
            }
            val list = groups.getOrPut(header) { mutableListOf() }
            list.add(item)
        }
        return groups
    }
}
