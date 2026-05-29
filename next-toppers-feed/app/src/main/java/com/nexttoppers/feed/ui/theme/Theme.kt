package com.nexttoppers.feed.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary           = NeonGreen,
    onPrimary         = BackgroundBlack,
    primaryContainer  = SurfaceElevated,
    onPrimaryContainer= NeonGreen,

    secondary         = NeonCyan,
    onSecondary       = BackgroundBlack,
    secondaryContainer= SurfaceCard,
    onSecondaryContainer = NeonCyan,

    tertiary          = PremiumGold,
    onTertiary        = BackgroundBlack,

    background        = BackgroundBlack,
    onBackground      = TextPrimary,

    surface           = SurfaceDark,
    onSurface         = TextPrimary,
    surfaceVariant    = SurfaceCard,
    onSurfaceVariant  = TextSecondary,

    outline           = TextMuted,
    outlineVariant    = SurfaceElevated,

    error             = ErrorRed,
    onError           = TextPrimary,

    inverseSurface    = TextPrimary,
    inverseOnSurface  = BackgroundBlack,
    inversePrimary    = NeonGreenDim
)

@Composable
fun NextToppersFeedTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundBlack.toArgb()
            window.navigationBarColor = SurfaceDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        content     = content
    )
}
