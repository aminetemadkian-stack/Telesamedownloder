package com.example.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.local.VideoDao
import com.example.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class StorageStats(
    val appUsedBytes: Long,
    val deviceFreeBytes: Long,
    val deviceTotalBytes: Long,
    val downloadedVideosCount: Int,
    val maxCacheLimitBytes: Long // 0 for unlimited
) {
    val appUsedFormatted: String
        get() = formatBytes(appUsedBytes)

    val deviceFreeFormatted: String
        get() = formatBytes(deviceFreeBytes)

    val deviceTotalFormatted: String
        get() = formatBytes(deviceTotalBytes)

    val usagePercentage: Float
        get() = if (deviceTotalBytes > 0) ((deviceTotalBytes - deviceFreeBytes).toFloat() / deviceTotalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    val appUsagePercentageOfDevice: Float
        get() = if (deviceTotalBytes > 0) (appUsedBytes.toFloat() / deviceTotalBytes.toFloat()).coerceIn(0f, 1f) else 0f

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 MB"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }
    }
}

class StorageManager(
    private val context: Context,
    private val videoDao: VideoDao
) {
    private val prefs = context.getSharedPreferences("telestream_storage_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_CACHE_LIMIT = "max_cache_limit_bytes"
        const val PREF_AUTO_CLEAN_ENABLED = "auto_clean_enabled"

        val CACHE_LIMIT_PRESETS = listOf(
            500L * 1024 * 1024 to "500 MB",
            1024L * 1024 * 1024 to "1 GB",
            2048L * 1024 * 1024 to "2 GB",
            5120L * 1024 * 1024 to "5 GB",
            0L to "بدون محدودیت (نامحدود)"
        )
    }

    var maxCacheLimitBytes: Long
        get() = prefs.getLong(PREF_CACHE_LIMIT, 2048L * 1024 * 1024) // default 2GB
        set(value) = prefs.edit().putLong(PREF_CACHE_LIMIT, value).apply()

    var isAutoCleanEnabled: Boolean
        get() = prefs.getBoolean(PREF_AUTO_CLEAN_ENABLED, true)
        set(value) = prefs.edit().putBoolean(PREF_AUTO_CLEAN_ENABLED, value).apply()

    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val appUsed = calculateDirectorySize(downloadDir)

            var freeBytes = 10L * 1024 * 1024 * 1024
            var totalBytes = 64L * 1024 * 1024 * 1024
            try {
                val stat = StatFs(Environment.getDataDirectory().path)
                freeBytes = stat.availableBlocksLong * stat.blockSizeLong
                totalBytes = stat.blockCountLong * stat.blockSizeLong
            } catch (e: Exception) {
                // fallback on virtual or restricted sandbox
            }

            val downloadedList = try {
                videoDao.getDownloadedNonFavoriteVideosLRU()
            } catch (e: Exception) {
                emptyList()
            }
            val totalDownloadedCount = downloadedList.size

            return@withContext StorageStats(
                appUsedBytes = appUsed,
                deviceFreeBytes = freeBytes,
                deviceTotalBytes = totalBytes,
                downloadedVideosCount = totalDownloadedCount,
                maxCacheLimitBytes = maxCacheLimitBytes
            )
        } catch (e: Exception) {
            return@withContext StorageStats(
                appUsedBytes = 0L,
                deviceFreeBytes = 10L * 1024 * 1024 * 1024,
                deviceTotalBytes = 64L * 1024 * 1024 * 1024,
                downloadedVideosCount = 0,
                maxCacheLimitBytes = maxCacheLimitBytes
            )
        }
    }

    suspend fun smartCleanStorage(targetBytesToFree: Long = 0L): Pair<Int, Long> = withContext(Dispatchers.IO) {
        // Find non-favorite downloaded videos sorted by least recently played / oldest
        val nonFavorites = videoDao.getDownloadedNonFavoriteVideosLRU()
        var freedBytes = 0L
        var deletedCount = 0

        for (video in nonFavorites) {
            val file = video.localFilePath?.let { File(it) }
            val fileSize = file?.length() ?: video.fileSizeBytes
            if (file != null && file.exists()) {
                file.delete()
            }
            videoDao.updateDownloadStatus(video.id, isDownloaded = false, localPath = null, progress = 0f)
            freedBytes += fileSize
            deletedCount++

            if (targetBytesToFree > 0 && freedBytes >= targetBytesToFree) {
                break
            }
        }

        return@withContext Pair(deletedCount, freedBytes)
    }

    suspend fun checkAndPerformAutoClean(): Boolean = withContext(Dispatchers.IO) {
        if (!isAutoCleanEnabled || maxCacheLimitBytes <= 0) return@withContext false

        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val appUsed = calculateDirectorySize(downloadDir)

        if (appUsed > maxCacheLimitBytes) {
            val bytesToFree = appUsed - (maxCacheLimitBytes * 0.8).toLong() // clean down to 80%
            smartCleanStorage(bytesToFree)
            return@withContext true
        }
        return@withContext false
    }

    private fun calculateDirectorySize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirectorySize(f) else f.length()
        }
        return size
    }
}
