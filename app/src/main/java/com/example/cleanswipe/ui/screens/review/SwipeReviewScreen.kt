package com.example.cleanswipe.ui.screens.review

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleanswipe.data.model.MediaItem
import com.example.cleanswipe.ui.components.SwipeCardDeck
import com.example.cleanswipe.ui.components.SwipeDirection
import com.example.cleanswipe.util.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeReviewScreen(
    viewModel: SwipeReviewViewModel,
    onNavigateBack: () -> Unit,
    onExecuteTrash: (uris: List<Uri>) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var programmaticSwipeTrigger by remember { mutableStateOf<SwipeDirection?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentNumber = if (state.totalItems > 0) (state.currentIndex + 1).coerceAtMost(state.totalItems) else 0
                    Column {
                        Text(
                            text = "Sortir Cepat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currentNumber dari ${state.totalItems} Media",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showInfoDialog = true },
                        enabled = state.currentMedia != null
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Detail Info Media"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tombol Eksekusi Batch Sampah (muncul jika ada antrean sampah)
                if (state.pendingTrashList.isNotEmpty()) {
                    Button(
                        onClick = {
                            val uris = state.pendingTrashList.map { it.uri }
                            onExecuteTrash(uris)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pindahkan ke Sampah (${state.pendingTrashList.size} foto • ${Formatters.formatFileSize(state.pendingTrashSizeBytes)})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Tombol Kontrol Ergonomis Bawah (Satu Tangan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Hapus (Kiri)
                    FilledIconButton(
                        onClick = { programmaticSwipeTrigger = SwipeDirection.LEFT },
                        enabled = state.currentMedia != null,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Hapus (Swipe Kiri)",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Tombol Undo
                    FilledIconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.actionHistory.isNotEmpty(),
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = "Batalkan (Undo)",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Tombol Simpan (Kanan)
                    FilledIconButton(
                        onClick = { programmaticSwipeTrigger = SwipeDirection.RIGHT },
                        enabled = state.currentMedia != null,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            contentColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Simpan (Swipe Kanan)",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isDeckFinished) {
                DeckFinishedView(
                    savedCount = state.keepList.size,
                    trashCount = state.pendingTrashList.size,
                    trashSize = state.pendingTrashSizeBytes,
                    onApplyTrash = {
                        val uris = state.pendingTrashList.map { it.uri }
                        onExecuteTrash(uris)
                    },
                    onBackToGallery = onNavigateBack,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                SwipeCardDeck(
                    currentMedia = state.currentMedia,
                    nextMedia = state.nextMedia,
                    onSwiped = { direction ->
                        viewModel.handleSwipe(direction)
                    },
                    programmaticSwipeTrigger = programmaticSwipeTrigger,
                    onProgrammaticSwipeHandled = {
                        programmaticSwipeTrigger = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Dialog Info Detail Media
    if (showInfoDialog && state.currentMedia != null) {
        MediaInfoDialog(
            media = state.currentMedia!!,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
private fun DeckFinishedView(
    savedCount: Int,
    trashCount: Int,
    trashSize: Long,
    onApplyTrash: () -> Unit,
    onBackToGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(28.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Semua Media Ditinjau!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$savedCount disimpan • $trashCount ditandai sampah (${Formatters.formatFileSize(trashSize)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (trashCount > 0) {
                Button(
                    onClick = onApplyTrash,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Pindahkan ke Sampah Sekarang")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = onBackToGallery,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Kembali ke Galeri")
            }
        }
    }
}

@Composable
private fun MediaInfoDialog(
    media: MediaItem,
    onDismiss: () -> Unit
) {
    val dateStr = remember(media.dateModified) {
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        sdf.format(Date(media.dateModified * 1000L))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rincian Media",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(label = "Nama", value = media.displayName)
                InfoRow(label = "Tipe", value = if (media.isVideo) "Video (${Formatters.formatDuration(media.durationMs)})" else "Foto (${media.mimeType})")
                InfoRow(label = "Ukuran", value = Formatters.formatFileSize(media.size))
                InfoRow(label = "Album", value = media.bucketName ?: "Penyimpanan Internal")
                InfoRow(label = "Diubah", value = dateStr)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
