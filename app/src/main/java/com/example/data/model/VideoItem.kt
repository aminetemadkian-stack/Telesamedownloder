package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val channelTitle: String,
    val channelUsername: String,
    val telegramUrl: String,
    val streamUrl: String,
    val localFilePath: String? = null,
    val durationSeconds: Int = 0,
    val fileSizeBytes: Long = 0,
    val thumbnailUrl: String? = null,
    val caption: String = "",
    val category: String = "عمومی",
    val playlistId: Long? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val downloadSpeedText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val sourcePlatform: String = "TELEGRAM", // TELEGRAM, INSTAGRAM, DIRECT
    val customTags: String = "", // Comma-separated user tags
    val selectedQuality: String? = null, // e.g. "1080p", "720p", "480p", "audio_mp3"
    val lastPlayedAt: Long? = null, // Timestamp for LRU cache cleaner
    val importanceScore: Int = 0, // 0 = Normal, 1 = High, etc.
    val mediaType: String = "VIDEO", // "VIDEO", "PODCAST", "PDF"
    val pageCount: Int = 0, // For PDF documents
    val authorOrArtist: String = "" // For Podcasts or Book authors
) {
    val durationFormatted: String
        get() {
            if (isPdf) {
                return if (pageCount > 0) "$pageCount صفحه" else "سند PDF"
            }
            if (durationSeconds <= 0) return "00:00"
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }

    val fileSizeFormatted: String
        get() {
            if (fileSizeBytes <= 0) return "حجم نامشخص"
            val kb = fileSizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val effectivePlayUrl: String
        get() = localFilePath ?: streamUrl

    val tagList: List<String>
        get() {
            if (customTags.isBlank()) return emptyList()
            return customTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }

    val isPdf: Boolean
        get() = mediaType.equals("PDF", ignoreCase = true) ||
                telegramUrl.endsWith(".pdf", ignoreCase = true) ||
                effectivePlayUrl.endsWith(".pdf", ignoreCase = true)

    val isPodcast: Boolean
        get() = mediaType.equals("PODCAST", ignoreCase = true) ||
                mediaType.equals("AUDIO", ignoreCase = true) ||
                category.contains("پادکست", ignoreCase = true) ||
                effectivePlayUrl.endsWith(".mp3", ignoreCase = true) ||
                effectivePlayUrl.endsWith(".m4a", ignoreCase = true)

    val isVideo: Boolean
        get() = !isPdf && !isPodcast

    val mediaTypeLabel: String
        get() = when {
            isPdf -> "سند PDF"
            isPodcast -> "پادکست صوتی"
            else -> "ویدیو"
        }

    val isInstagram: Boolean
        get() = sourcePlatform == "INSTAGRAM" || telegramUrl.contains("instagram.com") || telegramUrl.contains("instagr.am")

    val isTelegram: Boolean
        get() = sourcePlatform == "TELEGRAM" || telegramUrl.contains("t.me") || telegramUrl.contains("telegram.me")

    fun getQualityOptions(): List<VideoQualityOption> {
        if (isPdf) {
            return listOf(
                VideoQualityOption(QualityLevel.HD_720P, fileSizeBytes)
            )
        }
        if (isPodcast) {
            return listOf(
                VideoQualityOption(QualityLevel.AUDIO_MP3, fileSizeBytes),
                VideoQualityOption(QualityLevel.LOW_360P, (fileSizeBytes * 0.6).toLong())
            )
        }
        val baseSize = if (fileSizeBytes > 0) fileSizeBytes else 20_000_000L
        return QualityLevel.entries.map { level ->
            val estimated = (baseSize * level.sizeMultiplier).toLong()
            VideoQualityOption(level = level, estimatedSizeBytes = estimated)
        }
    }
}
