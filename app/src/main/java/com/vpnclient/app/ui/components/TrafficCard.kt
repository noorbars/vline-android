package com.vpnclient.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnclient.app.data.api.models.UserProfile
import com.vpnclient.app.ui.theme.BorderPurple
import com.vpnclient.app.ui.theme.PrimaryBlue
import com.vpnclient.app.ui.theme.RingAmber
import com.vpnclient.app.ui.theme.RingCoral
import com.vpnclient.app.ui.theme.RingCyan
import com.vpnclient.app.ui.theme.RingPurple
import com.vpnclient.app.ui.theme.RingPurpleDeep
import com.vpnclient.app.ui.theme.SurfaceCardBorder
import com.vpnclient.app.ui.theme.SurfaceCardLight
import com.vpnclient.app.ui.theme.SurfaceCardPurple
import com.vpnclient.app.ui.theme.TextDarkMuted
import com.vpnclient.app.ui.theme.TextDarkPrimary
import com.vpnclient.app.ui.theme.TextDarkSecondary

@Composable
fun TrafficCard(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    val remainingDays = userProfile?.remainingDays ?: 0L
    val remainingFormatted = userProfile?.formattedRemainingTraffic() ?: "در حال دریافت..."
    val trafficPercent = userProfile?.trafficPercentRemaining ?: 100
    val daysPercent = userProfile?.daysPercentRemaining ?: 100

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. کارت حجم باقیمانده (Circular Traffic Meter Card)
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0F000000))
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCardLight)
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DataUsage,
                        contentDescription = null,
                        tint = RingCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "حجم باقیمانده",
                        color = TextDarkSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Circular Ring with Percentage
                CircularMetricRing(
                    percentage = trafficPercent,
                    gradientColors = listOf(RingCoral, RingAmber),
                    trackColor = Color(0xFFF1F5F9),
                    subLabel = "باقیمانده"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Detailed text
                Text(
                    text = PersianDigits.convert(remainingFormatted),
                    color = TextDarkPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. کارت زمان باقیمانده (Circular Days Meter Card - Lavender Health Style)
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0F000000))
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCardPurple)
                .border(1.dp, BorderPurple, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = RingPurpleDeep,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "زمان باقیمانده",
                        color = RingPurpleDeep,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Circular Ring with Percentage
                CircularMetricRing(
                    percentage = daysPercent,
                    gradientColors = listOf(RingPurple, RingPurpleDeep),
                    trackColor = Color(0xFFE9E4F7),
                    subLabel = "اعتبار"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Detailed text
                val daysText = if (remainingDays >= 0) {
                    "${PersianDigits.convert(remainingDays)} روز"
                } else {
                    "نامحدود"
                }
                Text(
                    text = daysText,
                    color = RingPurpleDeep,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CircularMetricRing(
    percentage: Int,
    gradientColors: List<Color>,
    trackColor: Color,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 1000),
        label = "ring_progress"
    )

    Box(
        modifier = modifier.size(86.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(86.dp)) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track Circle
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground Gradient Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = gradientColors,
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Percentage and Subtext
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${PersianDigits.convert(percentage)}%",
                color = TextDarkPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )
            Text(
                text = subLabel,
                color = TextDarkMuted,
                fontSize = 9.sp,
                lineHeight = 11.sp
            )
        }
    }
}
