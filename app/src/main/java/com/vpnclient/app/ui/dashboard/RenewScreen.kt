package com.vpnclient.app.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnclient.app.data.api.models.RenewalPlanDto
import com.vpnclient.app.ui.components.PersianDigits
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.AccentRed
import com.vpnclient.app.ui.theme.BackgroundDark
import com.vpnclient.app.ui.theme.CardBorder
import com.vpnclient.app.ui.theme.PrimaryBlue
import com.vpnclient.app.ui.theme.SurfaceDark
import com.vpnclient.app.ui.theme.TextMuted
import com.vpnclient.app.ui.theme.TextPrimary
import com.vpnclient.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.DecimalFormat

val DEFAULT_RENEWAL_PLANS = listOf(
    RenewalPlanDto("smart_1m", "renew_smart_1m", "۱ ماهه (۳۰ گیگ)", "۳۰ گیگابایت", 30, 30, 299000L, null),
    RenewalPlanDto("smart_3m", "renew_smart_3m", "۳ ماهه (۹۰ گیگ)", "۹۰ گیگابایت", 90, 90, 819000L, "پیشنهادی ⭐"),
    RenewalPlanDto("smart_6m", "renew_smart_6m", "۶ ماهه (۱۲۰ گیگ)", "۱۲۰ گیگابایت", 120, 180, 1519000L, null),
    RenewalPlanDto("smart_12m", "renew_smart_12m", "۱۲ ماهه (۲۴۰ گیگ)", "۲۴۰ گیگابایت", 240, 365, 2899000L, "اقتصادی")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewalBottomSheet(
    uiState: DashboardUiState,
    onDismiss: () -> Unit,
    onRefreshInfo: () -> Unit,
    onSubmitOrder: (planId: String, planTitle: String, paymentMethod: String, trackingCode: String?, receiptBase64: String?, note: String?) -> Unit,
    onCheckStatus: (orderId: Long?, orderNumber: String?) -> Unit,
    onCouponCodeChanged: (String) -> Unit = {},
    onValidateCoupon: (String) -> Unit = {},
    onClearCoupon: () -> Unit = {},
    onUseWalletPartialChanged: (Boolean) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        RenewScreenContent(
            uiState = uiState,
            onDismiss = onDismiss,
            onRefreshInfo = onRefreshInfo,
            onSubmitOrder = onSubmitOrder,
            onCheckStatus = onCheckStatus,
            onCouponCodeChanged = onCouponCodeChanged,
            onValidateCoupon = onValidateCoupon,
            onClearCoupon = onClearCoupon,
            onUseWalletPartialChanged = onUseWalletPartialChanged
        )
    }
}

@Composable
fun RenewScreenContent(
    uiState: DashboardUiState,
    onDismiss: () -> Unit,
    onRefreshInfo: () -> Unit,
    onSubmitOrder: (planId: String, planTitle: String, paymentMethod: String, trackingCode: String?, receiptBase64: String?, note: String?) -> Unit,
    onCheckStatus: (orderId: Long?, orderNumber: String?) -> Unit,
    onCouponCodeChanged: (String) -> Unit = {},
    onValidateCoupon: (String) -> Unit = {},
    onClearCoupon: () -> Unit = {},
    onUseWalletPartialChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val decimalFormatter = remember { DecimalFormat("#,###") }

    val renewData = uiState.renewInfoData
    val availablePlans = if (!renewData?.plans.isNullOrEmpty()) renewData!!.plans else DEFAULT_RENEWAL_PLANS

    var selectedPlan by remember(availablePlans) { mutableStateOf(availablePlans[0]) }
    var trackingCode by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var receiptBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var receiptBase64 by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                receiptBitmap = bitmap

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                val bytes = baos.toByteArray()
                receiptBase64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در بارگذاری تصویر فیش", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Determine current order state
    val pendingOrderId = uiState.submittedOrderId ?: renewData?.pendingOrder?.orderId
    val pendingOrderNumber = uiState.submittedOrderNumber ?: renewData?.pendingOrder?.orderNumber
    val isPendingReview = (uiState.renewalSubmittedSuccess && !uiState.isWalletRenewSuccess) ||
            (renewData?.pendingOrder != null && uiState.activeOrderStatus?.isApproved != true && uiState.activeOrderStatus?.isCancelled != true)

    val isApproved = uiState.activeOrderStatus?.isApproved == true
    val isCancelled = uiState.activeOrderStatus?.isCancelled == true
    val isWalletSuccess = uiState.isWalletRenewSuccess

    // Periodic check while waiting for approval
    LaunchedEffect(isPendingReview, pendingOrderId, isApproved, isCancelled) {
        if (isPendingReview && !isApproved && !isCancelled && pendingOrderId != null) {
            while (true) {
                delay(10000) // check every 10 seconds
                onCheckStatus(pendingOrderId, pendingOrderNumber)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = when {
                isApproved -> "approved"
                isWalletSuccess -> "wallet_success"
                isCancelled -> "cancelled"
                isPendingReview -> "pending"
                uiState.isLoadingRenewInfo -> "loading"
                else -> "form"
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "renew_screen_state"
        ) { targetState ->
            when (targetState) {
                "loading" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "در حال بارگذاری اطلاعات پلن‌ها و موجودی...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                "wallet_success" -> {
                    // Instant Wallet Success Screen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "تمدید آنی با موفقیت انجام شد! 🎉",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "هزینه از موجودی کیف پول شما کسر و سرویس فوراً در سرور تمدید شد.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.submittedOrderNumber != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "شماره سفارش: ${uiState.submittedOrderNumber}",
                                    color = PrimaryBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("متوجه شدم (بازگشت به برنامه)", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                "approved" -> {
                    // Admin Approved Screen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "اشتراک شما با موفقیت تمدید شد! 🎉",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "سفارش شما توسط مدیریت تایید و سرویس با موفقیت شارژ شد.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.activeOrderStatus?.expireDate?.let { exp ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "تاریخ انقضای جدید: ${PersianDigits.convert(exp)}",
                                    color = AccentGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("متوجه شدم", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                "cancelled" -> {
                    // Order Cancelled Screen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(AccentRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "سفارش توسط مدیریت لغو یا رد شد",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "سفارش ثبت‌شده لغو شده است. جهت اطلاعات بیشتر با پشتیبانی در ارتباط باشید یا مجدداً فیش معتبر ارسال فرمایید.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/vlinesup"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "ارتباط با پشتیبانی تلگرام (@vlinesup)", color = PrimaryBlue, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onRefreshInfo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("ثبت درخواست جدید", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                "pending" -> {
                    // Pending Review View with Live Polling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "در انتظار تایید مدیریت... ⏳",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val displayOrder = pendingOrderNumber ?: "در حال بررسی"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "شماره سفارش: $displayOrder",
                                color = PrimaryBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "اطلاعات فیش و سفارش شما در تلگرام برای مدیریت ارسال شده است.\nبه محض تایید توسط ادمین، سرویس به صورت خودکار فعال شده و تاریخ انقضا بروزرسانی می‌شود.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Status Check Button with Spinner
                        Button(
                            onClick = { onCheckStatus(pendingOrderId, pendingOrderNumber) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (uiState.isCheckingOrderStatus) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("در حال بررسی وضعیت...", fontSize = 13.sp, color = Color.White)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("بررسی مجدد وضعیت تایید", fontSize = 13.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/vlinesup"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "ارسال پیام به پشتیبانی تلگرام (@vlinesup)", color = PrimaryBlue, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("بستن پنجره", fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }

                else -> {
                    // Form View: Plan Selection + Wallet + Card-to-Card
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Title row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val isFirstPurchase = (uiState.userProfile?.expireTimestamp ?: 0L) == 0L
                                Text(
                                    text = if (isFirstPurchase) "خرید اشتراک سرویس" else "تمدید اشتراک سرویس",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "حساب کاربری: ${uiState.username.ifBlank { "کاربر وی‌لاین" }}",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Current Subscription Status Warning (if expired or near expiry)
                        val subStatus = renewData?.subscription
                        if (subStatus != null) {
                            val isExp = subStatus.isExpired
                            val bgColor = if (isExp) AccentRed.copy(alpha = 0.12f) else Color(0xFFF3F0FF)
                            val borderColor = if (isExp) AccentRed.copy(alpha = 0.4f) else Color(0xFFDDD6FE)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = if (isExp) "وضعیت: منقضی شده ⚠️" else "وضعیت: فعال ✅",
                                            color = if (isExp) AccentRed else AccentGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        subStatus.expireDate?.let { exp ->
                                            Text(
                                                text = "تاریخ انقضا: ${PersianDigits.convert(exp)}",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Wallet Balance Badge
                                    val walletBal = renewData.walletBalance
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryBlue.copy(alpha = 0.12f))
                                            .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "کیف پول: ${PersianDigits.convert(decimalFormatter.format(walletBal))} ت",
                                                color = PrimaryBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Text(
                            text = "۱. پلن مورد نظر برای تمدید را انتخاب کنید:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Plan Choices
                        availablePlans.forEach { plan ->
                            val isSelected = plan.id == selectedPlan.id
                            val priceStr = "${PersianDigits.convert(decimalFormatter.format(plan.price))} تومان"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else SurfaceDark)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PrimaryBlue else CardBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        if (selectedPlan.id != plan.id) {
                                            selectedPlan = plan
                                            if (uiState.appliedCoupon != null) {
                                                onClearCoupon()
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryBlue else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = plan.title,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            plan.tag?.let { tagText ->
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(PrimaryBlue.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = tagText,
                                                        color = PrimaryBlue,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "ترافیک: ${plan.traffic} • اعتبار: ${PersianDigits.convert(plan.durationDays)} روز",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = priceStr,
                                    color = if (isSelected) PrimaryBlue else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- COUPON / DISCOUNT CODE SECTION ---
                        val appliedCoupon = uiState.appliedCoupon
                        val finalPrice = appliedCoupon?.finalPrice ?: selectedPlan.price

                        if (appliedCoupon != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentGreen.copy(alpha = 0.12f))
                                    .border(1.dp, AccentGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "کد تخفیف ${appliedCoupon.code} اعمال شد",
                                            color = AccentGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "تخفیف: ${PersianDigits.convert(decimalFormatter.format(appliedCoupon.discountAmount))} تومان",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onClearCoupon,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف کد",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.couponCodeInput,
                                    onValueChange = onCouponCodeChanged,
                                    label = { Text("کد تخفیف (اختیاری)", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = CardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = PrimaryBlue,
                                        unfocusedLabelColor = TextMuted
                                    )
                                )

                                Button(
                                    onClick = { onValidateCoupon(selectedPlan.id) },
                                    enabled = !uiState.isValidatingCoupon && uiState.couponCodeInput.isNotBlank(),
                                    modifier = Modifier.height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    if (uiState.isValidatingCoupon) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text("اعمال", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            if (!uiState.couponError.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uiState.couponError,
                                    color = AccentRed,
                                    fontSize = 11.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- WALLET INSTANT OR PARTIAL PAYMENT OPTION ---
                        val currentWallet = renewData?.walletBalance ?: 0L
                        val isPartialWalletActive = currentWallet in 1 until finalPrice && uiState.useWalletPartial
                        val payableCardAmount = if (isPartialWalletActive) (finalPrice - currentWallet).coerceAtLeast(0L) else finalPrice

                        if (currentWallet in 1 until finalPrice) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.08f))
                                    .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .clickable { onUseWalletPartialChanged(!uiState.useWalletPartial) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "استفاده از موجودی کیف پول",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "کسر ${PersianDigits.convert(decimalFormatter.format(currentWallet))} تومان از کل مبلغ",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.useWalletPartial,
                                    onCheckedChange = onUseWalletPartialChanged,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryBlue
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (currentWallet >= finalPrice) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AccentGreen.copy(alpha = 0.08f))
                                    .border(1.5.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(AccentGreen.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = AccentGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "پرداخت آنی از کیف پول",
                                                color = AccentGreen,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "موجودی: ${PersianDigits.convert(decimalFormatter.format(currentWallet))} تومان (کافی)",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "بدون نیاز به واریز کارت و ارسال فیش، مبلغ این پلن (${PersianDigits.convert(decimalFormatter.format(finalPrice))} تومان) از موجودی کیف پول شما کسر و سرویس فوراً فعال می‌شود.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            onSubmitOrder(selectedPlan.id, selectedPlan.title, "wallet", null, null, null)
                                        },
                                        enabled = !uiState.isSubmittingRenewal,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                    ) {
                                        if (uiState.isSubmittingRenewal) {
                                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                        } else {
                                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "پرداخت آنی با کیف پول (${PersianDigits.convert(decimalFormatter.format(finalPrice))} تومان)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                                Text(
                                    text = "یا پرداخت کارت به کارت",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorder)
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // --- CARD-TO-CARD PAYMENT SECTION ---
                        Text(
                            text = "۲. واریز کارت به کارت و ارسال فیش:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Card Information Box
                        val cardNumber = renewData?.cardInfo?.cardNumber ?: "6037997573065639"
                        val cardName = renewData?.cardInfo?.cardName ?: "نوربار"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF111319))
                                .border(1.dp, Color(0xFF2A2D3C), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = Color(0xFF2DD4BF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "شماره کارت بانکی",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", cardNumber))
                                            Toast.makeText(context, "شماره کارت کپی شد", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "کپی",
                                            tint = Color(0xFF2DD4BF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = PersianDigits.convert(cardNumber.chunked(4).joinToString(" - ")),
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", cardNumber))
                                            Toast.makeText(context, "شماره کارت کپی شد", Toast.LENGTH_SHORT).show()
                                        }
                                )

                                Text(
                                    text = "به نام: $cardName",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                if (appliedCoupon != null) {
                                    Text(
                                        text = "مبلغ اصلی: ${PersianDigits.convert(decimalFormatter.format(selectedPlan.price))} تومان",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textDecoration = TextDecoration.LineThrough,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (isPartialWalletActive) {
                                    Text(
                                        text = "کسر از کیف پول: -${PersianDigits.convert(decimalFormatter.format(currentWallet))} تومان",
                                        color = AccentGreen,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Text(
                                    text = "مبلغ قابل پرداخت: ${PersianDigits.convert(decimalFormatter.format(payableCardAmount))} تومان",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Receipt Upload Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceDark)
                                .border(1.dp, if (receiptBitmap != null) PrimaryBlue else CardBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(12.dp)
                        ) {
                            if (receiptBitmap != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            bitmap = receiptBitmap!!.asImageBitmap(),
                                            contentDescription = "فیش واریزی",
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "تصویر فیش انتخاب شد ✅",
                                                color = AccentGreen,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "لمس برای تغییر تصویر",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedImageUri = null
                                            receiptBitmap = null
                                            receiptBase64 = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = AccentRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "انتخاب تصویر فیش واریزی از گالری",
                                        color = PrimaryBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tracking Code Input
                        OutlinedTextField(
                            value = trackingCode,
                            onValueChange = { trackingCode = it },
                            label = { Text("کد پیگیری یا شماره تراکنش (اختیاری)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // User Note Input
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("توضیحات یا یادداشت برای پشتیبانی (اختیاری)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedLabelColor = TextMuted
                            )
                        )

                        if (!uiState.renewalErrorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.renewalErrorMessage,
                                color = AccentRed,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button for Card-to-Card
                        Button(
                            onClick = {
                                if (receiptBase64 == null && trackingCode.isBlank()) {
                                    Toast.makeText(context, "لطفاً تصویر فیش یا کد پیگیری واریز را وارد کنید.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onSubmitOrder(
                                    selectedPlan.id,
                                    selectedPlan.title,
                                    "card",
                                    trackingCode.trim().ifEmpty { null },
                                    receiptBase64,
                                    note.trim().ifEmpty { null }
                                )
                            },
                            enabled = !uiState.isSubmittingRenewal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (uiState.isSubmittingRenewal) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("در حال ارسال فیش و ثبت سفارش...", fontSize = 13.sp, color = Color.White)
                            } else {
                                Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ارسال فیش و ثبت درخواست تمدید", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
