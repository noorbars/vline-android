package com.vpnclient.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.vpnclient.app.ui.components.PersianDigits
import com.vpnclient.app.ui.components.PulsingConnectButton
import com.vpnclient.app.ui.components.TrafficCard
import com.vpnclient.app.ui.theme.AccentAmber
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.AccentRed
import com.vpnclient.app.ui.theme.BackgroundDark
import com.vpnclient.app.ui.theme.CardBorder
import com.vpnclient.app.ui.theme.HeroCardBorder
import com.vpnclient.app.ui.theme.HeroCardDark
import com.vpnclient.app.ui.theme.HeroCardSurface
import com.vpnclient.app.ui.theme.PrimaryBlue
import com.vpnclient.app.ui.theme.SurfaceDark
import com.vpnclient.app.ui.theme.TextLightPrimary
import com.vpnclient.app.ui.theme.TextLightSecondary
import com.vpnclient.app.ui.theme.TextMuted
import com.vpnclient.app.ui.theme.TextPrimary
import com.vpnclient.app.ui.theme.TextSecondary
import com.vpnclient.app.vpn.service.VpnState

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onPermissionRequired: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val versionName = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.1"
        } catch (e: Exception) {
            "1.1"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.username.ifBlank { "کاربر وی‌لاین" },
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Action Buttons (Support, Refresh & Logout)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.setSupportSheetVisible(true) }) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "پشتیبانی",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.logout(context)
                            onLoggedOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "خروج",
                            tint = AccentRed.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Obsidian Card: Connect Button, Status & Route/Location Selectors (Matching top card in reference image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x22000000))
                    .clip(RoundedCornerShape(26.dp))
                    .background(HeroCardDark)
                    .border(1.dp, HeroCardBorder, RoundedCornerShape(26.dp))
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated Pulsing Connect Button
                    PulsingConnectButton(
                        vpnState = uiState.vpnState,
                        onClick = {
                            viewModel.toggleConnection(context, onPermissionRequired)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connection Status Text
                    val statusText = when (uiState.vpnState) {
                        is VpnState.Connected -> "متصل هستی"
                        is VpnState.Connecting -> "در حال اتصال…"
                        is VpnState.Disconnected -> "اتصال برقرار نیست"
                        is VpnState.Error -> (uiState.vpnState as VpnState.Error).message
                    }

                    val statusColor = when (uiState.vpnState) {
                        is VpnState.Connected -> AccentGreen
                        is VpnState.Connecting -> AccentAmber
                        is VpnState.Disconnected -> TextLightSecondary
                        is VpnState.Error -> AccentRed
                    }

                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector Pills: Route (مسیر اتصال) & Location (لوکیشن / سرور)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Route Selector Pill
                        Box(
                            modifier = Modifier
                                .weight(1.15f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HeroCardSurface)
                                .border(1.dp, HeroCardBorder, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setRouteSheetVisible(true) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = uiState.selectedRoute.title,
                                        color = TextLightPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextLightSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // 2. Location Selector Pill
                        Box(
                            modifier = Modifier
                                .weight(0.85f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(HeroCardSurface)
                                .border(1.dp, HeroCardBorder, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setLocationSheetVisible(true) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = uiState.selectedLocation.flagEmoji,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = uiState.selectedLocation.countryName,
                                        color = TextLightPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextLightSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // 3. Technical Connection Details Badge & Real-time Metrics Card (When Connected)
                    AnimatedVisibility(
                        visible = uiState.vpnState is VpnState.Connected,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val connectedState = uiState.vpnState as? VpnState.Connected
                        if (connectedState != null) {
                            val details = connectedState.details
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(HeroCardSurface)
                                    .border(1.dp, HeroCardBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                // Protocol & Inbound header row with Retest action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AccentGreen)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${details.protocol}  •  ${details.inboundRemark}",
                                            color = TextLightPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(enabled = !details.isTesting) {
                                                viewModel.retestMetrics(context)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (details.isTesting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = PrimaryBlue,
                                                strokeWidth = 1.5.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "سنجش مجدد",
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (details.isTesting) "سنجش…" else "سنجش مجدد",
                                            color = PrimaryBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Metrics details row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF141620))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Real Delay
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "تاخیر واقعی (Real Delay)",
                                            color = TextLightSecondary,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val delayText = if (details.realDelayMs != null && details.realDelayMs > 0) {
                                            "${PersianDigits.convert(details.realDelayMs)} ms"
                                        } else if (details.isTesting) {
                                            "…"
                                        } else {
                                            "نامشخص"
                                        }
                                        Text(
                                            text = delayText,
                                            color = if (details.realDelayMs != null && details.realDelayMs < 400) AccentGreen else AccentAmber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Raw TCP Ping
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "پینگ سرور (Ping)",
                                            color = TextLightSecondary,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val pingText = if (details.pingMs != null && details.pingMs > 0) {
                                            "${PersianDigits.convert(details.pingMs)} ms"
                                        } else if (details.isTesting) {
                                            "…"
                                        } else {
                                            "نامشخص"
                                        }
                                        Text(
                                            text = pingText,
                                            color = if (details.pingMs != null && details.pingMs < 300) AccentGreen else AccentAmber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Server Host
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "سرور مقصد",
                                            color = TextLightSecondary,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = details.serverAddress.ifBlank { "–" },
                                            color = TextLightPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Section: Domestic Traffic Toggle (نیم‌بها) + Quota Card + Renewal Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Domestic / Half-Price Internet Card (اینترنت نیم‌بها و سایت‌های داخلی)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isIranBypassEnabled) AccentGreen.copy(alpha = 0.15f) else Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (uiState.isIranBypassEnabled) AccentGreen else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "اینترنت نیم‌بها و سایت‌های داخلی",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (uiState.isIranBypassEnabled)
                                    "اینترنت نیم‌بها برای سایت‌های داخلی فعاله"
                                else
                                    "غیرفعال (تمام ترافیک از سرور عبور می‌کند)",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isIranBypassEnabled,
                        onCheckedChange = { viewModel.toggleIranBypass(context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BackgroundDark,
                            uncheckedBorderColor = CardBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. User Quota / Remaining Traffic Card
                TrafficCard(userProfile = uiState.userProfile)

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Subscription Renewal Action Button
                val isSubExpired = uiState.userProfile?.isExpired == true
                val daysRem = uiState.userProfile?.daysRemaining
                val isNearExpiry = daysRem != null && daysRem <= 3

                val renewBtnColor = when {
                    isSubExpired -> AccentRed
                    isNearExpiry -> Color(0xFFF59E0B)
                    else -> PrimaryBlue
                }

                val renewBtnBg = when {
                    isSubExpired -> AccentRed.copy(alpha = 0.16f)
                    isNearExpiry -> Color(0xFFF59E0B).copy(alpha = 0.16f)
                    else -> PrimaryBlue.copy(alpha = 0.12f)
                }

                val hasSubscription = (uiState.userProfile?.expireTimestamp ?: 0L) > 0L
                val renewBtnText = when {
                    !hasSubscription -> "خرید اشتراک"
                    isSubExpired -> "اشتراک شما منقضی شده است — لمس برای تمدید آنی"
                    isNearExpiry -> "اعتبار اشتراک رو به اتمام است (${PersianDigits.convert(daysRem ?: 0L)} روز باقی‌مانده) — تمدید"
                    else -> "تمدید اشتراک"
                }

                Button(
                    onClick = { viewModel.setRenewalSheetVisible(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = renewBtnBg),
                    border = BorderStroke(1.2.dp, renewBtnColor.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = renewBtnColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = renewBtnText,
                        color = renewBtnColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. App Version & In-App GitHub Update Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نسخه ${PersianDigits.convert(versionName)}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.checkForUpdate(context) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdateAlt,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "بررسی بروزرسانی",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Route Selector Bottom Sheet
        if (uiState.isRouteSheetVisible) {
            RouteSelectorBottomSheet(
                selectedRoute = uiState.selectedRoute,
                onSelectRoute = { route ->
                    viewModel.selectRouteCategory(context, route)
                },
                onDismiss = { viewModel.setRouteSheetVisible(false) }
            )
        }

        // Location Selector Bottom Sheet
        if (uiState.isLocationSheetVisible) {
            LocationBottomSheet(
                selectedLocation = uiState.selectedLocation,
                onSelectLocation = { location ->
                    viewModel.selectLocation(location)
                },
                onDismiss = { viewModel.setLocationSheetVisible(false) }
            )
        }

        // In-App GitHub Update Bottom Sheet
        if (uiState.isUpdateSheetVisible) {
            UpdateBottomSheet(
                isChecking = uiState.isCheckingUpdate,
                updateInfo = uiState.appUpdateInfo,
                errorMessage = uiState.updateErrorMessage,
                downloadProgress = uiState.downloadProgress,
                downloadedFile = uiState.downloadedApkFile,
                onCheckUpdate = { viewModel.checkForUpdate(context) },
                onStartDownload = { info -> viewModel.startDownloadUpdate(context, info) },
                onInstall = { file -> viewModel.installDownloadedUpdate(context, file) },
                onDismiss = { viewModel.setUpdateSheetVisible(false) }
            )
        }

        // Renewal Bottom Sheet
        if (uiState.isRenewalSheetVisible) {
            RenewalBottomSheet(
                uiState = uiState,
                onDismiss = { viewModel.setRenewalSheetVisible(false) },
                onRefreshInfo = { viewModel.loadRenewInfo() },
                onResetOrderState = { viewModel.resetOrderState() },
                onSubmitOrder = { planId, planTitle, method, trackingCode, receiptBase64, note ->
                    viewModel.submitOrderRenew(planId, planTitle, method, trackingCode, receiptBase64, note)
                },
                onCheckStatus = { orderId, orderNumber ->
                    viewModel.checkOrderStatus(orderId, orderNumber)
                },
                onCouponCodeChanged = { viewModel.onCouponCodeChanged(it) },
                onValidateCoupon = { planId -> viewModel.validateCoupon(planId) },
                onClearCoupon = { viewModel.clearCoupon() },
                onUseWalletPartialChanged = { viewModel.onUseWalletPartialChanged(it) }
            )
        }

        // Support Bottom Sheet
        if (uiState.isSupportSheetVisible) {
            SupportBottomSheet(
                username = uiState.username,
                onDismiss = { viewModel.setSupportSheetVisible(false) }
            )
        }

        // Floating Cloud Toast Overlay (ابر پیام موقت)
        AnimatedVisibility(
            visible = uiState.infoMessage != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .zIndex(10f)
        ) {
            uiState.infoMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(HeroCardDark.copy(alpha = 0.96f))
                        .border(1.dp, AccentGreen.copy(alpha = 0.45f), RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        color = TextLightPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
