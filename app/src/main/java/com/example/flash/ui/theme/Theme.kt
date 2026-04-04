package com.example.flash.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TechBlue,
    secondary = TechPurple,
    tertiary = TechAccent,
    background = TechDark,
    surface = TechSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    secondary = TechPurple,
    tertiary = TechAccent,
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
)

@Composable
fun FlashTheme(
    themeMode: ThemeMode = ThemeMode.DEFAULT,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.DEFAULT -> DarkColorScheme
        ThemeMode.SYSTEM -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            val isLightAppearance = colorScheme.background.toArgb().let { 
                // Simple luminance check or explicit mode check
                themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !darkTheme)
            }
            insetsController.isAppearanceLightStatusBars = isLightAppearance
            insetsController.isAppearanceLightNavigationBars = isLightAppearance
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            Surface(color = colorScheme.background) {
                content()
            }
        }
    )
}
