package com.vpnclient.app.vpn.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object LatencyTester {

    /**
     * Measures raw TCP ping latency directly to the destination server.
     *
     * @param host Server hostname or IP
     * @param port Server port
     * @param timeoutMs Connection timeout in milliseconds
     * @return Round-trip time in milliseconds, or -1 if unreachable
     */
    suspend fun testTcpPing(host: String, port: Int, timeoutMs: Int = 2500): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                System.currentTimeMillis() - start
            }
        } catch (e: Exception) {
            -1L
        }
    }
}
