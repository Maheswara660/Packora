package com.maheswara660.packora.manager

import android.content.Context

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED,
    THEME_ORIGINAL, THEME_CYBER_LIME, THEME_RUBY_BLAZE, THEME_OCEAN_TEAL,
    THEME_FROST_WHITE, THEME_NEON_INDIGO, THEME_DEEP_SAPPHIRE, THEME_ELECTRIC_AZURE,
    THEME_EMERALD_GREEN, THEME_ROYAL_VIOLET, THEME_AMBER_SUNSET, THEME_STEALTH_ONYX
}

enum class AppColorAccent {
    SYSTEM,
    // 12 App Icon matched color accents
    ORIGINAL, CYBER_LIME, RUBY_BLAZE, OCEAN_TEAL,
    FROST_WHITE, NEON_INDIGO, DEEP_SAPPHIRE, ELECTRIC_AZURE,
    EMERALD_GREEN, ROYAL_VIOLET, AMBER_SUNSET, STEALTH_ONYX,
    // Classic color accents
    EMERALD, OCEAN, PURPLE, AMBER, CRIMSON, ROSE, CYAN, ORANGE, INDIGO, TEAL, LIME, CORAL,
    NEON_GREEN, ELECTRIC_BLUE, DEEP_VIOLET, MAGENTA, GOLD, MINT, PEACH, RUBY, SAPPHIRE
}

enum class UpdateInstallMode(val title: String, val subtitle: String) {
    MANUAL(
        title = "Manual",
        subtitle = "Never prompts installer automatically; build updates and tap Install on each card"
    ),
    AUTO_PROMPT(
        title = "Auto-Prompt",
        subtitle = "Automatically launches the package installer dialog once an update is compiled"
    )
}

class PackoraPreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("packora_user_settings", Context.MODE_PRIVATE)

    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString("app_theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            return try { AppThemeMode.valueOf(name) } catch (e: Exception) { AppThemeMode.SYSTEM }
        }
        set(value) {
            prefs.edit().putString("app_theme_mode", value.name).apply()
        }

    var colorAccent: AppColorAccent
        get() {
            val name = prefs.getString("app_color_accent", AppColorAccent.SYSTEM.name) ?: AppColorAccent.SYSTEM.name
            return try { AppColorAccent.valueOf(name) } catch (e: Exception) { AppColorAccent.SYSTEM }
        }
        set(value) {
            prefs.edit().putString("app_color_accent", value.name).apply()
        }

    var updateInstallMode: UpdateInstallMode
        get() {
            val name = prefs.getString("update_install_mode", UpdateInstallMode.AUTO_PROMPT.name) ?: UpdateInstallMode.AUTO_PROMPT.name
            return when (name) {
                "COMPLETELY_MANUAL", "MANUAL" -> UpdateInstallMode.MANUAL
                else -> UpdateInstallMode.AUTO_PROMPT
            }
        }
        set(value) {
            prefs.edit().putString("update_install_mode", value.name).apply()
        }

    var autoDeleteApkAfterInstall: Boolean
        get() = prefs.getBoolean("auto_delete_apk_after_install", true)
        set(value) = prefs.edit().putBoolean("auto_delete_apk_after_install", value).apply()

    var activeAppIcon: String
        get() = prefs.getString("active_app_icon", "original") ?: "original"
        set(value) = prefs.edit().putString("active_app_icon", value).apply()

    var useCustomStorageFolder: Boolean
        get() = prefs.getBoolean("use_custom_storage_folder", false)
        set(value) = prefs.edit().putBoolean("use_custom_storage_folder", value).apply()

    var customStorageFolder: String?
        get() = prefs.getString("custom_storage_folder", null)
        set(value) = prefs.edit().putString("custom_storage_folder", value).apply()

    var lastSeenChangelogVersion: String?
        get() = prefs.getString("last_seen_changelog_version", null)
        set(value) = prefs.edit().putString("last_seen_changelog_version", value).apply()
}
