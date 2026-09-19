package com.maheswara660.packora.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReleaseItem(
    val version: String,
    val date: String,
    val isLatest: Boolean = false,
    val summary: String,
    val changes: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    onBack: () -> Unit
) {
    val releases = listOf(
        ReleaseItem(
            version = "3.1.1",
            date = "September 19, 2026",
            isLatest = true,
            summary = "Compiled Updates Section in My Apps, Silent Batch Compilation, Pre-Compiled Update Detection & Clean Settings Loader",
            changes = listOf(
                "Compiled Updates Hub: Added a dedicated 'Compiled Updates Ready' section pinned to the top of My Apps displaying pre-compiled update APKs with direct 1-tap installation without recompiling.",
                "Silent Batch Updates: 'Update All' now compiles all eligible apps sequentially in the background without launching intrusive system package installer prompts between builds.",
                "Smart Pre-Compiled Update Detection: UpdateAppCard detects ready updates on disk, showing an 'Installed ➔ Ready' version badge and instant green 'INSTALL UPDATE' action button.",
                "Clean Settings Update Loader: Removed duplicate progress indicator from the left icon badge on 'Check for Updates', keeping the static update icon and displaying a single spinner on the right.",
                "Template WebAPK Shell Polish: Cleaned up experimental Picture-in-Picture and floating window handlers from the template module for lighter, more stable standalone WebAPK builds."
            )
        ),
        ReleaseItem(
            version = "3.1.0",
            date = "September 19, 2026",
            summary = "Floating Window Engine, Google Account Chooser Sync, Magic Link Login & Update System Parity",
            changes = listOf(
                "Floating Window Engine: Added native WINDOWING_MODE_FREEFORM with Picture-in-Picture fallback allowing WebAPKs to run as floating pop-up windows over games and apps.",
                "OEM Pop-Up View Parity: Declared MULTIWINDOW_LAUNCHER, Samsung Multi-Window (penwindow) metadata, and 600x800dp dimensions for One UI, HyperOS, and ColorOS/OxygenOS support.",
                "Google Sign-In 403 Disallowed Fix: Stripped X-Requested-With header via WebSettingsCompat to prevent Google OAuth from blocking in-app WebAPK logins.",
                "Clean Chrome User-Agent: Configured authentic modern Chrome User-Agent adhering to Google Identity Platform security policies.",
                "Native Google Account Chooser: Implemented custom modal dialog popup with darkened scrim backdrop rendering the Google account picker as an authentic centered modal.",
                "Passwordless Magic Link Clipboard Sync: Built automatic clipboard scanner on app resume detecting copied magic auth links with instant deep navigation.",
                "Direct Magic Link Paste Dialog: Added manual paste dialog with real-time validation to quickly paste and enter authentication links.",
                "Dedicated In-App Update Bottom Sheet: Converted the updates dialog into an App Update Ready! installation flow with version badges and primary install action.",
                "Updates Screen Card Parity: Standardized update cards to match My Apps and History 1:1 with elevated rounded surfaces, launcher icon boxes, and status chips.",
                "Settings Update Spinner: Replaced static Checking text on the update tile with a Material 3 circular progress indicator."
            )
        ),
        ReleaseItem(
            version = "3.0.0",
            date = "September 19, 2026",
            summary = "My Apps Hub Overhaul, Material 3 Delete BottomSheet, Icon Zoomer Steppers & Android 15 Permissions",
            changes = listOf(
                "My Apps Hub: Added real-time search with animated search bar, 3-mode sort bottom sheet (Recent, A–Z, Updates), and on-demand refresh scanner.",
                "Material 3 Delete BottomSheet: Replaced standard alert dialogs with a custom height-fitting bottom sheet featuring real app icons, version chips, and safety warning alerts.",
                "Direct Card Actions: Added Reuse Config (auto-populating original build settings) and Install APK buttons directly on installed app cards.",
                "Icon Zoomer Stepper Controls: Added - and + circular steppers for precise 1% fine-tuning alongside 10-point discrete tick marks (steps = 15) on the slider track.",
                "Ergonomic Bottom Navbar: Restored clear text titles beneath icons with an optimized 66dp height for comfortable thumb ergonomics.",
                "Harmonized Circular Badges: Standardized modern 40dp circular icon container badges across Dashboard, Settings preference tiles, and About cards.",
                "Android 13–15 Permissions: Declared native permissions for REQUEST_DELETE_PACKAGES, QUERY_ALL_PACKAGES, READ_MEDIA_IMAGES, and POST_NOTIFICATIONS.",
                "Dynamic Package Pipeline: Resolved duplicate package ID generation and ensured update builds inherit exact original configurations."
            )
        ),
        ReleaseItem(
            version = "2.4.0",
            date = "September 11, 2026",
            summary = "Smooth Compiling Engine, 3-Tier Bento Dashboard & Multi-ABI Split Builds",
            changes = listOf(
                "Smooth 0–100% Compiling Progress: Replaced jumpy discrete progress steps with a granular step-by-step counter for a smooth real-time compilation feel.",
                "Multi-ABI Splits: Added individual architecture builds (arm64-v8a, armeabi-v7a, x86, x86_64) alongside universal installer APKs.",
                "Enhanced Ad-Blocker Fallbacks: Real-time cleanup fallbacks, expanded blacklist domains, and CSS rules preventing empty ad whitespace without freezing modals.",
                "Respected Website Dark Mode: Disabled forced algorithmic darkening by default so WebAPKs honor native site themes automatically.",
                "Persistent Session Cookie Sync: Proactive cookie flushing during page finishes and lifecycle events to prevent login session drops.",
                "Google OAuth Sync: Integrated AccountManager for 1-tap Google SSO sign-in on supported web services.",
                "3-Tier Bento Dashboard: Organized dashboard into Hero Card, 5 Quick-Toggles, and 3 Inline Pop-Under Feature Cards.",
                "Extended Keystore Fields: Added full PKCS12 certificate identity controls (Author, Organization, Unit, Validity Years, Key Password).",
                "Full Factory Reset: Single action resets all form inputs, Quick Toggles, and SharedPreferences to defaults."
            )
        ),
        ReleaseItem(
            version = "2.3.0",
            date = "September 10, 2026",
            summary = "Ad & Gambling Link Interception, Crisp Icon Scaling & Material 3 Error Screens",
            changes = listOf(
                "Ad & Gambling Redirect Blocker: Intercepts popunders and promotional redirects to gambling and tracking domains in real time.",
                "Whitespace Collapsing: Dynamic CSS and MutationObserver collapse hidden ad space to zero height.",
                "Installed Native App Deep Linking: Resolves external links to native Android apps (YouTube, LinkedIn, Twitter, etc.).",
                "Web-to-System Notifications: Created NotificationBridge forwarding web app push alerts to Android status bar notifications.",
                "Crisp Icon Scaling: Adaptive foreground density calculations (xxxhdpi up to 432x432 px) eliminating launcher blurriness.",
                "Interactive Icon Zoom & 31+ Colors: Eyedropper palette with background fill colors and custom hex input.",
                "Material 3 Offline Screen: Modern offline CardView container with retry reload action."
            )
        ),
        ReleaseItem(
            version = "2.2.0",
            date = "August 26, 2026",
            summary = "AboutScreen Overhaul, WebRTC Permissions & Smart Version Auto-Increment",
            changes = listOf(
                "Modern BottomSheet Selection Dialogs: Glassmorphic cards with glowing active outlines for App Theme, Accent Color, and Browser Engine.",
                "AboutScreen Redesign: Added DeveloperCard, grouped action items with adaptive rounding, and live package metadata extraction.",
                "Smart History Auto-Versioning: Matches target URLs against previous builds to auto-increment versionCode and versionName.",
                "WebRTC Permissions: Injected camera, microphone, geolocation, and biometric authentication permissions for video/voice apps.",
                "Navigation BackStack: Integrated Jetpack Compose BackHandler for seamless system back gesture navigation."
            )
        ),
        ReleaseItem(
            version = "2.1.0",
            date = "August 14, 2026",
            summary = "Dynamic Bar Contrast & Algorithmic Dark Mode Engine",
            changes = listOf(
                "Dynamic Status & Navigation Bar Contrast: System bars sync with web page header theme colors with automatic light/dark icon contrast.",
                "Algorithmic Dark Mode: Added WebSettingsCompat darkening support to WebAPK shell matching system night mode."
            )
        ),
        ReleaseItem(
            version = "2.0.0",
            date = "July 31, 2026",
            summary = "Major Architecture Overhaul: Native In-House Template Module & 16KB Page Aligner",
            changes = listOf(
                "Native In-House Template Module (:template): Replaced external static shell binaries with an integrated Gradle template module compiled alongside Packora.",
                "16KB Page Alignment: Native ELF 16KB boundary alignment for .so libraries ensuring Android 15+ kernel compatibility.",
                "AAPT2 Resource Obfuscation: ArscRebuilder to discover and replace all 25+ obfuscated launcher icon resource paths.",
                "Automatic Favicon Fetcher: Automatically scrape high-resolution icons from HTML link tags and favicon APIs on URL entry.",
                "Android Mascot Fallback: Official green Android mascot head fallback icon.",
                "HTML5 File Uploads & Storage Access: WebChromeClient.onShowFileChooser with native system document picker.",
                "DownloadManager Notifications: Integrated system DownloadManager with POST_NOTIFICATIONS status bar alerts.",
                "Desktop Mode & Responsive Viewport: Responsive desktop viewport injection in WebAPK shell with dashboard toggle.",
                "Bento UI Dashboard: Streamlined Packora UI into a unified dashboard with sticky bottom build bar."
            )
        ),
        ReleaseItem(
            version = "1.1.0",
            date = "July 11, 2026",
            summary = "Custom Download Folders & Icon Packaging Safe Zone",
            changes = listOf(
                "Custom Download Folder Options: Utilize native Android Folder Picker to specify download storage destination.",
                "Download Notifications Support: WebAPKs injected with POST_NOTIFICATIONS for download completion alerts.",
                "Icon Proportion Balancing: Implemented 85% safe zone shrinking launcher icons by 15-20% to prevent over-scaled launcher icons."
            )
        ),
        ReleaseItem(
            version = "1.0.0",
            date = "June 30, 2026",
            summary = "Initial Release: Standalone On-Device Web-to-APK Compiler",
            changes = listOf(
                "Initial Release: Launching Packora as a powerful, on-device Web-to-APK builder.",
                "Native AXML Rebuilder: Modify package names, permissions, and app names directly via binary AXML manipulation.",
                "Dynamic APK Signing: Integrated robust V2/V3 APK Signature Scheme via apksig with custom on-the-fly Keystore generation.",
                "Web App Templating: Seamlessly wrap any URL into a high-performance WebAPK shell with customizable icons.",
                "Offline Compilation: Build fully functioning Android applications completely offline, directly on the device."
            )
        )
    )

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Changelog", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Text(
                            "Version history & release updates",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Settings"
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(releases, key = { it.version }) { release ->
                ReleaseCard(release)
            }
        }
    }
}

@Composable
private fun ReleaseCard(release: ReleaseItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (release.isLatest) 1.5.dp else 1.dp,
                color = if (release.isLatest) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (release.isLatest) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (release.isLatest) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "v${release.version}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (release.isLatest) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (release.isLatest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "LATEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = release.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = release.summary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                release.changes.forEach { change ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = change,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
