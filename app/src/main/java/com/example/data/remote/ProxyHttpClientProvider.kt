package com.example.data.remote

import com.example.data.model.ProxyConfig
import com.example.data.model.ProxyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object ProxyHttpClientProvider {

    @Volatile
    private var currentClient: OkHttpClient = createDefaultClient(null)

    fun getClient(proxyConfig: ProxyConfig? = null): OkHttpClient {
        if (proxyConfig == null || !proxyConfig.isActive || proxyConfig.type == ProxyType.DIRECT) {
            return createDefaultClient(null)
        }
        return createDefaultClient(proxyConfig)
    }

    private fun createDefaultClient(proxyConfig: ProxyConfig?): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)

        if (proxyConfig != null && proxyConfig.isActive && proxyConfig.host.isNotBlank() && proxyConfig.port > 0) {
            try {
                val proxy = when (proxyConfig.type) {
                    ProxyType.SOCKS5 -> Proxy(Proxy.Type.SOCKS, InetSocketAddress(proxyConfig.host, proxyConfig.port))
                    ProxyType.HTTP -> Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyConfig.host, proxyConfig.port))
                    ProxyType.MTPROTO -> {
                        // MTProto is handled via HTTP/SOCKS bridge or simulated proxy tunnel
                        Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyConfig.host, proxyConfig.port))
                    }
                    ProxyType.DIRECT -> null
                }
                if (proxy != null) {
                    builder.proxy(proxy)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return builder.build()
    }

    suspend fun testProxyPing(proxyConfig: ProxyConfig): Long = withContext(Dispatchers.IO) {
        val client = createDefaultClient(proxyConfig)
        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("https://www.google.com/generate_204")
                .header("User-Agent", "TeleStreamHub/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    return@withContext System.currentTimeMillis() - startTime
                }
            }
            return@withContext -1L
        } catch (e: Exception) {
            return@withContext -1L
        }
    }
}
