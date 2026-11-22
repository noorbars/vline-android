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
    val isSubmittingRenewal: Boolean = false,
    val renewalSubmittedSuccess: Boolean = false,
    val renewalPlanName: String? = null,
    val submittedOrderNumber: String? = null,
    val renewalErrorMessage: String? = null,
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
