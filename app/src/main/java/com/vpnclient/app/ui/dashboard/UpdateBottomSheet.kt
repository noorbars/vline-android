package com.vpnclient.app.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnclient.app.data.model.AppUpdateInfo
import com.vpnclient.app.ui.components.PersianDigits
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.AccentRed
import com.vpnclient.app.ui.theme.BorderPurple
import com.vpnclient.app.ui.theme.CardBorder
import com.vpnclient.app.ui.theme.PrimaryBlue
import com.vpnclient.app.ui.theme.PrimaryIndigo
import com.vpnclient.app.ui.theme.SurfaceCardPurple
import com.vpnclient.app.ui.theme.SurfaceDark
import com.vpnclient.app.ui.theme.TextDarkPrimary
import com.vpnclient.app.ui.theme.TextDarkSecondary
import com.vpnclient.app.ui.theme.TextMuted
import com.vpnclient.app.ui.theme.TextPrimary
import com.vpnclient.app.ui.theme.TextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    isChecking: Boolean,
    updateInfo: AppUpdateInfo?,
    errorMessage: String?,
    downloadProgress: Int, // 0 to 100, -1 for not downloading, 100 for completed
    downloadedFile: File?,
    onCheckUpdate: () -> Unit,
    onStartDownload: (AppUpdateInfo) -> Unit,
    onInstall: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                // 1. Loading / Checking State
                isChecking -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "در حال بررسی بروزرسانی...",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ارتباط با سرور گیت‌هاب جهت دریافت آخرین نسخه",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 2. Error State
                errorMessage != null -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "خطا در بررسی بروزرسانی",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCheckUpdate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(text = "تلاش مجدد", color = Color.White, fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo?.htmlUrl ?: "https://github.com/noorbars/vline-android"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "صفحه گیت‌هاب", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }

                // 3. Update Available State
                updateInfo != null && updateInfo.hasUpdate -> {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "نسخه جدید در دسترس است! 🚀",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "نسخه فعلی: ${PersianDigits.convert(updateInfo.currentVersion)}  ➜  نسخه جدید: ${PersianDigits.convert(updateInfo.latestVersion)}",
                        color = PrimaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Changelog Box (Lavender card with crisp high-contrast text and parsed markdown)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCardPurple)
                            .border(1.dp, BorderPurple, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryIndigo)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تغییرات این نسخه:",
                                color = PrimaryIndigo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FormattedChangelog(
                            notes = updateInfo.releaseNotes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 150.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Download Progress or Action Buttons
                    if (downloadProgress in 0..99) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = PrimaryBlue,
                                trackColor = Color(0xFFE2E8F0),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "در حال دانلود فایل بروزرسانی: ${PersianDigits.convert(downloadProgress)}٪",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    } else if (downloadedFile != null && downloadedFile.exists()) {
                        Button(
                            onClick = { onInstall(downloadedFile) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "نصب بروزرسانی", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onStartDownload(updateInfo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دانلود و نصب خودکار",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.htmlUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "مشاهده انتشار در گیت‌هاب", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                // 4. Already Up to Date State
                else -> {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "نرم‌افزار شما بروز است",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val curVer = updateInfo?.currentVersion ?: "1.1"
                    Text(
                        text = "شما در حال استفاده از آخرین نسخه موجود (${PersianDigits.convert(curVer)}) هستید.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(text = "متوجه شدم", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

fun buildMarkdownAnnotatedString(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("(\\*\\*(.*?)\\*\\*)|(`(.*?)`)")
        var lastIdx = 0
        for (match in regex.findAll(text)) {
            val range = match.range
            if (range.first > lastIdx) {
                withStyle(SpanStyle(color = defaultColor)) {
                    append(text.substring(lastIdx, range.first))
                }
            }
            if (match.value.startsWith("**")) {
                val boldContent = match.groupValues[2]
                withStyle(SpanStyle(color = defaultColor, fontWeight = FontWeight.Bold)) {
                    append(boldContent)
                }
            } else if (match.value.startsWith("`")) {
                val codeContent = match.groupValues[4]
                withStyle(SpanStyle(color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)) {
                    append(codeContent)
                }
            }
            lastIdx = range.last + 1
        }
        if (lastIdx < text.length) {
            withStyle(SpanStyle(color = defaultColor)) {
                append(text.substring(lastIdx))
            }
        }
    }
}

@Composable
fun FormattedChangelog(
    notes: String,
    modifier: Modifier = Modifier
) {
    val cleanNotes = notes.ifBlank { "بهبود عملکرد، ارتقای پایداری و رفع اشکالات جزئی" }
    val lines = cleanNotes.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (line in lines) {
            when {
                line.startsWith("#") -> {
                    val headerText = line.replace(Regex("^#+\\s*"), "").replace("**", "")
                    Text(
                        text = headerText,
                        color = PrimaryIndigo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                line.startsWith("-") || line.startsWith("*") -> {
                    val rawBulletText = line.replace(Regex("^[-*]\\s*"), "")
                    val annotated = buildMarkdownAnnotatedString(rawBulletText, TextDarkPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = PrimaryIndigo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = annotated,
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    val annotated = buildMarkdownAnnotatedString(line, TextDarkSecondary)
                    Text(
                        text = annotated,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
