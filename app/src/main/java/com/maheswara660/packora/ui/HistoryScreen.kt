package com.maheswara660.packora.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maheswara660.packora.installApkFile
import com.maheswara660.packora.manager.BuildHistoryManager
import com.maheswara660.packora.manager.HistoryItem
import java.io.File

enum class SortMode {
    NEWEST, OLDEST, NAME_AZ
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onReuseConfig: (HistoryItem) -> Unit
) {
    val context = LocalContext.current
    val historyManager = remember { BuildHistoryManager(context) }
    var historyList by remember { mutableStateOf(historyManager.getHistoryItems()) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(SortMode.NEWEST) }

    var showClearConfirmSheet by remember { mutableStateOf(false) }

    val filteredList = remember(historyList, searchQuery, sortMode) {
        var list = historyList.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true) ||
            it.targetUrl.contains(searchQuery, ignoreCase = true)
        }
        list = when (sortMode) {
            SortMode.NEWEST -> list.sortedByDescending { it.timestamp }
            SortMode.OLDEST -> list.sortedBy { it.timestamp }
            SortMode.NAME_AZ -> list.sortedBy { it.appName.lowercase() }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search apps...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.9f),
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Close Search")
                                }
                            }
                        )
                    } else {
                        Text("Build History", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search")
                        }
                    }

                    var showSortMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Newest First") },
                            onClick = { sortMode = SortMode.NEWEST; showSortMenu = false },
                            leadingIcon = { if (sortMode == SortMode.NEWEST) Icon(Icons.Outlined.Check, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest First") },
                            onClick = { sortMode = SortMode.OLDEST; showSortMenu = false },
                            leadingIcon = { if (sortMode == SortMode.OLDEST) Icon(Icons.Outlined.Check, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("App Name (A-Z)") },
                            onClick = { sortMode = SortMode.NAME_AZ; showSortMenu = false },
                            leadingIcon = { if (sortMode == SortMode.NAME_AZ) Icon(Icons.Outlined.Check, contentDescription = null) }
                        )
                    }

                    IconButton(onClick = { showClearConfirmSheet = true }, enabled = historyList.isNotEmpty()) {
                        Icon(Icons.Outlined.DeleteForever, contentDescription = "Clear History", tint = if (historyList.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha=0.3f))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No apps matching '$searchQuery'" else "No Build History Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Apps generated with Packora will appear here so you can easily update version code & name.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                .clickable { onReuseConfig(item) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.appName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = item.packageName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            historyManager.deleteHistoryItem(item.id)
                                            historyList = historyManager.getHistoryItems()
                                            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("v${item.versionName} (${item.versionCode})") }
                                    )
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(item.browserEngine) }
                                    )
                                }

                                Text(
                                    text = item.targetUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = item.formattedDate(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Button(
                                        onClick = { onReuseConfig(item) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.AutoMode, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reuse & Bump Version", fontSize = 12.sp)
                                    }

                                    if (!item.apkPath.isNullOrBlank() && File(item.apkPath).exists()) {
                                        OutlinedButton(
                                            onClick = { installApkFile(context, item.apkPath) },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Outlined.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Install", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

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
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Clear Build History?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Are you sure you want to permanently clear all generated app history? This action cannot be undone.",
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
                                historyManager.clearAllHistory()
                                historyList = emptyList()
                                showClearConfirmSheet = false
                                Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("CLEAR")
                        }
                    }
                }
            }
        }
    }
}
