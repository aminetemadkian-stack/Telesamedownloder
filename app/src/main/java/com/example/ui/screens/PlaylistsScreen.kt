package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.VideoItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.InstagramOrange
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.InstagramPurple
import com.example.ui.theme.TelegramBlue

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    allVideos: List<VideoItem>,
    isPersian: Boolean,
    onPlaylistClick: (Playlist) -> Unit,
    onToggleFavoritePlaylist: (Playlist) -> Unit,
    onToggleFavoriteChannel: (String, Boolean) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isPersian) "هنوز پلی‌لیستی وجود ندارد" else "No Playlists Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCreatePlaylistClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text(if (isPersian) "ساخت اولین پلی‌لیست" else "Create First Playlist", color = Color.White)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("playlists_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Header Banner Info
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = TelegramBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPersian) "مدیریت پلی‌لیست‌ها و کانال‌ها" else "Playlists & Channel Hub",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPersian) "می‌توانید کل ویدیوهای یک کانال یا پلی‌لیست را با یک کلیک به علاقه‌مندی‌ها ببرید."
                                    else "Star and transfer entire channel playlists directly into Favorites.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                items(playlists, key = { it.id }) { playlist ->
                    val videoCount = allVideos.count { it.playlistId == playlist.id || (playlist.channelUsername != null && it.channelUsername == playlist.channelUsername) }
                    val playlistVideos = allVideos.filter { it.playlistId == playlist.id || (playlist.channelUsername != null && it.channelUsername == playlist.channelUsername) }
                    val isEntirelyFavorite = playlistVideos.isNotEmpty() && playlistVideos.all { it.isFavorite }

                    PlaylistCard(
                        playlist = playlist,
                        videoCount = videoCount,
                        isPersian = isPersian,
                        isFavorite = playlist.isFavorite || isEntirelyFavorite,
                        onClick = { onPlaylistClick(playlist) },
                        onToggleFavorite = {
                            if (playlist.channelUsername != null) {
                                onToggleFavoriteChannel(playlist.channelUsername, !(playlist.isFavorite || isEntirelyFavorite))
                            } else {
                                onToggleFavoritePlaylist(playlist)
                            }
                        },
                        onDelete = { onDeletePlaylist(playlist.id) }
                    )
                }
            }
        }

        // Create Playlist FAB
        FloatingActionButton(
            onClick = onCreatePlaylistClick,
            containerColor = TelegramBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_playlist_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Playlist",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    videoCount: Int,
    isPersian: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val isInstagram = playlist.sourcePlatform == "INSTAGRAM" || playlist.iconName == "instagram"

    val icon: ImageVector = when (playlist.iconName) {
        "instagram" -> Icons.Default.CameraAlt
        "code" -> Icons.Default.Code
        "science" -> Icons.Default.Science
        "movie" -> Icons.Default.Movie
        else -> if (isInstagram) Icons.Default.CameraAlt else Icons.Default.VideoLibrary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("playlist_card_${playlist.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isInstagram) {
                                    Brush.linearGradient(listOf(InstagramPurple, InstagramPink, InstagramOrange))
                                } else {
                                    Brush.linearGradient(listOf(TelegramBlue, CyanAccent))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!playlist.channelUsername.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isInstagram) Icons.Default.CameraAlt else Icons.Default.Send,
                                    contentDescription = null,
                                    tint = if (isInstagram) InstagramPink else TelegramBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "@${playlist.channelUsername}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isInstagram) InstagramPink else TelegramBlue,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Playlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (playlist.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Video Count Pill + Star Transfer to Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isPersian) "$videoCount ویدیو" else "$videoCount Videos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Transfer entire playlist to favorites button
                FilledTonalButton(
                    onClick = onToggleFavorite,
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isFavorite) AmberAccent.copy(alpha = 0.2f) else TelegramBlue.copy(alpha = 0.12f),
                        contentColor = if (isFavorite) AmberAccent else TelegramBlue
                    ),
                    modifier = Modifier.testTag("transfer_playlist_favorite_button_${playlist.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFavorite) (if (isPersian) "در فیوریت‌ها ⭐" else "In Favorites")
                        else (if (isPersian) "انتقال کل به فیوریت ⭐" else "Move All to Favorites"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
