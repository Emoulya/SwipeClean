package com.example.cleanswipe.ui.screens.gallery

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import kotlin.math.hypot
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.ui.components.FilterBar
import com.example.cleanswipe.ui.components.MediaGridItem
import com.example.cleanswipe.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onStartSwipeReview: (initialIndex: Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val isCompact = state.gridMode == GalleryGridMode.MONTHLY
    val columnCount = state.gridMode.columns

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CleanSwipe",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.totalCount} Media • ${Formatters.formatFileSize(state.totalSizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Tombol Toggle Cepat 4 Kolom (Harian) / 6 Kolom (Bulanan)
                    IconButton(onClick = { viewModel.toggleGridMode() }) {
                        Icon(
                            imageVector = if (state.gridMode == GalleryGridMode.DAILY) {
                                Icons.Rounded.GridView
                            } else {
                                Icons.Rounded.ViewModule
                            },
                            contentDescription = if (state.gridMode == GalleryGridMode.DAILY) {
                                "Ubah ke 6 Kolom (Bulanan)"
                            } else {
                                "Ubah ke 4 Kolom (Harian)"
                            }
                        )
                    }

                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Muat Ulang"
                        )
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Pengaturan"
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
            // Action Banner: Start Swipe Cleaning
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
                            text = "Mulai Bersihkan Memori",
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

            // Filter Bar
            FilterBar(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            // Content Body dengan Gestur Pinch dan Spread
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.allMedia.isEmpty()) {
                    EmptyGalleryView(
                        filterName = state.selectedFilter.label,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        contentPadding = PaddingValues(
                            start = if (isCompact) 6.dp else 10.dp,
                            end = if (isCompact) 6.dp else 10.dp,
                            bottom = 110.dp
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
                                            // Konsumsi event segera agar LazyVerticalGrid TIDAK melakukan scroll saat ada 2 jari!
                                            event.changes.forEach { it.consume() }

                                            if (!hasSwitched) {
                                                val p1 = activePointers[0].position
                                                val p2 = activePointers[1].position
                                                val currentSpan = hypot(p1.x - p2.x, p1.y - p2.y)

                                                if (initialSpan == null) {
                                                    initialSpan = currentSpan
                                                } else {
                                                    val deltaSpan = currentSpan - initialSpan
                                                    val currentMode = viewModel.uiState.value.gridMode

                                                    // Spread (jari merenggang menjauh / zoom in > thresholdPx): Beralih ke 4 Kolom (Harian)
                                                    if (deltaSpan > thresholdPx && currentMode == GalleryGridMode.MONTHLY) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        viewModel.setGridMode(GalleryGridMode.DAILY)
                                                        hasSwitched = true
                                                    }
                                                    // Pinch (jari mencubit merapat / zoom out < -thresholdPx): Beralih ke 6 Kolom (Bulanan)
                                                    else if (deltaSpan < -thresholdPx && currentMode == GalleryGridMode.DAILY) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        viewModel.setGridMode(GalleryGridMode.MONTHLY)
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
                        state.currentGroupedMedia.forEach { (dateHeader, itemsInGroup) ->
                            item(
                                key = "header_${state.gridMode}_$dateHeader",
                                span = { GridItemSpan(columnCount) }
                            ) {
                                Text(
                                    text = dateHeader,
                                    style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                            fadeOutSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                            placementSpec = spring(
                                                stiffness = Spring.StiffnessMediumLow,
                                                dampingRatio = Spring.DampingRatioLowBouncy
                                            )
                                        )
                                        .padding(
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
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                        fadeOutSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        placementSpec = spring(
                                            stiffness = Spring.StiffnessMediumLow,
                                            dampingRatio = Spring.DampingRatioLowBouncy
                                        )
                                    ),
                                    onClick = {
                                        val itemIndex = state.allMedia.indexOfFirst { it.id == item.id }
                                        onStartSwipeReview(if (itemIndex >= 0) itemIndex else 0)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryView(
    filterName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tidak Ada Media",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tidak ditemukan media untuk filter \"$filterName\".",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
