package com.vpnclient.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnclient.app.data.model.LocationManager
import com.vpnclient.app.data.model.ServerLocation
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.CardBorder
import com.vpnclient.app.ui.theme.PrimaryBlue
import com.vpnclient.app.ui.theme.SurfaceDark
import com.vpnclient.app.ui.theme.TextMuted
import com.vpnclient.app.ui.theme.TextPrimary
import com.vpnclient.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    selectedLocation: ServerLocation,
    onSelectLocation: (ServerLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "انتخاب کشور / سرور",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "موقعیت جغرافیایی سرور خروجی خود را انتخاب کنید",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Locations List
            LocationManager.availableLocations.forEach { location ->
                val isSelected = location.id == selectedLocation.id && location.isAvailable
                val isAvailable = location.isAvailable

                val borderColor = if (isSelected) PrimaryBlue.copy(alpha = 0.8f) else CardBorder
                val cardBg = if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else SurfaceDark

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .alpha(if (isAvailable) 1f else 0.5f)
                        .clickable(enabled = isAvailable) {
                            onSelectLocation(location)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Flag Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = location.flagEmoji,
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = location.countryName,
                                        color = if (isSelected) PrimaryBlue else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )

                                    if (location.latencyLabel != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val badgeColor = if (isAvailable) AccentGreen else TextMuted
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(badgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = location.latencyLabel,
                                                color = badgeColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            enabled = isAvailable,
                            onClick = {
                                if (isAvailable) {
                                    onSelectLocation(location)
                                    onDismiss()
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryBlue,
                                unselectedColor = CardBorder,
                                disabledSelectedColor = TextMuted,
                                disabledUnselectedColor = CardBorder.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}
