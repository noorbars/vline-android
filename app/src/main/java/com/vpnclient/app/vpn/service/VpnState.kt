package com.vpnclient.app.vpn.service

data class ConnectionDetails(
    val protocol: String = "",
    val inboundRemark: String = "",
    val serverAddress: String = "",
    val security: String = "",
    val realDelayMs: Long? = null,
    val pingMs: Long? = null,
    val isTesting: Boolean = false
)

sealed class VpnState {
    data object Disconnected : VpnState()
    data object Connecting : VpnState()
    data class Connected(val details: ConnectionDetails = ConnectionDetails()) : VpnState()
    data class Error(val message: String) : VpnState()
}
