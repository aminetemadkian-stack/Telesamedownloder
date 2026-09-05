package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val channelUsername: String? = null,
    val isFavorite: Boolean = false,
    val colorHex: String = "#2AABEE",
    val iconName: String = "playlist",
    val sourcePlatform: String = "TELEGRAM", // TELEGRAM, INSTAGRAM, CUSTOM
    val createdAt: Long = System.currentTimeMillis()
)
