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
    onSurfaceVariant = Color.DarkGray
)

private val HackerColorScheme = darkColorScheme(
    primary = RadiumBlue,
    secondary = RadiumPink,
    tertiary = RadiumYellow,
    background = Color.Black,
    surface = Color(0xFF0D0D0D),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = RadiumGreen,
    onSurface = RadiumGreen,
    onSurfaceVariant = RadiumGreen.copy(alpha = 0.7f)
)

private val RadiumColorScheme = darkColorScheme(
    primary = RadiumGreen,
    secondary = RadiumPink,
    tertiary = RadiumBlue,
    background = Color(0xFF050505),
    surface = Color(0xFF0F0F0F),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = RadiumGreen.copy(alpha = 0.5f)
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFFCEE09), // Cyberpunk Yellow
    secondary = Color(0xFF00F0FF), // Cyan
    tertiary = Color(0xFFFF003C), // Cyberpunk Red
    background = Color(0xFF020408),
    surface = Color(0xFF12141D),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFFCEE09),
    onSurface = Color.White
)

private val NeonBlueColorScheme = darkColorScheme(
    primary = Color(0xFF00D1FF),
    secondary = Color(0xFF007A99),
    tertiary = Color(0xFFB3E5FC),
    background = Color(0xFF000B14),
    surface = Color(0xFF001A2B),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFF00D1FF),
    onSurface = Color.White
)

private val TerminalRedColorScheme = darkColorScheme(
    primary = Color(0xFFFF3B30),
    secondary = Color(0xFF4A0000),
    tertiary = Color(0xFFFF9500),
    background = Color(0xFF0A0000),
    surface = Color(0xFF1A0505),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFFF3B30),
    onSurface = Color(0xFFFF3B30)
)

@Composable
fun FlashTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> RadiumColorScheme
        ThemeMode.HACKER -> HackerColorScheme
        ThemeMode.CYBERPUNK -> CyberpunkColorScheme
        ThemeMode.NEON_BLUE -> NeonBlueColorScheme
        ThemeMode.TERMINAL_RED -> TerminalRedColorScheme
        ThemeMode.SYSTEM -> if (darkTheme) HackerColorScheme else LightColorScheme
    }

    val typography = when (themeMode) {
        ThemeMode.HACKER, ThemeMode.TERMINAL_RED -> HackerTypography
        else -> TechTypography
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            val isLightAppearance = themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !darkTheme)
            insetsController.isAppearanceLightStatusBars = isLightAppearance
            insetsController.isAppearanceLightNavigationBars = isLightAppearance
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = {
            Surface(color = colorScheme.background) {
                content()
            }
        }
    )
}
