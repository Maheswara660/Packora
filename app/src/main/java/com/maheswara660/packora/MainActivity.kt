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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class Screen {
    DASHBOARD, HISTORY, SETTINGS, ABOUT
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
    val navigationStack = remember { mutableStateListOf(Screen.DASHBOARD) }
    val currentScreen = navigationStack.lastOrNull() ?: Screen.DASHBOARD

    fun navigateTo(screen: Screen) {
        if (navigationStack.lastOrNull() != screen) {
            navigationStack.add(screen)
        }
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.lastIndex)
        }
    }

    BackHandler(enabled = navigationStack.size > 1) {
        navigateBack()
    }

    var url by remember { mutableStateOf("") }
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var versionCode by remember { mutableStateOf("1") }
    var versionName by remember { mutableStateOf("1.0.0") }
    var isDesktopMode by remember { mutableStateOf(false) }
    var selectedBrowserEngine by remember { mutableStateOf("INDIVIDUAL") }
    var allowCopying by remember { mutableStateOf(false) }

    val context = LocalContext.current

    when (currentScreen) {
        Screen.DASHBOARD -> {
            PackoraDashboard(
                url = url,
                onUrlChange = { url = it },
                appName = appName,
                onAppNameChange = { appName = it },
                packageName = packageName,
                onPackageNameChange = { packageName = it },
                versionCode = versionCode,
                onVersionCodeChange = { versionCode = it },
                versionName = versionName,
                onVersionNameChange = { versionName = it },
                isDesktopMode = isDesktopMode,
                onDesktopModeChange = { isDesktopMode = it },
                selectedBrowserEngine = selectedBrowserEngine,
                onBrowserEngineChange = { selectedBrowserEngine = it },
                allowCopying = allowCopying,
                onAllowCopyingChange = { allowCopying = it },
                onNavigateHistory = { navigateTo(Screen.HISTORY) },
                onNavigateSettings = { navigateTo(Screen.SETTINGS) }
            )
        }
        Screen.HISTORY -> {
            HistoryScreen(
                onBack = { navigateBack() },
                onReuseConfig = { item ->
                    url = item.targetUrl
                    appName = item.appName
                    packageName = item.packageName
                    versionCode = (item.versionCode + 1).toString()
                    versionName = incrementVersionString(item.versionName)
                    isDesktopMode = item.isDesktopMode
                    selectedBrowserEngine = item.browserEngine
                    allowCopying = item.allowCopying
                    Toast.makeText(context, "Loaded config for ${item.appName} (v${versionCode})", Toast.LENGTH_SHORT).show()
                    navigationStack.clear()
                    navigationStack.add(Screen.DASHBOARD)
                }
            )
        }
        Screen.SETTINGS -> {
            SettingsScreen(
                onBack = { navigateBack() },
                onNavigateAbout = { navigateTo(Screen.ABOUT) },
                onThemeModeChange = onThemeModeChange,
                onColorAccentChange = onColorAccentChange
            )
        }
        Screen.ABOUT -> {
            AboutScreen(
                onBack = { navigateBack() }
            )
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
    selectedBrowserEngine: String,
    onBrowserEngineChange: (String) -> Unit,
    allowCopying: Boolean,
    onAllowCopyingChange: (Boolean) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("packora_prefs", Context.MODE_PRIVATE)
    val historyManager = remember { BuildHistoryManager(context) }

    var useCustomDownloadFolder by remember { mutableStateOf(sharedPrefs.getBoolean("use_custom_download", false)) }
    var customDownloadFolder by remember { mutableStateOf(sharedPrefs.getString("custom_download_folder", "") ?: "") }

    var iconUri by remember { mutableStateOf<Uri?>(null) }
    var iconName by remember { mutableStateOf<String?>(null) }
    var autoFetchedIconBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isFetchingIcon by remember { mutableStateOf(false) }
    var iconSourceIndex by remember { mutableStateOf(0) }

    var useCustomKeystore by remember { mutableStateOf(false) }
    var keystorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var commonName by remember { mutableStateOf("") }

    var isBuilding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastBuiltApkPath by remember { mutableStateOf<String?>(null) }

    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var showClearConfirmSheet by remember { mutableStateOf(false) }

    var showZoomDialog by remember { mutableStateOf(false) }
    var showMultiIconSheet by remember { mutableStateOf(false) }
    var fetchedIconsList by remember { mutableStateOf<List<FetchedIconItem>>(emptyList()) }

    LaunchedEffect(url) {
        autoFetchedIconBitmap = null
        iconUri = null
        iconName = null
        iconSourceIndex = 0
        if (url.isNotBlank()) {
            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url

            val historyList = historyManager.getHistoryItems()
            val normTarget = fetchUrl.lowercase().trimEnd('/')
            val matching = historyList.filter { it.targetUrl.lowercase().trimEnd('/') == normTarget }
            if (matching.isNotEmpty()) {
                val maxCode = matching.maxOf { item -> item.versionCode }
                onVersionCodeChange((maxCode + 1).toString())
                val latestVer = matching.first().versionName
                onVersionNameChange(incrementVersionString(latestVer))
            } else {
                onVersionCodeChange("1")
                onVersionNameChange("1.0.0")
            }

            isFetchingIcon = true
            delay(500)
            try {
                val (fetched, _) = fetchPremiumIconWithSource(fetchUrl, 0)
                autoFetchedIconBitmap = fetched
            } catch (e: Exception) {
                autoFetchedIconBitmap = null
            } finally {
                isFetchingIcon = false
            }
        } else {
            isFetchingIcon = false
            onVersionCodeChange("1")
            onVersionNameChange("1.0.0")
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
                                                autoFetchedIconBitmap = fetched
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
                                    autoFetchedIconBitmap = null
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

            // Quick Toggles (Desktop & Storage)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDesktopMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDesktopMode) 8.dp else 4.dp),
                    modifier = Modifier.weight(1f).aspectRatio(1f).border(1.dp, if (isDesktopMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).clickable {
                        onDesktopModeChange(!isDesktopMode)
                        sharedPrefs.edit().putBoolean("desktop_mode", !isDesktopMode).apply()
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Outlined.DesktopMac, contentDescription = null, modifier = Modifier.size(36.dp), tint = if (isDesktopMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("Desktop", fontWeight = FontWeight.Bold, color = if (isDesktopMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            Text(if (isDesktopMode) "Enabled" else "Disabled", style = MaterialTheme.typography.labelSmall, color = if (isDesktopMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (useCustomDownloadFolder) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (useCustomDownloadFolder) 8.dp else 4.dp),
                    modifier = Modifier.weight(1f).aspectRatio(1f).border(1.dp, if (useCustomDownloadFolder) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).clickable {
                        useCustomDownloadFolder = !useCustomDownloadFolder
                        sharedPrefs.edit().putBoolean("use_custom_download", useCustomDownloadFolder).apply()
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Outlined.FolderZip, contentDescription = null, modifier = Modifier.size(36.dp), tint = if (useCustomDownloadFolder) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("Storage", fontWeight = FontWeight.Bold, color = if (useCustomDownloadFolder) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            Text(if (useCustomDownloadFolder) "Custom Path" else "Downloads", style = MaterialTheme.typography.labelSmall, color = if (useCustomDownloadFolder) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (useCustomDownloadFolder) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp)).clickable { folderPickerLauncher.launch(null) }
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(if (customDownloadFolder.isBlank()) "Tap to select folder" else customDownloadFolder, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Advanced Options Expandable
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).clickable { isAdvancedExpanded = !isAdvancedExpanded }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Advanced Identity & Security", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Text Copy Protection, Keystore, Versioning", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            if (isAdvancedExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isAdvancedExpanded) {
                        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Allow Text Copying", fontWeight = FontWeight.Bold)
                                    Text("Off by default to prevent web content selection & copying", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = allowCopying, onCheckedChange = onAllowCopyingChange)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            OutlinedTextField(value = packageName, onValueChange = onPackageNameChange, label = { Text("Custom Package Name") }, placeholder = { Text("com.example.myapp") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(value = versionCode, onValueChange = onVersionCodeChange, label = { Text("Version Code") }, placeholder = { Text("1") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                                OutlinedTextField(value = versionName, onValueChange = onVersionNameChange, label = { Text("Version Name") }, placeholder = { Text("1.0.0") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Inject Custom Keystore", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Switch(checked = useCustomKeystore, onCheckedChange = { useCustomKeystore = it })
                            }

                            if (useCustomKeystore) {
                                OutlinedTextField(value = keystorePassword, onValueChange = { keystorePassword = it }, label = { Text("Keystore Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation())
                                OutlinedTextField(value = keyAlias, onValueChange = { keyAlias = it }, label = { Text("Key Alias") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                OutlinedTextField(value = commonName, onValueChange = { commonName = it }, label = { Text("Common Name (CN)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
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
                        progress = 0f
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val builder = ApkBuilder(context)
                                val finalPackage = if (packageName.isBlank()) {
                                    "com.maheswara660.packora." + (appName.ifBlank { "app" }).trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                                } else packageName.trim()
                                val finalCode = versionCode.toIntOrNull() ?: 1
                                val finalName = versionName.ifBlank { "1.0.0" }

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
                                    keystorePassword = if (useCustomKeystore) keystorePassword else null,
                                    keyAlias = if (useCustomKeystore) keyAlias else null,
                                    commonName = if (useCustomKeystore) commonName else null,
                                    onProgress = { p, _ ->
                                        Handler(Looper.getMainLooper()).post { progress = p / 100f }
                                    }
                                )
                                Handler(Looper.getMainLooper()).post {
                                    isBuilding = false
                                    if (resultPath != null) {
                                        lastBuiltApkPath = resultPath
                                        showSuccessDialog = true

                                        historyManager.addHistoryItem(
                                            HistoryItem(
                                                appName = appName.ifBlank { "My App" },
                                                packageName = finalPackage,
                                                targetUrl = url,
                                                versionCode = finalCode,
                                                versionName = finalName,
                                                isDesktopMode = isDesktopMode,
                                                browserEngine = selectedBrowserEngine,
                                                allowCopying = allowCopying,
                                                apkPath = resultPath
                                            )
                                        )
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
                        Text("COMPILING ${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
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
                    autoFetchedIconBitmap = editedBitmap
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
                    autoFetchedIconBitmap = selectedBmp
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
                            text = "Are you sure you want to clear current website URL, app name, package details, and selected icon?",
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
                                autoFetchedIconBitmap = null
                                showClearConfirmSheet = false
                                Toast.makeText(context, "Details cleared", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CLEAR DETAILS", fontWeight = FontWeight.Bold)
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

    val colorOptions = remember {
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
    }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val baseBitmap = remember(currentBitmap) {
        currentBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
    }

    val previewBitmap = remember(scaleFactor, selectedColorIndex, baseBitmap) {
        zoomAndProcessBitmap(
            source = baseBitmap,
            scaleFactor = scaleFactor,
            bgColor = colorOptions[selectedColorIndex].second
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
                    text = "Icon Zoomer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Scale icon size and choose background color fill for transparent icons.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorOptions[selectedColorIndex].second)
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
                Text("Background Color Fill", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
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
