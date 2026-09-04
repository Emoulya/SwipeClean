package com.example.cleanswipe.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleanswipe.data.datasource.MediaStoreDataSource
import com.example.cleanswipe.data.preferences.SettingsManager
import com.example.cleanswipe.data.repository.MediaRepository
import com.example.cleanswipe.data.repository.MediaRepositoryImpl
import com.example.cleanswipe.ui.components.FloatingBottomBar
import com.example.cleanswipe.ui.components.MainTab
import com.example.cleanswipe.ui.screens.folder.FolderScreen
import com.example.cleanswipe.ui.screens.folder.FolderViewModel
import com.example.cleanswipe.ui.screens.gallery.GalleryScreen
import com.example.cleanswipe.ui.screens.gallery.GalleryViewModel
import com.example.cleanswipe.ui.screens.permission.PermissionScreen
import com.example.cleanswipe.ui.screens.review.SwipeReviewScreen
import com.example.cleanswipe.ui.screens.review.SwipeReviewViewModel
import com.example.cleanswipe.ui.screens.settings.SettingsScreen
import com.example.cleanswipe.ui.screens.trash.TrashBinScreen
import com.example.cleanswipe.ui.screens.trash.TrashBinViewModel
import kotlinx.coroutines.launch

enum class Screen {
    PERMISSION,
    MAIN,
    SWIPE_REVIEW,
    SETTINGS
}

@Composable
fun CleanSwipeApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Preferensi Pengaturan
    val settingsManager = remember { SettingsManager.getInstance(context) }

    // Repository & Data Source
    val repository: MediaRepository = remember {
        val dataSource = MediaStoreDataSource(context.applicationContext)
        MediaRepositoryImpl(dataSource)
    }

    // ViewModels
    val galleryViewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModel.provideFactory(repository)
    )
    val folderViewModel: FolderViewModel = viewModel(
        factory = FolderViewModel.provideFactory(repository)
    )
    val reviewViewModel: SwipeReviewViewModel = viewModel(
        factory = SwipeReviewViewModel.provideFactory(repository)
    )
    val trashViewModel: TrashBinViewModel = viewModel(
        factory = TrashBinViewModel.provideFactory(repository)
    )

    val trashState by trashViewModel.uiState.collectAsState()
    val folderState by folderViewModel.uiState.collectAsState()

    // Cek status izin media
    var currentScreen by remember {
        mutableStateOf(
            if (hasMediaPermission(context)) Screen.MAIN else Screen.PERMISSION
        )
    }

    // Pager State untuk Horizontal Slide antar Tab (Galeri, Folder, Sampah)
    val pagerState = rememberPagerState(
        initialPage = MainTab.GALLERY.ordinal,
        pageCount = { MainTab.entries.size }
    )

    // Muat data di background saat tab selesai bergeser (settled) tanpa mengganggu animasi slide
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                when (MainTab.entries[page]) {
                    MainTab.GALLERY -> galleryViewModel.refresh()
                    MainTab.FOLDERS -> folderViewModel.loadFolders(showLoading = false)
                    MainTab.TRASH -> trashViewModel.loadTrashedMedia(showLoading = false)
                }
            }
    }

    // Action Callback saat Scoped Storage dialog selesai
    var onActionSuccessCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Launcher untuk konfirmasi IntentSender Scoped Storage (createTrashRequest & createDeleteRequest)
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            galleryViewModel.refresh()
            folderViewModel.loadFolders()
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
            transitionSpec = {
                when {
                    // 1. Ke Settings (Slide in direction left, ease in)
                    initialState == Screen.MAIN && targetState == Screen.SETTINGS -> {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 300, easing = EaseIn)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseIn)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                    animationSpec = tween(durationMillis = 300, easing = EaseIn)
                                ) + fadeOut(animationSpec = tween(durationMillis = 200))
                            )
                    }

                    // 2. Back dari Settings (Slide out direction right, ease out)
                    initialState == Screen.SETTINGS && targetState == Screen.MAIN -> {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(durationMillis = 300, easing = EaseOut)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseOut)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(durationMillis = 300, easing = EaseOut)
                                ) + fadeOut(animationSpec = tween(durationMillis = 250))
                            )
                    }

                    // 3. Media di-klik -> Ke Swipe Review (Move in direction up, ease out)
                    targetState == Screen.SWIPE_REVIEW -> {
                        (slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(durationMillis = 320, easing = EaseOut)
                        ) + fadeIn(animationSpec = tween(durationMillis = 260, easing = EaseOut)))
                            .togetherWith(
                                scaleOut(
                                    targetScale = 0.92f,
                                    animationSpec = tween(durationMillis = 320, easing = EaseOut)
                                ) + fadeOut(animationSpec = tween(durationMillis = 200))
                            )
                    }

                    // 4. Back dari Swipe Review (Move out direction down, ease out)
                    initialState == Screen.SWIPE_REVIEW && targetState == Screen.MAIN -> {
                        (scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(durationMillis = 300, easing = EaseOut)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = EaseOut)))
                            .togetherWith(
                                slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(durationMillis = 300, easing = EaseOut)
                                ) + fadeOut(animationSpec = tween(durationMillis = 250))
                            )
                    }

                    // Transisi default (Permission <-> Main dll)
                    targetState == Screen.MAIN -> {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                    }

                    else -> {
                        (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)))
                            .togetherWith(fadeOut(animationSpec = tween(150)))
                    }
                }
            },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { screen ->
            when (screen) {
                Screen.PERMISSION -> {
                    PermissionScreen(
                        onPermissionGranted = {
                            currentScreen = Screen.MAIN
                            galleryViewModel.refresh()
                            folderViewModel.loadFolders()
                            trashViewModel.loadTrashedMedia()
                        }
                    )
                }

                Screen.MAIN -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // HorizontalPager untuk perpindahan antar tab via gestur slide / geser
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            userScrollEnabled = folderState.selectedAlbum == null,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (MainTab.entries[page]) {
                                MainTab.GALLERY -> {
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
                                        onNavigateToSettings = {
                                            currentScreen = Screen.SETTINGS
                                        }
                                    )
                                }

                                MainTab.FOLDERS -> {
                                    FolderScreen(
                                        viewModel = folderViewModel,
                                        onStartSwipeReview = { mediaList, initialIndex ->
                                            if (mediaList.isNotEmpty()) {
                                                reviewViewModel.initialize(mediaList, initialIndex)
                                                currentScreen = Screen.SWIPE_REVIEW
                                            } else {
                                                Toast.makeText(context, "Tidak ada media untuk disortir", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }

                                MainTab.TRASH -> {
                                    TrashBinScreen(
                                        viewModel = trashViewModel,
                                        onNavigateBack = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(MainTab.GALLERY.ordinal)
                                            }
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
                                            currentScreen = Screen.SETTINGS
                                        }
                                    )
                                }
                            }
                        }

                        // Floating Bottom Bar mengambang di atas konten tab
                        FloatingBottomBar(
                            currentTab = MainTab.entries[pagerState.currentPage],
                            onTabSelected = { tab ->
                                if (tab == MainTab.FOLDERS && pagerState.currentPage == MainTab.FOLDERS.ordinal) {
                                    folderViewModel.selectAlbum(null)
                                }
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.ordinal)
                                }
                            },
                            trashedCount = trashState.trashedMedia.size,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                Screen.SWIPE_REVIEW -> {
                    SwipeReviewScreen(
                        viewModel = reviewViewModel,
                        onNavigateBack = {
                            currentScreen = Screen.MAIN
                        },
                        onExecuteTrash = { uris, onSuccess ->
                            val request = repository.createTrashRequest(uris, isTrash = true)
                            if (request != null) {
                                onActionSuccessCallback = {
                                    reviewViewModel.clearPendingTrashAfterExecution()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("${uris.size} media berhasil dipindahkan ke Sampah")
                                    }
                                    onSuccess?.invoke()
                                }
                                intentSenderLauncher.launch(request)
                            }
                        }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        settingsManager = settingsManager,
                        onNavigateBack = {
                            currentScreen = Screen.MAIN
                        }
                    )
                }
            }
        }
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
