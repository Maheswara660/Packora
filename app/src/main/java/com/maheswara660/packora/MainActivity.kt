package com.maheswara660.packora

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.manager.AppColorAccent
import com.maheswara660.packora.manager.AppThemeMode
import com.maheswara660.packora.manager.BuildHistoryManager
import com.maheswara660.packora.manager.HistoryItem
import com.maheswara660.packora.manager.PackoraPreferencesManager
import androidx.activity.compose.BackHandler
import com.maheswara660.packora.ui.HistoryScreen
import com.maheswara660.packora.ui.SettingsScreen
import com.maheswara660.packora.ui.AboutScreen
import com.maheswara660.packora.ui.theme.PackoraTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

import com.maheswara660.packora.ui.MyAppsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                    }
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
    onColorAccentChange: (AppColorAccent) -> Unit
) {
    // Primary tab: BUILD | MY_APPS | HISTORY | SETTINGS
    var selectedTab by remember { mutableStateOf(Screen.BUILD) }
    // Secondary overlay (About screen opened from Settings)
    var showAbout by remember { mutableStateOf(false) }

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

    // Back: About → Settings → hold (let system handle app exit from Build)
    BackHandler(enabled = showAbout) { showAbout = false }
    BackHandler(enabled = !showAbout && selectedTab != Screen.BUILD) { selectedTab = Screen.BUILD }

    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }

    // Tab items for the bottom bar
    data class TabItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector)
    val tabs = listOf(
        TabItem(Screen.BUILD, "Build", Icons.Outlined.Build, Icons.Outlined.Build),
        TabItem(Screen.MY_APPS, "My Apps", Icons.Outlined.Inventory2, Icons.Outlined.Inventory2),
        TabItem(Screen.HISTORY, "History", Icons.Outlined.History, Icons.Outlined.History),
        TabItem(Screen.SETTINGS, "Settings", Icons.Outlined.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.screen,
                        onClick = { selectedTab = tab.screen },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == tab.screen) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == tab.screen) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    Screen.BUILD -> PackoraDashboard(
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
                    Screen.MY_APPS -> MyAppsScreen()
                    Screen.HISTORY -> HistoryScreen(
                        onBack = { selectedTab = Screen.BUILD },
                        onReuseConfig = { item ->
                            url = item.targetUrl
                            appName = item.appName
                            packageName = item.packageName
                            versionCode = (item.versionCode + 1).toString()
                            versionName = incrementVersionString(item.versionName)
                            isDesktopMode = item.isDesktopMode
                            selectedBrowserEngine = item.browserEngine
                            allowCopying = item.allowCopying
                            if (!item.iconPath.isNullOrBlank() && java.io.File(item.iconPath).exists()) {
                                try { autoFetchedIconBitmap = android.graphics.BitmapFactory.decodeFile(item.iconPath) } catch (e: Exception) {}
                            }
                            Toast.makeText(context, "Loaded config for ${item.appName}", Toast.LENGTH_SHORT).show()
                            selectedTab = Screen.BUILD
                        }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        onBack = { selectedTab = Screen.BUILD },
                        onNavigateAbout = { showAbout = true },
                        onThemeModeChange = onThemeModeChange,
                        onColorAccentChange = onColorAccentChange
                    )
                    Screen.ABOUT -> {} // handled above as overlay
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PackoraDashboard(
    url: String,
    onUrlChange: (String) -> Unit,
    appName: String,
    onAppNameChange: (String) -> Unit,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    versionCode: String,
    onVersionCodeChange: (String) -> Unit,
    versionName: String,
    onVersionNameChange: (String) -> Unit,
    isDesktopMode: Boolean,
    onDesktopModeChange: (Boolean) -> Unit,
    isForceDarkMode: Boolean,
    onForceDarkModeChange: (Boolean) -> Unit,
    enableZoom: Boolean,
    onEnableZoomChange: (Boolean) -> Unit,
    selectedBrowserEngine: String,
    onBrowserEngineChange: (String) -> Unit,
    allowCopying: Boolean,
    onAllowCopyingChange: (Boolean) -> Unit,
    autoFetchedIconBitmap: Bitmap?,
    onAutoFetchedIconBitmapChange: (Bitmap?) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("packora_prefs", Context.MODE_PRIVATE)
    val historyManager = remember { BuildHistoryManager(context) }

    var useCustomDownloadFolder by remember { mutableStateOf(sharedPrefs.getBoolean("use_custom_download", false)) }
    var customDownloadFolder by remember { mutableStateOf(sharedPrefs.getString("custom_download_folder", "") ?: "") }
    var isHideWebFooter by remember { mutableStateOf(sharedPrefs.getBoolean("hide_web_footer", false)) }

    var iconUri by remember { mutableStateOf<Uri?>(null) }
    var iconName by remember { mutableStateOf<String?>(null) }
    var isFetchingIcon by remember { mutableStateOf(false) }
    var iconSourceIndex by remember { mutableStateOf(0) }

    var useCustomKeystore by remember { mutableStateOf(false) }
    var keystorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }
    var commonName by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var organizationalUnit by remember { mutableStateOf("") }
    var validityYears by remember { mutableStateOf("25") }

    var isPackageIdentityExpanded by remember { mutableStateOf(false) }
    var isStorageFolderExpanded by remember { mutableStateOf(false) }
    var isKeystoreExpanded by remember { mutableStateOf(false) }

    var isBuilding by remember { mutableStateOf(false) }
    var targetProgressPercent by remember { mutableIntStateOf(0) }
    var animatedProgressPercent by remember { mutableIntStateOf(0) }

    LaunchedEffect(isBuilding, targetProgressPercent) {
        if (isBuilding) {
            while (animatedProgressPercent < targetProgressPercent && animatedProgressPercent <= 100) {
                animatedProgressPercent++
                delay(12)
            }
        } else {
            animatedProgressPercent = 0
            targetProgressPercent = 0
        }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastBuiltApkPath by remember { mutableStateOf<String?>(null) }

    var showClearConfirmSheet by remember { mutableStateOf(false) }

    var showZoomDialog by remember { mutableStateOf(false) }
    var showMultiIconSheet by remember { mutableStateOf(false) }
    var fetchedIconsList by remember { mutableStateOf<List<FetchedIconItem>>(emptyList()) }

    LaunchedEffect(url) {
        onAutoFetchedIconBitmapChange(null)
        iconUri = null
        iconName = null
        iconSourceIndex = 0
        if (url.isNotBlank()) {
            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            isFetchingIcon = true
            delay(500)
            try {
                val (fetched, _) = fetchPremiumIconWithSource(fetchUrl, 0)
                onAutoFetchedIconBitmapChange(fetched)
            } catch (e: Exception) {
                onAutoFetchedIconBitmapChange(null)
            } finally {
                isFetchingIcon = false
            }
        } else {
            isFetchingIcon = false
        }
    }

    val iconPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            iconUri = uri
            iconName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            } ?: "custom_icon.png"
        }
    }

    val folderPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            val path = uri.path ?: ""
            val folder = if (path.contains(":")) path.substringAfter(":") else "Downloads"
            customDownloadFolder = folder
            sharedPrefs.edit().putString("custom_download_folder", folder).apply()
        }
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val navBarBottomPx = WindowInsets.navigationBars.getBottom(density)
    val navBarBottomDp = with(density) { navBarBottomPx.toDp() }
    val isGestureNav = navBarBottomDp < 24.dp
    val dynamicBottomPadding = if (isGestureNav) 4.dp else navBarBottomDp + 8.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Packora", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                },
                actions = {
                    IconButton(onClick = { showClearConfirmSheet = true }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear Details", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onNavigateHistory) {
                        Icon(Icons.Outlined.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Outlined.Dashboard, contentDescription = "Dashboard / Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = dynamicBottomPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { iconPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val displayBitmap = remember(iconUri, autoFetchedIconBitmap) {
                            if (iconUri != null) {
                                try {
                                    context.contentResolver.openInputStream(iconUri!!).use { android.graphics.BitmapFactory.decodeStream(it) }
                                } catch (e: Exception) { null }
                            } else autoFetchedIconBitmap
                        }
                        if (displayBitmap != null) {
                            Image(bitmap = displayBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
                        } else {
                            if (isFetchingIcon) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = appName.ifBlank { "App Name" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Interactive Icon Retry, Zoom, Sources & Remove Buttons in 2x2 Matrix
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    if (url.isNotBlank()) {
                                        coroutineScope.launch {
                                            isFetchingIcon = true
                                            val nextSource = (iconSourceIndex + 1) % 7
                                            iconSourceIndex = nextSource
                                            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                            val (fetched, sourceName) = fetchPremiumIconWithSource(fetchUrl, nextSource)
                                            if (fetched != null) {
                                                iconUri = null
                                                iconName = null
                                                onAutoFetchedIconBitmapChange(fetched)
                                                Toast.makeText(context, "Fetched via $sourceName", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "No icon found via $sourceName", Toast.LENGTH_SHORT).show()
                                            }
                                            isFetchingIcon = false
                                        }
                                    } else {
                                        Toast.makeText(context, "Enter a website URL first", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Icon", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { showZoomDialog = true },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.ZoomIn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Zoom", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    if (url.isNotBlank()) {
                                        coroutineScope.launch {
                                            isFetchingIcon = true
                                            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                            fetchedIconsList = fetchAllAvailableIcons(fetchUrl)
                                            isFetchingIcon = false
                                            if (fetchedIconsList.isNotEmpty()) {
                                                showMultiIconSheet = true
                                            } else {
                                                Toast.makeText(context, "No icons found for this site", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Enter a website URL first", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Collections, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sources", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    iconUri = null
                                    iconName = null
                                    onAutoFetchedIconBitmapChange(null)
                                    Toast.makeText(context, "Icon removed (using default Mascot)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Website Details Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Website Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        placeholder = { Text("https://example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                    OutlinedTextField(
                        value = appName,
                        onValueChange = onAppNameChange,
                        placeholder = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                }
            }

            // Quick Toggles Bento Grid (Desktop Mode, Force Dark, Enable Zoom, Allow Copying, Hide Web Footer)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDesktopMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDesktopMode) 6.dp else 2.dp),
                        modifier = Modifier.weight(1f).border(1.dp, if (isDesktopMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            onDesktopModeChange(!isDesktopMode)
                            sharedPrefs.edit().putBoolean("desktop_mode", !isDesktopMode).apply()
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DesktopMac, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (isDesktopMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Desktop Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isDesktopMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                Text(if (isDesktopMode) "Desktop UA" else "Mobile UA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isForceDarkMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isForceDarkMode) 6.dp else 2.dp),
                        modifier = Modifier.weight(1f).border(1.dp, if (isForceDarkMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            onForceDarkModeChange(!isForceDarkMode)
                            sharedPrefs.edit().putBoolean("force_dark_mode", !isForceDarkMode).apply()
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DarkMode, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (isForceDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Force Dark", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isForceDarkMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                Text(if (isForceDarkMode) "Forced Dark" else "Web Theme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (enableZoom) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (enableZoom) 6.dp else 2.dp),
                        modifier = Modifier.weight(1f).border(1.dp, if (enableZoom) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            onEnableZoomChange(!enableZoom)
                            sharedPrefs.edit().putBoolean("enable_zoom", !enableZoom).apply()
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ZoomIn, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (enableZoom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Enable Zoom", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (enableZoom) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                Text(if (enableZoom) "Pinch Zoom" else "Disabled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (allowCopying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (allowCopying) 6.dp else 2.dp),
                        modifier = Modifier.weight(1f).border(1.dp, if (allowCopying) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            onAllowCopyingChange(!allowCopying)
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (allowCopying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Text Copying", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (allowCopying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                Text(if (allowCopying) "Allowed" else "Protected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isHideWebFooter) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isHideWebFooter) 6.dp else 2.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (isHideWebFooter) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                        isHideWebFooter = !isHideWebFooter
                        sharedPrefs.edit().putBoolean("hide_web_footer", isHideWebFooter).apply()
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CallToAction, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (isHideWebFooter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide Web Footer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (isHideWebFooter) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            Text(if (isHideWebFooter) "Enabled (Hiding Footers)" else "Disabled (Website Default)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Clickable Feature Option Cards (Package Identity, Storage Folder, Keystore)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                // 1. Package Identity & Versioning Card + Inline Pop-Under
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (packageName.isNotBlank() || versionCode.isNotBlank() || versionName.isNotBlank()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            isPackageIdentityExpanded = !isPackageIdentityExpanded
                        }
                    ) {
                        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Dns, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Package Identity & Versioning", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                val identitySummary = if (packageName.isNotBlank()) {
                                    "$packageName (${if (versionName.isNotBlank()) "v$versionName" else "v1.0.0"})"
                                } else {
                                    "Auto-Generated Package & Version"
                                }
                                Text(identitySummary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(if (isPackageIdentityExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    AnimatedVisibility(visible = isPackageIdentityExpanded) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Package Identity & Versioning", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                OutlinedTextField(
                                    value = packageName,
                                    onValueChange = onPackageNameChange,
                                    label = { Text("Custom Package Name") },
                                    placeholder = { Text("com.example.myapp") },
                                    leadingIcon = { Icon(Icons.Outlined.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = versionCode,
                                        onValueChange = onVersionCodeChange,
                                        label = { Text("Version Code") },
                                        placeholder = { Text("1") },
                                        leadingIcon = { Icon(Icons.Outlined.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = versionName,
                                        onValueChange = onVersionNameChange,
                                        label = { Text("Version Name") },
                                        placeholder = { Text("1.0.0") },
                                        leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = {
                                        onPackageNameChange("")
                                        onVersionCodeChange("")
                                        onVersionNameChange("")
                                    }) {
                                        Text("RESET TO AUTO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Storage Folder Card + Inline Pop-Under
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (useCustomDownloadFolder && customDownloadFolder.isNotBlank()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            isStorageFolderExpanded = !isStorageFolderExpanded
                        }
                    ) {
                        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Storage Folder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                val pathText = if (useCustomDownloadFolder && customDownloadFolder.isNotBlank()) customDownloadFolder else "Downloads/Packora (Default)"
                                Text(pathText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(if (isStorageFolderExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    AnimatedVisibility(visible = isStorageFolderExpanded) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("APK Output Storage Folder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = if (useCustomDownloadFolder && customDownloadFolder.isNotBlank()) customDownloadFolder else "Downloads/Packora (Public Downloads Folder)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { folderPickerLauncher.launch(null) },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("CHOOSE FOLDER", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                    if (useCustomDownloadFolder) {
                                        OutlinedButton(
                                            onClick = {
                                                useCustomDownloadFolder = false
                                                customDownloadFolder = ""
                                                sharedPrefs.edit().putBoolean("use_custom_download", false).remove("custom_download_folder").apply()
                                            },
                                            modifier = Modifier.height(44.dp),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text("RESET DEFAULT", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Signing Keystore Card + Inline Pop-Under
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (useCustomKeystore) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp)).clickable {
                            isKeystoreExpanded = !isKeystoreExpanded
                        }
                    ) {
                        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Signing Keystore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                val keyText = if (useCustomKeystore && keyAlias.isNotBlank()) keyAlias else "Packora Default Key"
                                Text(keyText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(if (isKeystoreExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    AnimatedVisibility(visible = isKeystoreExpanded) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Custom PKCS12 / JKS Signing Keystore", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = keystorePassword,
                                        onValueChange = { 
                                            keystorePassword = it
                                            useCustomKeystore = it.isNotBlank() || keyAlias.isNotBlank()
                                        },
                                        label = { Text("Store Password") },
                                        placeholder = { Text("Required") },
                                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = keyAlias,
                                        onValueChange = { 
                                            keyAlias = it
                                            useCustomKeystore = it.isNotBlank() || keystorePassword.isNotBlank()
                                        },
                                        label = { Text("Key Alias") },
                                        placeholder = { Text("Required") },
                                        leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = keyPassword,
                                        onValueChange = { keyPassword = it },
                                        label = { Text("Key Pass (Opt)") },
                                        placeholder = { Text("Same as store") },
                                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = validityYears,
                                        onValueChange = { validityYears = it },
                                        label = { Text("Validity (Years)") },
                                        placeholder = { Text("25") },
                                        leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }

                                OutlinedTextField(
                                    value = commonName,
                                    onValueChange = { commonName = it },
                                    label = { Text("Common Name (CN / Author)") },
                                    placeholder = { Text("Packora Publisher") },
                                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = organization,
                                        onValueChange = { organization = it },
                                        label = { Text("Organization (O)") },
                                        placeholder = { Text("Packora Studio") },
                                        leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = organizationalUnit,
                                        onValueChange = { organizationalUnit = it },
                                        label = { Text("Org Unit (OU)") },
                                        placeholder = { Text("Mobile Division") },
                                        leadingIcon = { Icon(Icons.Outlined.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = {
                                        useCustomKeystore = false
                                        keystorePassword = ""
                                        keyAlias = ""
                                        keyPassword = ""
                                        commonName = ""
                                        organization = ""
                                        organizationalUnit = ""
                                        validityYears = "25"
                                    }) {
                                        Text("RESET TO DEFAULT KEY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Compile Button Container (Edge-to-Edge Compact Padding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
            ) {
                Button(
                    onClick = {
                        isBuilding = true
                        targetProgressPercent = 0
                        animatedProgressPercent = 0
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val builder = ApkBuilder(context)
                                val finalPackage = if (packageName.isBlank()) {
                                    "com.maheswara660.packora." + (appName.ifBlank { "app" }).trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                                } else packageName.trim()
                                val historyList = historyManager.getHistoryItems()
                                val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                val normTarget = fetchUrl.lowercase().trimEnd('/')
                                val matching = historyList.filter { it.targetUrl.lowercase().trimEnd('/') == normTarget }

                                val finalCode = versionCode.toIntOrNull() ?: if (matching.isNotEmpty()) (matching.maxOf { it.versionCode } + 1) else 1
                                val finalName = versionName.ifBlank { if (matching.isNotEmpty()) incrementVersionString(matching.first().versionName) else "1.0.0" }

                                val inputBitmap: Bitmap? = if (iconUri != null) {
                                    context.contentResolver.openInputStream(iconUri!!).use {
                                        android.graphics.BitmapFactory.decodeStream(it)
                                    }
                                } else autoFetchedIconBitmap

                                val prefsManager = PackoraPreferencesManager(context)
                                val effectiveFolder = if (prefsManager.useCustomStorageFolder && !prefsManager.customStorageFolder.isNullOrBlank()) {
                                    prefsManager.customStorageFolder
                                } else if (useCustomDownloadFolder && customDownloadFolder.isNotBlank()) {
                                    customDownloadFolder
                                } else null

                                val resultPath = builder.buildApk(
                                    appName = appName.ifBlank { "My App" },
                                    packageName = finalPackage,
                                    targetUrl = url,
                                    versionCode = finalCode,
                                    versionName = finalName,
                                    iconBitmap = inputBitmap,
                                    disableHeader = true,
                                    outputPath = "${appName.replace(" ", "_")}.apk",
                                    customDownloadFolder = effectiveFolder,
                                    isDesktopMode = isDesktopMode,
                                    browserEngine = selectedBrowserEngine,
                                    allowCopying = allowCopying,
                                    isForceDarkMode = isForceDarkMode,
                                    enableZoom = enableZoom,
                                    hideWebFooter = isHideWebFooter,
                                    keystorePassword = if (useCustomKeystore && keystorePassword.isNotBlank()) keystorePassword else null,
                                    keyAlias = if (useCustomKeystore && keyAlias.isNotBlank()) keyAlias else null,
                                    commonName = if (useCustomKeystore && commonName.isNotBlank()) commonName else null,
                                    organization = if (useCustomKeystore && organization.isNotBlank()) organization else null,
                                    organizationalUnit = if (useCustomKeystore && organizationalUnit.isNotBlank()) organizationalUnit else null,
                                    validityYears = validityYears.toIntOrNull() ?: 25,
                                    keyPassword = if (useCustomKeystore && keyPassword.isNotBlank()) keyPassword else null,
                                    onProgress = { p, _ ->
                                        Handler(Looper.getMainLooper()).post {
                                            targetProgressPercent = maxOf(targetProgressPercent, p)
                                        }
                                    }
                                )
                                Handler(Looper.getMainLooper()).post {
                                    targetProgressPercent = 100
                                    coroutineScope.launch {
                                        while (animatedProgressPercent < 100) {
                                            kotlinx.coroutines.delay(12)
                                        }
                                        isBuilding = false
                                        if (resultPath != null) {
                                            lastBuiltApkPath = resultPath
                                            showSuccessDialog = true

                                            val itemId = java.util.UUID.randomUUID().toString()
                                            var savedIconPath: String? = null
                                            if (inputBitmap != null) {
                                                try {
                                                    val iconsDir = java.io.File(context.filesDir, "history_icons").apply { mkdirs() }
                                                    val iconFile = java.io.File(iconsDir, "${itemId}.png")
                                                    java.io.FileOutputStream(iconFile).use { out ->
                                                        inputBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                                                    }
                                                    savedIconPath = iconFile.absolutePath
                                                } catch (e: Exception) {}
                                            }

                                            historyManager.addHistoryItem(
                                                HistoryItem(
                                                    id = itemId,
                                                    appName = appName.ifBlank { "My App" },
                                                    packageName = finalPackage,
                                                    targetUrl = url,
                                                    versionCode = finalCode,
                                                    versionName = finalName,
                                                    isDesktopMode = isDesktopMode,
                                                    browserEngine = selectedBrowserEngine,
                                                    allowCopying = allowCopying,
                                                    apkPath = resultPath,
                                                    iconPath = savedIconPath
                                                )
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Handler(Looper.getMainLooper()).post { isBuilding = false }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !isBuilding && url.isNotBlank()
                ) {
                    if (isBuilding) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("COMPILING ${animatedProgressPercent}%", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    } else {
                        Icon(Icons.Outlined.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("COMPILE APK", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 16.sp)
                    }
                }
            }
        }

        if (showZoomDialog) {
            val displayBitmap = remember(iconUri, autoFetchedIconBitmap) {
                if (iconUri != null) {
                    try {
                        context.contentResolver.openInputStream(iconUri!!).use { android.graphics.BitmapFactory.decodeStream(it) }
                    } catch (e: Exception) { null }
                } else autoFetchedIconBitmap
            }
            IconZoomerBottomSheet(
                currentBitmap = displayBitmap,
                onDismiss = { showZoomDialog = false },
                onApply = { editedBitmap ->
                    iconUri = null
                    iconName = null
                    onAutoFetchedIconBitmapChange(editedBitmap)
                    Toast.makeText(context, "Zoomed icon applied", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showMultiIconSheet) {
            MultiIconPickerSheet(
                icons = fetchedIconsList,
                onDismiss = { showMultiIconSheet = false },
                onSelectIcon = { selectedBmp ->
                    iconUri = null
                    iconName = null
                    onAutoFetchedIconBitmapChange(selectedBmp)
                    Toast.makeText(context, "Selected icon applied", Toast.LENGTH_SHORT).show()
                }
            )
        }



        if (showClearConfirmSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showClearConfirmSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Reset Form Details?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Are you sure you want to reset all form inputs, website details, custom package settings, keystores, and quick toggles to default?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        OutlinedButton(
                            onClick = { showClearConfirmSheet = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("CANCEL", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                onUrlChange("")
                                onAppNameChange("")
                                onPackageNameChange("")
                                onVersionCodeChange("")
                                onVersionNameChange("")
                                iconUri = null
                                iconName = null
                                onAutoFetchedIconBitmapChange(null)

                                onDesktopModeChange(false)
                                onForceDarkModeChange(false)
                                onEnableZoomChange(false)
                                onAllowCopyingChange(false)
                                isHideWebFooter = false

                                useCustomDownloadFolder = false
                                customDownloadFolder = ""

                                useCustomKeystore = false
                                keystorePassword = ""
                                keyAlias = ""
                                keyPassword = ""
                                commonName = ""
                                organization = ""
                                organizationalUnit = ""
                                validityYears = "25"

                                isPackageIdentityExpanded = false
                                isStorageFolderExpanded = false
                                isKeystoreExpanded = false

                                sharedPrefs.edit()
                                    .putBoolean("desktop_mode", false)
                                    .putBoolean("force_dark_mode", false)
                                    .putBoolean("enable_zoom", false)
                                    .putBoolean("allow_copying", false)
                                    .putBoolean("hide_web_footer", false)
                                    .putBoolean("use_custom_download", false)
                                    .remove("custom_download_folder")
                                    .apply()

                                showClearConfirmSheet = false
                                Toast.makeText(context, "All form details and toggles reset to default", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESET EVERYTHING", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showSuccessDialog && lastBuiltApkPath != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showSuccessDialog = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "App Generated Successfully!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your custom WebAPK has been compiled, aligned, and signed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeIconBitmap = remember(autoFetchedIconBitmap, iconUri) {
                                autoFetchedIconBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
                            }
                            Image(
                                bitmap = activeIconBitmap.asImageBitmap(),
                                contentDescription = "App Icon",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appName.ifBlank { "Web App" },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = packageName.ifBlank { "com.web.app" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("v${versionName.ifBlank { "1.0.0" }} (${versionCode.ifBlank { "1" }})", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("Ready to Install", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                                        leadingIcon = { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary) }
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Button(
                            onClick = {
                                val cachedApk = File(context.cacheDir, "built_app.apk")
                                if (cachedApk.exists()) {
                                    installApkFile(context, cachedApk.absolutePath)
                                } else {
                                    installApkFile(context, lastBuiltApkPath!!)
                                }
                                showSuccessDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Outlined.Android, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INSTALL APK", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        OutlinedButton(
                            onClick = { showSuccessDialog = false },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("CLOSE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Helper Functions

suspend fun fetchAppTitle(urlString: String): String? = withContext(Dispatchers.IO) {
    try {
        val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
        connection.connectTimeout = 3000
        connection.readTimeout = 3000
        val html = connection.inputStream.bufferedReader().use { it.readText() }
        val titleMatch = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE).find(html)
        val title = titleMatch?.groupValues?.get(1)?.trim()
        if (title.isNullOrBlank()) null else title
    } catch (e: Exception) { null }
}

fun getAppNameFromUrl(url: String): String {
    return try {
        val uri = Uri.parse(url)
        val host = uri.host ?: "Web App"
        val parts = host.split(".")
        val domain = if (parts.size >= 2) {
            val idx = if (parts[0] == "www") 1 else 0
            parts[idx]
        } else {
            host
        }
        domain.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    } catch (e: Exception) {
        "Web App"
    }
}

data class FetchedIconItem(
    val sourceName: String,
    val bitmap: Bitmap
)

suspend fun fetchPremiumIcon(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
    val (bmp, _) = fetchPremiumIconWithSource(urlString, 0)
    bmp
}

suspend fun fetchPremiumIconWithSource(urlString: String, sourceIndex: Int = 0): Pair<Bitmap?, String> = withContext(Dispatchers.IO) {
    try {
        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext Pair(null, "Unknown")
        val scheme = uri.scheme ?: "https"

        when (sourceIndex % 7) {
            0 -> {
                // Direct Website HTML Icon scraping
                try {
                    val url = java.net.URL(urlString)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 4000
                    connection.readTimeout = 4000
                    connection.instanceFollowRedirects = true
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    val html = connection.inputStream.bufferedReader().use { it.readText() }

                    val iconRegex = Regex("<link[^>]+rel=\"[^\"]*(?:icon|shortcut|apple-touch-icon)[^\"]*\"[^>]+href=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
                    val matches = iconRegex.findAll(html).toList()

                    for (match in matches) {
                        var iconUrl = match.groupValues[1].trim()
                        if (iconUrl.startsWith("//")) {
                            iconUrl = "$scheme:$iconUrl"
                        } else if (!iconUrl.startsWith("http")) {
                            iconUrl = if (iconUrl.startsWith("/")) {
                                "$scheme://$host$iconUrl"
                            } else {
                                "$scheme://$host/$iconUrl"
                            }
                        }
                        val bmp = downloadAndValidate1To1Bitmap(iconUrl)
                        if (bmp != null) return@withContext Pair(bmp, "Direct Website HTML")
                    }
                    val rootFavicon = downloadAndValidate1To1Bitmap("$scheme://$host/favicon.ico")
                    if (rootFavicon != null) return@withContext Pair(rootFavicon, "Direct /favicon.ico")
                } catch (e: Exception) {}
                Pair(null, "Direct Website HTML")
            }
            1 -> {
                val appleTouch = downloadAndValidate1To1Bitmap("$scheme://$host/apple-touch-icon.png")
                Pair(appleTouch, "Apple Touch Icon")
            }
            2 -> {
                val googleBmp = downloadAndValidate1To1Bitmap("https://www.google.com/s2/favicons?domain=$host&sz=256")
                Pair(googleBmp, "Google Favicon API")
            }
            3 -> {
                val clearbit = downloadAndValidate1To1Bitmap("https://logo.clearbit.com/$host")
                Pair(clearbit, "Clearbit Logo API")
            }
            4 -> {
                val ddgBmp = downloadAndValidate1To1Bitmap("https://icons.duckduckgo.com/ip3/$host.ico")
                Pair(ddgBmp, "DuckDuckGo Icon API")
            }
            5 -> {
                val applePrecomposed = downloadAndValidate1To1Bitmap("$scheme://$host/apple-touch-icon-precomposed.png")
                Pair(applePrecomposed, "Apple Touch Precomposed")
            }
            6 -> {
                val rootFavicon = downloadAndValidate1To1Bitmap("$scheme://$host/favicon.ico")
                Pair(rootFavicon, "Root Favicon.ico")
            }
            else -> Pair(null, "Unknown Source")
        }
    } catch (e: Exception) {
        Pair(null, "Failed")
    }
}

suspend fun fetchAllAvailableIcons(urlString: String): List<FetchedIconItem> = withContext(Dispatchers.IO) {
    val results = mutableListOf<FetchedIconItem>()
    try {
        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext emptyList()
        val scheme = uri.scheme ?: "https"

        // 1. HTML Link & Meta icons (no limit)
        try {
            val url = java.net.URL(urlString)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            val html = connection.inputStream.bufferedReader().use { it.readText() }

            val iconRegex = Regex("<link[^>]+rel=\"[^\"]*(?:icon|shortcut|apple-touch-icon)[^\"]*\"[^>]+href=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
            val matches = iconRegex.findAll(html).toList()

            for ((index, match) in matches.withIndex()) {
                var iconUrl = match.groupValues[1].trim()
                if (iconUrl.startsWith("//")) {
                    iconUrl = "$scheme:$iconUrl"
                } else if (!iconUrl.startsWith("http")) {
                    iconUrl = if (iconUrl.startsWith("/")) "$scheme://$host$iconUrl" else "$scheme://$host/$iconUrl"
                }
                val bmp = downloadAndValidate1To1Bitmap(iconUrl)
                if (bmp != null) {
                    results.add(FetchedIconItem("Website Link #${index + 1}", bmp))
                }
            }

            // OG / Meta Images
            val ogRegex = Regex("<meta[^>]+property=\"[^\"]*og:image[^\"]*\"[^>]+content=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
            val ogMatch = ogRegex.find(html)
            if (ogMatch != null) {
                var ogUrl = ogMatch.groupValues[1].trim()
                if (ogUrl.startsWith("//")) ogUrl = "$scheme:$ogUrl"
                else if (!ogUrl.startsWith("http")) ogUrl = if (ogUrl.startsWith("/")) "$scheme://$host$ogUrl" else "$scheme://$host/$ogUrl"
                val ogBmp = downloadAndValidate1To1Bitmap(ogUrl)
                if (ogBmp != null) results.add(FetchedIconItem("OpenGraph Meta Image", ogBmp))
            }
        } catch (e: Exception) {}

        // 2. Apple Touch Icons
        val appleTouch = downloadAndValidate1To1Bitmap("$scheme://$host/apple-touch-icon.png")
        if (appleTouch != null) results.add(FetchedIconItem("Apple Touch Icon", appleTouch))
        val applePrecomposed = downloadAndValidate1To1Bitmap("$scheme://$host/apple-touch-icon-precomposed.png")
        if (applePrecomposed != null) results.add(FetchedIconItem("Apple Touch Precomposed", applePrecomposed))

        // 3. Google High Res Favicon
        val googleBmp = downloadAndValidate1To1Bitmap("https://www.google.com/s2/favicons?domain=$host&sz=256")
        if (googleBmp != null) results.add(FetchedIconItem("Google High-Res API", googleBmp))

        // 4. Clearbit Logo API
        val clearbit = downloadAndValidate1To1Bitmap("https://logo.clearbit.com/$host")
        if (clearbit != null) results.add(FetchedIconItem("Clearbit Logo API", clearbit))

        // 5. DuckDuckGo Icon
        val ddgBmp = downloadAndValidate1To1Bitmap("https://icons.duckduckgo.com/ip3/$host.ico")
        if (ddgBmp != null) results.add(FetchedIconItem("DuckDuckGo Icon", ddgBmp))

        // 6. Yandex Favicon API
        val yandexBmp = downloadAndValidate1To1Bitmap("https://favicon.yandex.net/favicon/$host?size=120")
        if (yandexBmp != null) results.add(FetchedIconItem("Yandex Favicon API", yandexBmp))

        // 7. Unavatar API
        val unavatarBmp = downloadAndValidate1To1Bitmap("https://unavatar.io/$host")
        if (unavatarBmp != null) results.add(FetchedIconItem("Unavatar API", unavatarBmp))

        // 8. Root Favicon
        val rootFavicon = downloadAndValidate1To1Bitmap("$scheme://$host/favicon.ico")
        if (rootFavicon != null) results.add(FetchedIconItem("Root Favicon.ico", rootFavicon))

    } catch (e: Exception) {}
    return@withContext results.distinctBy { "${it.bitmap.width}x${it.bitmap.height}" }
}

fun extractDominantCornerColor(bitmap: Bitmap?): Color {
    if (bitmap == null) return Color.Transparent
    return try {
        val w = bitmap.width
        val h = bitmap.height
        val c1 = bitmap.getPixel(minOf(4, w - 1), minOf(4, h - 1))
        val alpha = android.graphics.Color.alpha(c1)
        if (alpha > 180) {
            Color(c1)
        } else Color.Transparent
    } catch (e: Exception) {
        Color.Transparent
    }
}

fun zoomAndProcessBitmap(
    source: Bitmap,
    scaleFactor: Float,
    bgColor: Color
): Bitmap {
    val targetSize = 512
    val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)

    if (bgColor != Color.Transparent) {
        canvas.drawColor(bgColor.toArgb())
    }

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    val scaledWidth = (targetSize * scaleFactor).toInt()
    val scaledHeight = (targetSize * scaleFactor).toInt()
    val left = (targetSize - scaledWidth) / 2
    val top = (targetSize - scaledHeight) / 2

    val destRect = android.graphics.Rect(left, top, left + scaledWidth, top + scaledHeight)
    canvas.drawBitmap(source, null, destRect, paint)

    return output
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconZoomerBottomSheet(
    currentBitmap: Bitmap?,
    onDismiss: () -> Unit,
    onApply: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scaleFactor by remember { mutableFloatStateOf(1.0f) }

    val baseBitmap = remember(currentBitmap) {
        currentBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
    }

    val autoCornerColor = remember(baseBitmap) { extractDominantCornerColor(baseBitmap) }

    var customColor by remember { mutableStateOf<Color?>(null) }
    var showCustomHexDialog by remember { mutableStateOf(false) }

    val colorOptions = remember(autoCornerColor, customColor) {
        val list = mutableListOf<Pair<String, Color>>()
        if (autoCornerColor != Color.Transparent) {
            list.add("✨ Auto Match" to autoCornerColor)
        }
        if (customColor != null) {
            list.add("🎨 Eyedropper Color" to customColor!!)
        }
        list.addAll(
            listOf(
                "Transparent" to Color.Transparent,
                "White" to Color.White,
                "Black" to Color.Black,
                "Dark Gray" to Color(0xFF1E1E1E),
                "Charcoal" to Color(0xFF262626),
                "Slate Gray" to Color(0xFF334155),
                "Navy Blue" to Color(0xFF0F172A),
                "Deep Indigo" to Color(0xFF312E81),
                "Midnight Blue" to Color(0xFF172554),
                "Ocean Blue" to Color(0xFF0284C7),
                "Sky Blue" to Color(0xFF38BDF8),
                "Forest Green" to Color(0xFF14532D),
                "Emerald Green" to Color(0xFF059669),
                "Teal" to Color(0xFF0D9488),
                "Deep Teal" to Color(0xFF042F2E),
                "Crimson Red" to Color(0xFFBE123C),
                "Ruby Red" to Color(0xFFDC2626),
                "Burgundy" to Color(0xFF4C0519),
                "Vibrant Orange" to Color(0xFFEA580C),
                "Warm Amber" to Color(0xFFD97706),
                "Royal Purple" to Color(0xFF7E22CE),
                "Deep Purple" to Color(0xFF3B0764),
                "Hot Pink" to Color(0xFFDB2777),
                "Rose Pink" to Color(0xFFF43F5E),
                "Chocolate Brown" to Color(0xFF451A03),
                "Light Gray" to Color(0xFFF1F5F9),
                "Pastel Blue" to Color(0xFFE0F2FE),
                "Pastel Mint" to Color(0xFFD1FAE5),
                "Pastel Pink" to Color(0xFFFCE7F3),
                "Pastel Purple" to Color(0xFFF3E8FF),
                "Pastel Yellow" to Color(0xFFFEF3C7)
            )
        )
        list
    }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val previewBitmap = remember(scaleFactor, selectedColorIndex, colorOptions, baseBitmap) {
        val selectedColor = if (selectedColorIndex < colorOptions.size) colorOptions[selectedColorIndex].second else Color.Transparent
        zoomAndProcessBitmap(
            source = baseBitmap,
            scaleFactor = scaleFactor,
            bgColor = selectedColor
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ZoomIn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Icon Zoomer & Color Eyedropper",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Scale icon size, auto-match background, or pick exact hex colors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selectedColorIndex < colorOptions.size) colorOptions[selectedColorIndex].second else Color.Transparent)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Zoomed Preview",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Zoom Scale", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text("${(scaleFactor * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = scaleFactor,
                    onValueChange = { scaleFactor = it },
                    valueRange = 0.4f..2.0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Background Color Fill", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showCustomHexDialog = true }) {
                        Icon(Icons.Outlined.Colorize, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Custom Hex", style = MaterialTheme.typography.labelSmall)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions.size) { idx ->
                        val (name, colorVal) = colorOptions[idx]
                        FilterChip(
                            selected = selectedColorIndex == idx,
                            onClick = { selectedColorIndex = idx },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (colorVal != Color.Transparent) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(colorVal)
                                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onApply(previewBitmap)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("APPLY ICON", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiIconPickerSheet(
    icons: List<FetchedIconItem>,
    onDismiss: () -> Unit,
    onSelectIcon: (Bitmap) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Collections,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Select App Icon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Choose from highest quality icons fetched from website sources and APIs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(icons.size) { idx ->
                    val item = icons[idx]
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .size(110.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable {
                                onSelectIcon(item.bitmap)
                                onDismiss()
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                bitmap = item.bitmap.asImageBitmap(),
                                contentDescription = item.sourceName,
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.sourceName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun downloadAndValidate1To1Bitmap(urlStr: String): Bitmap? {
    val bitmap = downloadBitmap(urlStr) ?: return null
    if (bitmap.width < 16 || bitmap.height < 16) return null
    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
    return if (ratio in 0.7f..1.4f) bitmap else null
}

fun downloadBitmap(url: String): Bitmap? {
    return try {
        val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        connection.doInput = true
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.connect()
        val input = connection.inputStream
        android.graphics.BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        null
    }
}

suspend fun isUrlReachable(url: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 3000
        connection.readTimeout = 3000
        connection.responseCode in 200..399
    } catch (e: Exception) {
        false
    }
}

fun Context.appVersion(): String {
    return try {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        pInfo.versionName ?: "2.3.0"
    } catch (e: Exception) {
        "2.3.0"
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
