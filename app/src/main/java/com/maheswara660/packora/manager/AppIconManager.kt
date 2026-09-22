package com.maheswara660.packora.manager

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import com.maheswara660.packora.R

data class AppIconItem(
    val id: String,
    val displayName: String,
    val aliasName: String,
    @DrawableRes val previewRes: Int
)

object AppIconManager {
    val ICONS = listOf(
        AppIconItem(
            id = "original",
            displayName = "Original (Classic Blue)",
            aliasName = "com.maheswara660.packora.MainActivityAliasOriginal",
            previewRes = R.drawable.icon_preview_original
        ),
        AppIconItem(
            id = "cyber_lime",
            displayName = "Cyber Lime",
            aliasName = "com.maheswara660.packora.MainActivityAliasCyberLime",
            previewRes = R.drawable.icon_preview_cyber_lime
        ),
        AppIconItem(
            id = "ruby_blaze",
            displayName = "Ruby Blaze",
            aliasName = "com.maheswara660.packora.MainActivityAliasRubyBlaze",
            previewRes = R.drawable.icon_preview_ruby_blaze
        ),
        AppIconItem(
            id = "ocean_teal",
            displayName = "Ocean Teal",
            aliasName = "com.maheswara660.packora.MainActivityAliasOceanTeal",
            previewRes = R.drawable.icon_preview_ocean_teal
        ),
        AppIconItem(
            id = "frost_white",
            displayName = "Frost White",
            aliasName = "com.maheswara660.packora.MainActivityAliasFrostWhite",
            previewRes = R.drawable.icon_preview_frost_white
        ),
        AppIconItem(
            id = "neon_indigo",
            displayName = "Neon Indigo",
            aliasName = "com.maheswara660.packora.MainActivityAliasNeonIndigo",
            previewRes = R.drawable.icon_preview_neon_indigo
        ),
        AppIconItem(
            id = "deep_sapphire",
            displayName = "Deep Sapphire",
            aliasName = "com.maheswara660.packora.MainActivityAliasDeepSapphire",
            previewRes = R.drawable.icon_preview_deep_sapphire
        ),
        AppIconItem(
            id = "electric_azure",
            displayName = "Electric Azure",
            aliasName = "com.maheswara660.packora.MainActivityAliasElectricAzure",
            previewRes = R.drawable.icon_preview_electric_azure
        ),
        AppIconItem(
            id = "emerald_green",
            displayName = "Emerald Green",
            aliasName = "com.maheswara660.packora.MainActivityAliasEmeraldGreen",
            previewRes = R.drawable.icon_preview_emerald_green
        ),
        AppIconItem(
            id = "royal_violet",
            displayName = "Royal Violet",
            aliasName = "com.maheswara660.packora.MainActivityAliasRoyalViolet",
            previewRes = R.drawable.icon_preview_royal_violet
        ),
        AppIconItem(
            id = "amber_sunset",
            displayName = "Amber Sunset",
            aliasName = "com.maheswara660.packora.MainActivityAliasAmberSunset",
            previewRes = R.drawable.icon_preview_amber_sunset
        ),
        AppIconItem(
            id = "stealth_onyx",
            displayName = "Stealth Onyx",
            aliasName = "com.maheswara660.packora.MainActivityAliasStealthOnyx",
            previewRes = R.drawable.icon_preview_stealth_onyx
        )
    )

    fun getCurrentIcon(context: Context): AppIconItem {
        val prefs = PackoraPreferencesManager(context)
        val activeId = prefs.activeAppIcon
        return ICONS.find { it.id == activeId } ?: ICONS.first()
    }

    fun setAppIcon(context: Context, iconItem: AppIconItem) {
        val pm = context.packageManager
        val packageName = context.packageName

        for (icon in ICONS) {
            val componentName = ComponentName(packageName, icon.aliasName)
            val state = if (icon.id == iconItem.id) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    componentName,
                    state,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {}
        }

        PackoraPreferencesManager(context).activeAppIcon = iconItem.id
    }
}
