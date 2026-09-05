package com.example.data.model

enum class QualityLevel(
    val key: String,
    val titleFa: String,
    val titleEn: String,
    val resolution: String,
    val sizeMultiplier: Double,
    val isAudioOnly: Boolean = false
) {
    FHD_1080P("1080p", "کیفیت فوق‌العاده Full HD", "Full HD 1080p", "1920x1080", 1.8),
    HD_720P("720p", "کیفیت عالی HD (پیشنهادی)", "HD 720p (Recommended)", "1280x720", 1.0),
    SD_480P("480p", "کیفیت استاندارد SD", "Standard 480p", "854x480", 0.6),
    LOW_360P("360p", "صرفه‌جویی دیتا و حجم کم", "Data Saver 360p", "640x360", 0.35),
    AUDIO_MP3("audio_mp3", "استخراج فقط صوت (MP3)", "Audio Only (MP3)", "Audio 128kbps", 0.18, isAudioOnly = true);

    companion object {
        fun fromKey(key: String?): QualityLevel {
            return entries.find { it.key == key } ?: HD_720P
        }
    }
}

data class VideoQualityOption(
    val level: QualityLevel,
    val estimatedSizeBytes: Long,
    val isAvailable: Boolean = true
) {
    val sizeFormatted: String
        get() {
            val mb = estimatedSizeBytes / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", estimatedSizeBytes / 1024.0)
            }
        }
}
