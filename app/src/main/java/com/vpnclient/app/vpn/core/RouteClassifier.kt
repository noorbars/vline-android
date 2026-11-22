package com.vpnclient.app.vpn.core

import android.util.Log

object RouteClassifier {

    private const val TAG = "RouteClassifier"

    /**
     * Categorizes subscription VlessConfigs into user-facing route categories:
     * - STABLE: TLS route (Port 8443 / TLS)
     * - FAST: High-speed Reality route (Port 443 / Reality)
     * - SPECIAL_IR: Mobile optimized IPv6 route (Port 36689 / IPv6)
     */
    fun classify(configs: List<VlessConfig>): Map<RouteCategory, VlessConfig> {
        val result = mutableMapOf<RouteCategory, VlessConfig>()
        if (configs.isEmpty()) return result

        val remaining = configs.toMutableList()

        // 1. IPv6 Candidate (Special IR - Mobile Operator Route)
        val ipv6Candidate = remaining.firstOrNull { config ->
            val isIpv6Port = config.port == 36689
            val hostHasColon = config.host.contains(":")
            val remarkHasIpv6 = config.remark.contains("ipv6", ignoreCase = true) ||
                    config.remark.contains("v6", ignoreCase = true) ||
                    config.remark.contains("همراه", ignoreCase = true)
            isIpv6Port || hostHasColon || remarkHasIpv6
        }
        if (ipv6Candidate != null) {
            result[RouteCategory.SPECIAL_IR] = ipv6Candidate
            remaining.remove(ipv6Candidate)
        }

        // 2. Reality Candidate (Fast Route)
        val realityCandidate = remaining.firstOrNull { config ->
            val isPort443 = config.port == 443
            val isReality = (config.security.equals("reality", ignoreCase = true) || config.pbk.isNotBlank()) &&
                    config.port != 36689 && !config.host.contains(":")
            val remarkHasReality = config.remark.contains("reality", ignoreCase = true)
            isPort443 || isReality || remarkHasReality
        }
        if (realityCandidate != null) {
            result[RouteCategory.FAST] = realityCandidate
            remaining.remove(realityCandidate)
        }

        // 3. TLS Candidate (Stable Route)
        val tlsCandidate = remaining.firstOrNull { config ->
            val isPort8443 = config.port == 8443
            val isTls = config.security.equals("tls", ignoreCase = true)
            val remarkHasTls = config.remark.contains("tls", ignoreCase = true)
            isPort8443 || isTls || remarkHasTls
        } ?: remaining.firstOrNull()

        if (tlsCandidate != null) {
            result[RouteCategory.STABLE] = tlsCandidate
            remaining.remove(tlsCandidate)
        }

        // Fallbacks for any unassigned category
        val defaultFallback = result[RouteCategory.STABLE] ?: configs.first()
        if (!result.containsKey(RouteCategory.STABLE)) {
            result[RouteCategory.STABLE] = defaultFallback
        }
        if (!result.containsKey(RouteCategory.FAST)) {
            result[RouteCategory.FAST] = remaining.firstOrNull() ?: defaultFallback
        }
        if (!result.containsKey(RouteCategory.SPECIAL_IR)) {
            result[RouteCategory.SPECIAL_IR] = remaining.firstOrNull() ?: defaultFallback
        }

        Log.d(TAG, "Classified ${configs.size} configs: STABLE=${result[RouteCategory.STABLE]?.port}, " +
                "FAST=${result[RouteCategory.FAST]?.port}, SPECIAL_IR=${result[RouteCategory.SPECIAL_IR]?.port}")

        return result
    }

    /**
     * Returns candidates ordered strictly by Smart Connection priority:
     * Priority 1: STABLE (TLS)
     * Priority 2: FAST (Reality)
     * Priority 3: SPECIAL_IR (IPv6)
     */
    fun getSmartCandidates(configs: List<VlessConfig>): List<Pair<RouteCategory, VlessConfig>> {
        val classified = classify(configs)
        val priorities = listOf(RouteCategory.STABLE, RouteCategory.FAST, RouteCategory.SPECIAL_IR)
        val list = mutableListOf<Pair<RouteCategory, VlessConfig>>()
        val seen = mutableSetOf<String>()

        for (category in priorities) {
            val candidate = classified[category]
            if (candidate != null) {
                val identifier = "${candidate.host}:${candidate.port}:${candidate.security}:${candidate.pbk}"
                if (!seen.contains(identifier)) {
                    seen.add(identifier)
                    list.add(category to candidate)
                }
            }
        }

        if (list.isEmpty()) {
            configs.forEachIndexed { index, cfg ->
                val cat = when (index) {
                    0 -> RouteCategory.STABLE
                    1 -> RouteCategory.FAST
                    else -> RouteCategory.SPECIAL_IR
                }
                list.add(cat to cfg)
            }
        }

        return list
    }
}
