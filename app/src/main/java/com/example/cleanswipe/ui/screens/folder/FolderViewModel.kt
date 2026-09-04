package com.example.cleanswipe.ui.screens.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cleanswipe.data.model.MediaAlbum
import com.example.cleanswipe.data.model.MediaFilter
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.data.repository.MediaRepository
import com.example.cleanswipe.ui.screens.gallery.GalleryGridMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FolderUiState(
    val isLoading: Boolean = true,
    val pinnedAlbums: List<MediaAlbum> = emptyList(),
    val regularAlbums: List<MediaAlbum> = emptyList(),
    val selectedAlbum: MediaAlbum? = null,
    val albumGridMode: GalleryGridMode = GalleryGridMode.DAILY
)

class FolderViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allMedia = repository.getActiveMedia(MediaFilter.ALL)
                
                // 1. Siapkan 4 Pinned Albums
                val allPhotosCover = allMedia.firstOrNull()?.uri
                val allPhotosAlbum = MediaAlbum(
                    id = "pinned_all",
                    name = "All photos",
                    itemCount = allMedia.size,
                    coverUri = allPhotosCover,
                    isPinned = true,
                    mediaItems = allMedia
                )

                val cameraItems = allMedia.filter { 
                    it.bucketName?.contains("Camera", ignoreCase = true) == true
                }
                val cameraAlbum = MediaAlbum(
                    id = "pinned_camera",
                    name = "Camera",
                    itemCount = cameraItems.size,
                    coverUri = cameraItems.firstOrNull()?.uri,
                    isPinned = true,
                    mediaItems = cameraItems
                )

                val screenshotItems = allMedia.filter {
                    it.bucketName?.contains("Screenshots", ignoreCase = true) == true ||
                            it.displayName.contains("Screenshot", ignoreCase = true)
                }
                val screenshotAlbum = MediaAlbum(
                    id = "pinned_screenshots",
                    name = "Screenshots",
                    itemCount = screenshotItems.size,
                    coverUri = screenshotItems.firstOrNull()?.uri,
                    isPinned = true,
                    mediaItems = screenshotItems
                )

                val videoItems = allMedia.filter { it.isVideo }
                val videoAlbum = MediaAlbum(
                    id = "pinned_videos",
                    name = "Videos",
                    itemCount = videoItems.size,
                    coverUri = videoItems.firstOrNull()?.uri,
                    isPinned = true,
                    mediaItems = videoItems
                )

                val pinned = listOf(allPhotosAlbum, cameraAlbum, screenshotAlbum, videoAlbum)

                // 2. Siapkan Albums reguler dikelompokkan berdasarkan bucketName
                val groupedByBucket = allMedia.groupBy { it.bucketName ?: "Lainnya" }
                val albums = groupedByBucket.map { (bucketName, items) ->
                    MediaAlbum(
                        id = "album_$bucketName",
                        name = bucketName,
                        itemCount = items.size,
                        coverUri = items.firstOrNull()?.uri,
                        isPinned = false,
                        mediaItems = items
                    )
                }.sortedByDescending { it.itemCount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        pinnedAlbums = pinned,
                        regularAlbums = albums
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAlbum(album: MediaAlbum?) {
        _uiState.update { it.copy(selectedAlbum = album) }
    }

    fun toggleAlbumGridMode() {
        val nextMode = if (_uiState.value.albumGridMode == GalleryGridMode.DAILY) {
            GalleryGridMode.MONTHLY
        } else {
            GalleryGridMode.DAILY
        }
        _uiState.update { it.copy(albumGridMode = nextMode) }
    }

    fun setAlbumGridMode(mode: GalleryGridMode) {
        if (_uiState.value.albumGridMode == mode) return
        _uiState.update { it.copy(albumGridMode = mode) }
    }

    companion object {
        fun provideFactory(repository: MediaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FolderViewModel(repository) as T
                }
            }
    }
}
