package com.maheswara660.packora.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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

private fun createCustomIconColorScheme(
    bg: Color,
    surf: Color,
    container: Color,
    containerHigh: Color
): ColorScheme = darkColorScheme(
    background = bg,
    surface = surf,
    surfaceContainer = container,
    surfaceContainerHigh = containerHigh,
    surfaceContainerHighest = containerHigh.copy(alpha = 0.9f),
    surfaceContainerLow = bg,
    surfaceContainerLowest = bg,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF8FAFC),
    primary = Color(0xFF1676DF),
    secondary = Color(0xFF1676DF),
    primaryContainer = Color(0xFF1676DF).copy(alpha = 0.22f),
    onPrimaryContainer = Color.White
)

val CustomIconColorSchemes = mapOf(
    AppThemeMode.THEME_ORIGINAL to createCustomIconColorScheme(
        bg = Color(0xFF030A1C), surf = Color(0xFF071228), container = Color(0xFF0C1B38),
        containerHigh = Color(0xFF13254A)
    ),
    AppThemeMode.THEME_CYBER_LIME to createCustomIconColorScheme(
        bg = Color(0xFF060E02), surf = Color(0xFF0C1A05), container = Color(0xFF152A0B),
        containerHigh = Color(0xFF1E3A11)
    ),
    AppThemeMode.THEME_RUBY_BLAZE to createCustomIconColorScheme(
        bg = Color(0xFF120303), surf = Color(0xFF1E0707), container = Color(0xFF2D0D0D),
        containerHigh = Color(0xFF3F1414)
    ),
    AppThemeMode.THEME_OCEAN_TEAL to createCustomIconColorScheme(
        bg = Color(0xFF020E17), surf = Color(0xFF061826), container = Color(0xFF0C263B),
        containerHigh = Color(0xFF12344F)
    ),
    AppThemeMode.THEME_FROST_WHITE to createCustomIconColorScheme(
        bg = Color(0xFF0B1118), surf = Color(0xFF131D27), container = Color(0xFF1C2A38),
        containerHigh = Color(0xFF27384A)
    ),
    AppThemeMode.THEME_NEON_INDIGO to createCustomIconColorScheme(
        bg = Color(0xFF080417), surf = Color(0xFF110A2E), container = Color(0xFF1C1245),
        containerHigh = Color(0xFF2A1B63)
    ),
    AppThemeMode.THEME_DEEP_SAPPHIRE to createCustomIconColorScheme(
        bg = Color(0xFF030A1A), surf = Color(0xFF061533), container = Color(0xFF0C214D),
        containerHigh = Color(0xFF132E69)
    ),
    AppThemeMode.THEME_ELECTRIC_AZURE to createCustomIconColorScheme(
        bg = Color(0xFF010E1A), surf = Color(0xFF04192E), container = Color(0xFF082747),
        containerHigh = Color(0xFF0E3863)
    ),
    AppThemeMode.THEME_EMERALD_GREEN to createCustomIconColorScheme(
        bg = Color(0xFF020F06), surf = Color(0xFF061C0D), container = Color(0xFF0B2B16),
        containerHigh = Color(0xFF123D20)
    ),
    AppThemeMode.THEME_ROYAL_VIOLET to createCustomIconColorScheme(
        bg = Color(0xFF0E0317), surf = Color(0xFF1A072A), container = Color(0xFF290C40),
        containerHigh = Color(0xFF3B135C)
    ),
    AppThemeMode.THEME_AMBER_SUNSET to createCustomIconColorScheme(
        bg = Color(0xFF140700), surf = Color(0xFF240E02), container = Color(0xFF381805),
        containerHigh = Color(0xFF4F2309)
    ),
    AppThemeMode.THEME_STEALTH_ONYX to createCustomIconColorScheme(
        bg = Color(0xFF0A0D10), surf = Color(0xFF12171C), container = Color(0xFF1C232B),
        containerHigh = Color(0xFF26303B)
    )
)

fun getAppColorAccentColor(accent: AppColorAccent): Color? {
    return when (accent) {
        // 12 App Icon Color Schemes
        AppColorAccent.ORIGINAL -> Color(0xFF1676DF)
        AppColorAccent.CYBER_LIME -> Color(0xFF8BC34A)
        AppColorAccent.RUBY_BLAZE -> Color(0xFFE53935)
        AppColorAccent.OCEAN_TEAL -> Color(0xFF00ACC1)
        AppColorAccent.FROST_WHITE -> Color(0xFF90CAF9)
        AppColorAccent.NEON_INDIGO -> Color(0xFF5C6BC0)
        AppColorAccent.DEEP_SAPPHIRE -> Color(0xFF1E88E5)
        AppColorAccent.ELECTRIC_AZURE -> Color(0xFF00B0FF)
        AppColorAccent.EMERALD_GREEN -> Color(0xFF2E7D32)
        AppColorAccent.ROYAL_VIOLET -> Color(0xFF8E24AA)
        AppColorAccent.AMBER_SUNSET -> Color(0xFFFB8C00)
        AppColorAccent.STEALTH_ONYX -> Color(0xFF546E7A)
        // Additional Curated Accents
        AppColorAccent.EMERALD -> Color(0xFF00C853)
        AppColorAccent.OCEAN -> Color(0xFF0288D1)
        AppColorAccent.PURPLE -> Color(0xFF7B1FA2)
        AppColorAccent.AMBER -> Color(0xFFFFB300)
        AppColorAccent.CRIMSON -> Color(0xFFD32F2F)
        AppColorAccent.ROSE -> Color(0xFFE91E63)
        AppColorAccent.CYAN -> Color(0xFF00BCD4)
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
        AppColorAccent.SYSTEM -> null
    }
}

fun getAppThemeModeColor(themeMode: AppThemeMode): Color? {
    return CustomIconColorSchemes[themeMode]?.primary
}

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
        else -> true // Dark, AMOLED, and all 12 custom icon themes are dark/immersive
    }

    val context = LocalContext.current

    var baseScheme = when {
        CustomIconColorSchemes.containsKey(themeMode) -> CustomIconColorSchemes[themeMode]!!
        themeMode == AppThemeMode.AMOLED -> AmoledColorScheme
        colorAccent == AppColorAccent.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val defaultAccentColor = if (colorAccent == AppColorAccent.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
    } else {
        Color(0xFF1676DF)
    }

    val customAccentColor = getAppColorAccentColor(colorAccent) ?: defaultAccentColor
    baseScheme = baseScheme.copy(
        primary = customAccentColor,
        secondary = customAccentColor,
        primaryContainer = customAccentColor.copy(alpha = 0.22f),
        onPrimaryContainer = if (isDark) Color.White else customAccentColor
    )

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
