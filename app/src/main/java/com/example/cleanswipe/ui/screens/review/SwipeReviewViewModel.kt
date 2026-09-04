package com.example.cleanswipe.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.data.repository.MediaRepository
import com.example.cleanswipe.ui.components.SwipeDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SwipeAction(
    val media: MediaItem,
    val direction: SwipeDirection
)

data class SwipeReviewUiState(
    val mediaList: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val pendingTrashList: List<MediaItem> = emptyList(),
    val keepList: List<MediaItem> = emptyList(),
    val actionHistory: List<SwipeAction> = emptyList(),
    val isBatchExecuting: Boolean = false,
    val isInstantTrashMode: Boolean = false
) {
    val currentMedia: MediaItem?
        get() = mediaList.getOrNull(currentIndex)

    val nextMedia: MediaItem?
        get() = mediaList.getOrNull(currentIndex + 1)

    val totalItems: Int
        get() = mediaList.size

    val isDeckFinished: Boolean
        get() = currentIndex >= mediaList.size && mediaList.isNotEmpty()

    val pendingTrashSizeBytes: Long
        get() = pendingTrashList.sumOf { it.size }
}

class SwipeReviewViewModel(
    val repository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwipeReviewUiState())
    val uiState: StateFlow<SwipeReviewUiState> = _uiState.asStateFlow()

    fun initialize(media: List<MediaItem>, initialIndex: Int = 0) {
        _uiState.update {
            it.copy(
                mediaList = media,
                currentIndex = initialIndex.coerceIn(0, (media.size - 1).coerceAtLeast(0)),
                pendingTrashList = emptyList(),
                keepList = emptyList(),
                actionHistory = emptyList()
            )
        }
    }

    fun handleSwipe(direction: SwipeDirection) {
        val currentState = _uiState.value
        val currentItem = currentState.currentMedia ?: return

        val newAction = SwipeAction(currentItem, direction)
        val updatedPendingTrash = if (direction == SwipeDirection.LEFT) {
            currentState.pendingTrashList + currentItem
        } else {
            currentState.pendingTrashList
        }

        val updatedKeep = if (direction == SwipeDirection.RIGHT) {
            currentState.keepList + currentItem
        } else {
            currentState.keepList
        }

        _uiState.update {
            it.copy(
                currentIndex = it.currentIndex + 1,
                pendingTrashList = updatedPendingTrash,
                keepList = updatedKeep,
                actionHistory = it.actionHistory + newAction
            )
        }
    }

    fun undo() {
        val currentState = _uiState.value
        if (currentState.actionHistory.isEmpty()) return

        val lastAction = currentState.actionHistory.last()
        val remainingHistory = currentState.actionHistory.dropLast(1)

        val updatedPendingTrash = if (lastAction.direction == SwipeDirection.LEFT) {
            currentState.pendingTrashList.filterNot { it.id == lastAction.media.id }
        } else {
            currentState.pendingTrashList
        }

        val updatedKeep = if (lastAction.direction == SwipeDirection.RIGHT) {
            currentState.keepList.filterNot { it.id == lastAction.media.id }
        } else {
            currentState.keepList
        }

        _uiState.update {
            it.copy(
                currentIndex = (it.currentIndex - 1).coerceAtLeast(0),
                pendingTrashList = updatedPendingTrash,
                keepList = updatedKeep,
                actionHistory = remainingHistory
            )
        }
    }

    fun toggleInstantTrashMode() {
        _uiState.update {
            it.copy(isInstantTrashMode = !it.isInstantTrashMode)
        }
    }

    fun clearPendingTrashAfterExecution() {
        _uiState.update {
            it.copy(pendingTrashList = emptyList())
        }
    }

    companion object {
        fun provideFactory(repository: MediaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SwipeReviewViewModel(repository) as T
                }
            }
    }
}
