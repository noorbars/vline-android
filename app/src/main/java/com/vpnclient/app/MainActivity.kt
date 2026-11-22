package com.vpnclient.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.vpnclient.app.data.repository.AuthRepository
import com.vpnclient.app.data.repository.SubscriptionRepository
import com.vpnclient.app.ui.dashboard.DashboardScreen
import com.vpnclient.app.ui.dashboard.DashboardViewModel
import com.vpnclient.app.ui.login.LoginScreen
import com.vpnclient.app.ui.login.LoginViewModel
import com.vpnclient.app.ui.theme.BackgroundDark
import com.vpnclient.app.ui.theme.VpnClientTheme

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var subscriptionRepository: SubscriptionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val app = application as VpnApplication
        authRepository = AuthRepository(app.preferenceManager)
        subscriptionRepository = SubscriptionRepository(app.preferenceManager, authRepository)

        setContent {
            VpnClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    VpnAppContent(
                        activity = this,
                        authRepository = authRepository,
                        subscriptionRepository = subscriptionRepository
                    )
                }
            }
        }
    }
}

@Composable
fun VpnAppContent(
    activity: Activity,
    authRepository: AuthRepository,
    subscriptionRepository: SubscriptionRepository
) {
    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }

    // Android 13+ Notification permission requester
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val dashboardViewModel = remember {
        val app = activity.application as VpnApplication
        DashboardViewModel(authRepository, subscriptionRepository, app.preferenceManager)
    }

    // VpnService.prepare launcher for Android system VPN dialog
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dashboardViewModel.startVpnWithPreparedPermission(activity)
        }
    }

    if (!isLoggedIn) {
        val loginViewModel = remember {
            LoginViewModel(authRepository, subscriptionRepository)
        }
        LoginScreen(
            viewModel = loginViewModel,
            onLoginSuccess = {
                isLoggedIn = true
                dashboardViewModel.refreshData()
            }
        )
    } else {
        DashboardScreen(
            viewModel = dashboardViewModel,
            onPermissionRequired = {
                val prepareIntent = VpnService.prepare(activity)
                if (prepareIntent != null) {
                    vpnPermissionLauncher.launch(prepareIntent)
                }
            },
            onLoggedOut = {
                isLoggedIn = false
            }
        )
    }
}
