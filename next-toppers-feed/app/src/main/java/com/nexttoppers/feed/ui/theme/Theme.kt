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
    primary              = Md3Blue80,
    onPrimary            = Md3Blue20,
    primaryContainer     = Md3Blue30,
    onPrimaryContainer   = Md3Blue90,

    secondary            = Md3Green80,
    onSecondary          = Md3Green20,
    secondaryContainer   = Md3Green30,
    onSecondaryContainer = Md3Green90,

    tertiary             = PremiumGold,
    onTertiary           = Color(0xFF3B2200),
    tertiaryContainer    = Color(0xFF553300),
    onTertiaryContainer  = Color(0xFFFFDDAE),

    background           = BackgroundBlack,
    onBackground         = Neutral99,

    surface              = SurfaceDark,
    onSurface            = Neutral90,
    surfaceVariant       = SurfaceCard,
    onSurfaceVariant     = Neutral80,

    outline              = Neutral40,
    outlineVariant       = SurfaceElevated,

    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    inverseSurface       = Neutral90,
    inverseOnSurface     = Neutral20,
    inversePrimary       = Md3Blue40,

    scrim                = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary              = Md3Blue40,
    onPrimary            = Neutral100,
    primaryContainer     = Md3Blue90,
    onPrimaryContainer   = Md3Blue10,

    secondary            = Md3Green40,
    onSecondary          = Neutral100,
    secondaryContainer   = Md3Green90,
    onSecondaryContainer = Md3Green10,

    tertiary             = Color(0xFF7B5800),
    onTertiary           = Neutral100,
    tertiaryContainer    = Color(0xFFFFDDAE),
    onTertiaryContainer  = Color(0xFF261A00),

    background           = LightBackground,
    onBackground         = LightTextPrimary,

    surface              = LightSurface,
    onSurface            = LightTextPrimary,
    surfaceVariant       = LightCard,
    onSurfaceVariant     = LightTextSecond,

    outline              = LightBorder,
    outlineVariant       = LightElevated,

    error                = ErrorRed,
    onError              = Neutral100,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),

    inverseSurface       = Neutral10,
    inverseOnSurface     = Neutral95,
    inversePrimary       = Md3Blue80,

    scrim                = Color(0xFF000000)
)

@Composable
fun NextToppersFeedTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "light"  -> false
        "dark"   -> true
        else     -> systemDark
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val statusBarColor   = colorScheme.background
    val navBarColor      = if (useDark) SurfaceDark else LightSurface

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !useDark
                isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
