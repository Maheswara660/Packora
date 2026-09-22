package com.maheswara660.packora.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.incrementVersionString
import com.maheswara660.packora.installApkFile
import com.maheswara660.packora.manager.BuildHistoryManager
import androidx.compose.animation.*
import com.maheswara660.packora.installer.PackageInstallerHelper
import com.maheswara660.packora.manager.HistoryItem
import com.maheswara660.packora.manager.PackoraPreferencesManager
import com.maheswara660.packora.manager.UpdateInstallMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val historyManager = remember { BuildHistoryManager(context) }
    val prefsManager = remember { PackoraPreferencesManager(context) }

    var apps by remember { mutableStateOf<List<InstalledPackoraApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val buildingPackages = remember { mutableStateListOf<String>() }
    val buildProgress = remember { mutableStateMapOf<String, Int>() }
    val compiledApkPaths = remember { mutableStateMapOf<String, String>() }

    var isUpdatingAll by remember { mutableStateOf(false) }
    var updateAllProgressText by remember { mutableStateOf<String?>(null) }
    val installQueue = remember { mutableStateListOf<PendingInstallTask>() }
    var currentInstallingApp by remember { mutableStateOf<PendingInstallTask?>(null) }

    fun refreshApps() {
        coroutineScope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                val detected = detectInstalledPackoraApps(context, historyManager)
                withContext(Dispatchers.Main) {
                    apps = detected
                    // Also check if any apps already have compiled updates ready in history
                    detected.forEach { app ->
                        val historyApk = app.historyItem?.apkPath
                        if (!historyApk.isNullOrBlank() && File(historyApk).exists() &&
                            app.historyItem.versionCode > app.installedVersionCode
                        ) {
                            compiledApkPaths[app.packageName] = historyApk
                        }
                    }
                    isLoading = false
                }
            }
        }
    }

    fun advanceInstallQueue() {
        if (installQueue.isNotEmpty()) {
            val next = installQueue.removeAt(0)
            currentInstallingApp = next
            coroutineScope.launch {
                delay(600)
                val isSilent = prefsManager.updateInstallMode in listOf(
                    UpdateInstallMode.AUTOMATE_ALL,
                    UpdateInstallMode.UPDATE_ALL_ONLY
                )
                PackageInstallerHelper.installPackage(
                    context = context,
                    apkPath = next.apkPath,
                    packageName = next.packageName,
                    appName = next.appName,
                    silent = isSilent
                )
            }
        } else {
            currentInstallingApp = null
            Toast.makeText(context, "All updates installed successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    // BroadcastReceiver listening for completed package installations
    DisposableEffect(context, currentInstallingApp) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == Intent.ACTION_PACKAGE_REPLACED || action == Intent.ACTION_PACKAGE_ADDED) {
                    val data = intent.data?.schemeSpecificPart
                    val active = currentInstallingApp
                    if (data != null) {
                        compiledApkPaths.remove(data)
                        if (active != null && data == active.packageName) {
                            advanceInstallQueue()
                        }
                        refreshApps()
                    }
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        }
        androidx.core.content.ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (e: Exception) {}
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val active = currentInstallingApp
                if (active != null) {
                    try {
                        val pInfo = context.packageManager.getPackageInfo(active.packageName, 0)
                        val installedVC = PackageInfoCompat.getLongVersionCode(pInfo).toInt()
                        if (installedVC >= active.targetVersionCode) {
                            compiledApkPaths.remove(active.packageName)
                            advanceInstallQueue()
                        }
                    } catch (e: Exception) {}
                }
                refreshApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Apps that have updates available or have an active compiled APK ready
    val updateEligibleApps = remember(apps, compiledApkPaths.keys.toSet()) {
        apps.filter { it.hasUpdate || compiledApkPaths.containsKey(it.packageName) || it.historyItem != null }
    }

    fun triggerSingleUpdate(app: InstalledPackoraApp) {
        val item = app.historyItem ?: return
        if (app.packageName in buildingPackages) return

        coroutineScope.launch {
            buildingPackages.add(app.packageName)
            buildProgress[app.packageName] = 1

            val newVersionCode = (app.installedVersionCode + 1).coerceAtLeast(item.versionCode + 1)
            val newVersionName = incrementVersionString(app.installedVersionName)
            val outputName = "${item.packageName}_v${newVersionCode}.apk"

            val isCancelled = AtomicBoolean(false)
            val tickerJob = launch {
                for (targetPercent in 2..99) {
                    if (isCancelled.get()) break
                    delay(30)
                    buildProgress[app.packageName] = targetPercent
                }
            }

            var generatedApk: String? = null
            withContext(Dispatchers.IO) {
                val builder = ApkBuilder(context)
                val prefsManager = PackoraPreferencesManager(context)
                val customFolder = if (prefsManager.useCustomStorageFolder) prefsManager.customStorageFolder else null
                val inputBitmap = if (!item.iconPath.isNullOrBlank() && File(item.iconPath).exists()) {
                    try { BitmapFactory.decodeFile(item.iconPath) } catch (e: Exception) { null }
                } else null

                generatedApk = builder.buildApk(
                    appName = item.appName,
                    packageName = item.packageName,
                    targetUrl = item.targetUrl,
                    versionCode = newVersionCode,
                    versionName = newVersionName,
                    iconBitmap = inputBitmap,
                    disableHeader = true,
                    outputPath = outputName,
                    customDownloadFolder = customFolder,
                    isDesktopMode = item.isDesktopMode,
                    browserEngine = item.browserEngine,
                    allowCopying = item.allowCopying,
                    isForceDarkMode = item.isForceDarkMode,
                    enableZoom = item.enableZoom,
                    enableWebFooter = item.enableWebFooter,
                    hideWebFooter = !item.enableWebFooter,
                    keystorePassword = null,
                    keyAlias = null,
                    commonName = null,
                    organization = null,
                    organizationalUnit = null,
                    validityYears = 25,
                    keyPassword = null
                )
            }

            isCancelled.set(true)
            tickerJob.cancel()
            buildProgress[app.packageName] = 100
            delay(200)

            buildingPackages.remove(app.packageName)
            buildProgress.remove(app.packageName)

            if (generatedApk != null) {
                historyManager.addHistoryItem(
                    item.copy(
                        versionCode = newVersionCode,
                        versionName = newVersionName,
                        apkPath = generatedApk
                    )
                )
                compiledApkPaths[app.packageName] = generatedApk!!
                if (prefsManager.updateInstallMode != UpdateInstallMode.COMPLETELY_MANUAL) {
                    val isSilent = prefsManager.updateInstallMode == UpdateInstallMode.AUTOMATE_ALL
                    PackageInstallerHelper.installPackage(
                        context = context,
                        apkPath = generatedApk!!,
                        packageName = app.packageName,
                        appName = app.appName,
                        silent = isSilent
                    )
                } else {
                    Toast.makeText(context, "Update compiled for ${app.appName}! Tap Install to proceed.", Toast.LENGTH_SHORT).show()
                }
                refreshApps()
            } else {
                Toast.makeText(context, "Update build failed for ${app.appName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerUpdateAll() {
        val toUpdate = updateEligibleApps.filter { it.historyItem != null }
        if (toUpdate.isEmpty()) {
            Toast.makeText(context, "No WebAPKs with build configs to update", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            isUpdatingAll = true
            installQueue.clear()

            toUpdate.forEachIndexed { index, app ->
                val item = app.historyItem ?: return@forEachIndexed
                updateAllProgressText = "Building ${app.appName} (${index + 1}/${toUpdate.size})..."

                val newVersionCode = (app.installedVersionCode + 1).coerceAtLeast(item.versionCode + 1)
                val newVersionName = incrementVersionString(app.installedVersionName)
                val outputName = "${item.packageName}_v${newVersionCode}.apk"

                var finalApk: String? = null
                withContext(Dispatchers.IO) {
                    val builder = ApkBuilder(context)
                    val prefsManager = PackoraPreferencesManager(context)
                    val customFolder = if (prefsManager.useCustomStorageFolder) prefsManager.customStorageFolder else null
                    val inputBitmap = if (!item.iconPath.isNullOrBlank() && File(item.iconPath).exists()) {
                        try { BitmapFactory.decodeFile(item.iconPath) } catch (e: Exception) { null }
                    } else null

                    finalApk = builder.buildApk(
                        appName = item.appName,
                        packageName = item.packageName,
                        targetUrl = item.targetUrl,
                        versionCode = newVersionCode,
                        versionName = newVersionName,
                        iconBitmap = inputBitmap,
                        disableHeader = true,
                        outputPath = outputName,
                        customDownloadFolder = customFolder,
                        isDesktopMode = item.isDesktopMode,
                        browserEngine = item.browserEngine,
                        allowCopying = item.allowCopying,
                        isForceDarkMode = item.isForceDarkMode,
                        enableZoom = item.enableZoom,
                        enableWebFooter = item.enableWebFooter,
                        hideWebFooter = !item.enableWebFooter,
                        keystorePassword = null,
                        keyAlias = null,
                        commonName = null,
                        organization = null,
                        organizationalUnit = null,
                        validityYears = 25,
                        keyPassword = null
                    )
                }

                if (finalApk != null) {
                    historyManager.addHistoryItem(
                        item.copy(
                            versionCode = newVersionCode,
                            versionName = newVersionName,
                            apkPath = finalApk
                        )
                    )
                    installQueue.add(
                        PendingInstallTask(
                            packageName = app.packageName,
                            appName = app.appName,
                            apkPath = finalApk!!,
                            targetVersionCode = newVersionCode
                        )
                    )
                }
            }

            isUpdatingAll = false
            updateAllProgressText = null

            if (prefsManager.updateInstallMode == UpdateInstallMode.COMPLETELY_MANUAL) {
                Toast.makeText(context, "All updates compiled! Tap 'Install' on each card to install.", Toast.LENGTH_LONG).show()
                refreshApps()
            } else {
                if (installQueue.isNotEmpty()) {
                    advanceInstallQueue()
                } else {
                    Toast.makeText(context, "No packages could be compiled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(AppSortMode.NAME_AZ) }
    var showSortSheet by remember { mutableStateOf(false) }

    val filteredApps = remember(updateEligibleApps, searchQuery, sortMode) {
        var list = if (searchQuery.isBlank()) updateEligibleApps
        else updateEligibleApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
        when (sortMode) {
            AppSortMode.NAME_AZ -> list.sortedBy { it.appName.lowercase() }
            AppSortMode.NAME_ZA -> list.sortedByDescending { it.appName.lowercase() }
            AppSortMode.NEWEST -> list.sortedByDescending { it.historyItem?.timestamp ?: 0L }
            AppSortMode.OLDEST -> list.sortedBy { it.historyItem?.timestamp ?: 0L }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Updates", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        if (!isLoading) {
                            Text(
                                if (updateEligibleApps.isEmpty()) "All apps up to date"
                                else "${updateEligibleApps.size} update${if (updateEligibleApps.size == 1) "" else "s"} available",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                searchQuery = ""
                            } else {
                                isSearchActive = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Outlined.Close else Icons.Outlined.Search,
                            contentDescription = if (isSearchActive) "Close Search" else "Open Search"
                        )
                    }

                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort Updates")
                    }

                    IconButton(
                        onClick = {
                            refreshApps()
                            Toast.makeText(context, "Refreshed updates", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Updates")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(visible = isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search updates or packages...") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = "Clear search text")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Scanning for updates…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                updateEligibleApps.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "All Apps Up to Date",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Installed WebAPKs are running the latest version and runtime.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                filteredApps.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No updates matching '$searchQuery'", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Check spelling or try a different app name.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sequential Update Available Banner
                        item(key = "update_all_banner") {
                            val shouldHideSkipButton = prefsManager.updateInstallMode in listOf(
                                UpdateInstallMode.AUTOMATE_ALL,
                                UpdateInstallMode.UPDATE_ALL_ONLY
                            )
                            val isBatchSilent = shouldHideSkipButton

                            UpdatesAvailableBanner(
                                updateCount = updateEligibleApps.size,
                                isUpdatingAll = isUpdatingAll,
                                progressText = updateAllProgressText,
                                currentInstallingApp = currentInstallingApp,
                                queueSize = installQueue.size,
                                onUpdateAll = {
                                    if (currentInstallingApp != null) {
                                        PackageInstallerHelper.installPackage(
                                            context = context,
                                            apkPath = currentInstallingApp!!.apkPath,
                                            packageName = currentInstallingApp!!.packageName,
                                            appName = currentInstallingApp!!.appName,
                                            silent = isBatchSilent
                                        )
                                    } else {
                                        triggerUpdateAll()
                                    }
                                },
                                onSkipCurrentInstall = if (shouldHideSkipButton) null else { { advanceInstallQueue() } }
                            )
                        }

                        items(filteredApps, key = { it.packageName }) { app ->
                            val isBuilding = app.packageName in buildingPackages
                            val readyApkPath = compiledApkPaths[app.packageName]
                            val isReadyToInstall = !readyApkPath.isNullOrBlank() && File(readyApkPath).exists()
                            val shouldHideInstallButton = prefsManager.updateInstallMode == UpdateInstallMode.AUTOMATE_ALL &&
                                    prefsManager.autoDeleteApkAfterInstall

                            UpdateAppCard(
                                app = app,
                                isBuilding = isBuilding,
                                progress = buildProgress[app.packageName],
                                isReadyToInstall = isReadyToInstall,
                                hideInstallButton = shouldHideInstallButton,
                                onUpdate = { triggerSingleUpdate(app) },
                                onInstall = {
                                    readyApkPath?.let {
                                        val isSingleSilent = prefsManager.updateInstallMode == UpdateInstallMode.AUTOMATE_ALL
                                        PackageInstallerHelper.installPackage(
                                            context = context,
                                            apkPath = it,
                                            packageName = app.packageName,
                                            appName = app.appName,
                                            silent = isSingleSilent
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showSortSheet) {
            SelectionBottomSheetDialog(
                title = "Sort Updates",
                subtitle = "Choose how your available updates are arranged",
                icon = Icons.AutoMirrored.Outlined.Sort,
                options = listOf(
                    AppSortMode.NAME_AZ to "App Name (A-Z)",
                    AppSortMode.NAME_ZA to "App Name (Z-A)",
                    AppSortMode.NEWEST to "Recently Built",
                    AppSortMode.OLDEST to "Oldest Built"
                ),
                initialSelection = sortMode,
                isScrollable = false,
                onDismiss = { showSortSheet = false },
                onConfirm = { selected ->
                    sortMode = selected
                    showSortSheet = false
                }
            )
        }
    }
}

@Composable
private fun UpdateAppCard(
    app: InstalledPackoraApp,
    isBuilding: Boolean,
    progress: Int?,
    isReadyToInstall: Boolean,
    hideInstallButton: Boolean = false,
    onUpdate: () -> Unit,
    onInstall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isBuilding) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.icon != null) {
                        Image(
                            app.icon.asImageBitmap(),
                            app.appName,
                            Modifier.size(54.dp).clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Android,
                            null,
                            Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        app.packageName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                "Installed: v${app.installedVersionName}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (app.historyItem != null) {
                            val targetVer = app.historyItem.versionCode.coerceAtLeast(app.installedVersionCode + 1)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "Target: ($targetVer)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (app.historyItem != null) {
                        Spacer(Modifier.height(4.dp))
                        CompileSettingsBadges(app.historyItem)
                    }
                }
            }

            // Compilation Progress Bar
            AnimatedVisibility(
                visible = isBuilding,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Compiling WebAPK...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${progress ?: 0}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (progress ?: 0) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(Modifier.height(10.dp))

            // Action Buttons
            if (isReadyToInstall && !hideInstallButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onUpdate,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(Icons.Rounded.Update, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onInstall,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(Icons.Outlined.InstallMobile, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Install", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onUpdate,
                    enabled = !isBuilding,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    if (isBuilding) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("COMPILING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Rounded.Update, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("UPDATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun UpdatesAvailableBanner(
    updateCount: Int,
    isUpdatingAll: Boolean,
    progressText: String?,
    currentInstallingApp: PendingInstallTask?,
    queueSize: Int,
    onUpdateAll: () -> Unit,
    onSkipCurrentInstall: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Update,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isUpdatingAll) "Compiling Updates"
                        else if (currentInstallingApp != null) "Installing Updates"
                        else "Updates Available ($updateCount)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isUpdatingAll) (progressText ?: "Compiling in background...")
                        else if (currentInstallingApp != null) "Installing ${currentInstallingApp.appName} (${queueSize + 1} in queue)"
                        else "Update your installed WebAPKs sequentially",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isUpdatingAll) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            } else if (currentInstallingApp != null) {
                if (onSkipCurrentInstall != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onUpdateAll,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Outlined.InstallMobile, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PROMPT INSTALLER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onSkipCurrentInstall,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Outlined.SkipNext, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Skip", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Button(
                        onClick = onUpdateAll,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Icon(Icons.Outlined.InstallMobile, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PROMPT INSTALLER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onUpdateAll,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(Icons.Rounded.Update, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("UPDATE ALL ($updateCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
