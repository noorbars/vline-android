package com.vpnclient.app.ui.dashboard

import com.vpnclient.app.data.api.models.UserProfile
import com.vpnclient.app.data.model.AppUpdateInfo
import com.vpnclient.app.data.model.LocationManager
import com.vpnclient.app.data.model.ServerLocation
import com.vpnclient.app.vpn.core.RouteCategory
import com.vpnclient.app.vpn.service.VpnState
import java.io.File

data class DashboardUiState(
    val username: String = "",
    val vpnState: VpnState = VpnState.Disconnected,
    val userProfile: UserProfile? = null,
    val isRefreshing: Boolean = false,
    val infoMessage: String? = null,
    val isIranBypassEnabled: Boolean = true,
    val isRenewalSheetVisible: Boolean = false,
    val isSupportSheetVisible: Boolean = false,
    val isLoadingRenewInfo: Boolean = false,
    val renewInfoData: com.vpnclient.app.data.api.models.RenewInfoData? = null,
    val renewInfoError: String? = null,
    val isSubmittingRenewal: Boolean = false,
    val renewalSubmittedSuccess: Boolean = false,
    val isWalletRenewSuccess: Boolean = false,
    val renewalPlanName: String? = null,
    val submittedOrderNumber: String? = null,
    val submittedOrderId: Long? = null,
    val renewalErrorMessage: String? = null,
    val activeOrderStatus: com.vpnclient.app.data.api.models.OrderStatusData? = null,
    val isCheckingOrderStatus: Boolean = false,
    val selectedRoute: RouteCategory = RouteCategory.SMART,
    val isRouteSheetVisible: Boolean = false,
    val selectedLocation: ServerLocation = LocationManager.availableLocations.first(),
    val isLocationSheetVisible: Boolean = false,
    val isUpdateSheetVisible: Boolean = false,
    val isCheckingUpdate: Boolean = false,
    val appUpdateInfo: AppUpdateInfo? = null,
    val updateErrorMessage: String? = null,
    val downloadProgress: Int = -1,
    val downloadedApkFile: File? = null
)
