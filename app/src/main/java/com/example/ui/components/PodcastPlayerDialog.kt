package com.example.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VideoItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastPlayerDialog(
    video: VideoItem,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onDownloadPodcast: ((VideoItem) -> Unit)? = null
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(if (video.durationSeconds > 0) video.durationSeconds * 1000 else 120_000) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekSliderPosition by remember { mutableFloatStateOf(0f) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var speedMenuExpanded by remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    DisposableEffect(video.effectivePlayUrl) {
        try {
            isLoading = true
            val playPath = video.effectivePlayUrl
            if (playPath.startsWith("http://") || playPath.startsWith("https://")) {
                mediaPlayer.setDataSource(playPath)
            } else {
                mediaPlayer.setDataSource(context, android.net.Uri.fromFile(File(playPath)))
            }

            mediaPlayer.setOnPreparedListener { mp ->
                isLoading = false
                totalDurationMs = if (mp.duration > 0) mp.duration else totalDurationMs
                mp.start()
                isPlaying = true
            }

            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                currentPositionMs = totalDurationMs
            }

            mediaPlayer.setOnErrorListener { _, _, _ ->
                isLoading = false
                isPlaying = false
                true
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            isLoading = false
        }

        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }

    // Timer updater loop
    LaunchedEffect(isPlaying, isUserSeeking) {
        while (isPlaying && !isUserSeeking) {
            try {
                if (mediaPlayer.isPlaying) {
                    currentPositionMs = mediaPlayer.currentPosition
                }
            } catch (e: Exception) {
                // MediaPlayer state exception safety
            }
            delay(400)
        }
    }

    // Animation for rotating vinyl or waveform visualizer
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(22.dp))
                .testTag("podcast_player_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPersian) "پادکست و رسانه صوتی تلگرام" else "Telegram Podcast Player",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${video.channelUsername}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TelegramBlue,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { ForwardShareHelper.shareVideo(context, video) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("close_podcast_dialog")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Podcast Big Artwork with Vinyl Style Effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF2A2E3D), Color(0xFF141721))
                                )
                            )
                            .rotate(if (isPlaying) rotation else 0f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Artwork Image inside vinyl
                        if (!video.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(video.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(AmberAccent, Color(0xFFFF6F00)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Center Vinyl Pin
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Waveform Simulated Audio Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progressRatio = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs).coerceIn(0f, 1f) else 0f
                    for (i in 0..28) {
                        val barRatio = i / 28f
                        val isPassed = barRatio <= progressRatio
                        val waveHeight = if (isPlaying) {
                            (12 + 20 * sin(i * 0.4 + (currentPositionMs / 200.0))).toFloat().coerceIn(6f, 32f)
                        } else {
                            (8 + (i % 5) * 4).toFloat()
                        }
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(waveHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isPassed) AmberAccent else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Podcast Title and Channel Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${video.channelTitle} • ${video.fileSizeFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberAccent,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Slider & Timers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Slider(
                        value = if (isUserSeeking) seekSliderPosition else currentPositionMs.toFloat(),
                        onValueChange = {
                            isUserSeeking = true
                            seekSliderPosition = it
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            try {
                                mediaPlayer.seekTo(seekSliderPosition.toInt())
                                currentPositionMs = seekSliderPosition.toInt()
                            } catch (e: Exception) {
                                // Ignore
                            }
                        },
                        valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = AmberAccent,
                            activeTrackColor = AmberAccent,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatMs(if (isUserSeeking) seekSliderPosition.toInt() else currentPositionMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMs(totalDurationMs),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Media Controls: Rewind 15s, Play/Pause, Forward 15s, Speed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playback Speed Button
                    Box {
                        FilledTonalButton(
                            onClick = { speedMenuExpanded = true }
                        ) {
                            Text("${playbackSpeed}x", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = speedMenuExpanded,
                            onDismissRequest = { speedMenuExpanded = false }
                        ) {
                            listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}x", fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        playbackSpeed = speed
                                        speedMenuExpanded = false
                                        try {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                                mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed)
                                            }
                                        } catch (e: Exception) {
                                            // Handle unsupported speeds
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Replay 15s
                    IconButton(
                        onClick = {
                            try {
                                val newPos = (mediaPlayer.currentPosition - 15000).coerceAtLeast(0)
                                mediaPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                            } catch (e: Exception) {
                                // Ignore
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind 15s", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }

                    // Main Big Play/Pause Button
                    Surface(
                        shape = CircleShape,
                        color = AmberAccent,
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                try {
                                    if (isPlaying) {
                                        mediaPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        mediaPlayer.start()
                                        isPlaying = true
                                    }
                                } catch (e: Exception) {
                                    // Handle state error
                                }
                            }
                            .testTag("podcast_play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(28.dp))
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    // Forward 15s
                    IconButton(
                        onClick = {
                            try {
                                val newPos = (mediaPlayer.currentPosition + 15000).coerceAtMost(totalDurationMs)
                                mediaPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                            } catch (e: Exception) {
                                // Ignore
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Forward 15s", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }

                    // Open in Telegram
                    FilledTonalIconButton(
                        onClick = { ForwardShareHelper.openInTelegram(context, video.telegramUrl) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open Telegram")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Episode Description / Notes Card
                if (video.caption.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isPersian) "یادداشت‌ها و توضیحات اپیزود:" else "Episode Description & Notes:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = video.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Download MP3 Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (video.isDownloaded) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPersian) "فایل پادکست به صورت آفلاین ذخیره شده است (${video.fileSizeFormatted})"
                                        else "Podcast saved for offline playback (${video.fileSizeFormatted})",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { onDownloadPodcast?.invoke(video) },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("download_podcast_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPersian) "ذخیره و دانلود پادکست (MP3)" else "Save & Download Podcast (MP3)",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val hours = mins / 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, mins % 60, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
