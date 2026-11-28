package com.vpnclient.app.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnclient.app.ui.components.PersianDigits
import com.vpnclient.app.ui.theme.AccentGreen
import com.vpnclient.app.ui.theme.AccentRed
import com.vpnclient.app.ui.theme.BackgroundLight
import com.vpnclient.app.ui.theme.HeroCardDark
import com.vpnclient.app.ui.theme.PrimaryIndigo
import com.vpnclient.app.ui.theme.RingCoral
import com.vpnclient.app.ui.theme.SurfaceCardBorder
import com.vpnclient.app.ui.theme.SurfaceCardLight
import com.vpnclient.app.ui.theme.TextDarkMuted
import com.vpnclient.app.ui.theme.TextDarkPrimary
import com.vpnclient.app.ui.theme.TextDarkSecondary

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .imePadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon (Official rounded V-Line logo)
            Image(
                painter = painterResource(id = com.vpnclient.app.R.drawable.ic_vline_logo),
                contentDescription = "لوگوی وی‌لاین",
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val headerTitle = when {
                uiState.authMode == AuthMode.REGISTER && uiState.isRegisterOtpSent -> "تایید ایمیل (کد ۶ رقمی)"
                uiState.authMode == AuthMode.LOGIN -> "ورود به وی‌لاین"
                uiState.authMode == AuthMode.REGISTER -> "ساخت حساب کاربری"
                uiState.authMode == AuthMode.FORGOT_PASSWORD -> "بازیابی رمز عبور"
                else -> "ورود به وی‌لاین"
            }

            val headerSub = when {
                uiState.authMode == AuthMode.REGISTER && uiState.isRegisterOtpSent -> "کد تایید ارسال‌شده به ایمیل را وارد فرمایید"
                uiState.authMode == AuthMode.LOGIN -> "ایمیل و رمز عبور خود را وارد کنید"
                uiState.authMode == AuthMode.REGISTER -> "برای ساخت اکانت، ایمیل و رمز دلخواه را وارد کنید"
                uiState.authMode == AuthMode.FORGOT_PASSWORD -> if (uiState.isResetCodeSent)
                    "کد ۶ رقمی ارسال‌شده به ایمیل را وارد کنید"
                else
                    "ایمیل خود را جهت دریافت کد بازیابی وارد کنید"
                else -> ""
            }

            Text(
                text = headerTitle,
                color = TextDarkPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = headerSub,
                color = TextDarkSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Card (White elevated card with soft border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x1A000000))
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceCardLight)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Segmented Mode Selector Pill (ورود | ثبت‌نام) - Visible when not in sub-flow
                    val showSegmentedSelector = uiState.authMode != AuthMode.FORGOT_PASSWORD && !uiState.isRegisterOtpSent
                    if (showSegmentedSelector) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BackgroundLight)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                                .padding(3.dp)
                        ) {
                            // 1. ورود
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (uiState.authMode == AuthMode.LOGIN) HeroCardDark else Color.Transparent)
                                    .clickable { viewModel.setAuthMode(AuthMode.LOGIN) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ورود",
                                    color = if (uiState.authMode == AuthMode.LOGIN) Color.White else TextDarkSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // 2. ثبت‌نام
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (uiState.authMode == AuthMode.REGISTER) HeroCardDark else Color.Transparent)
                                    .clickable { viewModel.setAuthMode(AuthMode.REGISTER) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ثبت‌نام",
                                    color = if (uiState.authMode == AuthMode.REGISTER) Color.White else TextDarkSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // --- REGISTER STEP 2: OTP VERIFICATION VIEW ---
                    if (uiState.authMode == AuthMode.REGISTER && uiState.isRegisterOtpSent) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Target Email Display Chip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(HeroCardDark)
                                    .clickable { viewModel.cancelRegisterOtp() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = uiState.username,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ویرایش",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تغییر ایمیل",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "کد تایید ۶ رقمی به ایمیل بالا ارسال شد:",
                                color = TextDarkSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 6-Digit OTP Input Field (Bold, centered, wide letter spacing)
                            OutlinedTextField(
                                value = uiState.registerOtpCode,
                                onValueChange = viewModel::onRegisterOtpCodeChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "— — — — — —",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 6.sp,
                                        color = TextDarkMuted
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 8.sp,
                                    textAlign = TextAlign.Center,
                                    color = TextDarkPrimary
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = SurfaceCardBorder,
                                    focusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                    unfocusedContainerColor = BackgroundLight.copy(alpha = 0.5f)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.submit()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Row: Resend OTP (with cooldown) & Change Email
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (uiState.resendCooldownSeconds > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = TextDarkMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ارسال مجدد کد (${PersianDigits.convert(uiState.resendCooldownSeconds)} ثانیه)",
                                            color = TextDarkMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = if (uiState.isResendingOtp) "در حال ارسال..." else "ارسال مجدد کد",
                                        color = PrimaryIndigo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(enabled = !uiState.isResendingOtp) { viewModel.resendRegisterOtp() }
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    )
                                }

                                Text(
                                    text = "تغییر ایمیل / بازگشت",
                                    color = TextDarkSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.cancelRegisterOtp() }
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    } else {
                        // --- STANDARD FORM: LOGIN / REGISTER STEP 1 / FORGOT PASSWORD ---

                        // Email / Username Input
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = viewModel::onUsernameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    when (uiState.authMode) {
                                        AuthMode.LOGIN -> "ایمیل یا نام کاربری"
                                        AuthMode.REGISTER -> "ایمیل (نام کاربری)"
                                        AuthMode.FORGOT_PASSWORD -> "ایمیل ثبت‌شده"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.authMode == AuthMode.LOGIN) Icons.Default.Person else Icons.Default.Email,
                                    contentDescription = null,
                                    tint = TextDarkMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                unfocusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                focusedTextColor = TextDarkPrimary,
                                unfocusedTextColor = TextDarkPrimary,
                                focusedLabelColor = PrimaryIndigo,
                                unfocusedLabelColor = TextDarkMuted
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = if (uiState.authMode == AuthMode.FORGOT_PASSWORD && !uiState.isResetCodeSent) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (uiState.authMode == AuthMode.FORGOT_PASSWORD && !uiState.isResetCodeSent) {
                                        focusManager.clearFocus()
                                        viewModel.submit()
                                    }
                                }
                            )
                        )

                        // Reset Code Input (Only in Forgot Password mode when code is sent)
                        AnimatedVisibility(visible = uiState.authMode == AuthMode.FORGOT_PASSWORD && uiState.isResetCodeSent) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.resetCode,
                                    onValueChange = viewModel::onResetCodeChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("کد تایید ۶ رقمی") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = TextDarkMuted
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigo,
                                        unfocusedBorderColor = SurfaceCardBorder,
                                        focusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                        unfocusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                        focusedTextColor = TextDarkPrimary,
                                        unfocusedTextColor = TextDarkPrimary,
                                        focusedLabelColor = PrimaryIndigo,
                                        unfocusedLabelColor = TextDarkMuted
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    )
                                )
                            }
                        }

                        // Password Input (For Login, Register Step 1, or Forgot Password stage 2)
                        val showPasswordField = uiState.authMode != AuthMode.FORGOT_PASSWORD || uiState.isResetCodeSent
                        AnimatedVisibility(visible = showPasswordField) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.password,
                                    onValueChange = viewModel::onPasswordChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = {
                                        Text(
                                            if (uiState.authMode == AuthMode.FORGOT_PASSWORD) "رمز عبور جدید" else "رمز عبور"
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = TextDarkMuted
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isPasswordVisible) "مخفی کردن" else "نمایش",
                                                tint = TextDarkMuted
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigo,
                                        unfocusedBorderColor = SurfaceCardBorder,
                                        focusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                        unfocusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                        focusedTextColor = TextDarkPrimary,
                                        unfocusedTextColor = TextDarkPrimary,
                                        focusedLabelColor = PrimaryIndigo,
                                        unfocusedLabelColor = TextDarkMuted
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = if (uiState.authMode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (uiState.authMode != AuthMode.REGISTER) {
                                                focusManager.clearFocus()
                                                viewModel.submit()
                                            }
                                        }
                                    )
                                )
                            }
                        }

                        // Confirm Password Input (Only for Register Step 1)
                        AnimatedVisibility(visible = uiState.authMode == AuthMode.REGISTER) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.confirmPassword,
                                    onValueChange = viewModel::onConfirmPasswordChanged,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("تکرار رمز عبور") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = TextDarkMuted
                                        )
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigo,
                                        unfocusedBorderColor = SurfaceCardBorder,
                                        focusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                        unfocusedContainerColor = BackgroundLight.copy(alpha = 0.5f),
                                        focusedTextColor = TextDarkPrimary,
                                        unfocusedTextColor = TextDarkPrimary,
                                        focusedLabelColor = PrimaryIndigo,
                                        unfocusedLabelColor = TextDarkMuted
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.submit()
                                        }
                                    )
                                )
                            }
                        }

                        // "فراموشی رمز عبور؟" Link in Login mode
                        if (uiState.authMode == AuthMode.LOGIN) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "فراموشی رمز عبور؟",
                                    color = PrimaryIndigo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { viewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    // Success Feedback Card
                    if (uiState.successMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGreen.copy(alpha = 0.12f))
                                .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.successMessage!!,
                                color = AccentGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Error Feedback Card
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentRed.copy(alpha = 0.12f))
                                .border(1.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = AccentRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Action Button
                    val buttonText = when {
                        uiState.authMode == AuthMode.LOGIN -> "ورود"
                        uiState.authMode == AuthMode.REGISTER && uiState.isRegisterOtpSent -> "تایید و تکمیل ثبت‌نام"
                        uiState.authMode == AuthMode.REGISTER -> "ثبت‌نام"
                        uiState.authMode == AuthMode.FORGOT_PASSWORD -> if (uiState.isResetCodeSent) "بازیابی رمز" else "بازیابی رمز"
                        else -> "ورود"
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.submit()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HeroCardDark),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = buttonText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (uiState.authMode == AuthMode.FORGOT_PASSWORD) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "بازگشت به صفحه ورود",
                            color = PrimaryIndigo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { viewModel.setAuthMode(AuthMode.LOGIN) }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
