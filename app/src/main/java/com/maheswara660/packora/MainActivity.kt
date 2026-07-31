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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.ui.theme.PackoraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PackoraTheme {
                PackoraDashboard()
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PackoraDashboard() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("packora_prefs", Context.MODE_PRIVATE)

    var url by remember { mutableStateOf("") }
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var versionCode by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("") }
    
    var isDesktopMode by remember { mutableStateOf(false) }
    var useCustomDownloadFolder by remember { mutableStateOf(sharedPrefs.getBoolean("use_custom_download", false)) }
    var customDownloadFolder by remember { mutableStateOf(sharedPrefs.getString("custom_download_folder", "") ?: "") }
    
    var iconUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var iconName by remember { mutableStateOf<String?>(null) }
    var autoFetchedIconBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isFetchingIcon by remember { mutableStateOf(false) }

    var useCustomKeystore by remember { mutableStateOf(false) }
    var keystorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var commonName by remember { mutableStateOf("") }

    var isBuilding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastBuiltApkPath by remember { mutableStateOf<String?>(null) }
    
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            
            delay(1200)
            isFetchingIcon = true
            try {
                val fetched = fetchPremiumIcon(fetchUrl)
                if (fetched != null) autoFetchedIconBitmap = fetched
            } catch (e: Exception) {} finally { isFetchingIcon = false }
        }
    }

    val iconPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Packora", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Live Preview Hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.5f), RoundedCornerShape(20.dp))
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
                                    Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
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
                    }
                }

                // Website Details
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
                            onValueChange = { url = it },
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
                            onValueChange = { appName = it },
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

                // Toggles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDesktopMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDesktopMode) 8.dp else 4.dp),
                        modifier = Modifier.weight(1f).aspectRatio(1f).border(1.dp, if (isDesktopMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).clickable {
                            isDesktopMode = !isDesktopMode
                            sharedPrefs.edit().putBoolean("desktop_mode", isDesktopMode).apply()
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

                // Advanced Options Inline Expandable
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
                                Text("Advanced Security & Identity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Package name, Versioning, Custom Keystore", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(
                                if (isAdvancedExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(visible = isAdvancedExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(value = packageName, onValueChange = { packageName = it }, label = { Text("Custom Package Name") }, placeholder = { Text("com.maheswara660.packora.app") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    OutlinedTextField(value = versionCode, onValueChange = { versionCode = it }, label = { Text("Version Code") }, placeholder = { Text("1") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                                    OutlinedTextField(value = versionName, onValueChange = { versionName = it }, label = { Text("Version Name") }, placeholder = { Text("2.0.0") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                
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
            // Compile Button (Moved under Advanced Settings)
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
                                val finalName = versionName.ifBlank { "2.0.0" }
                                
                                val inputBitmap: Bitmap? = if (iconUri != null) {
                                    context.contentResolver.openInputStream(iconUri!!).use {
                                        android.graphics.BitmapFactory.decodeStream(it)
                                    }
                                } else autoFetchedIconBitmap

                                val resultPath = builder.buildApk(
                                    appName = appName.ifBlank { "My App" },
                                    packageName = finalPackage,
                                    targetUrl = url,
                                    versionCode = finalCode,
                                    versionName = finalName,
                                    iconBitmap = inputBitmap,
                                    disableHeader = true,
                                    outputPath = "${appName.replace(" ", "_")}.apk",
                                    customDownloadFolder = if (useCustomDownloadFolder) customDownloadFolder else null,
                                    isDesktopMode = isDesktopMode,
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isBuilding && url.isNotBlank() && appName.isNotBlank()
                ) {
                    if (isBuilding) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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
                            val cachedApk = java.io.File(context.cacheDir, "built_app.apk")
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
    try {
        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext null

        val sources = listOf(
            suspend label@{
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
                        if (bmp != null) return@label bmp
                    }
                    null
                } catch (e: Exception) {
                    null
                }
            },
            suspend { downloadAndValidate1To1Bitmap("https://icons.duckduckgo.com/ip3/$host.ico") },
            suspend { downloadAndValidate1To1Bitmap("https://www.google.com/s2/favicons?domain=$host&sz=256") },
            suspend { downloadAndValidate1To1Bitmap("https://$host/apple-touch-icon.png") },
            suspend { downloadAndValidate1To1Bitmap("https://$host/apple-touch-icon-precomposed.png") },
            suspend { downloadAndValidate1To1Bitmap("https://$host/favicon.ico") }
        )

        for (source in sources) {
            val bitmap = source()
            if (bitmap != null) return@withContext bitmap
        }
        null
    } catch (e: Exception) {
        null
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
        pInfo.versionName ?: "2.0.0"
    } catch (e: Exception) {
        "2.0.0"
    }
}

fun copyUriToCacheFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun ToastMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun installApkFile(context: Context, pathOrUri: String) {
    try {
        val uri = if (pathOrUri.startsWith("content://")) {
            Uri.parse(pathOrUri)
        } else {
            val file = File(pathOrUri)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        ToastMessage(context, "Could not launch installer. APK is saved at: $pathOrUri")
    }
}
