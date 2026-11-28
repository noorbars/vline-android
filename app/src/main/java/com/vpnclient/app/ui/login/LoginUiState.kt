package com.vpnclient.app.ui.login

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

data class LoginUiState(
    val authMode: AuthMode = AuthMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val registerOtpCode: String = "",
    val isRegisterOtpSent: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val isResendingOtp: Boolean = false,
    val resetCode: String = "",
    val isResetCodeSent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginSuccessful: Boolean = false
) {
    val isRegisterMode: Boolean get() = authMode == AuthMode.REGISTER
    val isForgotMode: Boolean get() = authMode == AuthMode.FORGOT_PASSWORD
}
