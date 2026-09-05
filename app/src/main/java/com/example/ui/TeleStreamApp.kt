package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ProxyType
import com.example.data.model.VideoItem
import com.example.ui.components.AddLinkDialog
import com.example.ui.components.AddProxyDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.PlaylistSelectorDialog
import com.example.ui.components.QualitySelectDialog
import com.example.ui.components.TagManagementDialog
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.ProxyScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.TeleStreamViewModel
import kotlinx.coroutines.flow.collectLatest

enum class ScreenTab {
    FEED,
    PLAYLISTS,
    DOWNLOADS,
    FAVORITES,
    PROXY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeleStreamApp(
    viewModel: TeleStreamViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val filteredVideos by viewModel.filteredVideos.collectAsState()
    val favoriteVideos by viewModel.favoriteVideos.collectAsState()
    val downloadedVideos by viewModel.downloadedVideos.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val favoritePlaylists by viewModel.favoritePlaylists.collectAsState()
    val allProxies by viewModel.allProxies.collectAsState()
    val activeProxy by viewModel.activeProxy.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.FEED) }
    var playlistTargetVideo by remember { mutableStateOf<VideoItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val layoutDirection = if (uiState.isRtlPersian) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(listOf(TelegramBlue, CyanAccent))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isRtlPersian) "تل‌استریم هاب" else "TeleStream Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        // Proxy status badge shortcut
                        val isProxyActive = activeProxy != null && activeProxy?.type != ProxyType.DIRECT
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isProxyActive) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clickable { currentTab = ScreenTab.PROXY }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isProxyActive) EmeraldGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isProxyActive) "پروکسی" else "مستقیم",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isProxyActive) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Language Toggle FA / EN
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("toggle_language_button")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TelegramBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (uiState.isRtlPersian) "FA" else "EN",
                                    color = TelegramBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    data class NavTabItem(
                        val tab: ScreenTab,
                        val label: String,
                        val icon: androidx.compose.ui.graphics.vector.ImageVector
                    )

                    val tabs = listOf(
                        NavTabItem(ScreenTab.FEED, if (uiState.isRtlPersian) "فید ویدیوها" else "Feed", Icons.Default.VideoLibrary),
                        NavTabItem(ScreenTab.PLAYLISTS, if (uiState.isRtlPersian) "پلی‌لیست‌ها" else "Playlists", Icons.Default.PlaylistPlay),
                        NavTabItem(ScreenTab.DOWNLOADS, if (uiState.isRtlPersian) "دانلودها" else "Downloads", Icons.Default.Download),
                        NavTabItem(ScreenTab.FAVORITES, if (uiState.isRtlPersian) "فیوریت‌ها" else "Favorites", Icons.Default.Star),
                        NavTabItem(ScreenTab.PROXY, if (uiState.isRtlPersian) "پروکسی" else "Proxy", Icons.Default.Shield)
                    )

                    tabs.forEach { item ->
                        val isSelected = currentTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = item.tab },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = TelegramBlue,
                                indicatorColor = TelegramBlue
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { tab ->
                when (tab) {
                    ScreenTab.FEED -> FeedScreen(
                        videos = filteredVideos,
                        playlists = allPlaylists,
                        searchQuery = uiState.searchQuery,
                        selectedCategory = uiState.selectedCategory,
                        selectedPlatform = uiState.selectedPlatform,
                        selectedMediaType = uiState.selectedMediaType,
                        selectedTag = uiState.selectedTag,
                        selectedPlaylistId = uiState.selectedPlaylistId,
                        availableCategories = availableCategories,
                        availableTags = availableTags,
                        isPersian = uiState.isRtlPersian,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategoryChange = { viewModel.setSelectedCategory(it) },
                        onPlatformChange = { viewModel.setSelectedPlatform(it) },
                        onMediaTypeChange = { viewModel.setSelectedMediaType(it) },
                        onTagChange = { viewModel.setSelectedTag(it) },
                        onPlaylistFilterChange = { viewModel.setSelectedPlaylist(it) },
                        onAddLinkClick = { viewModel.setAddingLinkDialog(true) },
                        onPlayVideo = { viewModel.playVideo(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteVideo(it) },
                        onDownloadVideo = { viewModel.openQualityDownloadDialog(it) },
                        onCancelDownload = { viewModel.cancelDownload(it) },
                        onDeleteVideo = { viewModel.deleteVideo(it) },
                        onAddToPlaylist = { playlistTargetVideo = it },
                        onEditTags = { viewModel.openTagEditor(it) }
                    )

                    ScreenTab.PLAYLISTS -> PlaylistsScreen(
                        playlists = allPlaylists,
                        allVideos = allVideos,
                        isPersian = uiState.isRtlPersian,
                        onPlaylistClick = { pl ->
                            viewModel.setSelectedPlaylist(pl.id)
                            currentTab = ScreenTab.FEED
                        },
                        onToggleFavoritePlaylist = { pl -> viewModel.toggleFavoritePlaylist(pl) },
                        onToggleFavoriteChannel = { ch, fav -> viewModel.toggleFavoriteChannelPlaylist(ch, fav) },
                        onCreatePlaylistClick = { viewModel.setCreatingPlaylistDialog(true) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) }
                    )

                    ScreenTab.DOWNLOADS -> DownloadsScreen(
                        downloadedVideos = downloadedVideos,
                        storageStats = storageStats,
                        isAutoCleanEnabled = viewModel.storageManager.isAutoCleanEnabled,
                        maxCacheLimitBytes = viewModel.storageManager.maxCacheLimitBytes,
                        isPersian = uiState.isRtlPersian,
                        onAutoCleanToggle = { viewModel.setAutoCleanEnabled(it) },
                        onCacheLimitChange = { viewModel.setCacheLimitBytes(it) },
                        onCleanStorageNow = { viewModel.performSmartStorageClean() },
                        onPlayVideo = { viewModel.playVideo(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteVideo(it) },
                        onDeleteVideo = { viewModel.deleteDownloadedFile(it) },
                        onAddToPlaylist = { playlistTargetVideo = it },
                        onEditTags = { viewModel.openTagEditor(it) }
                    )

                    ScreenTab.FAVORITES -> FavoritesScreen(
                        favoriteVideos = favoriteVideos,
                        favoritePlaylists = favoritePlaylists,
                        allVideos = allVideos,
                        isPersian = uiState.isRtlPersian,
                        onPlayVideo = { viewModel.playVideo(it) },
                        onToggleFavoriteVideo = { viewModel.toggleFavoriteVideo(it) },
                        onToggleFavoritePlaylist = { pl -> viewModel.toggleFavoritePlaylist(pl) },
                        onToggleFavoriteChannel = { ch, fav -> viewModel.toggleFavoriteChannelPlaylist(ch, fav) },
                        onDownloadVideo = { viewModel.openQualityDownloadDialog(it) },
                        onCancelDownload = { viewModel.cancelDownload(it) },
                        onDeleteVideo = { viewModel.deleteVideo(it) },
                        onAddToPlaylist = { playlistTargetVideo = it },
                        onPlaylistClick = { pl ->
                            viewModel.setSelectedPlaylist(pl.id)
                            currentTab = ScreenTab.FEED
                        }
                    )

                    ScreenTab.PROXY -> ProxyScreen(
                        proxies = allProxies,
                        activeProxy = activeProxy,
                        isPersian = uiState.isRtlPersian,
                        onSelectActiveProxy = { viewModel.setActiveProxy(it) },
                        onTestPing = { viewModel.testProxyPing(it) },
                        onAddProxyClick = { viewModel.setAddingProxyDialog(true) },
                        onDeleteProxy = { viewModel.deleteProxy(it) }
                    )
                }
            }

            // Global Dialogs

            // Add Media Link Dialog (Telegram & Instagram)
            if (uiState.isAddingLink) {
                AddLinkDialog(
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { viewModel.setAddingLinkDialog(false) },
                    onSubmit = { viewModel.addMediaLinks(it) }
                )
            }

            // Quality Select Download Dialog
            uiState.downloadQualityTargetVideo?.let { targetVideo ->
                QualitySelectDialog(
                    video = targetVideo,
                    storageStats = storageStats,
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { viewModel.openQualityDownloadDialog(null) },
                    onConfirmDownload = { quality ->
                        viewModel.startSmartDownload(targetVideo, quality)
                    }
                )
            }

            // Tag & Category Management Dialog
            uiState.editingTagsVideo?.let { targetVideo ->
                TagManagementDialog(
                    video = targetVideo,
                    availableCategories = availableCategories,
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { viewModel.openTagEditor(null) },
                    onSave = { tags, category ->
                        viewModel.updateVideoTagsAndCategory(targetVideo, tags, category)
                    }
                )
            }

            // Create Playlist Dialog
            if (uiState.isCreatingPlaylist) {
                CreatePlaylistDialog(
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { viewModel.setCreatingPlaylistDialog(false) },
                    onSubmit = { name, desc ->
                        viewModel.createPlaylist(name, desc)
                    }
                )
            }

            // Add Proxy Dialog
            if (uiState.isAddingProxy) {
                AddProxyDialog(
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { viewModel.setAddingProxyDialog(false) },
                    onSubmit = { name, type, host, port, secret ->
                        viewModel.addCustomProxy(name, type, host, port, secret)
                    }
                )
            }

            // Playlist Selection for Video
            if (playlistTargetVideo != null) {
                PlaylistSelectorDialog(
                    video = playlistTargetVideo!!,
                    playlists = allPlaylists,
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { playlistTargetVideo = null },
                    onSelectPlaylist = { plId ->
                        playlistTargetVideo?.let { v -> viewModel.assignVideoToPlaylist(v.id, plId) }
                        playlistTargetVideo = null
                    },
                    onCreateNewPlaylist = { name, desc ->
                        viewModel.createPlaylist(name, desc)
                    }
                )
            }

            // Full Video Player Dialog
            uiState.currentlyPlayingVideo?.let { playingVideo ->
                VideoPlayerDialog(
                    video = playingVideo,
                    isPersian = uiState.isRtlPersian,
                    onDismiss = { viewModel.playVideo(null) }
                )
            }
        }
    }
}
