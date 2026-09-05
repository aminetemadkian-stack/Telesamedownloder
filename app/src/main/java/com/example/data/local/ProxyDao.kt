package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProxyConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxyDao {
    @Query("SELECT * FROM proxy_configs ORDER BY id ASC")
    fun getAllProxies(): Flow<List<ProxyConfig>>

    @Query("SELECT * FROM proxy_configs WHERE isActive = 1 LIMIT 1")
    fun getActiveProxyFlow(): Flow<ProxyConfig?>

    @Query("SELECT * FROM proxy_configs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProxy(): ProxyConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProxy(proxy: ProxyConfig): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProxies(proxies: List<ProxyConfig>): List<Long>

    @Update
    suspend fun updateProxy(proxy: ProxyConfig)

    @Query("UPDATE proxy_configs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE proxy_configs SET isActive = (id = :id)")
    suspend fun setActiveProxy(id: Long)

    @Query("UPDATE proxy_configs SET pingMs = :pingMs WHERE id = :id")
    suspend fun updatePing(id: Long, pingMs: Long)

    @Query("DELETE FROM proxy_configs WHERE id = :id")
    suspend fun deleteProxyById(id: Long)
}
