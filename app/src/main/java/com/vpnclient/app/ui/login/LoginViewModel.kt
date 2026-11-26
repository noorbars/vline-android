package com.vpnclient.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpnclient.app.data.repository.AuthRepository
import com.vpnclient.app.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun setAuthMode(mode: AuthMode) {
        _uiState.update {
            it.copy(
                authMode = mode,
                errorMessage = null,
                successMessage = null,
                confirmPassword = "",
                resetCode = "",
                isResetCodeSent = false
            )
        }
    }

    fun setRegisterMode(isRegister: Boolean) {
        setAuthMode(if (isRegister) AuthMode.REGISTER else AuthMode.LOGIN)
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null, successMessage = null) }
    }

    fun onResetCodeChanged(resetCode: String) {
        _uiState.update { it.copy(resetCode = resetCode, errorMessage = null, successMessage = null) }
    }

    fun submit() {
        when (_uiState.value.authMode) {
            AuthMode.LOGIN -> login()
            AuthMode.REGISTER -> register()
            AuthMode.FORGOT_PASSWORD -> {
                if (_uiState.value.isResetCodeSent) {
                    resetPassword()
                } else {
                    forgotPassword()
                }
            }
        }
    }

    fun register() {
        val email = _uiState.value.username.trim()
        val password = _uiState.value.password.trim()
        val confirm = _uiState.value.confirmPassword.trim()

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "لطفاً یک آدرس ایمیل معتبر وارد کنید") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "رمز عبور باید حداقل ۶ کاراکتر باشد") }
            return
        }
        if (password != confirm) {
            _uiState.update { it.copy(errorMessage = "رمز عبور و تکرار آن یکسان نیستند") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.register(email, password)
            result.fold(
                onSuccess = { response ->
                    val subUrl = response.data?.subUrl
                    if (!subUrl.isNullOrBlank()) {
                        subscriptionRepository.fetchFromSubUrl(subUrl)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "خطا در ثبت‌نام. لطفاً دوباره تلاش کنید."
                        )
                    }
                }
            )
        }
    }

    fun login() {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password.trim()

        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "لطفاً نام کاربری و رمز عبور را وارد کنید") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(username, password)
            result.fold(
                onSuccess = { response ->
                    val subUrl = response.data?.subUrl
                    if (!subUrl.isNullOrBlank()) {
                        subscriptionRepository.fetchFromSubUrl(subUrl)
                    } else {
                        subscriptionRepository.refreshSubscription()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "نام کاربری یا رمز عبور نامعتبر است"
                        )
                    }
                }
            )
        }
    }

    fun forgotPassword() {
        val email = _uiState.value.username.trim()
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "لطفاً آدرس ایمیل خود را به درستی وارد کنید") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            result.fold(
                onSuccess = { res ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isResetCodeSent = true,
                            successMessage = res.message ?: "کد تایید ۶ رقمی به ایمیل شما ارسال شد.",
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "خطا در ارسال کد بازیابی"
                        )
                    }
                }
            )
        }
    }

    fun resetPassword() {
        val email = _uiState.value.username.trim()
        val code = _uiState.value.resetCode.trim()
        val newPass = _uiState.value.password.trim()

        if (code.isBlank() || code.length < 4) {
            _uiState.update { it.copy(errorMessage = "لطفاً کد تایید دریافتی را وارد کنید") }
            return
        }
        if (newPass.length < 6) {
            _uiState.update { it.copy(errorMessage = "رمز عبور جدید باید حداقل ۶ کاراکتر باشد") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.resetPassword(email, code, newPass)
            result.fold(
                onSuccess = { res ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authMode = AuthMode.LOGIN,
                            isResetCodeSent = false,
                            resetCode = "",
                            successMessage = res.message ?: "رمز عبور با موفقیت تغییر یافت. اکنون وارد شوید.",
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "کد تایید نامعتبر یا منقضی است"
                        )
                    }
                }
            )
        }
    }
}
