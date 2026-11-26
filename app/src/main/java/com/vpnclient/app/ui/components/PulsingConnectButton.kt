package com.vpnclient.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vpnclient.app.ui.theme.AccentAmber
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.AccentGreenGlow
import com.vpnclient.app.ui.theme.HeroCardBorder
import com.vpnclient.app.ui.theme.HeroCardSurface
import com.vpnclient.app.ui.theme.RingCoral
import com.vpnclient.app.vpn.service.VpnState

@Composable
fun PulsingConnectButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val isConnected = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting

    val mainColor = when {
        isConnected -> AccentGreen
        isConnecting -> AccentAmber
        else -> HeroCardBorder
    }

    val glowColor = when {
        isConnected -> AccentGreenGlow
        isConnecting -> Color(0x33F59E0B)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring when connected or connecting
        if (isConnected || isConnecting) {
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(glowColor)
            )
        }

        // Inner main button (Deep obsidian with subtle border matching hero card in image)
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(HeroCardSurface)
                .border(2.dp, if (isConnected) AccentGreen else if (isConnecting) AccentAmber else HeroCardBorder, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(136.dp),
                    color = AccentAmber,
                    strokeWidth = 3.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) mainColor.copy(alpha = 0.15f) else Color.Transparent)
                )
            }

            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "اتصال",
                tint = if (isConnected) AccentGreen else if (isConnecting) AccentAmber else Color.White,
                modifier = Modifier.size(54.dp)
            )
        }
    }
}
