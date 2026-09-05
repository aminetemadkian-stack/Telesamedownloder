package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VideoItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.InstagramOrange
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.InstagramPurple
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoCard(
    video: VideoItem,
    isPersian: Boolean,
    onPlay: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onDownload: (VideoItem) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onDelete: (VideoItem) -> Unit,
    onAddToPlaylist: (VideoItem) -> Unit,
    onEditTags: ((VideoItem) -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}")
    ) {
        Column {
            // Thumbnail with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .clickable { onPlay(video) }
            ) {
                if (!video.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(video.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dark vignette gradient for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000))
                            )
                        )
                )

                // Big Centered Action Button (Play for Video/Podcast, Read for PDF)
                val centerButtonColor = when {
                    video.isPdf -> Color(0xFFE53935).copy(alpha = 0.9f)
                    video.isPodcast -> AmberAccent.copy(alpha = 0.9f)
                    video.isInstagram -> InstagramPink.copy(alpha = 0.9f)
                    else -> TelegramBlue.copy(alpha = 0.9f)
                }

                Surface(
                    shape = CircleShape,
                    color = centerButtonColor,
                    modifier = Modifier
                        .size(54.dp)
                        .align(Alignment.Center)
                ) {
                    val centerIcon = when {
                        video.isPdf -> Icons.Default.MenuBook
                        video.isPodcast -> Icons.Default.Headphones
                        else -> Icons.Default.PlayArrow
                    }
                    Icon(
                        imageVector = centerIcon,
                        contentDescription = "Open Media",
                        tint = if (video.isPodcast) Color.Black else Color.White,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize()
                    )
                }

                // Platform & Channel Badge (Top Start)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (video.isInstagram) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(InstagramPurple, InstagramPink, InstagramOrange)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Instagram",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (video.isPdf) Color(0xFFE53935) else if (video.isPodcast) AmberAccent else TelegramBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (video.isPdf) Icons.Default.PictureAsPdf else if (video.isPodcast) Icons.Default.Mic else Icons.Default.Send,
                                    contentDescription = "Telegram",
                                    tint = if (video.isPodcast) Color.Black else Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@${video.channelUsername}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Media Type Badge (Top End)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        video.isPdf -> Color(0xFFE53935).copy(alpha = 0.85f)
                        video.isPodcast -> AmberAccent.copy(alpha = 0.85f)
                        else -> Color.Black.copy(alpha = 0.7f)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = video.mediaTypeLabel,
                        color = if (video.isPodcast) Color.Black else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Duration or Page Count Badge (Bottom End)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = video.durationFormatted,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Downloaded Indicator or File Size Badge (Bottom Start)
                if (video.isDownloaded) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreen.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val qualityText = video.selectedQuality?.let { " • $it" } ?: ""
                            Text(
                                text = if (isPersian) "دانلود شده (${video.fileSizeFormatted}$qualityText)" else "Offline (${video.fileSizeFormatted}$qualityText)",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = video.fileSizeFormatted,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Downloading Progress Bar if actively downloading
            if (video.isDownloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPersian) "در حال دانلود: ${(video.downloadProgress * 100).toInt()}% • ${video.downloadSpeedText}"
                            else "Downloading: ${(video.downloadProgress * 100).toInt()}% • ${video.downloadSpeedText}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = { onCancelDownload(video.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Download",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { video.downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (video.isInstagram) InstagramPink else TelegramBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Video Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = video.channelTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (video.isInstagram) InstagramPink else TelegramBlue,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = video.category,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { onToggleFavorite(video) },
                        modifier = Modifier.testTag("favorite_button_${video.id}")
                    ) {
                        Icon(
                            imageVector = if (video.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (video.isFavorite) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (video.caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tags Flow Row
                if (video.tagList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        video.tagList.take(5).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = (if (video.isInstagram) InstagramPink else TelegramBlue).copy(alpha = 0.12f),
                                modifier = Modifier.clickable { onTagClick?.invoke(tag) }
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (video.isInstagram) InstagramPink else TelegramBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Bar
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Play / Read Button
                    val actionIcon = when {
                        video.isPdf -> Icons.Default.MenuBook
                        video.isPodcast -> Icons.Default.Headphones
                        else -> Icons.Default.PlayArrow
                    }
                    val actionLabel = when {
                        video.isPdf -> if (isPersian) "مطالعه سند" else "Read PDF"
                        video.isPodcast -> if (isPersian) "پخش پادکست" else "Play Audio"
                        else -> if (isPersian) "پخش ویدیو" else "Play Video"
                    }

                    FilledTonalButton(
                        onClick = { onPlay(video) },
                        modifier = Modifier.testTag("play_button_${video.id}")
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(actionLabel, fontSize = 12.sp)
                    }

                    // Smart Download Button (opens quality selection dialog)
                    if (!video.isDownloaded && !video.isDownloading) {
                        val downloadLabel = when {
                            video.isPdf -> if (isPersian) "ذخیره PDF" else "Save PDF"
                            video.isPodcast -> if (isPersian) "دانلود پادکست" else "Save MP3"
                            else -> if (isPersian) "دانلود هوشمند" else "Smart Download"
                        }
                        OutlinedButton(
                            onClick = { onDownload(video) },
                            modifier = Modifier.testTag("download_button_${video.id}")
                        ) {
                            Icon(
                                imageVector = if (video.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(downloadLabel, fontSize = 12.sp)
                        }
                    }

                    // Tag and Category Management Button
                    if (onEditTags != null) {
                        FilledTonalIconButton(
                            onClick = { onEditTags(video) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("edit_tags_button_${video.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = "Edit Tags & Category",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Forward / Share Button
                    FilledTonalIconButton(
                        onClick = { ForwardShareHelper.shareVideo(context, video) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("forward_button_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Forward / Share",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Add to Playlist Button
                    FilledTonalIconButton(
                        onClick = { onAddToPlaylist(video) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to Playlist",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Open in Telegram/Instagram Link Button
                    FilledTonalIconButton(
                        onClick = { ForwardShareHelper.openInTelegram(context, video.telegramUrl) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Link",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Delete Confirmation Prompt
                AnimatedVisibility(visible = showDeleteConfirm) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (isPersian) "آیا از حذف این ویدیو (و فایل دانلود شده) اطمینان دارید؟" else "Are you sure you want to delete this video?",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showDeleteConfirm = false }
                            ) {
                                Text(if (isPersian) "انصراف" else "Cancel", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    showDeleteConfirm = false
                                    onDelete(video)
                                },
                                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text(if (isPersian) "بله، حذف شود" else "Delete", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
