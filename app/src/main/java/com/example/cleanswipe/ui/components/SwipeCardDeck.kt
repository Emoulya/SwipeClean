package com.example.cleanswipe.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.cleanswipe.data.model.MediaItem
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SwipeDirection {
    LEFT,   // Trash
    RIGHT   // Keep
}

@Composable
fun SwipeCardDeck(
    currentMedia: MediaItem?,
    nextMedia: MediaItem?,
    onSwiped: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
    programmaticSwipeTrigger: SwipeDirection? = null,
    onProgrammaticSwipeHandled: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    // Batas threshold ergonomis & ringan (15% lebar layar, bukan 28% yang memberatkan)
    val thresholdPx = screenWidthPx * 0.15f
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var isAnimatingOut by remember { mutableStateOf(false) }

    // Reset posisi animasi saat item kartu berubah
    LaunchedEffect(currentMedia?.id) {
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
        isAnimatingOut = false
        hasTriggeredHaptic = false
    }

    // Tangani swipe programatik (misalnya dari tombol bawah)
    LaunchedEffect(programmaticSwipeTrigger) {
        programmaticSwipeTrigger?.let { direction ->
            if (isAnimatingOut) return@let
            isAnimatingOut = true
            val targetX = if (direction == SwipeDirection.RIGHT) screenWidthPx * 1.5f else -screenWidthPx * 1.5f
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            offsetX.animateTo(
                targetValue = targetX,
                animationSpec = tween(durationMillis = 150)
            )
            onSwiped(direction)
            onProgrammaticSwipeHandled()
        }
    }

    val dragProgress by remember {
        derivedStateOf { (abs(offsetX.value) / thresholdPx).coerceIn(0f, 1f) }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Kartu Lapis Kedua (Background Deck)
        if (nextMedia != null) {
            val bgScale = 0.93f + (0.07f * dragProgress)
            val bgOffsetY = with(density) { (20.dp.toPx() * (1f - dragProgress)).toDp() }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .offset(y = bgOffsetY)
                    .scale(bgScale),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp
            ) {
                MediaCardContent(
                    media = nextMedia,
                    isTopCard = false
                )
            }
        }

        // Kartu Lapis Pertama (Foreground Swipe Card)
        if (currentMedia != null) {
            val rotationZ = (offsetX.value / thresholdPx).coerceIn(-2.5f, 2.5f) * 4.5f

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .offset {
                        IntOffset(
                            x = offsetX.value.roundToInt(),
                            y = offsetY.value.roundToInt()
                        )
                    }
                    .graphicsLayer {
                        this.rotationZ = rotationZ
                    }
                    .pointerInput(currentMedia.id) {
                        val velocityTracker = VelocityTracker()
                        detectDragGestures(
                            onDragStart = {
                                if (isAnimatingOut) return@detectDragGestures
                                hasTriggeredHaptic = false
                                velocityTracker.resetTracking()
                            },
                            onDrag = { change, dragAmount ->
                                if (isAnimatingOut) return@detectDragGestures
                                change.consume()
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y * 0.35f)

                                    if (!hasTriggeredHaptic && abs(offsetX.value) >= thresholdPx) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        hasTriggeredHaptic = true
                                    } else if (hasTriggeredHaptic && abs(offsetX.value) < thresholdPx) {
                                        hasTriggeredHaptic = false
                                    }
                                }
                            },
                            onDragEnd = {
                                if (isAnimatingOut) return@detectDragGestures
                                val currentX = offsetX.value
                                val velocityX = velocityTracker.calculateVelocity().x

                                // Fling responsif: jentikan ringan (> 280 px/s) ATAU pergeseran melampaui thresholdPx (15% lebar layar)
                                val flingMinVelocity = 280f
                                val flingMinDistance = 15f
                                val isSwipeRight = currentX > 0 && (currentX >= thresholdPx || (velocityX > flingMinVelocity && currentX > flingMinDistance))
                                val isSwipeLeft = currentX < 0 && (currentX <= -thresholdPx || (velocityX < -flingMinVelocity && currentX < -flingMinDistance))

                                coroutineScope.launch {
                                    if (isSwipeRight) {
                                        isAnimatingOut = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        offsetX.animateTo(
                                            targetValue = screenWidthPx * 1.5f,
                                            animationSpec = tween(150)
                                        )
                                        onSwiped(SwipeDirection.RIGHT)
                                    } else if (isSwipeLeft) {
                                        isAnimatingOut = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        offsetX.animateTo(
                                            targetValue = -screenWidthPx * 1.5f,
                                            animationSpec = tween(150)
                                        )
                                        onSwiped(SwipeDirection.LEFT)
                                    } else {
                                        // Spring kembali ke posisi semula secara halus
                                        launch {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                        }
                                        launch {
                                            offsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            onDragCancel = {
                                if (isAnimatingOut) return@detectDragGestures
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, spring())
                                    offsetY.animateTo(0f, spring())
                                }
                            }
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MediaCardContent(
                        media = currentMedia,
                        isTopCard = true
                    )

                    // Overlay Badge: SIMPAN (Swipe Kanan)
                    val keepProgress = (offsetX.value / thresholdPx).coerceIn(0f, 1.25f)
                    val keepAlpha = keepProgress.coerceIn(0f, 1f)
                    val isKeepTriggered = offsetX.value >= thresholdPx
                    if (keepAlpha > 0.05f) {
                        SwipeStampOverlay(
                            text = "SIMPAN",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF10B981),
                            isConfirmed = isKeepTriggered,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(24.dp)
                                .rotate(-14f)
                                .graphicsLayer { alpha = keepAlpha }
                        )
                    }

                    // Overlay Badge: HAPUS (Swipe Kiri)
                    val trashProgress = (-offsetX.value / thresholdPx).coerceIn(0f, 1.25f)
                    val trashAlpha = trashProgress.coerceIn(0f, 1f)
                    val isTrashTriggered = -offsetX.value >= thresholdPx
                    if (trashAlpha > 0.05f) {
                        SwipeStampOverlay(
                            text = "HAPUS",
                            icon = Icons.Rounded.Delete,
                            color = Color(0xFFEF4444),
                            isConfirmed = isTrashTriggered,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(24.dp)
                                .rotate(14f)
                                .graphicsLayer { alpha = trashAlpha }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaCardContent(
    media: MediaItem,
    isTopCard: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF18181B))
    ) {
        if (media.isVideo && isTopCard) {
            VideoPlayerView(
                videoUri = media.uri,
                isTopCard = true,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(media.uri)
                    .apply {
                        if (media.isVideo) {
                            videoFrameMillis(500)
                        }
                    }
                    .crossfade(true)
                    .build(),
                contentDescription = media.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SwipeStampOverlay(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isConfirmed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale = if (isConfirmed) 1.08f else 1f
    val borderWidth = if (isConfirmed) 3.5.dp else 2.5.dp
    val bgAlpha = if (isConfirmed) 0.35f else 0.18f

    Box(
        modifier = modifier
            .scale(scale)
            .border(width = borderWidth, color = color, shape = RoundedCornerShape(12.dp))
            .background(color.copy(alpha = bgAlpha), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
