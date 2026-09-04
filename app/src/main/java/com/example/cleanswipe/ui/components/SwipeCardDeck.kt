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
    val thresholdPx = screenWidthPx * 0.28f
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Reset posisi animasi saat item kartu berubah
    LaunchedEffect(currentMedia?.id) {
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
        hasTriggeredHaptic = false
    }

    // Tangani swipe programatik (misalnya dari tombol bawah)
    LaunchedEffect(programmaticSwipeTrigger) {
        programmaticSwipeTrigger?.let { direction ->
            val targetX = if (direction == SwipeDirection.RIGHT) screenWidthPx * 1.5f else -screenWidthPx * 1.5f
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            offsetX.animateTo(
                targetValue = targetX,
                animationSpec = tween(durationMillis = 260)
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
            val rotationZ = (offsetX.value / screenWidthPx) * 20f

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
                                hasTriggeredHaptic = false
                                velocityTracker.resetTracking()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y * 0.4f)

                                    if (!hasTriggeredHaptic && abs(offsetX.value) >= thresholdPx) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        hasTriggeredHaptic = true
                                    } else if (hasTriggeredHaptic && abs(offsetX.value) < thresholdPx) {
                                        hasTriggeredHaptic = false
                                    }
                                }
                            },
                            onDragEnd = {
                                val velocityX = velocityTracker.calculateVelocity().x
                                val isFlingRight = velocityX > 1000f
                                val isFlingLeft = velocityX < -1000f

                                coroutineScope.launch {
                                    if (offsetX.value > thresholdPx || isFlingRight) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        offsetX.animateTo(
                                            targetValue = screenWidthPx * 1.5f,
                                            animationSpec = tween(220)
                                        )
                                        onSwiped(SwipeDirection.RIGHT)
                                    } else if (offsetX.value < -thresholdPx || isFlingLeft) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        offsetX.animateTo(
                                            targetValue = -screenWidthPx * 1.5f,
                                            animationSpec = tween(220)
                                        )
                                        onSwiped(SwipeDirection.LEFT)
                                    } else {
                                        // Spring kembali ke posisi semula
                                        launch {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }
                                        launch {
                                            offsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                    offsetY.animateTo(0f)
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
                    val keepAlpha = (offsetX.value / (thresholdPx * 0.75f)).coerceIn(0f, 1f)
                    if (keepAlpha > 0.05f) {
                        SwipeStampOverlay(
                            text = "SIMPAN",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(24.dp)
                                .rotate(-14f)
                                .graphicsLayer { alpha = keepAlpha }
                        )
                    }

                    // Overlay Badge: HAPUS (Swipe Kiri)
                    val trashAlpha = (-offsetX.value / (thresholdPx * 0.75f)).coerceIn(0f, 1f)
                    if (trashAlpha > 0.05f) {
                        SwipeStampOverlay(
                            text = "HAPUS",
                            icon = Icons.Rounded.Delete,
                            color = Color(0xFFEF4444),
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
        if (media.isVideo) {
            VideoPlayerView(
                videoUri = media.uri,
                isTopCard = isTopCard,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(media.uri)
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(width = 3.dp, color = color, shape = RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
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
