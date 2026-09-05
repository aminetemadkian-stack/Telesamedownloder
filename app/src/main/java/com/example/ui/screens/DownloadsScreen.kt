package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.data.repository.StorageStats
import com.example.ui.components.StorageManagementCard
import com.example.ui.components.VideoCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun DownloadsScreen(
    downloadedVideos: List<VideoItem>,
    storageStats: StorageStats?,
    isAutoCleanEnabled: Boolean,
    maxCacheLimitBytes: Long,
    isPersian: Boolean,
    onAutoCleanToggle: (Boolean) -> Unit,
    onCacheLimitChange: (Long) -> Unit,
    onCleanStorageNow: () -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit,
    onAddToPlaylist: (VideoItem) -> Unit,
    onEditTags: ((VideoItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedDownloadType by remember { mutableStateOf("ALL") }

    val filteredDownloads = downloadedVideos.filter { video ->
        when (selectedDownloadType) {
            "VIDEO" -> video.isVideo
            "PODCAST" -> video.isPodcast
            "PDF" -> video.isPdf
            else -> true
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Smart Storage Manager Interactive Card
        StorageManagementCard(
            storageStats = storageStats,
            isAutoCleanEnabled = isAutoCleanEnabled,
            maxCacheLimitBytes = maxCacheLimitBytes,
            isPersian = isPersian,
            onAutoCleanToggle = onAutoCleanToggle,
            onCacheLimitChange = onCacheLimitChange,
            onCleanStorageNow = onCleanStorageNow,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Downloads Format Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val typeOptions = listOf(
                "ALL" to (if (isPersian) "همه دانلودها (${downloadedVideos.size})" else "All (${downloadedVideos.size})"),
                "VIDEO" to (if (isPersian) "ویدیوها (${downloadedVideos.count { it.isVideo }})" else "Videos"),
                "PODCAST" to (if (isPersian) "پادکست‌ها (${downloadedVideos.count { it.isPodcast }})" else "Podcasts"),
                "PDF" to (if (isPersian) "فایل‌های PDF (${downloadedVideos.count { it.isPdf }})" else "PDFs")
            )

            typeOptions.forEach { (code, label) ->
                val isSelected = selectedDownloadType == code
                val chipColor = when (code) {
                    "PODCAST" -> AmberAccent
                    "PDF" -> Color(0xFFE53935)
                    "VIDEO" -> TelegramBlue
                    else -> MaterialTheme.colorScheme.primary
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDownloadType = code },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        selectedLabelColor = if (code == "PODCAST") Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("download_filter_chip_$code")
                )
            }
        }

        if (filteredDownloads.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val emptyTitle = when (selectedDownloadType) {
                    "PDF" -> if (isPersian) "هیچ فایل PDF ذخیره نشده است" else "No Saved PDF Files"
                    "PODCAST" -> if (isPersian) "هیچ پادکستی دانلود نشده است" else "No Saved Podcasts"
                    "VIDEO" -> if (isPersian) "هیچ ویدیویی دانلود نشده است" else "No Downloaded Videos"
                    else -> if (isPersian) "هیچ فایلی دانلود نشده است" else "No Downloaded Media"
                }

                Text(
                    text = emptyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPersian) "در بخش فید روی دکمه ذخیره/دانلود ویدیوها، پادکست‌ها یا اسناد PDF کلیک کنید تا برای دسترسی آفلاین ذخیره شوند."
                    else "Tap 'Save' on any video, podcast, or PDF in the feed to store it for offline access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("downloads_videos_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredDownloads, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        isPersian = isPersian,
                        onPlay = onPlayVideo,
                        onToggleFavorite = onToggleFavorite,
                        onDownload = {},
                        onCancelDownload = {},
                        onDelete = onDeleteVideo,
                        onAddToPlaylist = onAddToPlaylist,
                        onEditTags = onEditTags
                    )
                }
            }
        }
    }
}
