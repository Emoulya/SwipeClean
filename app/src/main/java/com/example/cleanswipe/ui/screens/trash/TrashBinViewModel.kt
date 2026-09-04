package com.example.cleanswipe.ui.screens.trash

import android.net.Uri
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrashBinUiState(
    val isLoading: Boolean = true,
    val trashedMedia: List<MediaItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val totalSizeBytes: Long = 0L
) {
    val selectedMedia: List<MediaItem>
        get() = trashedMedia.filter { it.id in selectedIds }

    val selectedSizeBytes: Long
        get() = selectedMedia.sumOf { it.size }

    val isAllSelected: Boolean
        get() = trashedMedia.isNotEmpty() && selectedIds.size == trashedMedia.size
}

class TrashBinViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashBinUiState())
    val uiState: StateFlow<TrashBinUiState> = _uiState.asStateFlow()

    init {
        loadTrashedMedia()
    }

    fun loadTrashedMedia(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading || _uiState.value.trashedMedia.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            try {
                val list = repository.getTrashedMedia()
                val totalSize = list.sumOf { it.size }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trashedMedia = list,
                        selectedIds = emptySet(),
                        totalSizeBytes = totalSize
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleSelection(id: Long) {
        _uiState.update {
            val newSelected = it.selectedIds.toMutableSet()
            if (newSelected.contains(id)) {
                newSelected.remove(id)
            } else {
                newSelected.add(id)
            }
            it.copy(selectedIds = newSelected)
        }
    }

    fun toggleSelectAll() {
        _uiState.update {
            if (it.isAllSelected) {
                it.copy(selectedIds = emptySet())
            } else {
                it.copy(selectedIds = it.trashedMedia.map { m -> m.id }.toSet())
            }
        }
    }

    fun createRestoreRequest(uris: List<Uri>): IntentSenderRequest? {
        // isTrash = false untuk memulihkan (Restore)
        return repository.createTrashRequest(uris, isTrash = false)
    }

    fun createEmptyTrashRequest(uris: List<Uri>): IntentSenderRequest? {
        // Permanently delete via MediaStore.createDeleteRequest
        return repository.createDeleteRequest(uris)
    }

    companion object {
        fun provideFactory(repository: MediaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TrashBinViewModel(repository) as T
                }
            }
    }
}
