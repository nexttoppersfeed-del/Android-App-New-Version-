package com.nexttoppers.feed.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary              = AccentCyan,
    onPrimary            = BackgroundBlack,
    primaryContainer     = SurfaceElevated,
    onPrimaryContainer   = AccentCyan,

    secondary            = AccentEmerald,
    onSecondary          = BackgroundBlack,
    secondaryContainer   = SurfaceCard,
    onSecondaryContainer = AccentEmerald,

    tertiary             = PremiumGold,
    onTertiary           = BackgroundBlack,

    background           = BackgroundBlack,
    onBackground         = TextPrimary,

    surface              = SurfaceDark,
    onSurface            = TextPrimary,
    surfaceVariant       = SurfaceCard,
    onSurfaceVariant     = TextSecondary,

    outline              = TextMuted,
    outlineVariant       = SurfaceElevated,

    error                = ErrorRed,
    onError              = TextPrimary,

    inverseSurface       = TextPrimary,
    inverseOnSurface     = BackgroundBlack,
    inversePrimary       = NeonGreenDim
)

private val LightColorScheme = lightColorScheme(
    primary              = AccentCyan,
    onPrimary            = LightBackground,
    primaryContainer     = LightCard,
    onPrimaryContainer   = Color(0xFF0369A1),

    secondary            = AccentEmerald,
    onSecondary          = LightBackground,
    secondaryContainer   = LightCard,
    onSecondaryContainer = Color(0xFF047857),

    tertiary             = PremiumGold,
    onTertiary           = LightBackground,

    background           = LightBackground,
    onBackground         = LightTextPrimary,

    surface              = LightSurface,
    onSurface            = LightTextPrimary,
    surfaceVariant       = LightCard,
    onSurfaceVariant     = LightTextSecond,

    outline              = LightBorder,
    outlineVariant       = LightElevated,

    error                = ErrorRed,
    onError              = LightBackground,

    inverseSurface       = LightTextPrimary,
    inverseOnSurface     = LightBackground,
    inversePrimary       = AccentCyan
)

@Composable
fun NextToppersFeedTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "light"  -> false
        "system" -> systemDark
        else     -> true
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val statusBarColor   = if (useDark) BackgroundBlack else LightBackground
    val navBarColor      = if (useDark) SurfaceDark else LightSurface
    val lightBars        = !useDark

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = statusBarColor.toArgb()
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = lightBars
                isAppearanceLightNavigationBars = lightBars
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
