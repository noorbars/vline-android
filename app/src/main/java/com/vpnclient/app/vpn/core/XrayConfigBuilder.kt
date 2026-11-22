package com.vpnclient.app.vpn.core

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object XrayConfigBuilder {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private val DOMESTIC_IRANIAN_DOMAINS = listOf(
        "regexp:.*\\.ir$",
        "domain:digikala.com",
        "domain:snapp.ir",
        "domain:snapp.taxi",
        "domain:snapp.express",
        "domain:divar.ir",
        "domain:torob.com",
        "domain:aparat.com",
        "domain:telewebion.com",
        "domain:rubika.ir",
        "domain:bale.ai",
        "domain:eitaa.com",
        "domain:gap.im",
        "domain:shaparak.ir",
        "domain:bmi.ir",
        "domain:bankmellat.ir",
        "domain:bte.ir",
        "domain:tejaratbank.ir",
        "domain:sb24.com",
        "domain:parsian-bank.ir",
        "domain:bsi.ir",
        "domain:sadadpsp.ir",
        "domain:asanpardakht.ir",
        "domain:zarinpal.com"
    )

    /**
     * Generates a fully integrated Xray configuration JSON with native TUN inbound and safe routing rules.
     */
    fun buildConfig(
        vlessConfig: VlessConfig,
        socksPort: Int = 10808,
        httpPort: Int = 10809,
        bypassIran: Boolean = true
    ): String {
        val root = JsonObject()

        // 1. Log Settings
        val logObj = JsonObject().apply {
            addProperty("loglevel", "warning")
        }
        root.add("log", logObj)

        // 2. DNS Settings (Standard ultra-fast IPv4 resolvers via Proxy)
        val dnsObj = JsonObject().apply {
            val servers = JsonArray().apply {
                add("1.1.1.1")
                add("8.8.8.8")
            }
            add("servers", servers)
            addProperty("queryStrategy", "UseIP")
        }
        root.add("dns", dnsObj)

        // 3. Inbounds: Native TUN inbound (captures device packets) + Local SOCKS & HTTP
        val inbounds = JsonArray().apply {
            // SOCKS inbound on localhost
            add(JsonObject().apply {
                addProperty("tag", "socks")
                addProperty("port", socksPort)
                addProperty("listen", "127.0.0.1")
                addProperty("protocol", "socks")
                add("settings", JsonObject().apply {
                    addProperty("auth", "noauth")
                    addProperty("udp", true)
                    addProperty("userLevel", 8)
                })
                add("sniffing", JsonObject().apply {
                    addProperty("enabled", true)
                    val destOverride = JsonArray().apply {
                        add("http")
                        add("tls")
                    }
                    add("destOverride", destOverride)
                })
            })

            // NATIVE TUN INBOUND: Routes packets from the Android TUN interface directly into Xray
            add(JsonObject().apply {
                addProperty("tag", "tun")
                addProperty("protocol", "tun")
                add("settings", JsonObject().apply {
                    addProperty("name", "xray0")
                    addProperty("mtu", 1500)
                    addProperty("userLevel", 8)
                })
                add("sniffing", JsonObject().apply {
                    addProperty("enabled", true)
                    val destOverride = JsonArray().apply {
                        add("http")
                        add("tls")
                    }
                    add("destOverride", destOverride)
                })
            })

            // Local HTTP proxy
            add(JsonObject().apply {
                addProperty("tag", "http")
                addProperty("port", httpPort)
                addProperty("listen", "127.0.0.1")
                addProperty("protocol", "http")
            })
        }
        root.add("inbounds", inbounds)

        // 4. Outbounds (Proxy, Direct, Block, Dns-Out)
        val outbounds = JsonArray().apply {
            // Proxy outbound (VLESS + Reality / TLS)
            add(buildProxyOutbound(vlessConfig))

            // Direct outbound (Freedom) for half-price domestic Iranian traffic
            add(JsonObject().apply {
                addProperty("tag", "direct")
                addProperty("protocol", "freedom")
                add("settings", JsonObject())
            })

            // Block outbound (Blackhole)
            add(JsonObject().apply {
                addProperty("tag", "block")
                addProperty("protocol", "blackhole")
                add("settings", JsonObject().apply {
                    add("response", JsonObject().apply {
                        addProperty("type", "http")
                    })
                })
            })

            // DNS Outbound: Hijacks port 53 DNS queries to internal Xray DNS resolver
            add(JsonObject().apply {
                addProperty("tag", "dns-out")
                addProperty("protocol", "dns")
            })
        }
        root.add("outbounds", outbounds)

        // 5. Routing Rules
        val routingObj = JsonObject().apply {
            addProperty("domainStrategy", "IPIfNonMatch")
            val rules = JsonArray()

            // Rule 0: Hijack port 53 DNS queries from TUN & SOCKS into internal DNS resolver
            rules.add(JsonObject().apply {
                addProperty("type", "field")
                val inboundsList = JsonArray().apply {
                    add("tun")
                    add("socks")
                }
                add("inboundTag", inboundsList)
                addProperty("port", "53")
                addProperty("outboundTag", "dns-out")
            })

            // Rule 1: Route outbound DNS queries directly through proxy
            rules.add(JsonObject().apply {
                addProperty("type", "field")
                addProperty("port", "53")
                addProperty("outboundTag", "proxy")
            })

            if (bypassIran) {
                // Rule 2: Domestic Iranian Domains -> Direct (Half-price, no VPN)
                val domainList = JsonArray()
                DOMESTIC_IRANIAN_DOMAINS.forEach { domainList.add(it) }

                rules.add(JsonObject().apply {
                    addProperty("type", "field")
                    addProperty("outboundTag", "direct")
                    add("domain", domainList)
                })

                // Rule 3: Iranian & Local Private IPs -> Direct (Explicit CIDR list, zero file dependency)
                val ipList = JsonArray().apply {
                    add("10.0.0.0/8")
                    add("172.16.0.0/12")
                    add("192.168.0.0/16")
                    add("127.0.0.0/8")
                    add("169.254.0.0/16")
                    add("::1/128")
                    add("fc00::/7")
                    add("fe80::/10")
                }
                rules.add(JsonObject().apply {
                    addProperty("type", "field")
                    addProperty("outboundTag", "direct")
                    add("ip", ipList)
                })
            }

            // Rule 4: All other internet traffic through Proxy
            rules.add(JsonObject().apply {
                addProperty("type", "field")
                addProperty("outboundTag", "proxy")
                addProperty("network", "tcp,udp")
            })

            add("rules", rules)
        }
        root.add("routing", routingObj)

        return gson.toJson(root)
    }

    private fun buildProxyOutbound(config: VlessConfig): JsonObject {
        val proxy = JsonObject().apply {
            addProperty("tag", "proxy")
            addProperty("protocol", "vless")

            // Settings
            val settings = JsonObject().apply {
                val vnext = JsonArray().apply {
                    val node = JsonObject().apply {
                        addProperty("address", config.host)
                        addProperty("port", config.port)
                        val users = JsonArray().apply {
                            val user = JsonObject().apply {
                                addProperty("id", config.uuid)
                                addProperty("encryption", config.encryption.ifBlank { "none" })
                                if (config.flow.isNotBlank()) {
                                    addProperty("flow", config.flow)
                                }
                            }
                            add(user)
                        }
                        add("users", users)
                    }
                    add(node)
                }
                add("vnext", vnext)
            }
            add("settings", settings)

            // StreamSettings
            val streamSettings = JsonObject().apply {
                addProperty("network", config.type.ifBlank { "tcp" })
                addProperty("security", config.security.ifBlank { "none" })

                if (config.security.equals("reality", ignoreCase = true)) {
                    val reality = JsonObject().apply {
                        addProperty("show", false)
                        addProperty("fingerprint", config.fp.ifBlank { "chrome" })
                        addProperty("serverName", config.sni)
                        addProperty("publicKey", config.pbk)
                        addProperty("shortId", config.sid)
                        addProperty("spiderX", "")
                    }
                    add("realitySettings", reality)
                } else if (config.security.equals("tls", ignoreCase = true)) {
                    val tls = JsonObject().apply {
                        addProperty("serverName", config.sni)
                        addProperty("allowInsecure", false)
                    }
                    add("tlsSettings", tls)
                }

                if (config.type.equals("ws", ignoreCase = true)) {
                    val ws = JsonObject().apply {
                        addProperty("path", config.path.ifBlank { "/" })
                        if (config.sni.isNotBlank()) {
                            val headers = JsonObject().apply {
                                addProperty("Host", config.sni)
                            }
                            add("headers", headers)
                        }
                    }
                    add("wsSettings", ws)
                } else if (config.type.equals("grpc", ignoreCase = true)) {
                    val grpc = JsonObject().apply {
                        addProperty("serviceName", config.path)
                    }
                    add("grpcSettings", grpc)
                }
            }
            add("streamSettings", streamSettings)
        }
        return proxy
    }
}
