package com.example.data.repository

import android.content.Context
import android.os.Environment
import com.example.data.local.VideoDao
import com.example.data.model.ProxyConfig
import com.example.data.model.QualityLevel
import com.example.data.model.VideoItem
import com.example.data.remote.ProxyHttpClientProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class DownloadManager(
    private val context: Context,
    private val videoDao: VideoDao,
    private val storageManager: StorageManager? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val activeDownloadJobs = ConcurrentHashMap<Long, Job>()

    private val _downloadingIds = MutableStateFlow<Set<Long>>(emptySet())
    val downloadingIds = _downloadingIds.asStateFlow()

    fun startDownload(
        video: VideoItem,
        quality: QualityLevel = QualityLevel.HD_720P,
        proxyConfig: ProxyConfig? = null
    ) {
        if (video.isDownloaded && video.localFilePath != null && File(video.localFilePath).exists()) {
            return
        }

        if (activeDownloadJobs.containsKey(video.id)) {
            return
        }

        val job = scope.launch {
            val targetDirectoryType = when {
                video.isPdf -> Environment.DIRECTORY_DOCUMENTS
                video.isPodcast || quality.isAudioOnly -> Environment.DIRECTORY_MUSIC
                else -> Environment.DIRECTORY_MOVIES
            }
            val storageDir = context.getExternalFilesDir(targetDirectoryType) ?: context.filesDir
            if (!storageDir.exists()) storageDir.mkdirs()

            val safeChannelName = video.channelUsername.ifBlank { "media" }
            val extension = when {
                video.isPdf -> "pdf"
                video.isPodcast || quality.isAudioOnly -> "mp3"
                else -> "mp4"
            }
            val fileName = "telestream_${safeChannelName}_${video.id}_${quality.key}_${System.currentTimeMillis()}.$extension"
            val destinationFile = File(storageDir, fileName)

            val targetEstimatedSize = (if (video.fileSizeBytes > 0) video.fileSizeBytes else 20_000_000L) * quality.sizeMultiplier

            try {
                _downloadingIds.value = _downloadingIds.value + video.id
                videoDao.updateDownloadProgress(video.id, isDownloading = true, progress = 0.05f, speedText = "اتصال...")

                val client = ProxyHttpClientProvider.getClient(proxyConfig)
                val request = Request.Builder()
                    .url(video.streamUrl)
                    .header("User-Agent", "TeleStreamHub/1.0")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("HTTP Error: ${response.code}")
                }

                val body = response.body ?: throw Exception("Empty response body")
                val totalBytes = if (body.contentLength() > 0) body.contentLength() else targetEstimatedSize.toLong()
                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(destinationFile)

                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                var downloadedBytes: Long = 0
                var lastProgressUpdate = System.currentTimeMillis()
                var bytesSinceLastUpdate: Long = 0

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (!isActive) {
                                destinationFile.delete()
                                throw CancellationException("Download cancelled")
                            }

                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            bytesSinceLastUpdate += bytesRead

                            val now = System.currentTimeMillis()
                            val timeDiff = now - lastProgressUpdate

                            if (timeDiff >= 350) {
                                val progress = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 0.99f) else 0.5f
                                val speedKbps = if (timeDiff > 0) (bytesSinceLastUpdate / 1024.0) / (timeDiff / 1000.0) else 0.0
                                val speedText = if (speedKbps > 1024) {
                                    String.format("%.1f MB/s", speedKbps / 1024)
                                } else {
                                    String.format("%.0f KB/s", speedKbps)
                                }

                                videoDao.updateDownloadProgress(video.id, isDownloading = true, progress = progress, speedText = speedText)
                                lastProgressUpdate = now
                                bytesSinceLastUpdate = 0
                            }
                        }
                        output.flush()
                    }
                }

                val finalFileLength = if (destinationFile.length() > 0) destinationFile.length() else targetEstimatedSize.toLong()

                // Completed successfully! Update status with quality
                videoDao.updateDownloadStatusWithQuality(
                    id = video.id,
                    isDownloaded = true,
                    localPath = destinationFile.absolutePath,
                    progress = 1.0f,
                    quality = quality.key,
                    fileSizeBytes = finalFileLength
                )

                // Trigger smart auto clean if cache limit exceeded
                storageManager?.checkAndPerformAutoClean()

            } catch (e: CancellationException) {
                destinationFile.delete()
                videoDao.updateDownloadProgress(video.id, isDownloading = false, progress = 0f, speedText = "")
            } catch (e: Exception) {
                e.printStackTrace()
                destinationFile.delete()
                videoDao.updateDownloadProgress(video.id, isDownloading = false, progress = 0f, speedText = "خطا در دانلود")
            } finally {
                activeDownloadJobs.remove(video.id)
                _downloadingIds.value = _downloadingIds.value - video.id
            }
        }

        activeDownloadJobs[video.id] = job
    }

    fun cancelDownload(videoId: Long) {
        val job = activeDownloadJobs[videoId]
        job?.cancel()
        activeDownloadJobs.remove(videoId)
        _downloadingIds.value = _downloadingIds.value - videoId
        scope.launch {
            videoDao.updateDownloadProgress(videoId, isDownloading = false, progress = 0f, speedText = "")
        }
    }

    suspend fun deleteLocalFile(video: VideoItem): Boolean = withContext(Dispatchers.IO) {
        try {
            cancelDownload(video.id)
            if (video.localFilePath != null) {
                val file = File(video.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            videoDao.updateDownloadStatus(video.id, isDownloaded = false, localPath = null, progress = 0f)
            true
        } catch (e: Exception) {
            false
        }
    }
}
