package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Query("SELECT * FROM playlists WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritePlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE channelUsername = :channelUsername LIMIT 1")
    suspend fun getPlaylistByChannel(channelUsername: String): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("UPDATE playlists SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE playlists SET isFavorite = :isFavorite WHERE channelUsername = :channelUsername")
    suspend fun setChannelPlaylistFavorite(channelUsername: String, isFavorite: Boolean)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)
}
