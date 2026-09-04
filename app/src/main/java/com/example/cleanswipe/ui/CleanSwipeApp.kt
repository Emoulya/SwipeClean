package com.example.cleanswipe.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleanswipe.data.datasource.MediaStoreDataSource
import com.example.cleanswipe.data.repository.MediaRepository
import com.example.cleanswipe.data.repository.MediaRepositoryImpl
import com.example.cleanswipe.ui.screens.gallery.GalleryScreen
import com.example.cleanswipe.ui.screens.gallery.GalleryViewModel
import com.example.cleanswipe.ui.screens.permission.PermissionScreen
import com.example.cleanswipe.ui.screens.review.SwipeReviewScreen
import com.example.cleanswipe.ui.screens.review.SwipeReviewViewModel
import com.example.cleanswipe.ui.screens.settings.RetentionSettingsSheet
import com.example.cleanswipe.ui.screens.trash.TrashBinScreen
import com.example.cleanswipe.ui.screens.trash.TrashBinViewModel
import kotlinx.coroutines.launch

enum class Screen {
    PERMISSION,
    GALLERY,
    SWIPE_REVIEW,
    TRASH_BIN
}

@Composable
fun CleanSwipeApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Repository & Data Source
    val repository: MediaRepository = remember {
        val dataSource = MediaStoreDataSource(context.applicationContext)
        MediaRepositoryImpl(dataSource)
    }

    // ViewModels
    val galleryViewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModel.provideFactory(repository)
    )
    val reviewViewModel: SwipeReviewViewModel = viewModel(
        factory = SwipeReviewViewModel.provideFactory(repository)
    )
    val trashViewModel: TrashBinViewModel = viewModel(
        factory = TrashBinViewModel.provideFactory(repository)
    )

    // Cek status izin media
    var currentScreen by remember {
        mutableStateOf(
            if (hasMediaPermission(context)) Screen.GALLERY else Screen.PERMISSION
        )
    }

    // Siklus retensi sampah (default 30 hari)
    var retentionDays by remember { mutableIntStateOf(30) }
    var showRetentionSheet by remember { mutableStateOf(false) }

    // Action Callback saat Scoped Storage dialog selesai
    var onActionSuccessCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Launcher untuk konfirmasi IntentSender Scoped Storage (createTrashRequest & createDeleteRequest)
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            galleryViewModel.refresh()
            trashViewModel.loadTrashedMedia()
            onActionSuccessCallback?.invoke()
            onActionSuccessCallback = null
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Operasi dibatalkan atau tidak disetujui")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { screen ->
            when (screen) {
                Screen.PERMISSION -> {
                    PermissionScreen(
                        onPermissionGranted = {
                            currentScreen = Screen.GALLERY
                            galleryViewModel.refresh()
                        }
                    )
                }

                Screen.GALLERY -> {
                    GalleryScreen(
                        viewModel = galleryViewModel,
                        onStartSwipeReview = { initialIndex ->
                            val currentList = galleryViewModel.uiState.value.allMedia
                            if (currentList.isNotEmpty()) {
                                reviewViewModel.initialize(currentList, initialIndex)
                                currentScreen = Screen.SWIPE_REVIEW
                            } else {
                                Toast.makeText(context, "Tidak ada media untuk disortir", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onNavigateToTrashBin = {
                            trashViewModel.loadTrashedMedia()
                            currentScreen = Screen.TRASH_BIN
                        }
                    )
                }

                Screen.SWIPE_REVIEW -> {
                    SwipeReviewScreen(
                        viewModel = reviewViewModel,
                        onNavigateBack = {
                            currentScreen = Screen.GALLERY
                            galleryViewModel.refresh()
                        },
                        onExecuteTrash = { uris ->
                            val request = repository.createTrashRequest(uris, isTrash = true)
                            if (request != null) {
                                onActionSuccessCallback = {
                                    reviewViewModel.clearPendingTrashAfterExecution()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("${uris.size} media berhasil dipindahkan ke Sampah")
                                    }
                                }
                                intentSenderLauncher.launch(request)
                            }
                        }
                    )
                }

                Screen.TRASH_BIN -> {
                    TrashBinScreen(
                        viewModel = trashViewModel,
                        onNavigateBack = {
                            currentScreen = Screen.GALLERY
                            galleryViewModel.refresh()
                        },
                        onRestoreMedia = { uris ->
                            val request = trashViewModel.createRestoreRequest(uris)
                            if (request != null) {
                                onActionSuccessCallback = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("${uris.size} media berhasil dipulihkan ke Galeri")
                                    }
                                }
                                intentSenderLauncher.launch(request)
                            }
                        },
                        onPermanentDeleteMedia = { uris ->
                            val request = trashViewModel.createEmptyTrashRequest(uris)
                            if (request != null) {
                                onActionSuccessCallback = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("${uris.size} media dihapus permanen")
                                    }
                                }
                                intentSenderLauncher.launch(request)
                            }
                        },
                        onOpenRetentionSettings = {
                            showRetentionSheet = true
                        }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet untuk Pengaturan Retensi Sampah
    if (showRetentionSheet) {
        RetentionSettingsSheet(
            currentDays = retentionDays,
            onSaveRetentionDays = { newDays ->
                retentionDays = newDays
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Siklus retensi diperbarui ke $newDays hari")
                }
            },
            onDismiss = { showRetentionSheet = false }
        )
    }
}

private fun hasMediaPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasImages = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
        val hasVideos = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED
        hasImages && hasVideos
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}
