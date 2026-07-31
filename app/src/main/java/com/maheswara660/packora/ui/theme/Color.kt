package com.maheswara660.packora.ui.theme

import androidx.compose.ui.graphics.Color

// Core Premium Colors (Midnight Blue / Amethyst Vibe)
val BackgroundDark = Color(0xFF090A10)
val BackgroundAmoled = Color(0xFF000000)
val BackgroundLight = Color(0xFFF2F4F8)

val SurfaceDark = Color(0xFF13141F)
val SurfaceLight = Color(0xFFFFFFFF)

val OnBackgroundDark = Color(0xFFE2E4EB)
val OnBackgroundLight = Color(0xFF1C1D24)
val OnSurfaceDark = Color(0xFFE2E4EB)
val OnSurfaceLight = Color(0xFF1C1D24)
val OnSurfaceVariantDark = Color(0xFFA1A3B0)
val OnSurfaceVariantLight = Color(0xFF6E7180)

val SurfaceContainerDark = Color(0xFF1A1C29)
val SurfaceContainerHighDark = Color(0xFF222436)
val SurfaceContainerHighestDark = Color(0xFF2B2E45)

val SurfaceContainerLight = Color(0xFFEBEFF5)
val SurfaceContainerHighLight = Color(0xFFDFE4ED)
val SurfaceContainerHighestLight = Color(0xFFD2D9E6)

// Base primary fallback
val PrimaryDark = Color(0xFF818CF8)
val OnPrimaryDark = Color(0xFFFFFFFF)
val PrimaryContainerDark = Color(0xFF4F46E5)
val OnPrimaryContainerDark = Color(0xFFFFFFFF)

val PrimaryLight = Color(0xFF4F46E5)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE0E7FF)
val OnPrimaryContainerLight = Color(0xFF3730A3)

val CustomAccents = listOf(
    Pair("Amethyst", Color(0xFF9333EA)),
    Pair("Sapphire", Color(0xFF3B82F6)),
    Pair("Emerald", Color(0xFF10B981)),
    Pair("Ruby", Color(0xFFEF4444)),
    Pair("Amber", Color(0xFFF59E0B)),
    Pair("Rose", Color(0xFFF43F5E)),
    Pair("Midnight", Color(0xFF4F46E5)),
    Pair("Neon", Color(0xFF39FF14))
)

// Gradients for Hero Area
val HeroGradientAmethyst = listOf(Color(0xFF9333EA).copy(alpha = 0.2f), Color.Transparent)
val HeroGradientSapphire = listOf(Color(0xFF3B82F6).copy(alpha = 0.2f), Color.Transparent)
