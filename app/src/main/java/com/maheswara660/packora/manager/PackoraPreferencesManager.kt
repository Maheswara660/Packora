package com.maheswara660.packora.manager

import android.content.Context

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

enum class AppColorAccent {
    SYSTEM, EMERALD, OCEAN, PURPLE, AMBER, CRIMSON, ROSE, CYAN, ORANGE, INDIGO, TEAL, LIME, CORAL,
    NEON_GREEN, ELECTRIC_BLUE, DEEP_VIOLET, MAGENTA, GOLD, MINT, PEACH, RUBY, SAPPHIRE
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

    var useCustomStorageFolder: Boolean
        get() = prefs.getBoolean("use_custom_storage_folder", false)
        set(value) = prefs.edit().putBoolean("use_custom_storage_folder", value).apply()

    var customStorageFolder: String?
        get() = prefs.getString("custom_storage_folder", null)
        set(value) = prefs.edit().putString("custom_storage_folder", value).apply()
}
