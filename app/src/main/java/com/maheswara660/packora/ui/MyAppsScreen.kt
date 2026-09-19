package com.maheswara660.packora.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.core.content.pm.PackageInfoCompat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.incrementVersionString
import com.maheswara660.packora.installApkFile
import com.maheswara660.packora.manager.BuildHistoryManager
import com.maheswara660.packora.manager.HistoryItem
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class AppSortMode {
    NAME_AZ, NEWEST, UPDATES_FIRST
}

data class InstalledPackoraApp(
    val packageName: String,
    val appName: String,
    val installedVersionCode: Int,
    val installedVersionName: String,
    val icon: Bitmap?,
    val historyItem: HistoryItem?,
    val hasUpdate: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppsScreen(
    onReuseConfig: ((HistoryItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val historyManager = remember { BuildHistoryManager(context) }

    var apps by remember { mutableStateOf<List<InstalledPackoraApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var appToUninstall by remember { mutableStateOf<InstalledPackoraApp?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val detected = detectInstalledPackoraApps(context, historyManager)
            withContext(Dispatchers.Main) {
                apps = detected
                isLoading = false
            }
        }
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

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(AppSortMode.NEWEST) }
    var showSortSheet by remember { mutableStateOf(false) }

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

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Apps", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        if (!isLoading) {
                            Text(
                                "${apps.size} app${if (apps.size == 1) "" else "s"} installed",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Three icons from Build History screen: Search, Sort, Refresh
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

                    IconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                isLoading = true
                                val detected = detectInstalledPackoraApps(context, historyManager)
                                withContext(Dispatchers.Main) {
                                    apps = detected
                                    isLoading = false
                                    Toast.makeText(context, "Refreshed installed apps", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Apps")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        placeholder = { Text("Search installed apps or packages...") },
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
                        Text("Scanning installed apps…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                apps.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Inventory2, null, Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("No apps yet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Apps you build with Packora will appear here.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                filteredApps.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SearchOff, null, Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text("No apps matching '$searchQuery'", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Check spelling or try a different app name.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    val compiledUpdates = remember(filteredApps) {
                        filteredApps.filter {
                            it.hasUpdate &&
                            !it.historyItem?.apkPath.isNullOrBlank() &&
                            File(it.historyItem!!.apkPath).exists() &&
                            (it.historyItem.versionCode > it.installedVersionCode)
                        }
                    }
                    val regularApps = remember(filteredApps, compiledUpdates) {
                        filteredApps.filter { it !in compiledUpdates }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (compiledUpdates.isNotEmpty()) {
                            item(key = "header_compiled_updates") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 4.dp, start = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.SystemUpdate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Compiled Updates Ready (${compiledUpdates.size})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            items(compiledUpdates, key = { "compiled_update_${it.packageName}" }) { app ->
                                CompiledUpdateCard(
                                    app = app,
                                    onInstallUpdate = { app.historyItem?.apkPath?.let { installApkFile(context, it) } },
                                    onReuseConfig = onReuseConfig,
                                    onUninstall = { appToUninstall = app }
                                )
                            }

                            if (regularApps.isNotEmpty()) {
                                item(key = "header_installed_apps") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 4.dp, start = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.Apps,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Installed Applications (${regularApps.size})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        items(regularApps, key = { it.packageName }) { app ->
                            AppCard(
                                app = app,
                                onReuseConfig = onReuseConfig,
                                onOpen = { openApp(context, app.packageName) },
                                onUninstall = { appToUninstall = app }
                            )
                        }
                    }
                }
            }
        }
    }

    if (appToUninstall != null) {
        val target = appToUninstall!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { appToUninstall = null },
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Uninstall ${target.appName}?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Are you sure you want to remove this WebAPK application from your device?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Target App Overview Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (target.icon != null) {
                                Image(
                                    bitmap = target.icon.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Android,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = target.appName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = target.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Text(
                                    text = "v${target.installedVersionName} (${target.installedVersionCode})",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Warning Notice Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "This will trigger Android's native uninstaller and delete local application data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = { appToUninstall = null },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            uninstallApp(context, target.packageName)
                            appToUninstall = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UNINSTALL", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

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
}

@Composable
fun CompileSettingsBadges(item: HistoryItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Desktop / Mobile
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = if (item.isDesktopMode) "Desktop" else "Mobile",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Dark Mode
        if (item.isForceDarkMode) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Dark Mode",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Zoom
        if (item.enableZoom) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Zoom",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Copying
        if (item.allowCopying) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Copying",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Footer Mode
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = if (item.enableWebFooter) "Footer On" else "Hide Footer",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Browser Engine
        val engineLabel = when (item.browserEngine) {
            "GECKOVIEW" -> "GeckoView"
            "CHROMIUM" -> "Chromium"
            else -> "Default"
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = engineLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CompiledUpdateCard(
    app: InstalledPackoraApp,
    onInstallUpdate: () -> Unit,
    onReuseConfig: ((HistoryItem) -> Unit)? = null,
    onUninstall: () -> Unit
) {
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
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            app.appName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "UPDATE READY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        app.packageName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Installed: v${app.installedVersionName} ➔ Ready: v${app.historyItem?.versionName ?: "Update"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (app.historyItem != null) {
                        Spacer(Modifier.height(4.dp))
                        CompileSettingsBadges(app.historyItem)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onInstallUpdate,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Outlined.InstallMobile, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("INSTALL UPDATE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                if (onReuseConfig != null && app.historyItem != null) {
                    FilledTonalIconButton(
                        onClick = { onReuseConfig(app.historyItem) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Outlined.AutoMode, contentDescription = "Reuse Config", modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(
                    onClick = onUninstall,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, "Uninstall", Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    app: InstalledPackoraApp,
    onReuseConfig: ((HistoryItem) -> Unit)? = null,
    onOpen: () -> Unit,
    onUninstall: () -> Unit
) {
    val context = LocalContext.current
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
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        app.packageName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            "v${app.installedVersionName} (${app.installedVersionCode})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (app.historyItem != null) {
                        Spacer(Modifier.height(4.dp))
                        CompileSettingsBadges(app.historyItem)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                if (onReuseConfig != null && app.historyItem != null) {
                    FilledTonalIconButton(
                        onClick = { onReuseConfig(app.historyItem) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Outlined.AutoMode, contentDescription = "Reuse Config", modifier = Modifier.size(18.dp))
                    }
                }
                if (app.historyItem?.apkPath != null && File(app.historyItem.apkPath).exists()) {
                    FilledTonalIconButton(
                        onClick = { installApkFile(context, app.historyItem.apkPath) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Outlined.InstallMobile, contentDescription = "Install APK", modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(
                    onClick = onUninstall,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, "Uninstall", Modifier.size(20.dp))
                }
            }
        }
    }
}

fun detectInstalledPackoraApps(context: Context, historyManager: BuildHistoryManager): List<InstalledPackoraApp> {
    val pm = context.packageManager
    val historyPackages = historyManager.getAllPackageNames()
    val packoraPrefix = "com.maheswara660.packora."
    return try {
        pm.getInstalledPackages(PackageManager.GET_META_DATA)
            .filter { pkg ->
                (pkg.packageName.startsWith(packoraPrefix) || pkg.packageName in historyPackages) &&
                pkg.packageName != context.packageName
            }
            .map { pkg ->
                val historyItem = historyManager.getLatestForPackage(pkg.packageName)
                val installedVC = PackageInfoCompat.getLongVersionCode(pkg).toInt()
                val historyVC = historyItem?.versionCode ?: installedVC
                val icon: Bitmap? = try {
                    pm.getApplicationIcon(pkg.packageName).toBitmap(56, 56)
                } catch (e: Exception) { null }
                InstalledPackoraApp(
                    packageName = pkg.packageName,
                    appName = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName,
                    installedVersionCode = installedVC,
                    installedVersionName = pkg.versionName ?: "1.0.0",
                    icon = icon,
                    historyItem = historyItem,
                    hasUpdate = historyVC > installedVC
                )
            }
            .sortedWith(compareByDescending<InstalledPackoraApp> { it.hasUpdate }.thenBy { it.appName })
    } catch (e: Exception) { emptyList() }
}

suspend fun buildAndInstall(
    context: Context,
    historyManager: BuildHistoryManager,
    item: HistoryItem,
    onProgressUpdate: (Int) -> Unit,
    onBuildingChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    onBuildingChange(true)
    onProgressUpdate(0)
    withContext(Dispatchers.IO) {
        try {
            val builder = ApkBuilder(context)
            val newVersionCode = item.versionCode + 1
            val newVersionName = incrementVersionString(item.versionName)
            val outputName = "${item.appName.replace(" ", "_")}_update.apk"
            val inputBitmap = if (!item.iconPath.isNullOrBlank() && java.io.File(item.iconPath).exists()) {
                try { android.graphics.BitmapFactory.decodeFile(item.iconPath) } catch (e: Exception) { null }
            } else null

            val resultPath = builder.buildApk(
                appName = item.appName,
                packageName = item.packageName,
                targetUrl = item.targetUrl,
                versionCode = newVersionCode,
                versionName = newVersionName,
                iconBitmap = inputBitmap,
                disableHeader = true,
                outputPath = outputName,
                customDownloadFolder = null,
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
                onProgress = { pct, _ -> Handler(Looper.getMainLooper()).post { onProgressUpdate(pct) } }
            )
            withContext(Dispatchers.Main) { onProgressUpdate(100) }
            if (resultPath != null) {
                // Update history with new version
                historyManager.addHistoryItem(
                    item.copy(versionCode = newVersionCode, versionName = newVersionName, apkPath = resultPath)
                )
                withContext(Dispatchers.Main) {
                    triggerInstall(context, File(resultPath))
                    onDone()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update build failed", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Build error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            withContext(Dispatchers.Main) { onBuildingChange(false) }
        }
    }
}

fun triggerInstall(context: Context, apkFile: File) {
    installApkFile(context, apkFile.absolutePath)
}

fun openApp(context: Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(intent) }
        else Toast.makeText(context, "Could not open app", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) { Toast.makeText(context, "Could not open app", Toast.LENGTH_SHORT).show() }
}

fun uninstallApp(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            Toast.makeText(context, "Uninstall from App Info", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Toast.makeText(context, "Could not uninstall: ${ex.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
