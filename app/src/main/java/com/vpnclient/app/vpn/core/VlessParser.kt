package com.vpnclient.app.vpn.core

import android.net.Uri
import java.net.URLDecoder

data class VlessConfig(
    val uuid: String,
    val host: String,
    val port: Int,
    val security: String = "none",
    val encryption: String = "none",
    val type: String = "tcp",
    val flow: String = "",
    val sni: String = "",
    val pbk: String = "",
    val sid: String = "",
    val fp: String = "chrome",
    val path: String = "",
    val headerType: String = "",
    val remark: String = "VLine Server"
)

object VlessParser {

    /**
     * Parses all VLESS links found in a multi-line or subscription string.
     */
    fun parseAll(rawContent: String): List<VlessConfig> {
        if (rawContent.isBlank()) return emptyList()
        return rawContent.lines()
            .map { it.trim() }
            .filter { it.startsWith("vless://", ignoreCase = true) }
            .mapNotNull { parse(it) }
    }

    /**
     * Robust parser for standard 3X-UI VLESS / Reality / TLS / IPv6 configurations.
     */
    fun parse(link: String): VlessConfig? {
        return try {
            val trimmed = link.trim()
            if (!trimmed.startsWith("vless://", ignoreCase = true)) return null

            var uuid: String? = null
            var host: String? = null
            var port: Int = 443

            // 1. First attempt: Direct manual parsing which reliably handles IPv6 bracketed addresses
            val noScheme = trimmed.substring(8) // remove "vless://"
            val atIndex = noScheme.indexOf('@')

            if (atIndex != -1) {
                uuid = noScheme.substring(0, atIndex).trim()
                val afterAt = noScheme.substring(atIndex + 1)
                val queryIndex = afterAt.indexOf('?')
                val fragmentIndex = afterAt.indexOf('#')
                val endIndex = when {
                    queryIndex != -1 && fragmentIndex != -1 -> minOf(queryIndex, fragmentIndex)
                    queryIndex != -1 -> queryIndex
                    fragmentIndex != -1 -> fragmentIndex
                    else -> afterAt.length
                }
                val hostPortPart = afterAt.substring(0, endIndex).trim()

                if (hostPortPart.startsWith("[")) {
                    // IPv6 address: e.g. [2a12:bec4:1cbf:8dd4::1]:36689
                    val closing = hostPortPart.indexOf(']')
                    if (closing != -1) {
                        host = hostPortPart.substring(1, closing).trim()
                        val portPart = hostPortPart.substring(closing + 1).removePrefix(":").trim()
                        port = portPart.toIntOrNull() ?: 443
                    }
                } else if (hostPortPart.contains(':')) {
                    // Standard host:port
                    val lastColon = hostPortPart.lastIndexOf(':')
                    val possiblePort = hostPortPart.substring(lastColon + 1).trim().toIntOrNull()
                    if (possiblePort != null) {
                        host = hostPortPart.substring(0, lastColon).trim()
                        port = possiblePort
                    } else {
                        host = hostPortPart.trim()
                        port = 443
                    }
                } else {
                    host = hostPortPart.trim()
                    port = 443
                }
            }

            // 2. Fallback to Android Uri parsing if manual parsing failed
            if (uuid.isNullOrBlank() || host.isNullOrBlank()) {
                try {
                    val uri = Uri.parse(trimmed)
                    if (uuid.isNullOrBlank()) uuid = uri.userInfo
                    if (host.isNullOrBlank()) host = uri.host?.removePrefix("[")?.removeSuffix("]")
                    if (uri.port != -1) port = uri.port
                } catch (ignored: Exception) {}
            }

            if (uuid.isNullOrBlank() || host.isNullOrBlank()) return null

            // 3. Extract query parameters
            val queryMap = mutableMapOf<String, String>()
            val queryStart = trimmed.indexOf('?')
            val fragmentStart = trimmed.indexOf('#')

            if (queryStart != -1) {
                val queryStr = if (fragmentStart != -1 && fragmentStart > queryStart) {
                    trimmed.substring(queryStart + 1, fragmentStart)
                } else {
                    trimmed.substring(queryStart + 1)
                }
                for (param in queryStr.split('&')) {
                    val kv = param.split('=')
                    if (kv.size == 2) {
                        queryMap[kv[0].trim().lowercase()] = URLDecoder.decode(kv[1].trim(), "UTF-8")
                    }
                }
            }

            val security = queryMap["security"] ?: "none"
            val encryption = queryMap["encryption"] ?: "none"
            val type = queryMap["type"] ?: "tcp"
            val flow = queryMap["flow"] ?: ""
            val sni = queryMap["sni"] ?: queryMap["host"] ?: ""
            val pbk = queryMap["pbk"] ?: ""
            val sid = queryMap["sid"] ?: ""
            val fp = queryMap["fp"] ?: "chrome"
            val path = queryMap["path"] ?: ""
            val headerType = queryMap["headertype"] ?: ""

            // 4. Extract remark from fragment
            val remark = if (fragmentStart != -1) {
                try {
                    URLDecoder.decode(trimmed.substring(fragmentStart + 1).trim(), "UTF-8")
                } catch (e: Exception) {
                    trimmed.substring(fragmentStart + 1).trim()
                }
            } else {
                "VLine Server"
            }

            VlessConfig(
                uuid = uuid,
                host = host,
                port = port,
                security = security,
                encryption = encryption,
                type = type,
                flow = flow,
                sni = sni,
                pbk = pbk,
                sid = sid,
                fp = fp,
                path = path,
                headerType = headerType,
                remark = remark.ifBlank { "VLine Server" }
            )
        } catch (e: Exception) {
            null
        }
    }
}
