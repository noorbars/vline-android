package com.vpnclient.app.ui.theme

import android.app.Activity
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val AppColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = AccentGreen,
    background = BackgroundLight,
    surface = SurfaceCardLight,
    onPrimary = TextLightPrimary,
    onSecondary = TextLightPrimary,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary
)

@Composable
fun VpnClientTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = BackgroundLight.toArgb()
                it.navigationBarColor = BackgroundLight.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography
    ) {
        // Enforce RTL Persian layout direction and Vazirmatn font across the entire app
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalTextStyle provides TextStyle(
                fontFamily = VazirmatnFontFamily,
                color = TextDarkPrimary
            )
        ) {
            content()
        }
    }
}
