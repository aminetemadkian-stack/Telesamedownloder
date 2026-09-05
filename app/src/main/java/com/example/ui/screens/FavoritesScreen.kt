package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Playlist
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TelegramBlue

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteVideos: List<VideoItem>,
    favoritePlaylists: List<Playlist>,
    allVideos: List<VideoItem>,
    isPersian: Boolean,
    onPlayVideo: (VideoItem) -> Unit,
    onToggleFavoriteVideo: (VideoItem) -> Unit,
    onToggleFavoritePlaylist: (Playlist) -> Unit,
    onToggleFavoriteChannel: (String, Boolean) -> Unit,
    onDownloadVideo: (VideoItem) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit,
    onAddToPlaylist: (VideoItem) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = if (isPersian) listOf("ویدیوهای برگزیده (${favoriteVideos.size})", "پلی‌لیست‌های برگزیده (${favoritePlaylists.size})")
    else listOf("Starred Videos (${favoriteVideos.size})", "Starred Playlists (${favoritePlaylists.size})")

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TelegramBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (selectedTabIndex == 0) {
            // Favorite Videos Tab
            if (favoriteVideos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(AmberAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPersian) "هیچ ویدیویی در فیوریت نیست" else "No Favorite Videos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPersian) "با ستاره‌دار کردن ویدیوها یا انتقال کل پلی‌لیست یک کانال، آن‌ها را اینجا مشاهده کنید."
                        else "Star individual videos or move entire channel playlists here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("favorite_videos_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoriteVideos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            isPersian = isPersian,
                            onPlay = onPlayVideo,
                            onToggleFavorite = onToggleFavoriteVideo,
                            onDownload = onDownloadVideo,
                            onCancelDownload = onCancelDownload,
                            onDelete = onDeleteVideo,
                            onAddToPlaylist = onAddToPlaylist
                        )
                    }
                }
            }
        } else {
            // Favorite Playlists Tab
            if (favoritePlaylists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(AmberAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPersian) "پلی‌لیست برگزیده‌ای ثبت نشده است" else "No Favorite Playlists",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPersian) "در بخش پلی‌لیست‌ها روی دکمه «انتقال کل به فیوریت ⭐» بزنید تا کل کانال یا پلی‌لیست به اینجا اضافه شود."
                        else "In Playlists tab, tap 'Move All to Favorites' to add entire playlists here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("favorite_playlists_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoritePlaylists, key = { it.id }) { playlist ->
                        val videoCount = allVideos.count { it.playlistId == playlist.id || (playlist.channelUsername != null && it.channelUsername == playlist.channelUsername) }

                        PlaylistCard(
                            playlist = playlist,
                            videoCount = videoCount,
                            isPersian = isPersian,
                            isFavorite = true,
                            onClick = { onPlaylistClick(playlist) },
                            onToggleFavorite = {
                                if (playlist.channelUsername != null) {
                                    onToggleFavoriteChannel(playlist.channelUsername, false)
                                } else {
                                    onToggleFavoritePlaylist(playlist)
                                }
                            },
                            onDelete = {}
                        )
                    }
                }
            }
        }
    }
}
