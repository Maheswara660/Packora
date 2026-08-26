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
    var selectedBrowserEngine by remember { mutableStateOf("SYSTEM_DEFAULT") }
    var selectedDns by remember { mutableStateOf("SYSTEM") }
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
                selectedDns = selectedDns,
                onDnsChange = { selectedDns = it },
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
                    selectedDns = item.dnsProvider
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
    selectedDns: String,
    onDnsChange: (String) -> Unit,
    allowCopying: Boolean,
    onAllowCopyingChange: (Boolean) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("packora_prefs", Context.MODE_PRIVATE)
    val historyManager = remember { BuildHistoryManager(context) }

    var customDnsUrl by remember { mutableStateOf("") }
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
    var showBrowserEngineSheet by remember { mutableStateOf(false) }

    // Direct site HTML icon fetching first on URL entry
    LaunchedEffect(url) {
        autoFetchedIconBitmap = null
        iconUri = null
        iconName = null
        iconSourceIndex = 0
        if (url.isNotBlank()) {
            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url

            // Smart Version Auto-Increment based on History for this URL to avoid parsing update errors
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
            // Live Preview Hero Card
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

                    // Interactive Icon Retry & Remove Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                if (url.isNotBlank()) {
                                    coroutineScope.launch {
                                        isFetchingIcon = true
                                        val nextSource = (iconSourceIndex + 1) % 5
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
                            label = { Text("Retry Icon", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )

                        AssistChip(
                            onClick = {
                                iconUri = null
                                iconName = null
                                autoFetchedIconBitmap = null
                                Toast.makeText(context, "Icon removed (using default Mascot)", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("Remove", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
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

            // Browser Engine Selection Card (Opens Bottom Sheet Menu with OK & CANCEL)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { showBrowserEngineSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Browser Engine", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        val engineLabel = when (selectedBrowserEngine) {
                            "SYSTEM_DEFAULT" -> "System Default (Custom Tabs)"
                            "BUILT_IN" -> "Built-in Shell"
                            "INDIVIDUAL" -> "Individual Standalone"
                            else -> selectedBrowserEngine
                        }
                        Text(engineLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            Text("Advanced Network & Identity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("DNS, Text Copy Protection, Keystore, Versioning", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            if (isAdvancedExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isAdvancedExpanded) {
                        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // DNS Provider Selector
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("DNS Provider", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val dnsProviders = listOf(
                                        "SYSTEM" to "System Default",
                                        "CLOUDFLARE" to "Cloudflare (1.1.1.1)",
                                        "GOOGLE" to "Google DNS",
                                        "ADGUARD" to "AdGuard Ad-Block",
                                        "QUAD9" to "Quad9",
                                        "CONTROLD" to "ControlD",
                                        "OPENDNS" to "OpenDNS",
                                        "NEXTDNS" to "NextDNS",
                                        "CUSTOM" to "Custom DoH"
                                    )
                                    dnsProviders.forEach { (key, label) ->
                                        FilterChip(
                                            selected = selectedDns == key,
                                            onClick = { onDnsChange(key) },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                                if (selectedDns == "CUSTOM") {
                                    OutlinedTextField(
                                        value = customDnsUrl,
                                        onValueChange = { customDnsUrl = it },
                                        label = { Text("Custom DoH / DNS URL") },
                                        placeholder = { Text("https://dns.example.com/dns-query") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Text Copy Prevention Toggle (Default: OFF)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Allow Text Copying", fontWeight = FontWeight.Bold)
                                    Text("Off by default to prevent web content selection & copying", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = allowCopying, onCheckedChange = onAllowCopyingChange)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            OutlinedTextField(value = packageName, onValueChange = onPackageNameChange, label = { Text("Custom Package Name") }, placeholder = { Text("com.maheswara660.packora.app") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(value = versionCode, onValueChange = onVersionCodeChange, label = { Text("Version Code") }, placeholder = { Text("1") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                                OutlinedTextField(value = versionName, onValueChange = onVersionNameChange, label = { Text("Version Name") }, placeholder = { Text("2.2.0") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
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

            // Compile Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
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
                                val finalName = versionName.ifBlank { "2.2.0" }

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
                                    selectedDns = selectedDns,
                                    customDnsUrl = customDnsUrl,
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

                                        // Record generated app into Build History
                                        historyManager.addHistoryItem(
                                            HistoryItem(
                                                appName = appName.ifBlank { "My App" },
                                                packageName = finalPackage,
                                                targetUrl = url,
                                                versionCode = finalCode,
                                                versionName = finalName,
                                                isDesktopMode = isDesktopMode,
                                                browserEngine = selectedBrowserEngine,
                                                dnsProvider = selectedDns,
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
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
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
        } // End of Column

        // Browser Engine Selection Bottom Sheet Selector
        if (showBrowserEngineSheet) {
            com.maheswara660.packora.ui.SelectionBottomSheetDialog(
                title = "Select Browser Engine",
                subtitle = "Choose web runtime engine for your generated standalone WebAPK",
                icon = Icons.Outlined.Language,
                options = listOf(
                    "SYSTEM_DEFAULT" to "System Default (Chrome Custom Tabs — Shares device logins & cookies)",
                    "BUILT_IN" to "Built-in Shell (Standalone embedded WebView)",
                    "INDIVIDUAL" to "Individual Standalone (Private isolated container per app)"
                ),
                initialSelection = selectedBrowserEngine,
                onDismiss = { showBrowserEngineSheet = false },
                onConfirm = { selected ->
                    onBrowserEngineChange(selected)
                    showBrowserEngineSheet = false
                }
            )
        }

        // Clear Details Confirmation Bottom Sheet
        if (showClearConfirmSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showClearConfirmSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Clear Filled Details?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Are you sure you want to clear all entered URL, App Name, and Package Name fields?",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showClearConfirmSheet = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("CANCEL")
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
                            Text("CLEAR DETAILS")
                        }
                    }
                }
            }
        }

        if (showSuccessDialog && lastBuiltApkPath != null) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showSuccessDialog = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("App Generated Successfully!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    Text("Your app is ready. What would you like to do?", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("INSTALL", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    OutlinedButton(
                        onClick = { showSuccessDialog = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("CLOSE")
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

suspend fun fetchPremiumIcon(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
    val (bmp, _) = fetchPremiumIconWithSource(urlString, 0)
    bmp
}

suspend fun fetchPremiumIconWithSource(urlString: String, sourceIndex: Int = 0): Pair<Bitmap?, String> = withContext(Dispatchers.IO) {
    try {
        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext Pair(null, "Unknown")

        when (sourceIndex % 5) {
            0 -> {
                // Direct Website HTML Icon scraping
                try {
                    val url = java.net.URL(urlString)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.instanceFollowRedirects = true
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    val html = connection.inputStream.bufferedReader().use { it.readText() }

                    val iconRegex = Regex("<link[^>]+rel=\"[^\"]*(?:icon|shortcut|apple-touch-icon)[^\"]*\"[^>]+href=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
                    val matches = iconRegex.findAll(html).toList()

                    for (match in matches) {
                        var iconUrl = match.groupValues[1].trim()
                        if (iconUrl.startsWith("//")) {
                            iconUrl = "${uri.scheme ?: "https"}:$iconUrl"
                        } else if (!iconUrl.startsWith("http")) {
                            iconUrl = if (iconUrl.startsWith("/")) {
                                "${uri.scheme ?: "https"}://$host$iconUrl"
                            } else {
                                "${uri.scheme ?: "https"}://$host/$iconUrl"
                            }
                        }
                        val bmp = downloadAndValidate1To1Bitmap(iconUrl)
                        if (bmp != null) return@withContext Pair(bmp, "Direct Website HTML")
                    }
                    val rootFavicon = downloadAndValidate1To1Bitmap("${uri.scheme ?: "https"}://$host/favicon.ico")
                    if (rootFavicon != null) return@withContext Pair(rootFavicon, "Direct /favicon.ico")
                } catch (e: Exception) {}
                Pair(null, "Direct Website HTML")
            }
            1 -> {
                val appleTouch = downloadAndValidate1To1Bitmap("https://$host/apple-touch-icon.png")
                Pair(appleTouch, "Apple Touch Icon")
            }
            2 -> {
                val googleBmp = downloadAndValidate1To1Bitmap("https://www.google.com/s2/favicons?domain=$host&sz=256")
                Pair(googleBmp, "Google Favicon API")
            }
            3 -> {
                val ddgBmp = downloadAndValidate1To1Bitmap("https://icons.duckduckgo.com/ip3/$host.ico")
                Pair(ddgBmp, "DuckDuckGo Icon API")
            }
            4 -> {
                val rootFavicon = downloadAndValidate1To1Bitmap("https://$host/favicon.ico")
                Pair(rootFavicon, "Root Favicon")
            }
            else -> Pair(null, "Unknown Source")
        }
    } catch (e: Exception) {
        Pair(null, "Failed")
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
        pInfo.versionName ?: "2.2.0"
    } catch (e: Exception) {
        "2.2.0"
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
