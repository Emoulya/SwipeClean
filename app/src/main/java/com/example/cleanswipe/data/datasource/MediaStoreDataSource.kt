package com.example.cleanswipe.data.datasource

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import com.example.cleanswipe.data.model.MediaFilter
import com.example.cleanswipe.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreDataSource(private val context: Context) {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    suspend fun queryActiveMedia(filter: MediaFilter = MediaFilter.ALL): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val mediaList = mutableListOf<MediaItem>()

            if (filter != MediaFilter.VIDEOS) {
                mediaList.addAll(queryImages(isTrashed = false))
            }

            if (filter != MediaFilter.SCREENSHOTS) {
                mediaList.addAll(queryVideos(isTrashed = false))
            }

            // Urutkan berdasarkan tanggal termutakhir
            val sortedList = mediaList.sortedByDescending { it.dateModified }

            // Terapkan filter khusus
            when (filter) {
                MediaFilter.ALL -> sortedList
                MediaFilter.SCREENSHOTS -> sortedList.filter { item ->
                    item.bucketName?.contains("Screenshots", ignoreCase = true) == true ||
                            item.displayName.contains("Screenshot", ignoreCase = true)
                }
                MediaFilter.VIDEOS -> sortedList.filter { it.isVideo }
                MediaFilter.LARGE_FILES -> sortedList.filter { it.size >= 20 * 1024 * 1024L } // >= 20 MB
            }
        }

    suspend fun queryTrashedMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val trashedList = mutableListOf<MediaItem>()
        trashedList.addAll(queryImages(isTrashed = true))
        trashedList.addAll(queryVideos(isTrashed = true))
        trashedList.sortedByDescending { it.dateModified }
    }

    private fun queryImages(isTrashed: Boolean): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.IS_TRASHED
        )

        val bundle = Bundle().apply {
            putInt(
                MediaStore.QUERY_ARG_MATCH_TRASHED,
                if (isTrashed) MediaStore.MATCH_ONLY else MediaStore.MATCH_EXCLUDE
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Images.Media.DATE_MODIFIED)
            )
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
        }

        try {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                bundle,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val trashedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.IS_TRASHED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val trashedVal = cursor.getInt(trashedColumn) == 1

                    items.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = cursor.getString(nameColumn) ?: "IMG_$id",
                            size = cursor.getLong(sizeColumn),
                            dateAdded = cursor.getLong(dateAddedColumn),
                            dateModified = cursor.getLong(dateModifiedColumn),
                            mimeType = cursor.getString(mimeTypeColumn) ?: "image/*",
                            durationMs = null,
                            isVideo = false,
                            bucketName = cursor.getString(bucketColumn),
                            isTrashed = trashedVal
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return items
    }

    private fun queryVideos(isTrashed: Boolean): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.IS_TRASHED
        )

        val bundle = Bundle().apply {
            putInt(
                MediaStore.QUERY_ARG_MATCH_TRASHED,
                if (isTrashed) MediaStore.MATCH_ONLY else MediaStore.MATCH_EXCLUDE
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Video.Media.DATE_MODIFIED)
            )
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
        }

        try {
            contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                bundle,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val trashedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.IS_TRASHED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val trashedVal = cursor.getInt(trashedColumn) == 1

                    items.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = cursor.getString(nameColumn) ?: "VID_$id",
                            size = cursor.getLong(sizeColumn),
                            dateAdded = cursor.getLong(dateAddedColumn),
                            dateModified = cursor.getLong(dateModifiedColumn),
                            mimeType = cursor.getString(mimeTypeColumn) ?: "video/*",
                            durationMs = cursor.getLong(durationColumn),
                            isVideo = true,
                            bucketName = cursor.getString(bucketColumn),
                            isTrashed = trashedVal
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return items
    }

    /**
     * Membuat IntentSenderRequest untuk memindahkan media ke Sampah atau memulihkan (Restore).
     * @param uris Daftar URI yang akan diubah status sampahnya
     * @param isTrash true untuk memindahkan ke Sampah, false untuk memulihkan (Restore)
     */
    fun createTrashRequest(uris: List<Uri>, isTrash: Boolean): IntentSenderRequest? {
        if (uris.isEmpty()) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createTrashRequest(contentResolver, uris, isTrash)
            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
        } else {
            null
        }
    }

    /**
     * Membuat IntentSenderRequest untuk menghapus media secara permanen (Empty Trash).
     */
    fun createDeleteRequest(uris: List<Uri>): IntentSenderRequest? {
        if (uris.isEmpty()) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)
            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
        } else {
            null
        }
    }
}
