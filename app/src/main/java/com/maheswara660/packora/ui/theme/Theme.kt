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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF0E1414),
    surface = Color(0xFF0E1414),
    surfaceContainer = Color(0xFF161D1D),
    onBackground = Color(0xFFDEE3E3),
    onSurface = Color(0xFFDEE3E3)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF4FBFA),
    surface = Color(0xFFF4FBFA),
    surfaceContainer = Color(0xFFE9EFEE),
    onBackground = Color(0xFF161D1D),
    onSurface = Color(0xFF161D1D)
)

@Composable
fun PackoraTheme(
    themeConfig: String = "SYSTEM",
    accentColorIndex: Int = 0, // Default to Teal (index 0) matching Chronora/Calcora defaults
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeConfig) {
        "LIGHT" -> false
        "DARK" -> true
        "AMOLED" -> true
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val dynamicColor = accentColorIndex == -1

    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    if (accentColorIndex >= 0) {
        val accent = CustomAccents.getOrNull(accentColorIndex)
        if (accent != null) {
            colorScheme = colorScheme.copy(
                primary = accent.primary,
                secondary = accent.secondary,
                outline = accent.primary.copy(alpha = 0.5f)
            )
        }
    }

    if (darkTheme && themeConfig == "AMOLED") {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceBright = Color(0xFF1A1A1A),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color(0xFF121212),
            surfaceContainerHighest = Color(0xFF1A1A1A),
            surfaceVariant = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color.White,
            outline = Color.White.copy(alpha = 0.2f),
            outlineVariant = Color.White.copy(alpha = 0.1f)
        )
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}