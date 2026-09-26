package com.maheswara660.packora.model

import androidx.compose.runtime.Stable

/**
 * Supported build target application types in Packora.
 */
enum class PackoraAppType(val displayName: String, val description: String) {
    WEB("Web Application", "Converts any web URL into a high-performance standalone Android WebAPK"),
    HTML("Offline HTML Pack", "Bundles local HTML, CSS, JavaScript, and assets into an offline app"),
    FRONTEND("Frontend SPA", "Optimized runtime for single-page applications built with React, Vue, Vite, or Svelte"),
    MULTI_WEB("Multi-Site Hub", "Aggregates multiple web destinations into tabs, drawer menus, or card feeds"),
    NODEJS("Node.js Runtime", "Runs on-device Node.js 18.20 with npm scripts in a dedicated process"),
    PHP("PHP & SQLite", "Native PHP 8.4 runtime with Composer and local SQLite database support"),
    PYTHON("Python Server", "Runs Python 3.14 with Flask, FastAPI, Django, and uvicorn on-device"),
    GO("Go Binary", "Compiles and executes native Go 1.26 static binaries and HTTP servers"),
    WORDPRESS("WordPress Portable", "Standalone local WordPress site running over embedded PHP and SQLite"),
    MEDIA("Media Streamer", "Audio and video streaming app with system MediaSession lock-screen controls"),
    GALLERY("Media Gallery", "Local image and photo showcase with responsive grid and carousel layouts"),
    APP_CLONER("App Rebrander", "Clones and customizes installed APKs with new identity and certificates");

    val isServerRuntime: Boolean
        get() = this in SERVER_RUNTIMES

    companion object {
        val SERVER_RUNTIMES = setOf(NODEJS, PHP, PYTHON, GO, WORDPRESS)

        fun fromName(name: String?): PackoraAppType =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: WEB
    }
}

/**
 * Comprehensive project model for Packora applications.
 */
@Stable
data class PackoraApp(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appName: String,
    val targetUrl: String,
    val packageName: String,
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val iconPath: String? = null,
    val appType: PackoraAppType = PackoraAppType.WEB,

    // Core Capabilities
    val webConfig: PackoraWebConfig = PackoraWebConfig(),
    val networkConfig: PackoraNetworkConfig = PackoraNetworkConfig(),
    val privacyConfig: PackoraPrivacyConfig = PackoraPrivacyConfig(),
    val adBlockConfig: PackoraAdBlockConfig = PackoraAdBlockConfig(),
    val securityConfig: PackoraSecurityConfig = PackoraSecurityConfig(),
    val splashConfig: PackoraSplashConfig = PackoraSplashConfig(),
    val serverConfig: PackoraServerConfig? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Stable
data class PackoraWebConfig(
    val desktopMode: Boolean = false,
    val forceDarkMode: Boolean = false,
    val enableZoom: Boolean = true,
    val allowCopying: Boolean = true,
    val enableSwipeRefresh: Boolean = true,
    val customUserAgent: String? = null,
    val openExternalLinksInCustomTabs: Boolean = true,
    val downloadEnabled: Boolean = true,
    val customDownloadFolder: String? = null,
    val toolbarEnabled: Boolean = false,
    val consoleDebugEnabled: Boolean = false,
    val findInPageEnabled: Boolean = false
)

enum class PackoraDnsProvider(val displayName: String, val dohUrl: String) {
    SYSTEM("System Default", ""),
    CLOUDFLARE("Cloudflare DNS", "https://cloudflare-dns.com/dns-query"),
    GOOGLE("Google Public DNS", "https://dns.google/dns-query"),
    ADGUARD("AdGuard DNS (Ad-Blocking)", "https://dns.adguard-dns.com/dns-query"),
    NEXTDNS("NextDNS", "https://dns.nextdns.io/dns-query"),
    CLEANBROWSING("CleanBrowsing Security", "https://doh.cleanbrowsing.org/doh/security-filter/"),
    QUAD9("Quad9 DNS", "https://dns.quad9.net/dns-query"),
    MULLVAD("Mullvad DoH", "https://doh.mullvad.net/dns-query"),
    CUSTOM("Custom DoH Endpoint", "")
}

@Stable
data class PackoraNetworkConfig(
    val dohProvider: PackoraDnsProvider = PackoraDnsProvider.SYSTEM,
    val customDohUrl: String = "",
    val strictDoh: Boolean = false,
    val enableEch: Boolean = false,
    val tlsFingerprintEnabled: Boolean = false,
    val tlsFingerprintTemplate: String = "CHROME_131",
    val corsBypassEnabled: Boolean = false,
    val proxyEnabled: Boolean = false,
    val proxyType: String = "HTTP", // HTTP, SOCKS5, PAC
    val proxyHost: String = "",
    val proxyPort: Int = 8080
) {
    val effectiveDohUrl: String
        get() = if (dohProvider == PackoraDnsProvider.CUSTOM) customDohUrl else dohProvider.dohUrl
}

@Stable
data class PackoraPrivacyConfig(
    val disguiseFingerprint: Boolean = false,
    val maskCanvas: Boolean = true,
    val maskWebGL: Boolean = true,
    val maskAudioContext: Boolean = true,
    val maskClientRects: Boolean = true,
    val maskWebRtcIp: Boolean = true,
    val maskTimezone: Boolean = false,
    val targetTimezone: String = "UTC",
    val isolateCookies: Boolean = false,
    val clearDataOnExit: Boolean = false
)

@Stable
data class PackoraAdBlockConfig(
    val enabled: Boolean = false,
    val blockTrackers: Boolean = true,
    val cosmeticFiltering: Boolean = true,
    val customHostsRules: List<String> = emptyList()
)

@Stable
data class PackoraSecurityConfig(
    val encryptAssets: Boolean = false,
    val encryptionPassword: String = "",
    val antiDebugEnabled: Boolean = false,
    val antiTamperEnabled: Boolean = false,
    val perAppSigningEnabled: Boolean = true
)

@Stable
data class PackoraSplashConfig(
    val enabled: Boolean = false,
    val imagePath: String? = null,
    val durationSeconds: Int = 2,
    val allowSkip: Boolean = true
)

@Stable
data class PackoraServerConfig(
    val port: Int = 8080,
    val entryScript: String = "server.js",
    val startupTimeoutSeconds: Int = 30,
    val environmentVariables: Map<String, String> = emptyMap()
)
