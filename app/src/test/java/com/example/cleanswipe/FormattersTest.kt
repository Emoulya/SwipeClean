package com.example.cleanswipe

import android.net.Uri
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testFormatFileSize() {
        assertEquals("0 B", Formatters.formatFileSize(0))
        assertEquals("500 B", Formatters.formatFileSize(500))
        assertEquals("1 KB", Formatters.formatFileSize(1024))
        assertEquals("1.5 KB", Formatters.formatFileSize(1536))
        assertEquals("1 MB", Formatters.formatFileSize(1024 * 1024))
        assertEquals("20 MB", Formatters.formatFileSize(20 * 1024 * 1024))
        assertEquals("1.5 GB", Formatters.formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testFormatDuration() {
        assertEquals("", Formatters.formatDuration(null))
        assertEquals("", Formatters.formatDuration(0))
        assertEquals("00:45", Formatters.formatDuration(45_000))
        assertEquals("01:30", Formatters.formatDuration(90_000))
        assertEquals("12:05", Formatters.formatDuration(725_000))
    }
}
