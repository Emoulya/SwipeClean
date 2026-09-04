package com.example.cleanswipe.ui.screens.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cleanswipe.data.model.MediaAlbum

@Composable
fun FolderScreen(
    viewModel: FolderViewModel,
    onAlbumClick: (MediaAlbum) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0E))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 110.dp // Ruang ekstra agar tidak tertutup floating bottom bar
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section: Pinned Title
                item(span = { GridItemSpan(3) }) {
                    Text(
                        text = "Pinned",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Section: Pinned 2x2 Grid Items
                item(span = { GridItemSpan(3) }) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val pinned = state.pinnedAlbums
                        if (pinned.size >= 4) {
                            // Baris 1: All photos & Camera
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PinnedAlbumCard(
                                    album = pinned[0],
                                    onClick = { onAlbumClick(pinned[0]) },
                                    modifier = Modifier.weight(1f)
                                )
                                PinnedAlbumCard(
                                    album = pinned[1],
                                    onClick = { onAlbumClick(pinned[1]) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Baris 2: Screenshots & Videos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PinnedAlbumCard(
                                    album = pinned[2],
                                    onClick = { onAlbumClick(pinned[2]) },
                                    modifier = Modifier.weight(1f)
                                )
                                PinnedAlbumCard(
                                    album = pinned[3],
                                    onClick = { onAlbumClick(pinned[3]) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section: Albums Title
                item(span = { GridItemSpan(3) }) {
                    Text(
                        text = "Albums",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                    )
                }

                // Section: Albums 3-Column Grid
                items(state.regularAlbums, key = { it.id }) { album ->
                    AlbumGridItem(
                        album = album,
                        onClick = { onAlbumClick(album) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedAlbumCard(
    album: MediaAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1C1C1E),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Cover Image Thumbnail (Square Rounded)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                if (album.coverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(album.coverUri)
                            .size(160, 160)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${album.itemCount} file",
                    color = Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun AlbumGridItem(
    album: MediaAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Thumbnail Persegi Rounded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1C1C1E)),
            contentAlignment = Alignment.Center
        ) {
            if (album.coverUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(album.coverUri)
                        .size(256, 256)
                        .crossfade(true)
                        .build(),
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Nama Folder
        Text(
            text = album.name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Jumlah File
        Text(
            text = "${album.itemCount}",
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
