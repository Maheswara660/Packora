package com.maheswara660.packora.ui

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
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
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.incrementVersionString
import com.maheswara660.packora.installApkFile
import com.maheswara660.packora.manager.BuildHistoryManager
import com.maheswara660.packora.manager.HistoryItem
import com.maheswara660.packora.manager.PackoraPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SingleUpdateReady(
    val app: InstalledPackoraApp,
    val apkPath: String,
    val newVersionCode: Int,
    val newVersionName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val historyManager = remember { BuildHistoryManager(context) }

    var apps by remember { mutableStateOf<List<InstalledPackoraApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val buildProgress = remember { mutableStateMapOf<String, Int?>() }
    val buildingPackages = remember { mutableStateListOf<String>() }
    var isUpdatingAll by remember { mutableStateOf(false) }

    // Bottom sheet state for single app update ready
    var singleUpdateReady by remember { mutableStateOf<SingleUpdateReady?>(null) }

    // Search and Sort
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(AppSortMode.NAME_AZ) }
    var showSortSheet by remember { mutableStateOf(false) }

    fun refreshApps() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            val detected = detectInstalledPackoraApps(context, historyManager)
            withContext(Dispatchers.Main) {
                apps = detected
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch(Dispatchers.IO) {
                    val detected = detectInstalledPackoraApps(context, historyManager)
                    withContext(Dispatchers.Main) {
                        apps = detected
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredApps = remember(apps, searchQuery, sortMode) {
        var list = apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
        list = when (sortMode) {
            AppSortMode.NAME_AZ -> list.sortedBy { it.appName.lowercase() }
            AppSortMode.NEWEST -> list.sortedByDescending { it.historyItem?.timestamp ?: 0L }
            AppSortMode.UPDATES_FIRST -> list.sortedWith(
                compareByDescending<InstalledPackoraApp> { it.hasUpdate }
                    .thenBy { it.appName.lowercase() }
            )
        }
        list
    }

    // Single App Update Function (Shows Bottom Sheet on completion)
    fun triggerSingleUpdate(app: InstalledPackoraApp) {
        val item = app.historyItem ?: return
        coroutineScope.launch {
            buildingPackages.add(app.packageName)
            buildProgress[app.packageName] = 0
            withContext(Dispatchers.IO) {
                try {
                    val builder = ApkBuilder(context)
                    val prefsManager = PackoraPreferencesManager(context)
                    val customFolder = prefsManager.customStorageFolder
                    val effectiveFolder = if (!customFolder.isNullOrBlank() && File(customFolder).exists()) {
                        customFolder
                    } else null

                    val newVersionCode = item.versionCode + 1
                    val newVersionName = incrementVersionString(item.versionName)
                    val outputName = "${item.appName.replace(" ", "_")}_update.apk"
                    val inputBitmap = if (!item.iconPath.isNullOrBlank() && File(item.iconPath).exists()) {
                        try { android.graphics.BitmapFactory.decodeFile(item.iconPath) } catch (e: Exception) { null }
                    } else {
                        app.icon
                    }

                    val resultPath = builder.buildApk(
                        appName = item.appName,
                        packageName = item.packageName,
                        targetUrl = item.targetUrl,
                        versionCode = newVersionCode,
                        versionName = newVersionName,
                        iconBitmap = inputBitmap,
                        disableHeader = item.disableHeader,
                        outputPath = outputName,
                        customDownloadFolder = effectiveFolder,
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
                        keyPassword = null,
                        onProgress = { pct, _ -> Handler(Looper.getMainLooper()).post { buildProgress[app.packageName] = pct } }
                    )
                    withContext(Dispatchers.Main) { buildProgress[app.packageName] = 100 }
                    if (resultPath != null) {
                        historyManager.addHistoryItem(
                            item.copy(versionCode = newVersionCode, versionName = newVersionName, apkPath = resultPath)
                        )
                        withContext(Dispatchers.Main) {
                            singleUpdateReady = SingleUpdateReady(
                                app = app,
                                apkPath = resultPath,
                                newVersionCode = newVersionCode,
                                newVersionName = newVersionName
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to compile update for ${app.appName}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error updating ${app.appName}: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        buildingPackages.remove(app.packageName)
                    }
                }
            }
        }
    }

    // Update All Function (Sequentially compiles and auto-triggers native package installer)
    fun triggerUpdateAll() {
        val toUpdate = apps.filter { it.historyItem != null }
        if (toUpdate.isEmpty()) {
            Toast.makeText(context, "No installed apps with build configs to update", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            isUpdatingAll = true
            toUpdate.forEach { app ->
                val item = app.historyItem ?: return@forEach
                buildingPackages.add(app.packageName)
                buildProgress[app.packageName] = 0

                val resultPath = withContext(Dispatchers.IO) {
                    try {
                        val builder = ApkBuilder(context)
                        val prefsManager = PackoraPreferencesManager(context)
                        val customFolder = prefsManager.customStorageFolder
                        val effectiveFolder = if (!customFolder.isNullOrBlank() && File(customFolder).exists()) {
                            customFolder
                        } else null

                        val newVersionCode = item.versionCode + 1
                        val newVersionName = incrementVersionString(item.versionName)
                        val outputName = "${item.appName.replace(" ", "_")}_update.apk"
                        val inputBitmap = if (!item.iconPath.isNullOrBlank() && File(item.iconPath).exists()) {
                            try { android.graphics.BitmapFactory.decodeFile(item.iconPath) } catch (e: Exception) { null }
                        } else {
                            app.icon
                        }

                        builder.buildApk(
                            appName = item.appName,
                            packageName = item.packageName,
                            targetUrl = item.targetUrl,
                            versionCode = newVersionCode,
                            versionName = newVersionName,
                            iconBitmap = inputBitmap,
                            disableHeader = item.disableHeader,
                            outputPath = outputName,
                            customDownloadFolder = effectiveFolder,
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
                            keyPassword = null,
                            onProgress = { pct, _ -> Handler(Looper.getMainLooper()).post { buildProgress[app.packageName] = pct } }
                        )?.also { path ->
                            historyManager.addHistoryItem(
                                item.copy(versionCode = newVersionCode, versionName = newVersionName, apkPath = path)
                            )
                        }
                    } catch (e: Exception) { null }
                }

                buildingPackages.remove(app.packageName)
                buildProgress[app.packageName] = 100
            }
            isUpdatingAll = false
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "All updates compiled! You can install them individually.", Toast.LENGTH_LONG).show()
            }
            refreshApps()
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
                                "${apps.size} installed app${if (apps.size == 1) "" else "s"}",
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
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort Apps")
                    }

                    IconButton(onClick = { refreshApps(); Toast.makeText(context, "Refreshed apps", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Apps")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search installed apps…", fontSize = 14.sp) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Outlined.Close, null, Modifier.size(16.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                    }
                } else if (filteredApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Upgrade,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                if (searchQuery.isNotEmpty()) "No matching apps found" else "No Packora Apps Installed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                if (searchQuery.isNotEmpty()) "Try a different search query." else "Apps built with Packora will appear here for on-demand updating with preserved settings.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 86.dp // Padding for anchored Update All button
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            UpdateAppCard(
                                app = app,
                                progress = buildProgress[app.packageName],
                                isBuilding = app.packageName in buildingPackages,
                                onUpdate = { triggerSingleUpdate(app) }
                            )
                        }
                    }
                }
            }

            // Anchored "UPDATE ALL" button dock above bottom navbar
            if (apps.isNotEmpty() && !isLoading) {
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
                                onClick = { triggerUpdateAll() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(20.dp),
                                enabled = !isUpdatingAll && buildingPackages.isEmpty()
                            ) {
                                if (isUpdatingAll) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text("UPDATING APPS…", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                } else {
                                    Icon(Icons.Outlined.SystemUpdate, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "UPDATE ALL (${apps.size})",
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sort Bottom Sheet Dialog
    if (showSortSheet) {
        SelectionBottomSheetDialog(
            title = "Sort Installed Apps",
            subtitle = "Choose how your installed WebAPKs are arranged",
            icon = Icons.AutoMirrored.Outlined.Sort,
            options = listOf(
                AppSortMode.NEWEST to "Recently Installed",
                AppSortMode.NAME_AZ to "App Name (A-Z)",
                AppSortMode.UPDATES_FIRST to "Updates Available First"
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

    // Single Update Ready Bottom Sheet (matches Install App in Dashboard)
    if (singleUpdateReady != null) {
        val ready = singleUpdateReady!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { singleUpdateReady = null },
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
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "App Update Ready!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "The updated WebAPK for ${ready.app.appName} has been compiled and is ready to install.",
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
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ready.app.icon != null) {
                                Image(
                                    bitmap = ready.app.icon.asImageBitmap(),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Android,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ready.app.appName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ready.app.packageName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("v${ready.newVersionName} (${ready.newVersionCode})", style = MaterialTheme.typography.labelSmall) }
                                )
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Verified Update", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                                    leadingIcon = { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary) }
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Button(
                        onClick = {
                            triggerInstall(context, File(ready.apkPath))
                            singleUpdateReady = null
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Outlined.SystemUpdate, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INSTALL UPDATE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    OutlinedButton(
                        onClick = { singleUpdateReady = null },
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

// App Card in Updates Screen: Contains ONLY the Update action button
@Composable
private fun UpdateAppCard(
    app: InstalledPackoraApp,
    progress: Int?,
    isBuilding: Boolean,
    onUpdate: () -> Unit
) {
    val context = LocalContext.current
    val animatedProgress by animateFloatAsState(
        targetValue = (progress ?: 0) / 100f,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "update_progress_${app.packageName}"
    )

    val hasCompiledUpdate = app.historyItem?.apkPath != null &&
            File(app.historyItem.apkPath).exists() &&
            (app.historyItem.versionCode > app.installedVersionCode)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (hasCompiledUpdate) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.icon != null) {
                        Image(
                            bitmap = app.icon.asImageBitmap(),
                            contentDescription = app.appName,
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Android,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (hasCompiledUpdate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = if (hasCompiledUpdate) {
                                "Installed: v${app.installedVersionName} ➔ Ready: v${app.historyItem!!.versionName}"
                            } else {
                                "Installed: v${app.installedVersionName} (${app.installedVersionCode})"
                            },
                            fontSize = 10.sp,
                            fontWeight = if (hasCompiledUpdate) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasCompiledUpdate) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (app.historyItem != null) {
                        Spacer(Modifier.height(4.dp))
                        CompileSettingsBadges(app.historyItem)
                    }
                }
            }

            AnimatedVisibility(visible = isBuilding) {
                Column(Modifier.padding(top = 14.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Compiling update…", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        Text(
                            "${progress ?: 0}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            }

            if (!isBuilding) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Spacer(Modifier.height(10.dp))
                if (hasCompiledUpdate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { triggerInstall(context, File(app.historyItem!!.apkPath)) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Icon(Icons.Outlined.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "INSTALL UPDATE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        FilledTonalIconButton(
                            onClick = onUpdate,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Recompile", modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onUpdate,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            enabled = app.historyItem != null
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (app.historyItem != null) "UPDATE APP" else "NO BUILD CONFIG FOUND",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
