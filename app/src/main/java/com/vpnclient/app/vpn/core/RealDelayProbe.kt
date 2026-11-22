package com.vpnclient.app.vpn.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object RealDelayProbe {

    private const val PROBE_URL = "https://www.gstatic.com/generate_204"

    /**
     * Measures HTTP Real Delay to https://www.gstatic.com/generate_204 through the local Xray HTTP proxy port.
     * This verifies end-to-end packet transmission, TLS handshake, and remote proxy health.
     *
     * @param httpPort Local Xray HTTP proxy port (default 10809)
     * @param timeoutMs Timeout in milliseconds (user requested 2500ms)
     * @return Real round-trip latency in milliseconds, or -1L if unreachable / timed out
     */
    suspend fun testRealDelay(
        httpPort: Int = 10809,
        timeoutMs: Long = 2500L
    ): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", httpPort))
            val client = OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .build()

            val request = Request.Builder()
                .url(PROBE_URL)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .header("Cache-Control", "no-cache")
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
