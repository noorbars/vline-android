package com.vpnclient.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnclient.app.ui.components.PersianDigits
import com.vpnclient.app.ui.components.PulsingConnectButton
import com.vpnclient.app.ui.components.TrafficCard
import com.vpnclient.app.ui.theme.AccentAmber
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.AccentRed
import com.vpnclient.app.ui.theme.BackgroundDark
import com.vpnclient.app.ui.theme.CardBorder
import com.vpnclient.app.ui.theme.PrimaryBlue
import com.vpnclient.app.ui.theme.SurfaceDark
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

            // Center Area: Pulsing Button, Status Display & Route/Location Selectors
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Pulsing Connect Button
                PulsingConnectButton(
                    vpnState = uiState.vpnState,
                    onClick = {
                        viewModel.toggleConnection(context, onPermissionRequired)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

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
                    is VpnState.Disconnected -> TextMuted
                    is VpnState.Error -> AccentRed
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector Pills: Route (مسیر اتصال) & Location (لوکیشن / سرور)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Route Selector Pill
                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
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
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 2. Location Selector Pill
                    Box(
                        modifier = Modifier
                            .weight(0.85f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
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
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
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
                                .background(SurfaceDark)
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
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
                                        color = TextPrimary,
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
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Real Delay
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "تاخیر واقعی (Real Delay)",
                                        color = TextMuted,
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
                                        color = TextMuted,
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
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = details.serverAddress.ifBlank { "–" },
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
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
                // Dynamic feedback toast/banner if available
                AnimatedVisibility(
                    visible = uiState.infoMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.infoMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = AccentGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }

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
                Button(
                    onClick = { viewModel.setRenewalSheetVisible(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تمدید اشتراک",
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
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
                username = uiState.username,
                isSubmitting = uiState.isSubmittingRenewal,
                isSubmittedSuccess = uiState.renewalSubmittedSuccess,
                submittedOrderNumber = uiState.submittedOrderNumber,
                submittedPlanName = uiState.renewalPlanName,
                errorMessage = uiState.renewalErrorMessage,
                onDismiss = { viewModel.setRenewalSheetVisible(false) },
                onSubmit = { plan, trackingCode, receiptBase64, note ->
                    viewModel.submitRenewalRequest(plan, trackingCode, receiptBase64, note)
                }
            )
        }

        // Support Bottom Sheet
        if (uiState.isSupportSheetVisible) {
            SupportBottomSheet(
                username = uiState.username,
                onDismiss = { viewModel.setSupportSheetVisible(false) }
            )
        }
    }
}
