package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.ProxyConfig
import com.example.data.model.ProxyType
import com.example.data.model.QualityLevel
import com.example.data.model.VideoItem
import com.example.data.repository.DownloadManager
import com.example.data.repository.ProxyRepository
import com.example.data.repository.StorageManager
import com.example.data.repository.StorageStats
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TeleStreamUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "همه",
    val selectedPlatform: String = "ALL", // ALL, TELEGRAM, INSTAGRAM
    val selectedMediaType: String = "ALL", // ALL, VIDEO, PODCAST, PDF
    val selectedTag: String? = null,
    val selectedPlaylistId: Long? = null,
    val isAddingLink: Boolean = false,
    val isCreatingPlaylist: Boolean = false,
    val isAddingProxy: Boolean = false,
    val editingTagsVideo: VideoItem? = null,
    val downloadQualityTargetVideo: VideoItem? = null,
    val currentlyPlayingVideo: VideoItem? = null,
    val isRtlPersian: Boolean = true,
    val errorMessage: String? = null
)

class TeleStreamViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val storageManager = StorageManager(application, database.videoDao())
    val downloadManager = DownloadManager(application, database.videoDao(), storageManager)
    val proxyRepository = ProxyRepository(database.proxyDao())
    val videoRepository = VideoRepository(database.videoDao(), database.playlistDao(), proxyRepository, downloadManager, storageManager)

    private val _uiState = MutableStateFlow(TeleStreamUiState())
    val uiState: StateFlow<TeleStreamUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()

    val allVideos: StateFlow<List<VideoItem>> = videoRepository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteVideos: StateFlow<List<VideoItem>> = videoRepository.favoriteVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedVideos: StateFlow<List<VideoItem>> = videoRepository.downloadedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = videoRepository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePlaylists: StateFlow<List<Playlist>> = videoRepository.favoritePlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProxies: StateFlow<List<ProxyConfig>> = proxyRepository.allProxies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProxy: StateFlow<ProxyConfig?> = proxyRepository.activeProxyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableCategories: StateFlow<List<String>> = videoRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableTags: StateFlow<List<String>> = allVideos.map { list ->
        val tagsSet = linkedSetOf<String>()
        list.forEach { v ->
            tagsSet.addAll(v.tagList)
        }
        tagsSet.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Feed Videos based on search, platform, category, tag, & playlist
    val filteredVideos: StateFlow<List<VideoItem>> = combine(
        allVideos,
        _uiState
    ) { videos, state ->
        videos.filter { video ->
            val matchesPlatform = when (state.selectedPlatform) {
                "TELEGRAM" -> video.isTelegram
                "INSTAGRAM" -> video.isInstagram
                else -> true
            }

            val matchesMediaType = when (state.selectedMediaType) {
                "VIDEO" -> video.isVideo
                "PODCAST" -> video.isPodcast
                "PDF" -> video.isPdf
                else -> true
            }

            val matchesCategory = (state.selectedCategory == "همه") || (video.category == state.selectedCategory)
            val matchesPlaylist = (state.selectedPlaylistId == null) || (video.playlistId == state.selectedPlaylistId)
            val matchesTag = (state.selectedTag == null) || video.tagList.contains(state.selectedTag)

            val matchesSearch = state.searchQuery.isBlank() ||
                    video.title.contains(state.searchQuery, ignoreCase = true) ||
                    video.channelTitle.contains(state.searchQuery, ignoreCase = true) ||
                    video.channelUsername.contains(state.searchQuery, ignoreCase = true) ||
                    video.caption.contains(state.searchQuery, ignoreCase = true) ||
                    video.customTags.contains(state.searchQuery, ignoreCase = true)

            matchesPlatform && matchesMediaType && matchesCategory && matchesPlaylist && matchesTag && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                proxyRepository.initializeDefaultProxiesIfEmpty()
            } catch (e: Exception) {
                android.util.Log.e("TeleStream", "Error initializing proxies", e)
            }
            try {
                videoRepository.initializeSeedDataIfEmpty()
            } catch (e: Exception) {
                android.util.Log.e("TeleStream", "Error initializing seed data", e)
            }
            try {
                refreshStorageStats()
            } catch (e: Exception) {
                android.util.Log.e("TeleStream", "Error refreshing storage stats", e)
            }
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            _storageStats.value = storageManager.getStorageStats()
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSelectedPlatform(platform: String) {
        _uiState.value = _uiState.value.copy(selectedPlatform = platform)
    }

    fun setSelectedMediaType(mediaType: String) {
        _uiState.value = _uiState.value.copy(selectedMediaType = mediaType)
    }

    fun setSelectedTag(tag: String?) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
    }

    fun setSelectedPlaylist(playlistId: Long?) {
        _uiState.value = _uiState.value.copy(selectedPlaylistId = playlistId)
    }

    fun toggleLanguage() {
        _uiState.value = _uiState.value.copy(isRtlPersian = !_uiState.value.isRtlPersian)
    }

    fun setAddingLinkDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isAddingLink = show)
    }

    fun setCreatingPlaylistDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isCreatingPlaylist = show)
    }

    fun setAddingProxyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isAddingProxy = show)
    }

    fun openTagEditor(video: VideoItem?) {
        _uiState.value = _uiState.value.copy(editingTagsVideo = video)
    }

    fun openQualityDownloadDialog(video: VideoItem?) {
        _uiState.value = _uiState.value.copy(downloadQualityTargetVideo = video)
    }

    fun playVideo(video: VideoItem?) {
        if (video != null) {
            viewModelScope.launch {
                videoRepository.updateLastPlayed(video.id)
            }
        }
        _uiState.value = _uiState.value.copy(currentlyPlayingVideo = video)
    }

    fun addMediaLinks(rawUrls: String) {
        viewModelScope.launch {
            val result = videoRepository.addMediaLinks(rawUrls)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _toastEvent.emit("با موفقیت $count ویدیو و پلی‌لیست اضافه شد!")
                setAddingLinkDialog(false)
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطا در پردازش لینک‌ها"
                _toastEvent.emit("خطا: $err")
            }
        }
    }

    fun updateVideoTagsAndCategory(video: VideoItem, tags: String, category: String) {
        viewModelScope.launch {
            videoRepository.updateVideoTagsAndCategory(video.id, tags, category)
            _toastEvent.emit("برچسب‌ها و دسته‌بندی با موفقیت ذخیره شد.")
            openTagEditor(null)
        }
    }

    fun toggleFavoriteVideo(video: VideoItem) {
        viewModelScope.launch {
            videoRepository.toggleFavoriteVideo(video.id, video.isFavorite)
            val msg = if (!video.isFavorite) "به نشان‌شده‌ها (فیوریت) اضافه شد ⭐" else "از فیوریت حذف شد"
            _toastEvent.emit(msg)
        }
    }

    fun toggleFavoriteChannelPlaylist(channelUsername: String, makeFavorite: Boolean) {
        viewModelScope.launch {
            videoRepository.toggleFavoriteChannelPlaylist(channelUsername, makeFavorite)
            val msg = if (makeFavorite) "پلی‌لیست و ویدیوهای @$channelUsername به فیوریت منتقل شدند ⭐" else "از فیوریت خارج شد"
            _toastEvent.emit(msg)
        }
    }

    fun toggleFavoritePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            videoRepository.toggleFavoritePlaylist(playlist.id, !playlist.isFavorite)
            val msg = if (!playlist.isFavorite) "پلی‌لیست «${playlist.name}» به فیوریت اضافه شد ⭐" else "از فیوریت حذف شد"
            _toastEvent.emit(msg)
        }
    }

    fun startSmartDownload(video: VideoItem, quality: QualityLevel) {
        viewModelScope.launch {
            videoRepository.startSmartDownload(video, quality)
            openQualityDownloadDialog(null)
            _toastEvent.emit("دانلود «${video.title.take(20)}» با کیفیت ${quality.key} آغاز شد...")
            refreshStorageStats()
        }
    }

    fun cancelDownload(videoId: Long) {
        downloadManager.cancelDownload(videoId)
        viewModelScope.launch {
            _toastEvent.emit("دانلود متوقف شد.")
            refreshStorageStats()
        }
    }

    fun deleteVideo(video: VideoItem) {
        viewModelScope.launch {
            videoRepository.deleteVideo(video)
            _toastEvent.emit("ویدیو پاک شد.")
            refreshStorageStats()
        }
    }

    fun deleteDownloadedFile(video: VideoItem) {
        viewModelScope.launch {
            videoRepository.deleteDownloadedFileOnly(video)
            _toastEvent.emit("فایل ویدیوی دانلود شده از حافظه پاک شد.")
            refreshStorageStats()
        }
    }

    fun performSmartStorageClean() {
        viewModelScope.launch {
            val (count, freedBytes) = videoRepository.performSmartStorageClean()
            if (count > 0) {
                val formattedFreed = StorageStats.formatBytes(freedBytes)
                _toastEvent.emit("پاک‌سازی هوشمند: $count ویدیوی قدیمی پاک و $formattedFreed فضا آزاد شد! 🧹")
            } else {
                _toastEvent.emit("فضای ذخیره‌سازی بهینه است. هیچ فایل قدیمی غیر نشان‌شده‌ای یافت نشد.")
            }
            refreshStorageStats()
        }
    }

    fun setAutoCleanEnabled(enabled: Boolean) {
        storageManager.isAutoCleanEnabled = enabled
        refreshStorageStats()
    }

    fun setCacheLimitBytes(bytes: Long) {
        storageManager.maxCacheLimitBytes = bytes
        refreshStorageStats()
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            videoRepository.createPlaylist(name, description)
            _toastEvent.emit("پلی‌لیست «$name» ایجاد شد.")
            setCreatingPlaylistDialog(false)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            videoRepository.deletePlaylist(playlistId)
            _toastEvent.emit("پلی‌لیست حذف شد.")
        }
    }

    fun assignVideoToPlaylist(videoId: Long, playlistId: Long?) {
        viewModelScope.launch {
            videoRepository.setVideoPlaylist(videoId, playlistId)
            _toastEvent.emit("ویدیو به پلی‌لیست منتقل شد.")
        }
    }

    fun setActiveProxy(proxyId: Long) {
        viewModelScope.launch {
            proxyRepository.setActiveProxy(proxyId)
            _toastEvent.emit("پروکسی فعال تغییر یافت.")
        }
    }

    fun testProxyPing(proxy: ProxyConfig) {
        viewModelScope.launch {
            _toastEvent.emit("در حال بررسی تاخیر (پینگ) پروکسی...")
            val ping = proxyRepository.testProxyPing(proxy)
            if (ping > 0) {
                _toastEvent.emit("پینگ: ${ping}ms - متصل")
            } else {
                _toastEvent.emit("پروکسی پاسخگو نبود یا تایم‌اوت داد.")
            }
        }
    }

    fun addCustomProxy(name: String, type: ProxyType, host: String, port: Int, secret: String?) {
        viewModelScope.launch {
            proxyRepository.addProxy(name, type, host, port, secret)
            _toastEvent.emit("پروکسی با موفقیت اضافه شد.")
            setAddingProxyDialog(false)
        }
    }

    fun deleteProxy(id: Long) {
        viewModelScope.launch {
            proxyRepository.deleteProxy(id)
            _toastEvent.emit("پروکسی حذف شد.")
        }
    }
}
