package com.example.data.repository

import com.example.data.local.ProxyDao
import com.example.data.model.ProxyConfig
import com.example.data.model.ProxyType
import com.example.data.remote.ProxyHttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class ProxyRepository(
    private val proxyDao: ProxyDao
) {
    val allProxies: Flow<List<ProxyConfig>> = proxyDao.getAllProxies()
    val activeProxyFlow: Flow<ProxyConfig?> = proxyDao.getActiveProxyFlow()

    suspend fun initializeDefaultProxiesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = allProxies.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val presets = listOf(
                ProxyConfig(
                    name = "اتصال مستقیم (بدون پروکسی)",
                    type = ProxyType.DIRECT,
                    host = "",
                    port = 0,
                    isActive = true,
                    pingMs = 35,
                    isPreset = true
                ),
                ProxyConfig(
                    name = "MTProto Telegram Proxy (سریع)",
                    type = ProxyType.MTPROTO,
                    host = "149.154.167.50",
                    port = 443,
                    secret = "ee000000000000000000000000000000007777772e676f6f676c652e636f6d",
                    isActive = false,
                    pingMs = 120,
                    isPreset = true
                ),
                ProxyConfig(
                    name = "SOCKS5 Turbo Proxy",
                    type = ProxyType.SOCKS5,
                    host = "127.0.0.1",
                    port = 10808,
                    isActive = false,
                    pingMs = -1,
                    isPreset = true
                ),
                ProxyConfig(
                    name = "HTTP Cloudflare Bridge",
                    type = ProxyType.HTTP,
                    host = "104.16.132.229",
                    port = 8080,
                    isActive = false,
                    pingMs = -1,
                    isPreset = true
                )
            )
            proxyDao.insertProxies(presets)
        }
    }

    suspend fun getActiveProxy(): ProxyConfig? = withContext(Dispatchers.IO) {
        proxyDao.getActiveProxy()
    }

    suspend fun setActiveProxy(id: Long) = withContext(Dispatchers.IO) {
        proxyDao.setActiveProxy(id)
    }

    suspend fun addProxy(
        name: String,
        type: ProxyType,
        host: String,
        port: Int,
        secret: String? = null,
        user: String? = null,
        pass: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val proxy = ProxyConfig(
            name = name.ifBlank { "پروکسی اختصاصی ${type.name}" },
            type = type,
            host = host.trim(),
            port = port,
            secret = secret?.trim()?.ifBlank { null },
            username = user?.trim()?.ifBlank { null },
            password = pass?.trim()?.ifBlank { null },
            isActive = false,
            pingMs = -1,
            isPreset = false
        )
        proxyDao.insertProxy(proxy)
    }

    suspend fun testProxyPing(proxy: ProxyConfig): Long = withContext(Dispatchers.IO) {
        val ping = ProxyHttpClientProvider.testProxyPing(proxy)
        proxyDao.updatePing(proxy.id, ping)
        ping
    }

    suspend fun deleteProxy(id: Long) = withContext(Dispatchers.IO) {
        proxyDao.deleteProxyById(id)
    }
}
