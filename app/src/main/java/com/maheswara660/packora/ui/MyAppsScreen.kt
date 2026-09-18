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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.incrementVersionString
import com.maheswara660.packora.manager.BuildHistoryManager
import com.maheswara660.packora.manager.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
fun MyAppsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val historyManager = remember { BuildHistoryManager(context) }

    var apps by remember { mutableStateOf<List<InstalledPackoraApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val buildProgress = remember { mutableStateMapOf<String, Int?>() }
    val buildingPackages = remember { mutableStateListOf<String>() }

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

    val updateCount = apps.count { it.hasUpdate }

    Scaffold(
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
                    AnimatedVisibility(
                        visible = updateCount > 0 && buildingPackages.isEmpty(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Button(
                            onClick = {
                                val toUpdate = apps.filter { it.hasUpdate && it.historyItem != null }
                                toUpdate.forEach { app ->
                                    val item = app.historyItem!!
                                    coroutineScope.launch {
                                        buildAndInstall(
                                            context = context,
                                            historyManager = historyManager,
                                            item = item,
                                            onProgressUpdate = { buildProgress[app.packageName] = it },
                                            onBuildingChange = { b ->
                                                if (b) buildingPackages.add(app.packageName)
                                                else buildingPackages.remove(app.packageName)
                                            },
                                            onDone = {
                                                coroutineScope.launch {
                                                    apps = detectInstalledPackoraApps(context, historyManager)
                                                }
                                            }
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Update All ($updateCount)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Scanning installed apps…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            apps.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
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
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (updateCount > 0) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(8.dp))
                            Text("Updates available", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    items(apps.filter { it.hasUpdate }) { app ->
                        AppCard(
                            app = app,
                            progress = buildProgress[app.packageName],
                            isBuilding = app.packageName in buildingPackages,
                            onUpdate = {
                                val item = app.historyItem ?: return@AppCard
                                coroutineScope.launch {
                                    buildAndInstall(
                                        context, historyManager, item,
                                        onProgressUpdate = { buildProgress[app.packageName] = it },
                                        onBuildingChange = { b ->
                                            if (b) buildingPackages.add(app.packageName)
                                            else buildingPackages.remove(app.packageName)
                                        },
                                        onDone = {
                                            coroutineScope.launch {
                                                apps = detectInstalledPackoraApps(context, historyManager)
                                            }
                                        }
                                    )
                                }
                            },
                            onOpen = { openApp(context, app.packageName) },
                            onUninstall = { uninstallApp(context, app.packageName) }
                        )
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Up to date", fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                items(apps.filter { !it.hasUpdate }) { app ->
                    AppCard(app = app, progress = buildProgress[app.packageName],
                        isBuilding = app.packageName in buildingPackages,
                        onUpdate = null,
                        onOpen = { openApp(context, app.packageName) },
                        onUninstall = { uninstallApp(context, app.packageName) })
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    app: InstalledPackoraApp,
    progress: Int?,
    isBuilding: Boolean,
    onUpdate: (() -> Unit)?,
    onOpen: () -> Unit,
    onUninstall: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progress ?: 0) / 100f,
        animationSpec = tween(80, easing = LinearEasing), label = "progress"
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.hasUpdate)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.icon != null) {
                        Image(app.icon.asImageBitmap(), app.appName,
                            Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)))
                    } else {
                        Icon(Icons.Rounded.Android, null, Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false))
                        if (app.hasUpdate) {
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                                Text("Update", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(app.packageName, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Text("v${app.installedVersionName}", fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        if (app.hasUpdate && app.historyItem != null) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Rounded.ArrowForward, null, Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Surface(shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                                Text("v${app.historyItem.versionName}", fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = isBuilding) {
                Column(Modifier.padding(top = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Compiling…", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("${progress ?: 0}%", fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            }
            if (!isBuilding) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpen, shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.OpenInNew, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Open", fontSize = 13.sp)
                    }
                    if (onUpdate != null) {
                        Button(onClick = onUpdate, shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Refresh, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Update", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    IconButton(onClick = onUninstall,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Outlined.DeleteOutline, "Uninstall", Modifier.size(20.dp))
                    }
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
                val installedVC = pkg.versionCode
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
            val resultPath = builder.buildApk(
                appName = item.appName,
                packageName = item.packageName,
                targetUrl = item.targetUrl,
                versionCode = newVersionCode,
                versionName = newVersionName,
                iconBitmap = null,
                disableHeader = true,
                outputPath = outputName,
                customDownloadFolder = null,
                isDesktopMode = item.isDesktopMode,
                browserEngine = item.browserEngine,
                allowCopying = item.allowCopying,
                isForceDarkMode = false,
                enableZoom = false,
                hideWebFooter = true,
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
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.provider", apkFile)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = uri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tap to install manually from Downloads", Toast.LENGTH_LONG).show()
    }
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
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) { Toast.makeText(context, "Could not uninstall", Toast.LENGTH_SHORT).show() }
}
