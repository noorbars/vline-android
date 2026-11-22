package com.vpnclient.app.vpn.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object PingTester {

    private val TEST_URLS = listOf(
        "http://cp.cloudflare.com/generate_204",
        "http://www.google.com/generate_204"
    )

    /**
     * Measures latency in milliseconds using HTTP proxy first (which avoids local Android host DNS hangs),
     * and falling back to SOCKS proxy.
     */
    suspend fun testLatency(
        httpPort: Int = 10809,
        socksPort: Int = 10808,
        timeoutMs: Long = 5000
    ): Long = withContext(Dispatchers.IO) {
        // 1. Test via local HTTP proxy (Xray handles DNS resolution remotely)
        for (url in TEST_URLS) {
            val delay = testUrlViaProxy(
                Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", httpPort)),
                url,
                timeoutMs
            )
            if (delay > 0) return@withContext delay
        }

        // 2. Fallback: Test via local SOCKS proxy
        for (url in TEST_URLS) {
            val delay = testUrlViaProxy(
                Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", socksPort)),
                url,
                timeoutMs
            )
            if (delay > 0) return@withContext delay
        }

        -1L
    }

    private fun testUrlViaProxy(proxy: Proxy, url: String, timeoutMs: Long): Long {
        val start = System.currentTimeMillis()
        return try {
            val client = OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    System.currentTimeMillis() - start
                } else {
                    -1L
                }
            }
        } catch (e: Exception) {
            -1L
        }
    }
}

