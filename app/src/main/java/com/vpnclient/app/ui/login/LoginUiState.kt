package com.vpnclient.app.ui.login

data class LoginUiState(
    val isRegisterMode: Boolean = false,
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)
