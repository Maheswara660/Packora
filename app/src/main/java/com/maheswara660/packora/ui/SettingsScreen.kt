package com.maheswara660.packora.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maheswara660.packora.manager.AppColorAccent
import com.maheswara660.packora.manager.AppThemeMode
import com.maheswara660.packora.manager.PackoraPreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateAbout: () -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onColorAccentChange: (AppColorAccent) -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PackoraPreferencesManager(context) }

    var currentTheme by remember { mutableStateOf(prefsManager.themeMode) }
    var currentAccent by remember { mutableStateOf(prefsManager.colorAccent) }
    var customStorageFolder by remember { mutableStateOf(prefsManager.customStorageFolder) }
    var useCustomStorage by remember { mutableStateOf(prefsManager.useCustomStorageFolder) }

    var showThemeSheet by remember { mutableStateOf(false) }
    var showAccentSheet by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            val path = uri.path ?: uri.toString()
            val folder = if (path.contains(":")) path.substringAfter(":") else path
            customStorageFolder = folder
            useCustomStorage = true
            prefsManager.customStorageFolder = folder
            prefsManager.useCustomStorageFolder = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Theme Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { showThemeSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        val themeLabel = when (currentTheme) {
                            AppThemeMode.SYSTEM -> "System Default"
                            AppThemeMode.LIGHT -> "Light Theme"
                            AppThemeMode.DARK -> "Dark Theme"
                            AppThemeMode.AMOLED -> "AMOLED Pitch Dark"
                        }
                        Text(themeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Color Accent Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { showAccentSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Color Accent", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        val accentLabel = when (currentAccent) {
                            AppColorAccent.SYSTEM -> "System Default (Material You)"
                            AppColorAccent.EMERALD -> "Emerald Green"
                            AppColorAccent.OCEAN -> "Ocean Blue"
                            AppColorAccent.PURPLE -> "Deep Purple"
                            AppColorAccent.AMBER -> "Warm Amber"
                            AppColorAccent.CRIMSON -> "Crimson Red"
                            AppColorAccent.ROSE -> "Rose Pink"
                            AppColorAccent.CYAN -> "Cyan Breeze"
                            AppColorAccent.ORANGE -> "Sunset Orange"
                            AppColorAccent.INDIGO -> "Midnight Indigo"
                            AppColorAccent.TEAL -> "Teal Mint"
                            AppColorAccent.LIME -> "Lime Gold"
                            AppColorAccent.CORAL -> "Coral Flame"
                            AppColorAccent.NEON_GREEN -> "Neon Green"
                            AppColorAccent.ELECTRIC_BLUE -> "Electric Blue"
                            AppColorAccent.DEEP_VIOLET -> "Deep Violet"
                            AppColorAccent.MAGENTA -> "Magenta Pink"
                            AppColorAccent.GOLD -> "Pure Gold"
                            AppColorAccent.MINT -> "Fresh Mint"
                            AppColorAccent.PEACH -> "Soft Peach"
                            AppColorAccent.RUBY -> "Ruby Red"
                            AppColorAccent.SAPPHIRE -> "Royal Sapphire"
                        }
                        Text(accentLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // APK Download Location Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { folderPickerLauncher.launch(null) }
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("APK Output Location", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            val locationLabel = if (useCustomStorage && !customStorageFolder.isNullOrBlank()) {
                                customStorageFolder!!
                            } else {
                                "Downloads/Packora/* (Default)"
                            }
                            Text(locationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (useCustomStorage) {
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {
                                    useCustomStorage = false
                                    customStorageFolder = null
                                    prefsManager.useCustomStorageFolder = false
                                    prefsManager.customStorageFolder = null
                                }
                            ) {
                                Text("Reset to Default (Downloads/Packora)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // About Packora Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { onNavigateAbout() }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About Packora", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Version, developer credits, & key features", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // App Theme Bottom Sheet Selector
        if (showThemeSheet) {
            SelectionBottomSheetDialog(
                title = "Select App Theme",
                subtitle = "Choose light, dark, or AMOLED pitch black appearance",
                icon = Icons.Outlined.DarkMode,
                options = listOf(
                    AppThemeMode.SYSTEM to "System Default",
                    AppThemeMode.LIGHT to "Light Theme",
                    AppThemeMode.DARK to "Dark Theme",
                    AppThemeMode.AMOLED to "AMOLED Pitch Dark"
                ),
                initialSelection = currentTheme,
                isScrollable = false,
                onDismiss = { showThemeSheet = false },
                onConfirm = { selected ->
                    currentTheme = selected
                    prefsManager.themeMode = selected
                    onThemeModeChange(selected)
                    showThemeSheet = false
                }
            )
        }

        // Color Accent Bottom Sheet Selector
        if (showAccentSheet) {
            SelectionBottomSheetDialog(
                title = "Select Color Accent",
                subtitle = "Select system dynamic colors or custom palette",
                icon = Icons.Outlined.Palette,
                options = listOf(
                    AppColorAccent.SYSTEM to "System Default (Material You)",
                    AppColorAccent.EMERALD to "Emerald Green",
                    AppColorAccent.OCEAN to "Ocean Blue",
                    AppColorAccent.PURPLE to "Deep Purple",
                    AppColorAccent.AMBER to "Warm Amber",
                    AppColorAccent.CRIMSON to "Crimson Red",
                    AppColorAccent.ROSE to "Rose Pink",
                    AppColorAccent.CYAN to "Cyan Breeze",
                    AppColorAccent.ORANGE to "Sunset Orange",
                    AppColorAccent.INDIGO to "Midnight Indigo",
                    AppColorAccent.TEAL to "Teal Mint",
                    AppColorAccent.LIME to "Lime Gold",
                    AppColorAccent.CORAL to "Coral Flame",
                    AppColorAccent.NEON_GREEN to "Neon Green",
                    AppColorAccent.ELECTRIC_BLUE to "Electric Blue",
                    AppColorAccent.DEEP_VIOLET to "Deep Violet",
                    AppColorAccent.MAGENTA to "Magenta Pink",
                    AppColorAccent.GOLD to "Pure Gold",
                    AppColorAccent.MINT to "Fresh Mint",
                    AppColorAccent.PEACH to "Soft Peach",
                    AppColorAccent.RUBY to "Ruby Red",
                    AppColorAccent.SAPPHIRE to "Royal Sapphire"
                ),
                initialSelection = currentAccent,
                isScrollable = true,
                onDismiss = { showAccentSheet = false },
                onConfirm = { selected ->
                    currentAccent = selected
                    prefsManager.colorAccent = selected
                    onColorAccentChange(selected)
                    showAccentSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionBottomSheetDialog(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<Pair<T, String>>,
    initialSelection: T,
    isScrollable: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (T) -> Unit
) {
    var tempSelection by remember { mutableStateOf(initialSelection) }
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            val listModifier = if (isScrollable) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (value, label) ->
                    val isSelected = tempSelection == value
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { tempSelection = value }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { tempSelection = value }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onConfirm(tempSelection) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("APPLY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
