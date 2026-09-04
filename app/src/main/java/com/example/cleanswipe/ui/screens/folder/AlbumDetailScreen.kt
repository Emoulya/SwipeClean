package com.example.cleanswipe.ui.screens.folder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleanswipe.data.model.MediaAlbum
import com.example.cleanswipe.ui.components.MediaGridItem
import com.example.cleanswipe.ui.screens.gallery.GalleryGridMode
import com.example.cleanswipe.util.Formatters
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: MediaAlbum,
    gridMode: GalleryGridMode,
    onToggleGridMode: () -> Unit,
    onSetGridMode: (GalleryGridMode) -> Unit,
    onNavigateBack: () -> Unit,
    onStartSwipeReview: (initialIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intersep tombol kembali Android fisik / gesture
    BackHandler(onBack = onNavigateBack)

    val currentGridMode by rememberUpdatedState(gridMode)
    val currentOnSetGridMode by rememberUpdatedState(onSetGridMode)

    val haptic = LocalHapticFeedback.current
    val isCompact = gridMode == GalleryGridMode.MONTHLY
    val columnCount = gridMode.columns

    val totalSizeBytes = remember(album.mediaItems) {
        album.mediaItems.sumOf { it.size }
    }

    val dailyGrouped = remember(album.mediaItems) {
        Formatters.groupMediaDaily(album.mediaItems)
    }
    val monthlyGrouped = remember(album.mediaItems) {
        Formatters.groupMediaMonthly(album.mediaItems)
    }
    val currentGrouped = if (gridMode == GalleryGridMode.DAILY) dailyGrouped else monthlyGrouped

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali ke Folder"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${album.itemCount} Media • ${Formatters.formatFileSize(totalSizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleGridMode) {
                        Icon(
                            imageVector = if (gridMode == GalleryGridMode.DAILY) {
                                Icons.Rounded.GridView
                            } else {
                                Icons.Rounded.ViewModule
                            },
                            contentDescription = if (gridMode == GalleryGridMode.DAILY) {
                                "Ubah ke 6 Kolom (Bulanan)"
                            } else {
                                "Ubah ke 4 Kolom (Harian)"
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (album.mediaItems.isNotEmpty()) {
                // Banner "Mulai Bersihkan Memori" untuk album ini
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bersihkan ${album.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Geser kartu: Kanan simpan, Kiri sampah",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onStartSwipeReview(0) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesomeMotion,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sortir",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Grid Media Album
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        contentPadding = PaddingValues(
                            start = if (isCompact) 6.dp else 10.dp,
                            end = if (isCompact) 6.dp else 10.dp,
                            bottom = 110.dp // Ruang ekstra agar tidak tertutup floating bottom bar
                        ),
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 5.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 5.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                val thresholdPx = 20.dp.toPx()
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    var initialSpan: Float? = null
                                    var hasSwitched = false

                                    do {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                        val activePointers = event.changes.filter { it.pressed }

                                        if (activePointers.size >= 2) {
                                            // Konsumsi event agar tidak bentrok dengan scroll grid saat 2 jari
                                            event.changes.forEach { it.consume() }

                                            if (!hasSwitched) {
                                                val p1 = activePointers[0].position
                                                val p2 = activePointers[1].position
                                                val currentSpan = hypot(p1.x - p2.x, p1.y - p2.y)

                                                if (initialSpan == null) {
                                                    initialSpan = currentSpan
                                                } else {
                                                    val deltaSpan = currentSpan - initialSpan
                                                    val mode = currentGridMode

                                                    // Spread (merenggang / zoom in): Beralih ke 4 Kolom (Harian)
                                                    if (deltaSpan > thresholdPx && mode == GalleryGridMode.MONTHLY) {
                                                         haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                         currentOnSetGridMode(GalleryGridMode.DAILY)
                                                         hasSwitched = true
                                                     }
                                                     // Pinch (mencubit / zoom out): Beralih ke 6 Kolom (Bulanan)
                                                     else if (deltaSpan < -thresholdPx && mode == GalleryGridMode.DAILY) {
                                                         haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                         currentOnSetGridMode(GalleryGridMode.MONTHLY)
                                                         hasSwitched = true
                                                     }
                                                }
                                            }
                                        } else {
                                            initialSpan = null
                                            if (activePointers.isEmpty()) {
                                                hasSwitched = false
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                    ) {
                        currentGrouped.forEach { (dateHeader, itemsInGroup) ->
                            item(
                                key = "album_header_${gridMode}_$dateHeader",
                                span = { GridItemSpan(columnCount) }
                            ) {
                                Text(
                                    text = dateHeader,
                                    style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(
                                        top = if (isCompact) 14.dp else 12.dp,
                                        bottom = 6.dp,
                                        start = 4.dp
                                    )
                                )
                            }

                            items(itemsInGroup, key = { it.id }) { item ->
                                MediaGridItem(
                                    item = item,
                                    isCompact = isCompact,
                                    onClick = {
                                        val itemIndex = album.mediaItems.indexOfFirst { it.id == item.id }
                                        onStartSwipeReview(if (itemIndex >= 0) itemIndex else 0)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Empty state jika album tidak memiliki media
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Folder Kosong",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tidak ada foto atau video di folder \"${album.name}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
