package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: Long): VideoItem?

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE isDownloaded = 1 ORDER BY createdAt DESC")
    fun getDownloadedVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE isDownloaded = 1 AND isFavorite = 0 ORDER BY CASE WHEN lastPlayedAt IS NULL THEN 0 ELSE lastPlayedAt END ASC, createdAt ASC")
    suspend fun getDownloadedNonFavoriteVideosLRU(): List<VideoItem>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId ORDER BY createdAt DESC")
    fun getVideosByPlaylist(playlistId: Long): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE channelUsername = :channelUsername ORDER BY createdAt DESC")
    fun getVideosByChannel(channelUsername: String): Flow<List<VideoItem>>

    @Query("SELECT DISTINCT channelUsername FROM videos WHERE channelUsername != ''")
    fun getAllChannels(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM videos WHERE category != ''")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoItem>): List<Long>

    @Update
    suspend fun updateVideo(video: VideoItem)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE channelUsername = :channelUsername")
    suspend fun setChannelVideosFavorite(channelUsername: String, isFavorite: Boolean)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE playlistId = :playlistId")
    suspend fun setPlaylistVideosFavorite(playlistId: Long, isFavorite: Boolean)

    @Query("UPDATE videos SET playlistId = :playlistId WHERE id = :id")
    suspend fun setVideoPlaylist(id: Long, playlistId: Long?)

    @Query("UPDATE videos SET playlistId = :playlistId WHERE channelUsername = :channelUsername")
    suspend fun setChannelVideosPlaylist(channelUsername: String, playlistId: Long?)

    @Query("UPDATE videos SET isDownloaded = :isDownloaded, localFilePath = :localPath, downloadProgress = :progress, isDownloading = 0, downloadSpeedText = '', selectedQuality = :quality, fileSizeBytes = :fileSizeBytes WHERE id = :id")
    suspend fun updateDownloadStatusWithQuality(id: Long, isDownloaded: Boolean, localPath: String?, progress: Float, quality: String?, fileSizeBytes: Long)

    @Query("UPDATE videos SET isDownloaded = :isDownloaded, localFilePath = :localPath, downloadProgress = :progress, isDownloading = 0, downloadSpeedText = '' WHERE id = :id")
    suspend fun updateDownloadStatus(id: Long, isDownloaded: Boolean, localPath: String?, progress: Float)

    @Query("UPDATE videos SET isDownloading = :isDownloading, downloadProgress = :progress, downloadSpeedText = :speedText WHERE id = :id")
    suspend fun updateDownloadProgress(id: Long, isDownloading: Boolean, progress: Float, speedText: String)

    @Query("UPDATE videos SET customTags = :customTags, category = :category WHERE id = :id")
    suspend fun updateTagsAndCategory(id: Long, customTags: String, category: String)

    @Query("UPDATE videos SET lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun updateLastPlayed(id: Long, timestamp: Long)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM videos WHERE id IN (:ids)")
    suspend fun deleteVideosByIds(ids: List<Long>)

    @Query("DELETE FROM videos")
    suspend fun clearAll()
}
