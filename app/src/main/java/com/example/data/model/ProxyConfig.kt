package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProxyType {
    DIRECT,
    SOCKS5,
    HTTP,
    MTPROTO
}

@Entity(tableName = "proxy_configs")
data class ProxyConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: ProxyType = ProxyType.SOCKS5,
    val host: String = "",
    val port: Int = 1080,
    val secret: String? = null,
    val username: String? = null,
    val password: String? = null,
    val isActive: Boolean = false,
    val pingMs: Long = -1, // -1 means untested, >0 is latency
    val isPreset: Boolean = false
) {
    val displayDetails: String
        get() = when (type) {
            ProxyType.DIRECT -> "Direct Internet Connection"
            ProxyType.MTPROTO -> "MTProto://$host:$port"
            ProxyType.SOCKS5 -> "SOCKS5://$host:$port"
            ProxyType.HTTP -> "HTTP://$host:$port"
        }
}
