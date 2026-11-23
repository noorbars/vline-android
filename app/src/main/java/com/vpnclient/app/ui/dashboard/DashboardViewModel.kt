package com.vpnclient.app.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpnclient.app.data.local.PreferenceManager
import com.vpnclient.app.data.model.AppUpdateInfo
import com.vpnclient.app.data.model.LocationManager
import com.vpnclient.app.data.model.ServerLocation
import com.vpnclient.app.data.repository.AuthRepository
import com.vpnclient.app.data.repository.SubscriptionRepository
import com.vpnclient.app.data.updater.UpdateManager
import com.vpnclient.app.vpn.core.RouteCategory
import com.vpnclient.app.vpn.service.VpnState
import com.vpnclient.app.vpn.service.XrayVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            username = authRepository.getCachedUsername(),
            userProfile = subscriptionRepository.getCachedProfile(),
            isIranBypassEnabled = preferenceManager.isIranBypassEnabled,
            selectedRoute = RouteCategory.fromKey(preferenceManager.selectedRouteCategory),
            selectedLocation = LocationManager.getActiveLocation(preferenceManager.selectedLocationId)
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Collect real-time VPN service state
        viewModelScope.launch {
            XrayVpnService.vpnState.collect { state ->
                _uiState.update { it.copy(vpnState = state) }
            }
        }

        // Auto-refresh config and quota on startup
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, infoMessage = null) }
            val result = subscriptionRepository.refreshSubscription()
            val updatedProfile = subscriptionRepository.getCachedProfile()
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    userProfile = updatedProfile,
                    infoMessage = if (result.isSuccess) "پیکربندی با موفقیت بروزرسانی شد" else null
                )
            }
        }
    }

    fun toggleIranBypass(context: Context) {
        val current = _uiState.value.isIranBypassEnabled
        val updated = !current
        preferenceManager.isIranBypassEnabled = updated
        _uiState.update {
            it.copy(
                isIranBypassEnabled = updated,
                infoMessage = if (updated) "اینترنت نیم‌بها برای سایت‌های داخلی فعال شد" else "حالت ترافیک تمام بین‌الملل فعال شد"
            )
        }

        // If currently connected, reload config to apply routing rules immediately
        if (_uiState.value.vpnState is VpnState.Connected) {
            val config = subscriptionRepository.getCachedConfig()
            if (!config.isNullOrBlank()) {
                XrayVpnService.reloadConfig(context, config)
            }
        }
    }

    fun selectRouteCategory(context: Context, category: RouteCategory) {
        preferenceManager.selectedRouteCategory = category.key
        _uiState.update {
            it.copy(
                selectedRoute = category,
                isRouteSheetVisible = false,
                infoMessage = "مسیر فعال: ${category.title}"
            )
        }

        // If currently connected or connecting, reload tunnel with new route
        val currentState = _uiState.value.vpnState
        if (currentState is VpnState.Connected || currentState is VpnState.Connecting) {
            val config = subscriptionRepository.getCachedConfig()
            if (!config.isNullOrBlank()) {
                XrayVpnService.reloadRoute(context, config, category.key)
            }
        }
    }

    fun selectLocation(location: ServerLocation) {
        if (!location.isAvailable) return
        preferenceManager.selectedLocationId = location.id
        _uiState.update {
            it.copy(
                selectedLocation = location,
                isLocationSheetVisible = false,
                infoMessage = "سرور انتخاب شده: ${location.countryName}"
            )
        }
    }

    fun setRouteSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isRouteSheetVisible = visible) }
    }

    fun setLocationSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isLocationSheetVisible = visible) }
    }

    fun toggleConnection(context: Context, onPermissionRequired: () -> Unit) {
        when (_uiState.value.vpnState) {
            is VpnState.Disconnected, is VpnState.Error -> {
                val config = subscriptionRepository.getCachedConfig()
                if (config.isNullOrBlank()) {
                    _uiState.update { it.copy(infoMessage = "پیکربندی دریافت نشده است. لطفاً بروزرسانی کنید.") }
                    return
                }
                val vpnIntent = android.net.VpnService.prepare(context)
                if (vpnIntent != null) {
                    onPermissionRequired()
                } else {
                    XrayVpnService.start(context, config, _uiState.value.selectedRoute.key)
                }
            }
            is VpnState.Connected, is VpnState.Connecting -> {
                XrayVpnService.stop(context)
            }
        }
    }

    fun startVpnWithPreparedPermission(context: Context) {
        val config = subscriptionRepository.getCachedConfig() ?: return
        XrayVpnService.start(context, config, _uiState.value.selectedRoute.key)
    }

    fun retestMetrics(context: Context) {
        XrayVpnService.retestMetrics(context)
    }

    // In-App Update Management
    fun setUpdateSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isUpdateSheetVisible = visible) }
    }

    fun checkForUpdate(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdateSheetVisible = true,
                    isCheckingUpdate = true,
                    updateErrorMessage = null,
                    downloadProgress = -1,
                    downloadedApkFile = null
                )
            }

            val updateManager = UpdateManager(context)
            val currentVersion = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "1.1"
            } catch (e: Exception) {
                "1.1"
            }

            val result = updateManager.checkForUpdate(currentVersion)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCheckingUpdate = false,
                        appUpdateInfo = result.getOrNull()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCheckingUpdate = false,
                        updateErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "خطا در ارتباط با سرور بروزرسانی"
                    )
                }
            }
        }
    }

    fun startDownloadUpdate(context: Context, updateInfo: AppUpdateInfo) {
        val downloadUrl = updateInfo.downloadUrl ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(downloadProgress = 0, updateErrorMessage = null) }
            val updateManager = UpdateManager(context)
            val targetName = "VLineClient-v${updateInfo.latestVersion}.apk"

            val result = updateManager.downloadApk(
                downloadUrl = downloadUrl,
                targetFileName = targetName,
                onProgress = { percent, _, _ ->
                    _uiState.update { it.copy(downloadProgress = percent) }
                }
            )

            if (result.isSuccess) {
                val file = result.getOrNull()
                _uiState.update {
                    it.copy(
                        downloadProgress = 100,
                        downloadedApkFile = file
                    )
                }
                file?.let { updateManager.installApk(it) }
            } else {
                _uiState.update {
                    it.copy(
                        downloadProgress = -1,
                        updateErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "خطا در دانلود فایل بروزرسانی"
                    )
                }
            }
        }
    }

    fun installDownloadedUpdate(context: Context, file: File) {
        val updateManager = UpdateManager(context)
        updateManager.installApk(file)
    }

    fun setRenewalSheetVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                isRenewalSheetVisible = visible,
                isSubmittingRenewal = false,
                renewalSubmittedSuccess = false,
                isWalletRenewSuccess = false,
                renewalErrorMessage = null
            )
        }
        if (visible) {
            loadRenewInfo()
        }
    }

    fun loadRenewInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRenewInfo = true, renewInfoError = null) }
            val result = subscriptionRepository.getRenewInfo()
            if (result.isSuccess) {
                val data = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoadingRenewInfo = false,
                        renewInfoData = data,
                        renewInfoError = null,
                        submittedOrderId = data?.pendingOrder?.orderId,
                        submittedOrderNumber = data?.pendingOrder?.orderNumber
                    )
                }
                // If there's an existing pending order, check its latest status
                data?.pendingOrder?.let { pending ->
                    checkOrderStatus(orderId = pending.orderId, orderNumber = pending.orderNumber)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingRenewInfo = false,
                        renewInfoError = result.exceptionOrNull()?.localizedMessage ?: "خطا در دریافت اطلاعات تمدید"
                    )
                }
            }
        }
    }

    fun submitOrderRenew(
        planId: String,
        planTitle: String,
        paymentMethod: String, // "wallet" or "card"
        trackingCode: String? = null,
        receiptBase64: String? = null,
        note: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingRenewal = true,
                    renewalErrorMessage = null,
                    renewalSubmittedSuccess = false,
                    isWalletRenewSuccess = false
                )
            }

            val result = subscriptionRepository.submitOrderRenew(
                planId = planId,
                paymentMethod = paymentMethod,
                trackingCode = trackingCode,
                receiptBase64 = receiptBase64,
                note = note
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                val isWallet = paymentMethod == "wallet" && response?.isApproved == true
                _uiState.update {
                    it.copy(
                        isSubmittingRenewal = false,
                        renewalSubmittedSuccess = true,
                        isWalletRenewSuccess = isWallet,
                        renewalPlanName = planTitle,
                        submittedOrderId = response?.orderId,
                        submittedOrderNumber = response?.orderNumber ?: "VL-RENEW",
                        renewalErrorMessage = null
                    )
                }
                if (isWallet) {
                    // Instantly refresh subscription on dashboard
                    refreshData()
                    loadRenewInfo()
                } else {
                    // Check order status and start polling
                    response?.orderId?.let { oId ->
                        checkOrderStatus(orderId = oId, orderNumber = response.orderNumber)
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmittingRenewal = false,
                        renewalErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "خطا در ثبت سفارش تمدید"
                    )
                }
            }
        }
    }

    fun checkOrderStatus(orderId: Long? = null, orderNumber: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOrderStatus = true) }
            val result = subscriptionRepository.getOrderStatus(orderId = orderId, orderNumber = orderNumber)
            if (result.isSuccess) {
                val statusData = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isCheckingOrderStatus = false,
                        activeOrderStatus = statusData
                    )
                }
                if (statusData?.isApproved == true) {
                    // Order approved! Update dashboard and subscription
                    refreshData()
                }
            } else {
                _uiState.update { it.copy(isCheckingOrderStatus = false) }
            }
        }
    }

    fun setSupportSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isSupportSheetVisible = visible) }
    }

    fun submitRenewalRequest(
        plan: RenewalPlan,
        trackingCode: String,
        receiptBase64: String?,
        note: String
    ) {
        submitOrderRenew(
            planId = plan.id,
            planTitle = plan.title,
            paymentMethod = "card",
            trackingCode = trackingCode,
            receiptBase64 = receiptBase64,
            note = note
        )
    }

    fun logout(context: Context) {
        XrayVpnService.stop(context)
        authRepository.logout()
    }
}
