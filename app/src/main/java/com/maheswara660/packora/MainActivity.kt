package com.maheswara660.packora

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.maheswara660.packora.builder.ApkBuilder
import com.maheswara660.packora.ui.theme.CustomAccents
import com.maheswara660.packora.ui.theme.PackoraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class Screen {
    WELCOME,
    HOME,
    SETTINGS,
    ABOUT,
    HOW_TO_USE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("packora_prefs", Context.MODE_PRIVATE)

        setContent {
            var themeConfig by remember {
                mutableStateOf(sharedPrefs.getString("theme_config", "SYSTEM") ?: "SYSTEM")
            }
            var accentColorIndex by remember {
                mutableStateOf(sharedPrefs.getInt("accent_color_index", -1)) // Default to Dynamic Material You
            }
            var currentScreen by remember {
                mutableStateOf(
                    if (sharedPrefs.getBoolean("show_welcome", true)) Screen.WELCOME else Screen.HOME
                )
            }
            PackoraTheme(themeConfig = themeConfig, accentColorIndex = accentColorIndex) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.WELCOME -> {
                            WelcomeScreen(
                                onContinueClick = {
                                    currentScreen = Screen.HOW_TO_USE
                                }
                            )
                        }
                        Screen.HOW_TO_USE -> {
                            HowToUseScreen(
                                isFromWelcome = true,
                                onNavigateBack = {
                                    sharedPrefs.edit().putBoolean("show_welcome", false).apply()
                                    currentScreen = Screen.HOME
                                }
                            )
                        }
                        Screen.HOME -> {
                            HomeScreen(
                                onNavigateTo = { screen -> currentScreen = screen }
                            )
                        }
                        Screen.SETTINGS -> {
                            SettingsScreen(
                                themeConfig = themeConfig,
                                accentColorIndex = accentColorIndex,
                                onThemeChange = { theme ->
                                    sharedPrefs.edit().putString("theme_config", theme).apply()
                                    themeConfig = theme
                                },
                                onAccentChange = { index ->
                                    sharedPrefs.edit().putInt("accent_color_index", index).apply()
                                    accentColorIndex = index
                                },
                                onNavigateToAbout = { currentScreen = Screen.ABOUT },
                                onNavigateToHowToUse = { currentScreen = Screen.HOW_TO_USE },
                                onNavigateBack = { currentScreen = Screen.HOME }
                            )
                        }
                        Screen.ABOUT -> {
                            AboutScreen(
                                onNavigateBack = { currentScreen = Screen.SETTINGS }
                            )
                        }

                    }
                }
            }
        }
    }
}

// Universal rule to avoid navigation bars padding on Gestural mode, but apply it in Button mode
fun Modifier.navigationPaddingIfButtonNavigation(context: Context): Modifier {
    val isGestureMode = try {
        Settings.Secure.getInt(context.contentResolver, "navigation_mode") == 2
    } catch (e: Exception) {
        false
    }
    return if (isGestureMode) this else this.navigationBarsPadding()
}

@Composable
fun PackoraScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val cornerRadius = 24.dp
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                topBar()
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                content(PaddingValues(0.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            floatingActionButton()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackoraTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        modifier = modifier,
        windowInsets = WindowInsets(0.dp), // Set to 0.dp to allow PackoraScreen to handle status bars padding cleanly
        navigationIcon = { navigationIcon?.invoke() },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent, 
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun WelcomeScreen(onContinueClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var visible by remember { mutableStateOf(false) }

    val assetBitmap = remember {
        try {
            context.assets.open("ws_app_icon.png").use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream).asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    val gradientBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp)
            .navigationPaddingIfButtonNavigation(context)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn(tween(400)) + androidx.compose.animation.slideInVertically(tween(400)) { -40 }
                ) {
                    if (assetBitmap != null) {
                        Image(
                            bitmap = assetBitmap,
                            contentDescription = null,
                            modifier = Modifier.size(144.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(144.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn(tween(400, 200)) + androidx.compose.animation.slideInVertically(tween(400, 200)) { 40 }
                ) {
                    Text(
                        text = "Packora",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn(tween(400, 400))
                ) {
                    Text(
                        text = "Convert websites into high-performance, signed Android APKs directly on your device.",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn(tween(400, 600)) + androidx.compose.animation.expandVertically(tween(400, 600))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Text("Web", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.SettingsSuggest, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                                Text("Build", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Android, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF10B981), modifier = Modifier.size(32.dp))
                                Text("APK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(0xFF10B981))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            AnimatedVisibility(
                visible = visible,
                enter = androidx.compose.animation.fadeIn(tween(400, 800)) + androidx.compose.animation.slideInVertically(tween(400, 800)) { 40 }
            ) {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("CONTINUE", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateTo: (Screen) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = context.getSharedPreferences("packora_prefs", Context.MODE_PRIVATE)
    val customExportUri = sharedPrefs.getString("custom_export_uri", null)

    var url by remember { mutableStateOf("") }
    var appName by remember { mutableStateOf("") }
    
    var isAdvancedOptionsExpanded by remember { mutableStateOf(false) }
    var packageName by remember { mutableStateOf("") }
    var versionCode by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("") }

    var useCustomDownloadFolder by remember { mutableStateOf(false) }
    var customDownloadFolder by remember { mutableStateOf("") }

    var iconUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var iconName by remember { mutableStateOf<String?>(null) }
    var autoFetchedIconBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isFetchingIcon by remember { mutableStateOf(false) }

    var useCustomKeystore by remember { mutableStateOf(false) }
    var keystorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var commonName by remember { mutableStateOf("") }

    var progress by remember { mutableStateOf(0f) }
    var isBuilding by remember { mutableStateOf(false) }
    var lastBuiltApkPath by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Auto-fetch app icon from URL (Scraping 1:1 Aspect ratio icons)
    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            val fetchUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url

            kotlinx.coroutines.delay(1000) // Debounce URL keystrokes
            isFetchingIcon = true
            try {
                val fetched = fetchPremiumIcon(fetchUrl)
                if (fetched != null) {
                    autoFetchedIconBitmap = fetched
                }
            } catch (e: Exception) {
            } finally {
                isFetchingIcon = false
            }
        }
    }

    val iconPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            iconUri = uri
            iconName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else null
            } ?: "custom_icon.png"
        }
    }

    val downloadFolderPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val path = uri.path
            if (path != null && path.contains(":")) {
                customDownloadFolder = path.substringAfter(":")
            } else {
                customDownloadFolder = "Downloads"
            }
        }
    }

    androidx.activity.compose.BackHandler(enabled = !isBuilding) {
    }

    PackoraScreen(
        topBar = {
            PackoraTopAppBar(
                title = "Packora",
                navigationIcon = {
                },
                actions = {
                    IconButton(onClick = {
                        url = ""
                        appName = ""
                        packageName = ""
                        versionCode = ""
                        versionName = ""
                        iconUri = null
                        iconName = null
                        autoFetchedIconBitmap = null
                        useCustomKeystore = false
                        keystorePassword = ""
                        keyAlias = ""
                        commonName = ""
                        useCustomDownloadFolder = false
                        customDownloadFolder = ""
                    }, enabled = !isBuilding) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.Refresh, contentDescription = "Reset Form", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { onNavigateTo(Screen.SETTINGS) }, enabled = !isBuilding) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.Dashboard, contentDescription = "Dashboard / Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(top = 8.dp)
                .navigationPaddingIfButtonNavigation(context),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Primary Details",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Website URL") },
                        placeholder = { Text("https://example.com") },
                        leadingIcon = { Icon(androidx.compose.material.icons.Icons.Outlined.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("Application Name") },
                        placeholder = { Text("My App") },
                        leadingIcon = { Icon(androidx.compose.material.icons.Icons.Outlined.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )

                    Text(
                        text = "App Icon",
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val displayBitmap = remember(iconUri, autoFetchedIconBitmap) {
                            if (iconUri != null) {
                                try {
                                    context.contentResolver.openInputStream(iconUri!!).use {
                                        android.graphics.BitmapFactory.decodeStream(it)
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            } else {
                                autoFetchedIconBitmap
                            }
                        }

                        if (isFetchingIcon) {
                            Box(
                                modifier = Modifier.size(72.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PackoraLoader(size = 20)
                            }
                        } else {
                            val finalBitmap = displayBitmap ?: ApkBuilder.getDefaultMascotIcon(context)
                            Image(
                                bitmap = finalBitmap.asImageBitmap(),
                                contentDescription = "Preview icon",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { if (!isBuilding) iconPickerLauncher.launch("image/png") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (iconUri != null) androidx.compose.ui.graphics.Color(0xFF10B981) else MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (iconUri != null) androidx.compose.ui.graphics.Color(0xFF10B981) else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = iconName ?: "Select Image")
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAdvancedOptionsExpanded = !isAdvancedOptionsExpanded }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Advanced Settings",
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Icon(
                            imageVector = if (isAdvancedOptionsExpanded) androidx.compose.material.icons.Icons.Outlined.KeyboardArrowUp else androidx.compose.material.icons.Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Expand Advanced Options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isAdvancedOptionsExpanded,
                        enter = androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.shrinkVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = packageName,
                                onValueChange = { packageName = it },
                                label = { Text("Package Name") },
                                placeholder = { Text("com.example.myapp") },
                                leadingIcon = { Icon(androidx.compose.material.icons.Icons.Outlined.SettingsSuggest, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = versionCode,
                                    onValueChange = { versionCode = it },
                                    label = { Text("Version Code") },
                                    placeholder = { Text("1") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                                )
                                OutlinedTextField(
                                    value = versionName,
                                    onValueChange = { versionName = it },
                                    label = { Text("Version Name") },
                                    placeholder = { Text("1.0.0") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                                )
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom Download Folder",
                                    fontSize = 14.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                PackoraSwitch(
                                    checked = useCustomDownloadFolder,
                                    onCheckedChange = { useCustomDownloadFolder = it }
                                )
                            }

                            if (useCustomDownloadFolder) {
                                OutlinedButton(
                                    onClick = { downloadFolderPickerLauncher.launch(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Outlined.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = if (customDownloadFolder.isNotBlank()) customDownloadFolder else "Select Download Folder")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generate Custom Keystore",
                                    fontSize = 14.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                PackoraSwitch(
                                    checked = useCustomKeystore,
                                    onCheckedChange = { useCustomKeystore = it }
                                )
                            }

                            if (useCustomKeystore) {
                                OutlinedTextField(
                                    value = keystorePassword,
                                    onValueChange = { keystorePassword = it },
                                    label = { Text("Keystore Password") },
                                    placeholder = { Text("Password to encrypt generated keystore") },
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                                )

                                OutlinedTextField(
                                    value = keyAlias,
                                    onValueChange = { keyAlias = it },
                                    label = { Text("Key Alias") },
                                    placeholder = { Text("e.g. upload-alias") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                                )

                                OutlinedTextField(
                                    value = commonName,
                                    onValueChange = { commonName = it },
                                    label = { Text("Publisher / Organization Name (Optional)") },
                                    placeholder = { Text("e.g. My Organization LLC") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                                )
                            }
                        }
                    }
                }
            }

            if (isBuilding) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PackoraLoader(size = 48, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Packaging App...",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val isFormValid = url.isNotBlank() && appName.isNotBlank()
                    Button(
                    onClick = {
                        if (appName.isBlank() && url.isNotBlank()) {
                            appName = getAppNameFromUrl(url)
                        }

                        if (url.isBlank() || appName.isBlank()) {
                            ToastMessage(context, "Please fill in URL and Application Name")
                            return@Button
                        }
                        if (useCustomKeystore && (keystorePassword.isBlank() || keyAlias.isBlank())) {
                            ToastMessage(context, "Please enter a password and alias for keystore generation")
                            return@Button
                        }

                        isBuilding = true
                        progress = 0f
                        lastBuiltApkPath = null

                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val builder = ApkBuilder(context)
                                
                                val finalPackage = if (packageName.isBlank()) {
                                    "com.maheswara660." + appName.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
                                } else {
                                    packageName.trim()
                                }

                                val urlHash = url.trim().hashCode().toString()
                                val simpleCode = sharedPrefs.getInt("simple_vcode_$urlHash", 0) + 1
                                
                                val finalCode = versionCode.trim().toIntOrNull() ?: simpleCode
                                val finalName = if (versionName.isBlank()) "1.0.${finalCode - 1}" else versionName.trim()

                                val targetFileName = "${appName.trim().replace(" ", "_")}_v${finalName}.apk"

                                val inputBitmap: android.graphics.Bitmap? = if (iconUri != null) {
                                    context.contentResolver.openInputStream(iconUri!!).use {
                                        android.graphics.BitmapFactory.decodeStream(it)
                                    }
                                } else {
                                    autoFetchedIconBitmap
                                }

                                val resultPath = builder.buildApk(
                                    appName = appName.trim(),
                                    packageName = finalPackage,
                                    targetUrl = url.trim(),
                                    versionCode = finalCode,
                                    versionName = finalName,
                                    iconBitmap = inputBitmap,
                                    disableHeader = true,
                                    outputPath = targetFileName,
                                    customDownloadFolder = if (useCustomDownloadFolder && customDownloadFolder.isNotBlank()) customDownloadFolder.trim() else null,
                                    keystorePassword = if (useCustomKeystore) keystorePassword else null,
                                    keyAlias = if (useCustomKeystore) keyAlias else null,
                                    commonName = if (useCustomKeystore && commonName.isNotBlank()) commonName else null,
                                    onProgress = { p, _ ->
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            progress = p / 100f
                                        }
                                    }
                                )

                                if (resultPath != null) {
                                    sharedPrefs.edit().putInt("simple_vcode_$urlHash", finalCode).apply()
                                }

                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    isBuilding = false
                                    if (resultPath != null) {
                                        lastBuiltApkPath = resultPath
                                        showSuccessDialog = true
                                    } else {
                                        ToastMessage(context, "Build failed. Check project structure.")
                                    }
                                }
                            } catch (e: Exception) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    isBuilding = false
                                    ToastMessage(context, "Error: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "GENERATE APP",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    if (showSuccessDialog && lastBuiltApkPath != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFF10B981),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "App Built Successfully!",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your app is ready to be installed.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Path:\n$lastBuiltApkPath",
                    fontSize = 10.sp,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        installApkFile(context, lastBuiltApkPath!!)
                        showSuccessDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("INSTALL NOW", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showSuccessDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("CLOSE")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Auto-generates app name e.g. "Google" or "News Google" from website URL
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

// 1:1 Aspect ratio premium icon fetching engine
suspend fun fetchPremiumIcon(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val uri = Uri.parse(urlString)
        val host = uri.host ?: return@withContext null
        
        val sources = listOf(
            suspend {
                try {
                    val url = java.net.URL(urlString)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.instanceFollowRedirects = true
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                    val html = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    val appleTouchRegex = Regex("<link[^>]+rel=\"(?:apple-touch-icon|shortcut icon|icon)\"[^>]+href=\"([^\"]+)\"")
                    val match = appleTouchRegex.find(html)
                    if (match != null) {
                        var iconUrl = match.groupValues[1]
                        if (!iconUrl.startsWith("http")) {
                            iconUrl = if (iconUrl.startsWith("/")) {
                                "${uri.scheme}://$host$iconUrl"
                            } else {
                                "${uri.scheme}://$host/$iconUrl"
                            }
                        }
                        downloadAndValidate1To1Bitmap(iconUrl)
                    } else null
                } catch (e: Exception) {
                    null
                }
            },
            
            suspend {
                val clearbitUrl = "https://logo.clearbit.com/$host"
                downloadAndValidate1To1Bitmap(clearbitUrl)
            },
            
            suspend {
                val iconHorseUrl = "https://icon.horse/icon/$host"
                downloadAndValidate1To1Bitmap(iconHorseUrl)
            },
            
            suspend {
                val googleFaviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=$host"
                downloadAndValidate1To1Bitmap(googleFaviconUrl)
            }
        )
        
        for (source in sources) {
            val bitmap = source()
            if (bitmap != null) return@withContext bitmap
        }
    } catch (e: Exception) {
    }
    null
}

private fun downloadAndValidate1To1Bitmap(urlStr: String): Bitmap? {
    return try {
        val url = java.net.URL(urlStr)
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.inputStream.use { stream ->
            val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                val w = bitmap.width
                val h = bitmap.height
                val aspectRatio = w.toFloat() / h.toFloat()
                // Strict 1:1 aspect ratio check allowing small variance
                if (aspectRatio in 0.95f..1.05f && w >= 16) {
                    bitmap
                } else {
                    bitmap.recycle()
                    null
                }
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeConfig: String,
    accentColorIndex: Int,
    onThemeChange: (String) -> Unit,
    onAccentChange: (Int) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToHowToUse: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    val sharedPrefs = context.getSharedPreferences("packora_prefs", android.content.Context.MODE_PRIVATE)
    var customExportUri by remember { mutableStateOf(sharedPrefs.getString("custom_export_uri", null)) }
    val folderPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            sharedPrefs.edit().putString("custom_export_uri", uri.toString()).apply()
            customExportUri = uri.toString()
        }
    }


    PackoraScreen(
        topBar = {
            PackoraTopAppBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .navigationPaddingIfButtonNavigation(context),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Appearance",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
            )

            PackoraSegmentedListItem(
                isFirstItem = true,
                isLastItem = false,
                leadingIcon = Icons.Outlined.Palette,
                title = "Theme",
                supportingText = when (themeConfig) {
                    "SYSTEM" -> "System Default"
                    "LIGHT" -> "Light"
                    "DARK" -> "Dark"
                    "AMOLED" -> "AMOLED Black"
                    else -> "System Default"
                },
                onClick = { showThemeDialog = true }
            )

            PackoraSegmentedListItem(
                isFirstItem = false,
                isLastItem = true,
                leadingIcon = Icons.Outlined.ColorLens,
                title = "Accent Color",
                supportingText = if (accentColorIndex == -1) "Dynamic (Material You)" else CustomAccents.getOrNull(accentColorIndex)?.name ?: "Teal",
                onClick = { showAccentDialog = true }
            )

            Text(
                text = "Storage",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp).padding(top = 16.dp)
            )

            PackoraSegmentedListItem(
                isFirstItem = true,
                isLastItem = true,
                leadingIcon = Icons.Outlined.Folder,
                title = "Export Location",
                supportingText = if (customExportUri != null) "Custom Directory Selected" else "Default (Downloads/Packora)",
                onClick = { folderPickerLauncher.launch(null) }
            )
            
            Text(
                text = "About",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp).padding(top = 16.dp)
            )

            PackoraSegmentedListItem(
                isFirstItem = true,
                isLastItem = true,
                leadingIcon = Icons.Outlined.Info,
                title = "About Packora",
                supportingText = "Information, licenses, and more",
                onClick = onNavigateToAbout
            )
        }
    }

    if (showThemeDialog) {
        var localThemeConfig by remember { mutableStateOf(themeConfig) }
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showThemeDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).navigationBarsPadding()
            ) {
                Text(
                    text = "Select Theme",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
                HorizontalDivider()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val themes = listOf(
                        "SYSTEM" to "System Default",
                        "LIGHT" to "Light",
                        "DARK" to "Dark",
                        "AMOLED" to "AMOLED Black"
                    )
                    
                    themes.forEachIndexed { index, pair ->
                        val isFirst = index == 0
                        val isLast = index == themes.size - 1
                        
                        val shape = RoundedCornerShape(
                            topStart = if (isFirst) 24.dp else 0.dp,
                            topEnd = if (isFirst) 24.dp else 0.dp,
                            bottomStart = if (isLast) 24.dp else 0.dp,
                            bottomEnd = if (isLast) 24.dp else 0.dp
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .clickable { localThemeConfig = pair.first },
                            shape = shape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = localThemeConfig == pair.first,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(pair.second, fontSize = 16.sp, fontWeight = if(localThemeConfig == pair.first) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showThemeDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onThemeChange(localThemeConfig)
                            showThemeDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }

    if (showAccentDialog) {
        var localAccentIndex by remember { mutableStateOf(accentColorIndex) }
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showAccentDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).navigationBarsPadding()
            ) {
                Text(
                    text = "Select Accent Color",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
                HorizontalDivider()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val size = CustomAccents.size + 1 // +1 for Dynamic
                    
                    val dynShape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(dynShape)
                            .clickable { localAccentIndex = -1 },
                        shape = dynShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = localAccentIndex == -1,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Dynamic (Material You)", fontSize = 16.sp, fontWeight = if(localAccentIndex == -1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    CustomAccents.forEachIndexed { index, accent ->
                        val isLast = index == CustomAccents.size - 1
                        
                        val shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = if (isLast) 24.dp else 0.dp,
                            bottomEnd = if (isLast) 24.dp else 0.dp
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .clickable { localAccentIndex = index },
                            shape = shape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = localAccentIndex == index,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(accent.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(accent.name, fontSize = 16.sp, fontWeight = if(localAccentIndex == index) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAccentDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onAccentChange(localAccentIndex)
                            showAccentDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun PackoraSegmentedListItem(
    isFirstItem: Boolean,
    isLastItem: Boolean,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    supportingText: String,
    onClick: () -> Unit
) {
    val cornerRadius = 24.dp
    val shape = RoundedCornerShape(
        topStart = if (isFirstItem) cornerRadius else 0.dp,
        topEnd = if (isFirstItem) cornerRadius else 0.dp,
        bottomStart = if (isLastItem) cornerRadius else 0.dp,
        bottomEnd = if (isLastItem) cornerRadius else 0.dp
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = supportingText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PackoraSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(51.dp)
            .height(31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (onCheckedChange != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled
                    ) {
                        onCheckedChange.invoke(!checked)
                    }
                } else Modifier
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
        }
    }
}

@Composable
fun PackoraLoader(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    size: Int = 40
) {
    val loaderColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    val infiniteTransition = rememberInfiniteTransition(label = "packoraLoader")
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        repeat(8) { index ->
            PackoraDot(
                index = index,
                totalDots = 8,
                color = loaderColor,
                size = size,
                infiniteTransition = infiniteTransition
            )
        }
    }
}

@Composable
private fun PackoraDot(
    index: Int,
    totalDots: Int,
    color: Color,
    size: Int,
    infiniteTransition: InfiniteTransition
) {
    val delay = (index * 125)
    val duration = 1000
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration / 2, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale_$index"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration / 2, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha_$index"
    )

    Box(
        modifier = Modifier
            .size(size.dp)
            .graphicsLayer {
                rotationZ = index * (360f / totalDots)
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size((size / 5).dp)
                .scale(scale)
                .alpha(alpha)
                .background(color, CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    PackoraScreen(
        topBar = {
            PackoraTopAppBar(
                title = "About",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            AboutApp()
            
            Spacer(modifier = Modifier.height(32.dp))

            DeveloperCard()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Connect & Support",
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            AboutActionItem(
                icon = Icons.Outlined.Code,
                title = "GitHub Repository",
                description = "View the source code and contribute",
                isFirstItem = true,
                onClick = {
                    uriHandler.openUri("https://github.com/maheswara660/Packora")
                }
            )

            AboutActionItem(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Support Development",
                description = "Donate to help me continue developing Packora",
                onClick = {
                    uriHandler.openUri("https://ko-fi.com/maheswara660")
                }
            )

            AboutActionItem(
                icon = Icons.Outlined.Description,
                title = "Open Source License",
                description = "View the project's license",
                isLastItem = true,
                onClick = {
                    uriHandler.openUri("https://github.com/maheswara660/Packora/blob/main/LICENSE")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "© 2026 Maheswara660\nMade with ❤️ for the community",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AboutApp() {
    val context = LocalContext.current
    val appVersion = remember { context.appVersion() }
    
    val appIconBitmap = remember {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap().asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .padding(vertical = 32.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (appIconBitmap != null) {
            Image(
                bitmap = appIconBitmap,
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Layers,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Packora",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "v$appVersion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DeveloperCard() {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                uriHandler.openUri("https://github.com/maheswara660")
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Developed by Maheswara660",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Passionate Android Developer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun AboutActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(
            topStart = if (isFirstItem) 16.dp else 4.dp,
            topEnd = if (isFirstItem) 16.dp else 4.dp,
            bottomStart = if (isLastItem) 16.dp else 4.dp,
            bottomEnd = if (isLastItem) 16.dp else 4.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

fun Context.appVersion(): String {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: "1.1.0"
    } catch (e: Exception) {
        "1.1.0"
    }
}

fun copyUriToCacheFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val cacheFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream != null) {
                cacheFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                cacheFile
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

fun ToastMessage(context: Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
}
fun installApkFile(context: android.content.Context, pathOrUri: String) {
    try {
        val uri = if (pathOrUri.startsWith("content://")) {
            val contentUri = android.net.Uri.parse(pathOrUri)
            val tempFile = java.io.File(context.cacheDir, "temp_install.apk")
            context.contentResolver.openInputStream(contentUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            androidx.core.content.FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                tempFile
            )
        } else {
            val file = java.io.File(pathOrUri)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "File does not exist", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            androidx.core.content.FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error installing APK: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HowToUseScreen(isFromWelcome: Boolean, onNavigateBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val contentPadding = if (isFromWelcome) PaddingValues(24.dp) else PaddingValues(16.dp)
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { visible = true }



    val contentBody = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .navigationPaddingIfButtonNavigation(context),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFromWelcome) {
                Spacer(modifier = Modifier.height(32.dp))
                AnimatedVisibility(visible = visible, enter = androidx.compose.animation.fadeIn(tween(300)) + androidx.compose.animation.slideInVertically { -20 }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedVisibility(visible = visible, enter = androidx.compose.animation.fadeIn(tween(300, 100)) + androidx.compose.animation.slideInVertically { -20 }) {
                    Text(
                        text = "How to Use Packora",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val instructions = listOf(
                Pair(Icons.Outlined.Public, "Enter the full URL of the website you want to convert into an app (e.g. https://google.com)."),
                Pair(Icons.Outlined.Image, "Provide an Application Name. Packora will automatically fetch a logo for you, or you can upload a custom 1:1 icon."),
                Pair(Icons.Outlined.Settings, "Configure Advanced Settings if you need a custom Package Name, Version Code, or a Custom Keystore for Google Play."),
                Pair(Icons.Outlined.Android, "Tap Generate App! Once finished, you can install the APK directly from the success menu.")
            )

            instructions.forEachIndexed { index, step ->
                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn(tween(300, delayMillis = 200 + (index * 150))) + androidx.compose.animation.slideInHorizontally(tween(300, delayMillis = 200 + (index * 150))) { 40 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.first,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = step.second,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isFromWelcome) {
                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn(tween(300, 1000)) + androidx.compose.animation.slideInVertically(tween(300, 1000)) { 40 }
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }

    if (isFromWelcome) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            contentBody()
        }
    } else {
        PackoraScreen(
            topBar = {
                PackoraTopAppBar(
                    title = "How to Use",
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                contentBody()
            }
        }
    }
}
