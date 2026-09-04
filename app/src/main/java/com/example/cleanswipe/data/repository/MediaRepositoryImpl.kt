package com.example.cleanswipe.data.repository

import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import com.example.cleanswipe.data.datasource.MediaStoreDataSource
import com.example.cleanswipe.data.model.MediaFilter
import com.example.cleanswipe.data.model.MediaItem

class MediaRepositoryImpl(
    private val mediaStoreDataSource: MediaStoreDataSource
) : MediaRepository {

    override suspend fun getActiveMedia(filter: MediaFilter): List<MediaItem> {
        return mediaStoreDataSource.queryActiveMedia(filter)
    }

    override suspend fun getTrashedMedia(): List<MediaItem> {
        return mediaStoreDataSource.queryTrashedMedia()
    }

    override fun createTrashRequest(uris: List<Uri>, isTrash: Boolean): IntentSenderRequest? {
        return mediaStoreDataSource.createTrashRequest(uris, isTrash)
    }

    override fun createDeleteRequest(uris: List<Uri>): IntentSenderRequest? {
        return mediaStoreDataSource.createDeleteRequest(uris)
    }
}
