package com.example.cleanswipe.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.Dimension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Custom Coil Fetcher berkinerja tinggi yang memanfaatkan API hardware-accelerated
 * ContentResolver.loadThumbnail() bawaan Android 10+ (API 29+).
 *
 * Mengambil thumbnail langsung dari cache OS Android untuk foto dan video lokal,
 * tanpa perlu membuka dan mendecode berkas 50MP penuh atau memanggil MediaMetadataRetriever.
 */
class MediaStoreThumbnailFetcher(
    private val context: Context,
    private val data: Uri,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        return withContext(Dispatchers.IO) {
            try {
                val width = when (val w = options.size.width) {
                    is Dimension.Pixels -> w.px.coerceIn(128, 512)
                    else -> 256
                }
                val height = when (val h = options.size.height) {
                    is Dimension.Pixels -> h.px.coerceIn(128, 512)
                    else -> 256
                }
                val targetSize = Size(width, height)
                val signal = CancellationSignal()

                val bitmap = context.contentResolver.loadThumbnail(data, targetSize, signal)
                DrawableResult(
                    drawable = BitmapDrawable(context.resources, bitmap),
                    isSampled = true,
                    dataSource = DataSource.DISK
                )
            } catch (e: Exception) {
                // Jika thumbnail OS gagal, kembalikan null agar Coil fallback ke decoder standar
                null
            }
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (!isMediaStoreUri(data)) return null
            return MediaStoreThumbnailFetcher(context, data, options)
        }

        private fun isMediaStoreUri(uri: Uri): Boolean {
            return uri.scheme == ContentResolver.SCHEME_CONTENT &&
                    uri.authority == MediaStore.AUTHORITY
        }
    }
}
