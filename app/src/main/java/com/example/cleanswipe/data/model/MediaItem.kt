package com.example.cleanswipe.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val mimeType: String,
    val durationMs: Long? = null,
    val isVideo: Boolean = false,
    val bucketName: String? = null,
    val isTrashed: Boolean = false
)

enum class MediaFilter(val label: String) {
    ALL("Semua"),
    SCREENSHOTS("Tangkapan Layar"),
    VIDEOS("Video"),
    LARGE_FILES("File Besar (>20MB)")
}
