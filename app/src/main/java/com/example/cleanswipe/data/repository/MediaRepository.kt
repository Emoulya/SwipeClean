package com.example.cleanswipe.data.repository

import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import com.example.cleanswipe.data.model.MediaFilter
import com.example.cleanswipe.data.model.MediaItem

interface MediaRepository {
    suspend fun getActiveMedia(filter: MediaFilter = MediaFilter.ALL): List<MediaItem>
    suspend fun getTrashedMedia(): List<MediaItem>
    fun createTrashRequest(uris: List<Uri>, isTrash: Boolean): IntentSenderRequest?
    fun createDeleteRequest(uris: List<Uri>): IntentSenderRequest?
}
