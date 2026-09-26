package com.maheswara660.packora.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.incrementVersionString
import com.maheswara660.packora.installApkFile
import com.maheswara660.packora.manager.BuildHistoryManager
import com.maheswara660.packora.manager.HistoryItem
import com.maheswara660.packora.manager.PackoraPreferencesManager
import com.maheswara660.packora.model.PackoraAppType
import com.maheswara660.packora.model.PackoraPrivacyConfig
import com.maheswara660.packora.model.PackoraAdBlockConfig
import com.maheswara660.packora.model.PackoraNetworkConfig
import com.maheswara660.packora.model.PackoraDnsProvider
import com.maheswara660.packora.scraper.PackoraWebScraper
import com.maheswara660.packora.ui.components.PackoraDotLoader
import com.maheswara660.packora.ui.components.PackoraIosSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(
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
    onNavigateSettings: () -> Unit,
    isEnableWebFooter: Boolean = false,
    onEnableWebFooterChange: (Boolean) -> Unit = {},
    disguiseFingerprint: Boolean = false,
    onDisguiseFingerprintChange: (Boolean) -> Unit = {},
    adBlockEnabled: Boolean = false,
    onAdBlockEnabledChange: (Boolean) -> Unit = {},
    selectedDnsProvider: PackoraDnsProvider = PackoraDnsProvider.SYSTEM,
    onDnsProviderChange: (PackoraDnsProvider) -> Unit = {},
    perAppSigningEnabled: Boolean = true,
    onPerAppSigningEnabledChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("packora_prefs", Context.MODE_PRIVATE) }
    val historyManager = remember { BuildHistoryManager(context) }

    var useCustomDownloadFolder by remember { mutableStateOf(sharedPrefs.getBoolean("use_custom_download", false)) }
    var customDownloadFolder by remember { mutableStateOf(sharedPrefs.getString("custom_download_folder", "") ?: "") }

    var iconUri by remember { mutableStateOf<Uri?>(null) }
    var isFetchingIcon by remember { mutableStateOf(false) }
    var iconSourceIndex by remember { mutableIntStateOf(0) }

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

    var selectedAppType by remember { mutableStateOf(PackoraAppType.WEB) }

    // Privacy & Fingerprint Disguise State
    var isPrivacyExpanded by remember { mutableStateOf(false) }
    var maskCanvas by remember { mutableStateOf(true) }
    var maskWebGL by remember { mutableStateOf(true) }
    var maskAudioContext by remember { mutableStateOf(true) }
    var maskClientRects by remember { mutableStateOf(true) }
    var maskWebRtcIp by remember { mutableStateOf(true) }
    var clearDataOnExit by remember { mutableStateOf(false) }

    // Ad-Block & Tracker Defense State
    var isAdBlockExpanded by remember { mutableStateOf(false) }
    var blockTrackers by remember { mutableStateOf(true) }
    var cosmeticFiltering by remember { mutableStateOf(true) }

    // Network & DNS-over-HTTPS (DoH) State
    var isNetworkExpanded by remember { mutableStateOf(false) }
    var showDnsProviderSheet by remember { mutableStateOf(false) }
    var customDohUrl by remember { mutableStateOf("") }
    var strictDoh by remember { mutableStateOf(false) }
    var enableEch by remember { mutableStateOf(false) }

    // Per-App Signing Keystore

    // Offline HTML Scraper State
    var isScrapingOffline by remember { mutableStateOf(false) }
    var scrapeProgressStatus by remember { mutableStateOf("") }
    var scrapedOfflineFilesCount by remember { mutableIntStateOf(0) }

    var isBuilding by remember { mutableStateOf(false) }
    var targetProgressPercent by remember { mutableIntStateOf(0) }
    var animatedProgressPercent by remember { mutableIntStateOf(0) }

    LaunchedEffect(isBuilding) {
        if (isBuilding) {
            animatedProgressPercent = 1
            while (isBuilding && animatedProgressPercent < 100) {
                val target = targetProgressPercent
                val maxAllowed = if (target >= 100) 100 else minOf(maxOf(target, animatedProgressPercent + 1), 92)
                if (animatedProgressPercent < maxAllowed) {
                    animatedProgressPercent++
                }
                delay(if (target >= 100) 8L else 20L)
            }
        } else {
            animatedProgressPercent = 0
            targetProgressPercent = 0
        }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastBuiltApkPath by remember { mutableStateOf<String?>(null) }
    var lastBuiltPackageName by remember { mutableStateOf("") }
    var lastBuiltAppName by remember { mutableStateOf("") }
    var lastBuiltVersionName by remember { mutableStateOf("") }
    var lastBuiltVersionCode by remember { mutableIntStateOf(1) }
    var isUserEditedAppName by remember { mutableStateOf(false) }
    var showClearConfirmSheet by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }
    var showMultiIconSheet by remember { mutableStateOf(false) }
    var fetchedIconsList by remember { mutableStateOf<List<FetchedIconItem>>(emptyList()) }

    // Auto fetch icon and app title on URL change using PackoraWebAnalyzer
    LaunchedEffect(url) {
        onAutoFetchedIconBitmapChange(null)
        iconUri = null
        iconSourceIndex = 0
        if (url.isNotBlank()) {
            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            delay(400)
            isFetchingIcon = true
            try {
                val analysis = com.maheswara660.packora.analyzer.PackoraWebAnalyzer.analyze(fetchUrl)
                if (!isUserEditedAppName) {
                    val candidate = analysis.title ?: analysis.shortName ?: getAppNameFromUrl(fetchUrl)
                    if (candidate.isNotBlank()) {
                        onAppNameChange(candidate)
                    }
                }
                var foundBmp: Bitmap? = null
                for (icon in analysis.icons) {
                    val bmp = downloadAndValidate1To1Bitmap(icon.url)
                    if (bmp != null) {
                        foundBmp = bmp
                        break
                    }
                }
                if (foundBmp == null) {
                    val (fallbackBmp, _) = fetchPremiumIconWithSource(fetchUrl, 0)
                    foundBmp = fallbackBmp
                }
                onAutoFetchedIconBitmapChange(foundBmp)
            } catch (e: Exception) {
                if (!isUserEditedAppName) {
                    val inferred = getAppNameFromUrl(fetchUrl)
                    if (inferred.isNotBlank()) {
                        onAppNameChange(inferred)
                    }
                }
                try {
                    val (fetched, _) = fetchPremiumIconWithSource(fetchUrl, 0)
                    onAutoFetchedIconBitmapChange(fetched)
                } catch (ex: Exception) {
                    onAutoFetchedIconBitmapChange(null)
                }
            } finally {
                isFetchingIcon = false
            }
        } else {
            isFetchingIcon = false
        }
    }

    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            iconUri = uri
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
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

    val displayBitmap = remember(iconUri, autoFetchedIconBitmap) {
        if (iconUri != null) {
            try {
                context.contentResolver.openInputStream(iconUri!!).use { android.graphics.BitmapFactory.decodeStream(it) }
            } catch (e: Exception) { null }
        } else autoFetchedIconBitmap
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Packora",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Web to Native APK Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClearConfirmSheet = true }) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = "Reset Form",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 86.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // App Target Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        PackoraAppType.WEB to Icons.Outlined.Language,
                        PackoraAppType.HTML to Icons.Outlined.FolderZip,
                        PackoraAppType.FRONTEND to Icons.Outlined.Code,
                        PackoraAppType.MULTI_WEB to Icons.Outlined.Tab,
                        PackoraAppType.MEDIA to Icons.Outlined.PlayCircle
                    ).forEach { (type, icon) ->
                        val isSelected = selectedAppType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAppType = type },
                            label = { Text(type.displayName) },
                            leadingIcon = {
                                Icon(
                                    if (isSelected) Icons.Filled.Check else icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // Dedicated Scraper & Pack Generator Card for Offline HTML
                if (selectedAppType == PackoraAppType.HTML) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Offline Website Bundler", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Text(
                                "Crawls and compiles HTML, CSS, JavaScript, and assets into an offline standalone package with no network needed at runtime.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isScrapingOffline) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PackoraDotLoader(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = scrapeProgressStatus.ifBlank { "Scraping and bundling assets..." },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (url.isBlank()) {
                                            Toast.makeText(context, "Enter a valid URL to crawl and bundle offline", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isScrapingOffline = true
                                        scrapeProgressStatus = "Starting scraper..."
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                val scraper = PackoraWebScraper(context)
                                                val outDir = File(context.cacheDir, "offline_pack_${System.currentTimeMillis()}").apply { mkdirs() }
                                                val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                                val res = scraper.scrape(
                                                    PackoraWebScraper.ScrapeOptions(
                                                        targetUrl = fetchUrl,
                                                        maxDepth = 2,
                                                        maxFiles = 150,
                                                        outputDirectory = outDir
                                                    )
                                                ) { prog ->
                                                    Handler(Looper.getMainLooper()).post {
                                                        scrapedOfflineFilesCount = prog.downloadedCount
                                                        scrapeProgressStatus = "Downloaded ${prog.downloadedCount} assets (${prog.bytesDownloaded / 1024} KB)"
                                                    }
                                                }
                                                withContext(Dispatchers.Main) {
                                                    isScrapingOffline = false
                                                    if (res.isSuccess) {
                                                        Toast.makeText(context, "Offline pack ready! ${scrapedOfflineFilesCount} assets bundled.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "Scrape completed with warnings: ${res.exceptionOrNull()?.message ?: "Check URL"}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    isScrapingOffline = false
                                                    Toast.makeText(context, "Scraper error: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Outlined.DownloadForOffline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SCRAPE & BUNDLE OFFLINE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 1. Website Target URL Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Language,
                                contentDescription = "Target Website",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        placeholder = { Text("https://your-website.com", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    if (url.isNotBlank()) {
                        IconButton(onClick = { onUrlChange("") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    onUrlChange(clip.trim())
                                    Toast.makeText(context, "URL pasted from clipboard", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("PASTE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. App Identity Studio Card (Modern Unified Canvas, No Bento Boxes)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 76dp App Icon Squircle
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .clickable { iconPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val activeBitmap = displayBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
                            Image(
                                bitmap = activeBitmap.asImageBitmap(),
                                contentDescription = "App Icon",
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isFetchingIcon) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PackoraDotLoader(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // App Name & Package Info
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = appName,
                                onValueChange = {
                                    isUserEditedAppName = true
                                    onAppNameChange(it)
                                },
                                placeholder = { Text("App Name") },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val autoPkg = if (packageName.isNotBlank()) packageName else "com.maheswara660.packora.${appName.ifBlank { "app" }.trim().lowercase().replace(Regex("[^a-z0-9]"), "")}"
                            Text(
                                text = autoPkg,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                    // 3 Balanced Modern Action Buttons for Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                if (url.isNotBlank()) {
                                    coroutineScope.launch {
                                        isFetchingIcon = true
                                        val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                        fetchedIconsList = fetchAllAvailableIcons(fetchUrl)
                                        isFetchingIcon = false
                                        showMultiIconSheet = true
                                    }
                                } else {
                                    fetchedIconsList = emptyList()
                                    showMultiIconSheet = true
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Outlined.Collections, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sources", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalButton(
                            onClick = { showZoomDialog = true },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Outlined.ZoomIn, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zoom", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalButton(
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
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. Capabilities FilterChips
            Text(
                "Capabilities & Toggles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isDesktopMode,
                    onClick = {
                        onDesktopModeChange(!isDesktopMode)
                        sharedPrefs.edit().putBoolean("desktop_mode", !isDesktopMode).apply()
                    },
                    label = { Text("Desktop UA") },
                    leadingIcon = {
                        Icon(
                            if (isDesktopMode) Icons.Filled.Check else Icons.Outlined.DesktopMac,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                FilterChip(
                    selected = isForceDarkMode,
                    onClick = {
                        onForceDarkModeChange(!isForceDarkMode)
                        sharedPrefs.edit().putBoolean("force_dark_mode", !isForceDarkMode).apply()
                    },
                    label = { Text("Force Dark") },
                    leadingIcon = {
                        Icon(
                            if (isForceDarkMode) Icons.Filled.Check else Icons.Outlined.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                FilterChip(
                    selected = enableZoom,
                    onClick = {
                        onEnableZoomChange(!enableZoom)
                        sharedPrefs.edit().putBoolean("enable_zoom", !enableZoom).apply()
                    },
                    label = { Text("Pinch Zoom") },
                    leadingIcon = {
                        Icon(
                            if (enableZoom) Icons.Filled.Check else Icons.Outlined.ZoomIn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                FilterChip(
                    selected = allowCopying,
                    onClick = {
                        onAllowCopyingChange(!allowCopying)
                        sharedPrefs.edit().putBoolean("allow_copying", !allowCopying).apply()
                    },
                    label = { Text("Text Copy") },
                    leadingIcon = {
                        Icon(
                            if (allowCopying) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                FilterChip(
                    selected = isEnableWebFooter,
                    onClick = {
                        val newFooter = !isEnableWebFooter
                        onEnableWebFooterChange(newFooter)
                        sharedPrefs.edit()
                            .putBoolean("enable_web_footer", newFooter)
                            .putBoolean("hide_web_footer", !newFooter)
                            .apply()
                    },
                    label = { Text("Enable Footers") },
                    leadingIcon = {
                        Icon(
                            if (isEnableWebFooter) Icons.Filled.Check else Icons.Outlined.CallToAction,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // 4. Grouped Accordion Cards (Play Store Style)
            Text(
                "Build & Packaging Configuration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Accordion 1: Package Identity & Versioning
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPackageIdentityExpanded = !isPackageIdentityExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Package Identity & Versioning", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (packageName.isNotBlank()) "$packageName (${if (versionName.isNotBlank()) "v$versionName" else "v1.0.0"})" else "Automatic Package & Version",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (isPackageIdentityExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isPackageIdentityExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            OutlinedTextField(
                                value = packageName,
                                onValueChange = onPackageNameChange,
                                label = { Text("Package Name") },
                                placeholder = { Text("com.company.app") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = versionCode,
                                    onValueChange = onVersionCodeChange,
                                    label = { Text("Version Code") },
                                    placeholder = { Text("1") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                OutlinedTextField(
                                    value = versionName,
                                    onValueChange = onVersionNameChange,
                                    label = { Text("Version Name") },
                                    placeholder = { Text("1.0.0") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Accordion 2: Output Storage Folder
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isStorageFolderExpanded = !isStorageFolderExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Output Storage Folder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (useCustomDownloadFolder && customDownloadFolder.isNotBlank()) customDownloadFolder else "Downloads/Packora (Default)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (isStorageFolderExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isStorageFolderExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Button(
                                onClick = { folderPickerLauncher.launch(null) },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SELECT FOLDER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Accordion: Privacy & Fingerprint Disguise
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPrivacyExpanded = !isPrivacyExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy & Fingerprint Disguise", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (disguiseFingerprint) "Active • 50+ Spoofed Hardware Vectors" else "Standard Privacy Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (disguiseFingerprint) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (isPrivacyExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isPrivacyExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ConfigToggleRow(
                                title = "Disguise Device & Browser Fingerprint",
                                subtitle = "Injects stealth JS anti-fingerprinting spoofers into all WebViews",
                                checked = disguiseFingerprint,
                                onCheckedChange = { onDisguiseFingerprintChange(it) }
                            )

                            AnimatedVisibility(visible = disguiseFingerprint) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ConfigToggleRow(
                                        title = "Mask Canvas 2D Hash",
                                        subtitle = "Adds micro-jitter to prevent canvas image profiling",
                                        checked = maskCanvas,
                                        onCheckedChange = { maskCanvas = it }
                                    )
                                    ConfigToggleRow(
                                        title = "Mask WebGL GPU Vendor & Renderer",
                                        subtitle = "Emulates Qualcomm Adreno 750 / ANGLE GPU strings",
                                        checked = maskWebGL,
                                        onCheckedChange = { maskWebGL = it }
                                    )
                                    ConfigToggleRow(
                                        title = "Mask AudioContext Signatures",
                                        subtitle = "Perturbs oscillator frequency response against audio fingerprinting",
                                        checked = maskAudioContext,
                                        onCheckedChange = { maskAudioContext = it }
                                    )
                                    ConfigToggleRow(
                                        title = "Mask DOM ClientRects & Subpixel",
                                        subtitle = "Adds micro-jitter to font metrics and element bounding boxes",
                                        checked = maskClientRects,
                                        onCheckedChange = { maskClientRects = it }
                                    )
                                    ConfigToggleRow(
                                        title = "Block WebRTC Local IP Leaks",
                                        subtitle = "Stops STUN/TURN queries from exposing private IP addresses",
                                        checked = maskWebRtcIp,
                                        onCheckedChange = { maskWebRtcIp = it }
                                    )
                                    ConfigToggleRow(
                                        title = "Clear Cookies & Cache on Exit",
                                        subtitle = "Wipes all session storage and browsing history when closed",
                                        checked = clearDataOnExit,
                                        onCheckedChange = { clearDataOnExit = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Accordion: Ad-Block & Tracker Defense
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAdBlockExpanded = !isAdBlockExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Block, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ad-Block & Tracker Defense", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (adBlockEnabled) "Ad-Blocker Active • Cosmetic Hiding On" else "Ad-Blocker Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (adBlockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (isAdBlockExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isAdBlockExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ConfigToggleRow(
                                title = "High-Speed Ad-Blocker",
                                subtitle = "Intercepts network requests against known ad and banner domains",
                                checked = adBlockEnabled,
                                onCheckedChange = { onAdBlockEnabledChange(it) }
                            )

                            AnimatedVisibility(visible = adBlockEnabled) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ConfigToggleRow(
                                        title = "Block Telemetry & Trackers",
                                        subtitle = "Filters analytics, telemetry beacons, and cross-site profilers",
                                        checked = blockTrackers,
                                        onCheckedChange = { blockTrackers = it }
                                    )
                                    ConfigToggleRow(
                                        title = "Cosmetic Element Suppression",
                                        subtitle = "Hides empty ad frames and banner placeholders via CSS styling",
                                        checked = cosmeticFiltering,
                                        onCheckedChange = { cosmeticFiltering = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Accordion: Network & DNS-over-HTTPS
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isNetworkExpanded = !isNetworkExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Network & DNS-over-HTTPS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                selectedDnsProvider.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (isNetworkExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isNetworkExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDnsProviderSheet = true }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Encrypted DNS Provider", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(selectedDnsProvider.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (selectedDnsProvider == PackoraDnsProvider.CUSTOM) {
                                OutlinedTextField(
                                    value = customDohUrl,
                                    onValueChange = { customDohUrl = it },
                                    label = { Text("Custom DoH Query URL") },
                                    placeholder = { Text("https://example.com/dns-query") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            ConfigToggleRow(
                                title = "Strict DNS-over-HTTPS",
                                subtitle = "Forces all hostname lookups through encrypted resolver, preventing plaintext DNS leaks",
                                checked = strictDoh,
                                onCheckedChange = { strictDoh = it }
                            )

                            ConfigToggleRow(
                                title = "Encrypted Client Hello (ECH)",
                                subtitle = "Encrypts TLS Server Name Indication (SNI) against network eavesdroppers",
                                checked = enableEch,
                                onCheckedChange = { enableEch = it }
                            )
                        }
                    }
                }
            }

            // Accordion 3: Signing Keystore
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isKeystoreExpanded = !isKeystoreExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Signing Keystore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (useCustomKeystore && keyAlias.isNotBlank()) "Custom: $keyAlias" else "Packora Default Play Signing Key",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (isKeystoreExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isKeystoreExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            ConfigToggleRow(
                                title = "Per-App Isolated Keystore",
                                subtitle = "Derives an isolated, permanent 3072-bit RSA keystore unique to this package name",
                                checked = perAppSigningEnabled,
                                onCheckedChange = { onPerAppSigningEnabledChange(it) },
                                enabled = !useCustomKeystore
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = keystorePassword,
                                    onValueChange = {
                                        keystorePassword = it
                                        useCustomKeystore = it.isNotBlank() || keyAlias.isNotBlank()
                                    },
                                    label = { Text("Store Password") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = keyAlias,
                                    onValueChange = {
                                        keyAlias = it
                                        useCustomKeystore = it.isNotBlank() || keystorePassword.isNotBlank()
                                    },
                                    label = { Text("Key Alias") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = keyPassword,
                                    onValueChange = { keyPassword = it },
                                    label = { Text("Key Password (Opt)") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = validityYears,
                                    onValueChange = { validityYears = it },
                                    label = { Text("Validity (Years)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            OutlinedTextField(
                                value = commonName,
                                onValueChange = { commonName = it },
                                label = { Text("Common Name (Author)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = organization,
                                    onValueChange = { organization = it },
                                    label = { Text("Organization") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = organizationalUnit,
                                    onValueChange = { organizationalUnit = it },
                                    label = { Text("Org Unit") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

        }

        // Anchored Compile Button Dock matching UpdatesScreen
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    thickness = 0.5.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
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
                                        enableWebFooter = isEnableWebFooter,
                                        hideWebFooter = !isEnableWebFooter,
                                        keystorePassword = if (useCustomKeystore && keystorePassword.isNotBlank()) keystorePassword else null,
                                        keyAlias = if (useCustomKeystore && keyAlias.isNotBlank()) keyAlias else null,
                                        commonName = if (useCustomKeystore && commonName.isNotBlank()) commonName else null,
                                        organization = if (useCustomKeystore && organization.isNotBlank()) organization else null,
                                        organizationalUnit = if (useCustomKeystore && organizationalUnit.isNotBlank()) organizationalUnit else null,
                                        validityYears = validityYears.toIntOrNull() ?: 25,
                                        keyPassword = if (useCustomKeystore && keyPassword.isNotBlank()) keyPassword else null,
                                        privacyConfig = PackoraPrivacyConfig(
                                            disguiseFingerprint = disguiseFingerprint,
                                            maskCanvas = maskCanvas,
                                            maskWebGL = maskWebGL,
                                            maskAudioContext = maskAudioContext,
                                            maskClientRects = maskClientRects,
                                            maskWebRtcIp = maskWebRtcIp,
                                            clearDataOnExit = clearDataOnExit
                                        ),
                                        adBlockConfig = PackoraAdBlockConfig(
                                            enabled = adBlockEnabled,
                                            blockTrackers = blockTrackers,
                                            cosmeticFiltering = cosmeticFiltering
                                        ),
                                        networkConfig = PackoraNetworkConfig(
                                            dohProvider = selectedDnsProvider,
                                            customDohUrl = customDohUrl,
                                            strictDoh = strictDoh,
                                            enableEch = enableEch
                                        ),
                                        perAppSigningEnabled = perAppSigningEnabled,
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
                                                delay(10)
                                            }
                                            delay(200)
                                            isBuilding = false
                                            if (resultPath != null) {
                                                lastBuiltApkPath = resultPath
                                                lastBuiltPackageName = finalPackage
                                                lastBuiltAppName = appName.ifBlank { "My App" }
                                                lastBuiltVersionName = finalName
                                                lastBuiltVersionCode = finalCode
                                                showSuccessDialog = true

                                                val itemId = UUID.randomUUID().toString()
                                                var savedIconPath: String? = null
                                                if (inputBitmap != null) {
                                                    try {
                                                        val iconsDir = File(context.filesDir, "history_icons").apply { mkdirs() }
                                                        val iconFile = File(iconsDir, "$itemId.png")
                                                        FileOutputStream(iconFile).use { out ->
                                                            inputBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
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
                                                        isForceDarkMode = isForceDarkMode,
                                                        enableZoom = enableZoom,
                                                        enableWebFooter = isEnableWebFooter,
                                                        hideWebFooter = !isEnableWebFooter,
                                                        apkPath = resultPath,
                                                        iconPath = savedIconPath,
                                                        disguiseFingerprint = disguiseFingerprint,
                                                        adBlockEnabled = adBlockEnabled,
                                                        dohProvider = selectedDnsProvider.name,
                                                        perAppSigning = perAppSigningEnabled
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
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isBuilding && url.isNotBlank()
                    ) {
                        if (isBuilding) {
                            PackoraDotLoader(
                                color = MaterialTheme.colorScheme.onPrimary,
                                size = 22.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                "COMPILING ${animatedProgressPercent}%",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Icon(
                                Icons.Outlined.RocketLaunch,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "COMPILE WEBAPK",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

        // Zoom dialog
        if (showZoomDialog) {
            IconZoomerBottomSheet(
                currentBitmap = displayBitmap,
                onDismiss = { showZoomDialog = false },
                onApply = { editedBitmap ->
                    iconUri = null
                    onAutoFetchedIconBitmapChange(editedBitmap)
                    Toast.makeText(context, "Zoomed icon applied", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Multi-icon sources picker
        if (showMultiIconSheet) {
            MultiIconPickerSheet(
                icons = fetchedIconsList,
                onDismiss = { showMultiIconSheet = false },
                onSelectIcon = { selectedBmp ->
                    iconUri = null
                    onAutoFetchedIconBitmapChange(selectedBmp)
                    if (selectedBmp == null) {
                        Toast.makeText(context, "Icon reset to default mascot", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Selected icon applied", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Encrypted DNS Provider Selection Sheet
        if (showDnsProviderSheet) {
            DnsProviderBottomSheetDialog(
                currentProvider = selectedDnsProvider,
                onDismiss = { showDnsProviderSheet = false },
                onConfirm = { provider ->
                    onDnsProviderChange(provider)
                    showDnsProviderSheet = false
                }
            )
        }

        // Reset form confirmation bottom sheet
        if (showClearConfirmSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            LaunchedEffect(Unit) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
            }
            ModalBottomSheet(
                onDismissRequest = { showClearConfirmSheet = false },
                sheetState = sheetState,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
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
                            text = "Reset All Details?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Reset all form inputs, website details, custom package settings, keystores, and advanced options to default?",
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
                                onAutoFetchedIconBitmapChange(null)
                                iconSourceIndex = 0

                                onBrowserEngineChange("INDIVIDUAL")
                                onDesktopModeChange(false)
                                onForceDarkModeChange(false)
                                onEnableZoomChange(false)
                                onAllowCopyingChange(false)
                                onEnableWebFooterChange(false)
                                selectedAppType = PackoraAppType.WEB

                                useCustomDownloadFolder = false
                                customDownloadFolder = ""

                                onDisguiseFingerprintChange(false)
                                maskCanvas = true
                                maskWebGL = true
                                maskAudioContext = true
                                maskClientRects = true
                                maskWebRtcIp = true
                                clearDataOnExit = false

                                onAdBlockEnabledChange(false)
                                blockTrackers = true
                                cosmeticFiltering = true

                                onDnsProviderChange(PackoraDnsProvider.SYSTEM)
                                customDohUrl = ""
                                strictDoh = false
                                enableEch = false

                                onPerAppSigningEnabledChange(true)
                                useCustomKeystore = false
                                keystorePassword = ""
                                keyAlias = ""
                                keyPassword = ""
                                commonName = ""
                                organization = ""
                                organizationalUnit = ""
                                validityYears = "25"

                                isScrapingOffline = false
                                scrapeProgressStatus = ""
                                scrapedOfflineFilesCount = 0

                                isPackageIdentityExpanded = false
                                isStorageFolderExpanded = false
                                isKeystoreExpanded = false
                                isPrivacyExpanded = false
                                isAdBlockExpanded = false
                                isNetworkExpanded = false

                                sharedPrefs.edit()
                                    .putBoolean("desktop_mode", false)
                                    .putBoolean("force_dark_mode", false)
                                    .putBoolean("enable_zoom", false)
                                    .putBoolean("allow_copying", false)
                                    .putBoolean("enable_web_footer", false)
                                    .putBoolean("hide_web_footer", true)
                                    .putBoolean("use_custom_download", false)
                                    .remove("custom_download_folder")
                                    .apply()

                                showClearConfirmSheet = false
                                Toast.makeText(context, "All details cleared and reset", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("RESET ALL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Success dialog
        if (showSuccessDialog && lastBuiltApkPath != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            LaunchedEffect(Unit) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
            }
            ModalBottomSheet(
                onDismissRequest = { showSuccessDialog = false },
                sheetState = sheetState,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
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
                            text = "Your custom WebAPK is ready for installation.",
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
                            val activeIconBitmap = displayBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
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
                                    text = lastBuiltAppName.ifBlank { appName.ifBlank { "Web App" } },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lastBuiltPackageName.ifBlank { packageName.ifBlank { "com.web.app" } },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("v${lastBuiltVersionName.ifBlank { versionName.ifBlank { "1.0.0" } }} (${lastBuiltVersionCode})", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("Verified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                                        leadingIcon = { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary) }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showSuccessDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("CLOSE", fontWeight = FontWeight.Bold)
                        }

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
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Outlined.Android, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INSTALL", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Icon Zoomer & Color Eyedropper Bottom Sheet
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconZoomerBottomSheet(
    currentBitmap: Bitmap?,
    onDismiss: () -> Unit,
    onApply: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }

    var scaleFactor by remember { mutableFloatStateOf(1.0f) }

    val baseBitmap = remember(currentBitmap) {
        currentBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
    }

    val iconPalette = remember(baseBitmap) { extractIconColorPalette(baseBitmap) }
    var customColor by remember { mutableStateOf<Color?>(null) }
    var showCustomHexSheet by remember { mutableStateOf(false) }

    val colorOptions = remember(iconPalette, customColor) {
        val list = mutableListOf<Pair<String, Color>>()
        if (iconPalette.autoBackgroundColor != null) {
            list.add("✨ Auto Match" to iconPalette.autoBackgroundColor)
        }
        if (iconPalette.brandColor != null && iconPalette.brandColor != iconPalette.autoBackgroundColor) {
            list.add("✨ Brand Color" to iconPalette.brandColor)
        }
        if (iconPalette.accentColor != null && iconPalette.accentColor != iconPalette.autoBackgroundColor && iconPalette.accentColor != iconPalette.brandColor) {
            list.add("✨ Accent Color" to iconPalette.accentColor)
        }
        if (customColor != null) {
            list.add("🎨 Custom Color" to customColor!!)
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

    if (!showCustomHexSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ZoomIn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Icon Zoomer & Background Fill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Scale icon size, auto-match background, or pick custom colors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(22.dp))
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

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Zoom Scale", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${(scaleFactor * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Decrease by one only (-)
                        FilledTonalIconButton(
                            onClick = {
                                val currentPercent = (scaleFactor * 100).roundToInt()
                                if (currentPercent > 40) {
                                    scaleFactor = (currentPercent - 1) / 100f
                                }
                            },
                            enabled = (scaleFactor * 100).roundToInt() > 40,
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = "Decrease Zoom Scale",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Scaler Slider with points at every 10 points (40% to 200% -> 15 steps between)
                        Slider(
                            value = scaleFactor,
                            onValueChange = { scaleFactor = (it * 100).roundToInt() / 100f },
                            valueRange = 0.4f..2.0f,
                            steps = 15,
                            modifier = Modifier.weight(1f)
                        )

                        // Increase by one only (+)
                        FilledTonalIconButton(
                            onClick = {
                                val currentPercent = (scaleFactor * 100).roundToInt()
                                if (currentPercent < 200) {
                                    scaleFactor = (currentPercent + 1) / 100f
                                }
                            },
                            enabled = (scaleFactor * 100).roundToInt() < 200,
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Increase Zoom Scale",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Background Color Fill", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { showCustomHexSheet = true }) {
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
    } else {
        EnterHexColorBottomSheet(
            initialColor = customColor ?: (if (selectedColorIndex < colorOptions.size) colorOptions[selectedColorIndex].second else null),
            onDismiss = { showCustomHexSheet = false },
            onColorConfirmed = { chosenColor ->
                customColor = chosenColor
                showCustomHexSheet = false
                selectedColorIndex = 0
            }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// Multi-Icon Picker Sheet
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiIconPickerSheet(
    icons: List<FetchedIconItem>,
    onDismiss: () -> Unit,
    onSelectIcon: (Bitmap?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }
    val context = LocalContext.current
    val mascotBitmap = remember(context) { ApkBuilder.getDefaultMascotIcon(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
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
                    text = "Choose from default mascot or highest quality icons fetched from website sources.",
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
                // First Item: Default Mascot (Reset option)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .size(110.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                            .clickable {
                                onSelectIcon(null)
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
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Image(
                                    bitmap = mascotBitmap.asImageBitmap(),
                                    contentDescription = "Default Mascot",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RestartAlt,
                                        contentDescription = "Reset Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Default Mascot",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(icons.size) { idx ->
                    val item = icons[idx]
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .size(110.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
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

// -------------------------------------------------------------------------------------------------
// Icon Scrapers and Processors
// -------------------------------------------------------------------------------------------------

data class FetchedIconItem(
    val sourceName: String,
    val bitmap: Bitmap
)

fun getAppNameFromUrl(url: String): String {
    return try {
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        val uri = Uri.parse(cleanUrl)
        val host = uri.host ?: return ""
        if (!host.contains(".") || host.length < 4) return ""
        val parts = host.split(".").filter { it.isNotBlank() }
        if (parts.size < 2) return ""
        val domain = if (parts[0] == "www" && parts.size >= 3) {
            parts[1]
        } else if (parts[0] == "www") {
            parts.getOrNull(1) ?: parts[0]
        } else {
            parts[0]
        }
        if (domain.isBlank() || domain.length < 2) return ""
        domain.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    } catch (e: Exception) {
        ""
    }
}

suspend fun fetchPremiumIconWithSource(urlString: String, sourceIndex: Int = 0): Pair<Bitmap?, String> = withContext(Dispatchers.IO) {
    try {
        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext Pair(null, "Unknown")
        val scheme = uri.scheme ?: "https"

        when (sourceIndex % 7) {
            0 -> {
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
        try {
            val analysis = com.maheswara660.packora.analyzer.PackoraWebAnalyzer.analyze(urlString)
            for (icon in analysis.icons) {
                val bmp = downloadAndValidate1To1Bitmap(icon.url)
                if (bmp != null) {
                    val label = if (icon.size > 0) "${icon.source} (${icon.size}x${icon.size})" else icon.source
                    results.add(FetchedIconItem(label, bmp))
                }
            }
        } catch (e: Exception) {}

        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext emptyList()
        val scheme = uri.scheme ?: "https"

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

            val ogRegex = Regex("<meta[^>]+property=\"[^\"]*og:image[^\"]*\"[^>]+content=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
            val ogMatch = ogRegex.find(html)
            if (ogMatch != null) {
                var ogUrl = ogMatch.groupValues[1].trim()
                if (ogUrl.startsWith("//")) ogUrl = "$scheme:$ogUrl"
                else if (!ogUrl.startsWith("http")) ogUrl = if (ogUrl.startsWith("/")) "$scheme://$host$ogUrl" else "$scheme://$host/$ogUrl"
                val ogBmp = downloadAndValidate1To1Bitmap(ogUrl)
                if (ogBmp != null) results.add(FetchedIconItem("OpenGraph Image", ogBmp))
            }
        } catch (e: Exception) {}

        val appleTouch = downloadAndValidate1To1Bitmap("$scheme://$host/apple-touch-icon.png")
        if (appleTouch != null) results.add(FetchedIconItem("Apple Touch Icon", appleTouch))
        val applePrecomposed = downloadAndValidate1To1Bitmap("$scheme://$host/apple-touch-icon-precomposed.png")
        if (applePrecomposed != null) results.add(FetchedIconItem("Apple Precomposed", applePrecomposed))

        val googleBmp = downloadAndValidate1To1Bitmap("https://www.google.com/s2/favicons?domain=$host&sz=256")
        if (googleBmp != null) results.add(FetchedIconItem("Google Favicon", googleBmp))

        val clearbit = downloadAndValidate1To1Bitmap("https://logo.clearbit.com/$host")
        if (clearbit != null) results.add(FetchedIconItem("Clearbit Logo", clearbit))

        val ddgBmp = downloadAndValidate1To1Bitmap("https://icons.duckduckgo.com/ip3/$host.ico")
        if (ddgBmp != null) results.add(FetchedIconItem("DuckDuckGo Icon", ddgBmp))

        val yandexBmp = downloadAndValidate1To1Bitmap("https://favicon.yandex.net/favicon/$host?size=120")
        if (yandexBmp != null) results.add(FetchedIconItem("Yandex Favicon", yandexBmp))

        val unavatarBmp = downloadAndValidate1To1Bitmap("https://unavatar.io/$host")
        if (unavatarBmp != null) results.add(FetchedIconItem("Unavatar API", unavatarBmp))

        val rootFavicon = downloadAndValidate1To1Bitmap("$scheme://$host/favicon.ico")
        if (rootFavicon != null) results.add(FetchedIconItem("Root Favicon.ico", rootFavicon))

    } catch (e: Exception) {}
    return@withContext results.distinctBy { "${it.bitmap.width}x${it.bitmap.height}" }
}

data class IconPalette(
    val autoBackgroundColor: Color? = null,
    val brandColor: Color? = null,
    val accentColor: Color? = null
)

fun extractIconColorPalette(bitmap: Bitmap?): IconPalette {
    if (bitmap == null) return IconPalette()
    return try {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return IconPalette()

        // 1. Edge & Perimeter Color Detection (Inward 3% to 6% to bypass antialiased edge & masks)
        val edgeInsetX = maxOf(1, (w * 0.04f).toInt())
        val edgeInsetY = maxOf(1, (h * 0.04f).toInt())

        val edgeColors = mutableListOf<Int>()
        // Top and bottom edges
        for (x in edgeInsetX until (w - edgeInsetX) step maxOf(1, w / 40)) {
            edgeColors.add(bitmap.getPixel(x, edgeInsetY))
            edgeColors.add(bitmap.getPixel(x, h - 1 - edgeInsetY))
        }
        // Left and right edges
        for (y in edgeInsetY until (h - edgeInsetY) step maxOf(1, h / 40)) {
            edgeColors.add(bitmap.getPixel(edgeInsetX, y))
            edgeColors.add(bitmap.getPixel(w - 1 - edgeInsetX, y))
        }

        fun quantize(color: Int): Int {
            val a = android.graphics.Color.alpha(color)
            val r = android.graphics.Color.red(color) and 0xF8
            val g = android.graphics.Color.green(color) and 0xF8
            val b = android.graphics.Color.blue(color) and 0xF8
            return android.graphics.Color.argb(a, r, g, b)
        }

        val opaqueEdgePixels = edgeColors.filter { android.graphics.Color.alpha(it) > 180 }
        var detectedBgColor: Color? = null

        if (opaqueEdgePixels.isNotEmpty() && opaqueEdgePixels.size.toFloat() / edgeColors.size > 0.4f) {
            val edgeFreq = opaqueEdgePixels.groupBy { quantize(it) }.maxByOrNull { it.value.size }
            if (edgeFreq != null && edgeFreq.value.size.toFloat() / opaqueEdgePixels.size > 0.35f) {
                detectedBgColor = Color(edgeFreq.value.first())
            }
        }

        // 2. Foreground & Brand Color Extraction from inner core (15% to 85%)
        val innerStartX = (w * 0.15f).toInt()
        val innerEndX = (w * 0.85f).toInt()
        val innerStartY = (h * 0.15f).toInt()
        val innerEndY = (h * 0.85f).toInt()

        val stepX = maxOf(1, (innerEndX - innerStartX) / 30)
        val stepY = maxOf(1, (innerEndY - innerStartY) / 30)

        val innerColors = mutableListOf<Int>()
        for (x in innerStartX until innerEndX step stepX) {
            for (y in innerStartY until innerEndY step stepY) {
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.alpha(pixel) > 180) {
                    innerColors.add(pixel)
                }
            }
        }

        fun colorDistance(c1: Int, c2: Int): Double {
            val dr = android.graphics.Color.red(c1) - android.graphics.Color.red(c2)
            val dg = android.graphics.Color.green(c1) - android.graphics.Color.green(c2)
            val db = android.graphics.Color.blue(c1) - android.graphics.Color.blue(c2)
            return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
        }

        val bgInt = detectedBgColor?.toArgb()
        val foregroundColors = if (bgInt != null) {
            innerColors.filter { colorDistance(it, bgInt) > 40.0 }
        } else {
            innerColors
        }

        var detectedBrandColor: Color? = null
        var detectedAccentColor: Color? = null

        if (foregroundColors.isNotEmpty()) {
            val sortedBuckets = foregroundColors
                .groupBy { quantize(it) }
                .entries
                .sortedByDescending { it.value.size }

            if (sortedBuckets.isNotEmpty()) {
                val primaryEntry = sortedBuckets.first()
                detectedBrandColor = Color(primaryEntry.value.first())

                val primaryInt = primaryEntry.value.first()
                val secondaryEntry = sortedBuckets.drop(1).firstOrNull {
                    colorDistance(it.value.first(), primaryInt) > 60.0 &&
                    (bgInt == null || colorDistance(it.value.first(), bgInt) > 40.0)
                }
                if (secondaryEntry != null && secondaryEntry.value.size >= 4) {
                    detectedAccentColor = Color(secondaryEntry.value.first())
                }
            }
        }

        IconPalette(
            autoBackgroundColor = detectedBgColor,
            brandColor = detectedBrandColor,
            accentColor = detectedAccentColor
        )
    } catch (e: Exception) {
        IconPalette()
    }
}

fun extractDominantCornerColor(bitmap: Bitmap?): Color {
    return extractIconColorPalette(bitmap).autoBackgroundColor ?: Color.Transparent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterHexColorBottomSheet(
    initialColor: Color? = null,
    onDismiss: () -> Unit,
    onColorConfirmed: (Color) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var hexInput by remember {
        mutableStateOf(
            initialColor?.let {
                String.format("#%06X", 0xFFFFFF and it.toArgb())
            } ?: "#"
        )
    }
    val context = LocalContext.current
    val parsedColor: Color? = remember(hexInput) {
        try {
            val clean = hexInput.trim()
            if (clean.length in 4..9 && clean.startsWith("#")) {
                Color(android.graphics.Color.parseColor(clean))
            } else null
        } catch (e: Exception) {
            null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 24.dp)
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
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Enter Hex Color",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Specify a custom hex color code for your icon background fill",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Live Color Preview Chip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = parsedColor ?: MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (parsedColor != null) {
                        val isLight = (0.299f * parsedColor.red + 0.587f * parsedColor.green + 0.114f * parsedColor.blue) > 0.5f
                        Text(
                            text = hexInput.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color.Black else Color.White
                        )
                    } else {
                        Text(
                            text = "Color Preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    val clean = input.filter { it.isLetterOrDigit() || it == '#' }
                    hexInput = if (!clean.startsWith("#")) "#$clean" else clean
                },
                label = { Text("Hex Code") },
                placeholder = { Text("#FFFFFF or #1E88E5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(Icons.Outlined.Colorize, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (parsedColor != null) {
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                            onColorConfirmed(parsedColor)
                        } else {
                            Toast.makeText(context, "Please enter a valid hex color (e.g. #FF5722)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = parsedColor != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("SET COLOR", fontWeight = FontWeight.Bold)
                }
            }
        }
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

@Composable
fun ConfigToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.8f else 0.4f)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        PackoraIosSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsProviderBottomSheetDialog(
    currentProvider: PackoraDnsProvider,
    onDismiss: () -> Unit,
    onConfirm: (PackoraDnsProvider) -> Unit
) {
    var tempSelection by remember(currentProvider) { mutableStateOf(currentProvider) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
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
                    imageVector = Icons.Outlined.Dns,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Encrypted DNS Provider",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Select a secure DNS-over-HTTPS resolver for this WebAPK",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            val listModifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState())

            Column(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PackoraDnsProvider.entries.forEach { provider ->
                    val isSelected = tempSelection == provider
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                tempSelection = provider
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    tempSelection = provider
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = provider.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (provider.dohUrl.isNotBlank()) {
                                    Text(
                                        text = provider.dohUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        onConfirm(tempSelection)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("APPLY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
