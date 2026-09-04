package com.example.cleanswipe.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cleanswipe.data.model.MediaFilter
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.data.repository.MediaRepository
import com.example.cleanswipe.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryUiState(
    val isLoading: Boolean = true,
    val selectedFilter: MediaFilter = MediaFilter.ALL,
    val groupedMedia: Map<String, List<MediaItem>> = emptyMap(),
    val allMedia: List<MediaItem> = emptyList(),
    val totalCount: Int = 0,
    val totalSizeBytes: Long = 0L,
    val trashedCount: Int = 0,
    val trashedSizeBytes: Long = 0L
)

class GalleryViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadMedia()
    }

    fun loadMedia(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading || _uiState.value.allMedia.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            try {
                val media = repository.getActiveMedia(_uiState.value.selectedFilter)
                val trashed = repository.getTrashedMedia()

                val grouped = Formatters.groupMediaByDate(media)
                val totalSize = media.sumOf { it.size }
                val trashedSize = trashed.sumOf { it.size }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allMedia = media,
                        groupedMedia = grouped,
                        totalCount = media.size,
                        totalSizeBytes = totalSize,
                        trashedCount = trashed.size,
                        trashedSizeBytes = trashedSize
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setFilter(filter: MediaFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        loadMedia(showLoading = true)
    }

    fun refresh(showLoading: Boolean = false) {
        loadMedia(showLoading = showLoading)
    }

    companion object {
        fun provideFactory(repository: MediaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GalleryViewModel(repository) as T
                }
            }
    }
}
