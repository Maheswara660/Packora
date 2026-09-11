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
            AppColorAccent.CRIMSON -> Color(0xFFE53935)
            AppColorAccent.ROSE -> Color(0xFFEC407A)
            AppColorAccent.CYAN -> Color(0xFF00ACC1)
            AppColorAccent.ORANGE -> Color(0xFFFB8C00)
            AppColorAccent.INDIGO -> Color(0xFF3F51B5)
            AppColorAccent.TEAL -> Color(0xFF00BFA5)
            AppColorAccent.LIME -> Color(0xFFC0CA33)
            AppColorAccent.CORAL -> Color(0xFFFF7043)
            AppColorAccent.NEON_GREEN -> Color(0xFF39FF14)
            AppColorAccent.ELECTRIC_BLUE -> Color(0xFF00E5FF)
            AppColorAccent.DEEP_VIOLET -> Color(0xFF8E24AA)
            AppColorAccent.MAGENTA -> Color(0xFFD81B60)
            AppColorAccent.GOLD -> Color(0xFFFFD700)
            AppColorAccent.MINT -> Color(0xFFA8E6CF)
            AppColorAccent.PEACH -> Color(0xFFFF8B94)
            AppColorAccent.RUBY -> Color(0xFFC62828)
            AppColorAccent.SAPPHIRE -> Color(0xFF0D47A1)
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
