package com.maheswara660.packora.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.maheswara660.packora.manager.AppColorAccent
import com.maheswara660.packora.manager.AppThemeMode

private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()

private val AmoledColorScheme = darkColorScheme(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1A1A1A),
    surfaceContainerHighest = Color(0xFF242424),
    surfaceContainerLow = Color(0xFF080808),
    surfaceContainerLowest = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

@Composable
fun PackoraTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    colorAccent: AppColorAccent = AppColorAccent.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
    }

    val context = LocalContext.current

    var baseScheme = when {
        themeMode == AppThemeMode.AMOLED -> AmoledColorScheme
        colorAccent == AppColorAccent.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    if (colorAccent != AppColorAccent.SYSTEM) {
        val accentColor = when (colorAccent) {
            AppColorAccent.EMERALD -> Color(0xFF00A86B)
            AppColorAccent.OCEAN -> Color(0xFF0077BE)
            AppColorAccent.PURPLE -> Color(0xFF673AB7)
            AppColorAccent.AMBER -> Color(0xFFFF8F00)
            else -> baseScheme.primary
        }
        baseScheme = baseScheme.copy(
            primary = accentColor,
            secondary = accentColor,
            primaryContainer = accentColor.copy(alpha = 0.2f),
            onPrimaryContainer = if (isDark) Color.White else accentColor
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = baseScheme,
        typography = Typography,
        content = content
    )
}
