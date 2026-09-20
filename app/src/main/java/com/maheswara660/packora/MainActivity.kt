package com.maheswara660.packora

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.maheswara660.packora.manager.AppColorAccent
import com.maheswara660.packora.manager.AppThemeMode
import com.maheswara660.packora.manager.PackoraPreferencesManager
import com.maheswara660.packora.ui.AboutScreen
import com.maheswara660.packora.ui.BuildScreen
import com.maheswara660.packora.ui.ChangelogScreen
import com.maheswara660.packora.ui.HistoryScreen
import com.maheswara660.packora.ui.LatestChangelogBottomSheet
import com.maheswara660.packora.ui.MyAppsScreen
import com.maheswara660.packora.ui.SettingsScreen
import com.maheswara660.packora.ui.getLatestRelease
import com.maheswara660.packora.ui.theme.PackoraTheme
import java.io.File

enum class Screen {
    BUILD, MY_APPS, HISTORY, SETTINGS, ABOUT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefsManager = remember { PackoraPreferencesManager(context) }
            var appThemeMode by remember { mutableStateOf(prefsManager.themeMode) }
            var appColorAccent by remember { mutableStateOf(prefsManager.colorAccent) }

            PackoraTheme(themeMode = appThemeMode, colorAccent = appColorAccent) {
                MainAppNavigation(
                    appThemeMode = appThemeMode,
                    onThemeModeChange = {
                        appThemeMode = it
                        prefsManager.themeMode = it
                    },
                    appColorAccent = appColorAccent,
                    onColorAccentChange = {
                        appColorAccent = it
                        prefsManager.colorAccent = it
                    },
                    prefsManager = prefsManager
                )
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    appThemeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    appColorAccent: AppColorAccent,
    onColorAccentChange: (AppColorAccent) -> Unit,
    prefsManager: PackoraPreferencesManager
) {
    // Primary tab: BUILD | MY_APPS | HISTORY | SETTINGS
    var selectedTab by remember { mutableStateOf(Screen.BUILD) }
    // Secondary overlays (About & Changelog screens opened from Settings)
    var showAbout by remember { mutableStateOf(false) }
    var showChangelog by remember { mutableStateOf(false) }

    var url by remember { mutableStateOf("") }
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var versionCode by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("") }
    var isDesktopMode by remember { mutableStateOf(false) }
    var isForceDarkMode by remember { mutableStateOf(false) }
    var enableZoom by remember { mutableStateOf(false) }
    var selectedBrowserEngine by remember { mutableStateOf("INDIVIDUAL") }
    var allowCopying by remember { mutableStateOf(false) }
    var autoFetchedIconBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current

    // Back navigation handling
    BackHandler(enabled = showAbout) { showAbout = false }
    BackHandler(enabled = !showAbout && showChangelog) { showChangelog = false }
    BackHandler(enabled = !showAbout && !showChangelog && selectedTab != Screen.BUILD) { selectedTab = Screen.BUILD }

    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    if (showChangelog) {
        ChangelogScreen(onBack = { showChangelog = false })
        return
    }

    var showLatestChangelogSheet by remember { mutableStateOf(false) }
    val currentAppVersion = context.appVersion()

    LaunchedEffect(Unit) {
        val lastSeen = prefsManager.lastSeenChangelogVersion
        if (lastSeen != currentAppVersion) {
            showLatestChangelogSheet = true
        }
    }

    if (showLatestChangelogSheet) {
        LatestChangelogBottomSheet(
            release = getLatestRelease(),
            onDismiss = {
                prefsManager.lastSeenChangelogVersion = currentAppVersion
                showLatestChangelogSheet = false
            }
        )
    }

    data class TabItem(
        val screen: Screen,
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val tabs = listOf(
        TabItem(Screen.BUILD, "Build", Icons.Outlined.Build, Icons.Outlined.Build),
        TabItem(Screen.MY_APPS, "My Apps", Icons.Outlined.Inventory2, Icons.Outlined.Inventory2),
        TabItem(Screen.HISTORY, "History", Icons.Outlined.History, Icons.Outlined.History),
        TabItem(Screen.SETTINGS, "Settings", Icons.Outlined.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = 0.5.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { tab ->
                            val isSelected = selectedTab == tab.screen
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = ripple(bounded = false, radius = 28.dp)
                                    ) { selectedTab = tab.screen },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .width(56.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
) { innerPadding ->
    val handleReuseConfig: (com.maheswara660.packora.manager.HistoryItem) -> Unit = { item ->
        url = item.targetUrl
        appName = item.appName
        packageName = item.packageName
        versionCode = (item.versionCode + 1).toString()
        versionName = incrementVersionString(item.versionName)
        isDesktopMode = item.isDesktopMode
        isForceDarkMode = item.isForceDarkMode
        enableZoom = item.enableZoom
        selectedBrowserEngine = item.browserEngine
        allowCopying = item.allowCopying
        if (!item.iconPath.isNullOrBlank() && File(item.iconPath).exists()) {
            try {
                autoFetchedIconBitmap = android.graphics.BitmapFactory.decodeFile(item.iconPath)
            } catch (e: Exception) {}
        }
        selectedTab = Screen.BUILD
        Toast.makeText(context, "Loaded config for ${item.appName}", Toast.LENGTH_SHORT).show()
    }

    Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    Screen.BUILD -> BuildScreen(
                        url = url, onUrlChange = { url = it },
                        appName = appName, onAppNameChange = { appName = it },
                        packageName = packageName, onPackageNameChange = { packageName = it },
                        versionCode = versionCode, onVersionCodeChange = { versionCode = it },
                        versionName = versionName, onVersionNameChange = { versionName = it },
                        isDesktopMode = isDesktopMode, onDesktopModeChange = { isDesktopMode = it },
                        isForceDarkMode = isForceDarkMode, onForceDarkModeChange = { isForceDarkMode = it },
                        enableZoom = enableZoom, onEnableZoomChange = { enableZoom = it },
                        selectedBrowserEngine = selectedBrowserEngine, onBrowserEngineChange = { selectedBrowserEngine = it },
                        allowCopying = allowCopying, onAllowCopyingChange = { allowCopying = it },
                        autoFetchedIconBitmap = autoFetchedIconBitmap, onAutoFetchedIconBitmapChange = { autoFetchedIconBitmap = it },
                        onNavigateHistory = { selectedTab = Screen.HISTORY },
                        onNavigateSettings = { selectedTab = Screen.SETTINGS }
                    )
                    Screen.MY_APPS -> MyAppsScreen(onReuseConfig = handleReuseConfig)
                    Screen.HISTORY -> HistoryScreen(
                        onBack = { selectedTab = Screen.BUILD },
                        onReuseConfig = handleReuseConfig
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        onBack = { selectedTab = Screen.BUILD },
                        onNavigateAbout = { showAbout = true },
                        onNavigateChangelog = { showChangelog = true },
                        onThemeModeChange = onThemeModeChange,
                        onColorAccentChange = onColorAccentChange
                    )
                    Screen.ABOUT -> {} // Handled above as overlay
                }
            }
        }
    }
}

fun incrementVersionString(v: String): String {
    val parts = v.split(".").toMutableList()
    if (parts.isNotEmpty()) {
        val last = parts.last().toIntOrNull() ?: 0
        parts[parts.lastIndex] = (last + 1).toString()
        return parts.joinToString(".")
    }
    return "1.0.1"
}

fun Context.appVersion(): String {
    return try {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        pInfo.versionName ?: "3.2.3"
    } catch (e: Exception) {
        "3.2.3"
    }
}

@Suppress("DEPRECATION")
fun installApkFile(context: Context, apkPath: String) {
    try {
        val file = File(apkPath)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_ALLOW_REPLACE, true)
            putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Error launching installer: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
