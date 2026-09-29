package com.maheswara660.packora.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maheswara660.packora.manager.HistoryItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackoraAppInfoBottomSheetDialog(
    appName: String,
    packageName: String,
    versionName: String,
    versionCode: Int,
    iconBitmap: Bitmap? = null,
    historyItem: HistoryItem? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }

    val displayBitmap = iconBitmap ?: run {
        val path = historyItem?.iconPath
        if (!path.isNullOrBlank() && File(path).exists()) {
            try { android.graphics.BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        } else null
    }

    val item = historyItem

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 28.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Name & Package
            Text(
                text = appName.ifBlank { "Packora App" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Version Badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "v$versionName (Code $versionCode)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Information Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Architecture & Target Type
                val appType = item?.appType ?: "WEB"
                val appTypeLabel = when (appType.uppercase()) {
                    "HTML" -> "Offline HTML Website (No Network Needed)"
                    "FRONTEND" -> "Single Page App / PWA (Client Routing)"
                    "MULTI_WEB" -> "Multi-Web App (Multiple Tabs)"
                    "MEDIA" -> "Media Player (Keep Screen On)"
                    else -> "Standard Responsive Web App"
                }

                InfoCard(
                    title = "Application Architecture",
                    icon = Icons.Outlined.Layers
                ) {
                    InfoRow("Target Mode", appTypeLabel)
                    val targetUrl = item?.targetUrl ?: ""
                    if (targetUrl.isNotBlank()) {
                        InfoRow("Target URL", targetUrl)
                    }
                    InfoRow("Browser Engine", if (item?.browserEngine == "SYSTEM_DEFAULT") "System Chrome" else "Isolated WebView")
                    InfoRow("Display Mode", if (item?.isDesktopMode == true) "Desktop Viewport" else "Mobile Viewport")
                    InfoRow("Force Dark Mode", if (item?.isForceDarkMode == true) "Enabled" else "Standard")
                    InfoRow("Zoom Support", if (item?.enableZoom == true) "Enabled" else "Disabled")
                    InfoRow("Text Copying", if (item?.allowCopying == true) "Enabled" else "Disabled")
                    InfoRow("Web Footers", if (item?.enableWebFooter == true) "Shown" else "Hidden")
                    val folder = item?.customDownloadFolder ?: "Downloads/Packora"
                    InfoRow("Storage Location", folder)
                }

                // 2. Privacy & Anti-Fingerprinting Shield
                InfoCard(
                    title = "Stealth Privacy Shield",
                    icon = Icons.Outlined.Security
                ) {
                    val disguised = item?.disguiseFingerprint == true
                    InfoRow("Fingerprint Shield", if (disguised) "Active (50+ Vectors Protected)" else "Standard Profile")
                    if (disguised) {
                        InfoRow("Canvas & WebGL", "Subpixel Hash Randomization Active")
                        InfoRow("AudioContext", "Noise Injection Active")
                        InfoRow("ClientRects & DOM", "Layout Jitter Active")
                        InfoRow("WebRTC IP Shield", "Local IP Leak Prevention Active")
                        InfoRow("Data Lifetime", if (item.clearDataOnExit) "Clear Cookies & Cache On Exit" else "Persistent Sessions")
                    }
                }

                // 3. Ad-Blocker & Tracker Defense
                InfoCard(
                    title = "Ad & Tracker Defense",
                    icon = Icons.Outlined.Shield
                ) {
                    val adBlocked = item?.adBlockEnabled == true
                    InfoRow("Ad-Blocker Engine", if (adBlocked) "Active" else "Disabled")
                    if (adBlocked) {
                        InfoRow("Telemetry Trackers", if (item.blockTrackers) "Blocked" else "Allowed")
                        InfoRow("Cosmetic Filtering", if (item.cosmeticFiltering) "Active" else "Disabled")
                    }
                }

                // 4. Encrypted DNS & Security
                InfoCard(
                    title = "Network & Cryptography",
                    icon = Icons.Outlined.Dns
                ) {
                    val doh = item?.dohProvider ?: "SYSTEM"
                    val dohName = when (doh.uppercase()) {
                        "CLOUDFLARE" -> "Cloudflare (1.1.1.1)"
                        "CLOUDFLARE_SECURITY" -> "Cloudflare Security"
                        "CLOUDFLARE_FAMILY" -> "Cloudflare Family"
                        "GOOGLE" -> "Google Public DNS"
                        "ADGUARD" -> "AdGuard DNS"
                        "ADGUARD_FAMILY" -> "AdGuard Family"
                        "QUAD9" -> "Quad9 Secure"
                        "MULLVAD" -> "Mullvad Encrypted"
                        "CUSTOM" -> "Custom DoH (${item?.customDohUrl})"
                        else -> "System Default DNS"
                    }
                    InfoRow("DoH Resolver", dohName)
                    if (item?.strictDoh == true) {
                        InfoRow("Strict DoH", "Enforced (Fallback Blocked)")
                    }
                    if (item?.enableEch == true) {
                        InfoRow("Encrypted Client Hello", "ECH Enabled")
                    }
                    InfoRow("Signing Keystore", if (item?.perAppSigning != false) "Isolated Per-App Keystore" else "Shared Keystore")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Open App Button
                FilledTonalButton(
                    onClick = {
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                            if (intent != null) {
                                context.startActivity(intent)
                                onDismiss()
                            } else {
                                Toast.makeText(context, "App is not installed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot launch: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch App", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                // System App Settings
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open settings: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("System Info", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // UNDERSTOOD Button (Consistent with all other dialogs in Packora)
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "UNDERSTOOD",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}
