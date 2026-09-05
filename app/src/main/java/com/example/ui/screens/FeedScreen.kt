package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    videos: List<VideoItem>,
    playlists: List<Playlist>,
    searchQuery: String,
    selectedCategory: String,
    selectedPlatform: String, // ALL, TELEGRAM, INSTAGRAM
    selectedMediaType: String = "ALL", // ALL, VIDEO, PODCAST, PDF
    selectedTag: String?,
    selectedPlaylistId: Long?,
    availableCategories: List<String>,
    availableTags: List<String>,
    isPersian: Boolean,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPlatformChange: (String) -> Unit,
    onMediaTypeChange: (String) -> Unit = {},
    onTagChange: (String?) -> Unit,
    onPlaylistFilterChange: (Long?) -> Unit,
    onAddLinkClick: () -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit,
    onDownloadVideo: (VideoItem) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit,
    onAddToPlaylist: (VideoItem) -> Unit,
    onEditTags: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultCategories = listOf("همه", "تکنولوژی و فناوری", "آموزشی", "سینما و فیلم", "مستند و طبیعت", "پادکست", "عمومی")
    val combinedCategories = (defaultCategories + availableCategories).distinct()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            if (isPersian) "جستجو در ویدیوها، پادکست‌ها، کتاب‌های PDF و تگ‌ها..." else "Search videos, podcasts, PDFs & tags...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TelegramBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelegramBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("feed_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Media Type Filter Row (ALL, VIDEO, PODCAST, PDF)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val mediaTypeOptions = listOf(
                        "ALL" to (if (isPersian) "همه فرمت‌ها" else "All Types"),
                        "VIDEO" to (if (isPersian) "ویدیوها 🎬" else "Videos"),
                        "PODCAST" to (if (isPersian) "پادکست‌ها 🎙️" else "Podcasts"),
                        "PDF" to (if (isPersian) "اسناد PDF 📄" else "PDFs")
                    )

                    mediaTypeOptions.forEach { (typeCode, typeLabel) ->
                        val isSelected = selectedMediaType == typeCode
                        val typeColor = when (typeCode) {
                            "PODCAST" -> Color(0xFFFFB300)
                            "PDF" -> Color(0xFFE53935)
                            "VIDEO" -> TelegramBlue
                            else -> MaterialTheme.colorScheme.primary
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onMediaTypeChange(typeCode) },
                            label = {
                                Text(
                                    text = typeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = typeColor,
                                selectedLabelColor = if (typeCode == "PODCAST") Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("media_type_chip_$typeCode")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Platform Filter Bar (ALL, TELEGRAM, INSTAGRAM)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val platformOptions = listOf(
                        "ALL" to (if (isPersian) "همه پلتفرم‌ها" else "All Platforms"),
                        "TELEGRAM" to (if (isPersian) "تلگرام ✈️" else "Telegram"),
                        "INSTAGRAM" to (if (isPersian) "اینستاگرام 📸" else "Instagram")
                    )

                    platformOptions.forEach { (code, label) ->
                        val isSelected = selectedPlatform == code
                        val brandColor = when (code) {
                            "INSTAGRAM" -> InstagramPink
                            "TELEGRAM" -> TelegramBlue
                            else -> MaterialTheme.colorScheme.primary
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onPlatformChange(code) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                when (code) {
                                    "TELEGRAM" -> Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    "INSTAGRAM" -> Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    else -> null
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = brandColor,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("platform_chip_$code")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Categories Scrollable Chip Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    combinedCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChange(category) },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TelegramBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }

                // Tags Scrollable Chip Row (if available)
                if (availableTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTags.take(12).forEach { tag ->
                            val isSelected = selectedTag == tag
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTagChange(if (isSelected) null else tag) },
                                label = {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Label, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent.copy(alpha = 0.8f),
                                    selectedLabelColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Active Playlist or Tag Filter Banner
                if (selectedPlaylistId != null || selectedTag != null) {
                    val activePl = playlists.find { it.id == selectedPlaylistId }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TelegramBlue.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = TelegramBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val filterText = buildString {
                                    if (activePl != null) append("${if (isPersian) "پلی‌لیست:" else "Playlist:"} ${activePl.name} ")
                                    if (selectedTag != null) append("${if (isPersian) "برچسب:" else "Tag:"} #$selectedTag")
                                }
                                Text(
                                    text = filterText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TelegramBlue
                                )
                            }
                            IconButton(
                                onClick = {
                                    onPlaylistFilterChange(null)
                                    onTagChange(null)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Filter",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Video Cards List
            if (videos.isEmpty()) {
                // Empty State View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(TelegramBlue.copy(alpha = 0.2f), CyanAccent.copy(alpha = 0.2f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = TelegramBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPersian) "ویدیویی یافت نشد" else "No Videos Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPersian) "برای افزودن ویدیو از تلگرام یا اینستاگرام، روی دکمه + در پایین صفحه کلیک کنید یا فیلترها را ریست نمایید."
                        else "Tap the + button below to import Telegram/Instagram videos or clear filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("feed_videos_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            isPersian = isPersian,
                            onPlay = onPlayVideo,
                            onToggleFavorite = onToggleFavorite,
                            onDownload = onDownloadVideo,
                            onCancelDownload = onCancelDownload,
                            onDelete = onDeleteVideo,
                            onAddToPlaylist = onAddToPlaylist,
                            onEditTags = onEditTags,
                            onTagClick = { tag -> onTagChange(tag) }
                        )
                    }
                }
            }
        }

        // Add Media Link Floating Action Button
        FloatingActionButton(
            onClick = onAddLinkClick,
            containerColor = TelegramBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_telegram_link_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Media Link",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
