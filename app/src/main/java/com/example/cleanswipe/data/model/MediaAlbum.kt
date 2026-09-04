package com.example.cleanswipe.data.model

import android.net.Uri

data class MediaAlbum(
    val id: String,
    val name: String,
    val itemCount: Int,
    val coverUri: Uri?,
    val isPinned: Boolean = false,
    val mediaItems: List<MediaItem> = emptyList()
)
