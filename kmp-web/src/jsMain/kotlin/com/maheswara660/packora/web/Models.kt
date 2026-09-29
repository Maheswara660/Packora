package com.maheswara660.packora.web

/**
 * Packora App Architecture Targets
 */
enum class WebAppTarget(val id: String, val title: String, val badge: String, val description: String) {
    WEB("web", "Default Web App", "Universal PWA", "Full standalone WebAPK with independent task affinity and custom keystores."),
    HTML("html", "Offline HTML Pack", "100% Offline", "Self-contained local web assets bundled into assets/www/ with zero network dependency."),
    FRONTEND("frontend", "Frontend SPA", "SPA Router", "Single Page App with client-side history routing fallback for React, Vue, Vite, Nuxt."),
    MULTI_WEB("multi-web", "Multi-Web Hub", "Tabbed Workspace", "Aggregates multiple destinations with an interactive native dark pill navigation bar."),
    MEDIA("media", "Media Streamer", "Background Audio", "Video and audio player with auto-keep-screen-on and uninterrupted background playback.")
}

/**
 * 9 Encrypted DNS-over-HTTPS (DoH) Providers
 */
data class DnsResolver(val id: String, val name: String, val endpoint: String, val features: String)

val dohResolvers = listOf(
    DnsResolver("cloudflare", "Cloudflare DNS", "https://cloudflare-dns.com/dns-query", "Ultra-fast global Anycast (1.1.1.1)"),
    DnsResolver("google", "Google Public DNS", "https://dns.google/dns-query", "High-capacity worldwide infrastructure (8.8.8.8)"),
    DnsResolver("adguard", "AdGuard DNS", "https://dns.adguard-dns.com/dns-query", "Built-in ad & tracker blocking resolver"),
    DnsResolver("nextdns", "NextDNS", "https://dns.nextdns.io", "Customizable cloud privacy & analytics filtering"),
    DnsResolver("cleanbrowsing", "CleanBrowsing Security", "https://doh.cleanbrowsing.org/doh/security-filter/", "Malware, phishing & malicious domain blocker"),
    DnsResolver("quad9", "Quad9 DNS", "https://dns.quad9.net/dns-query", "Swiss privacy-first security & threat protection"),
    DnsResolver("mullvad", "Mullvad DoH", "https://doh.mullvad.net/dns-query", "Strict zero-logging audited privacy resolver"),
    DnsResolver("system", "System Default", "", "Default carrier / Wi-Fi local recursive resolver"),
    DnsResolver("custom", "Custom User Endpoint", "", "Custom user-specified RFC 8484 HTTPS endpoint")
)

/**
 * Core Reactive Simulation State
 */
class PackoraWebSimulatorState {
    var selectedTarget: WebAppTarget = WebAppTarget.WEB
    var targetUrl: String = "https://github.com"
    var appName: String = "GitHub WebAPK"
    var packageName: String = "com.maheswara660.github"
    var enablePrivacyShield: Boolean = true
    var enableAdBlocker: Boolean = true
    var enableFooterHider: Boolean = true
    var selectedDns: DnsResolver = dohResolvers[0]
    var isCompiling: Boolean = false
    var compilationStep: Int = 0
    var logs: MutableList<String> = mutableListOf()

    fun reset() {
        selectedTarget = WebAppTarget.WEB
        targetUrl = "https://github.com"
        appName = "GitHub WebAPK"
        packageName = "com.maheswara660.github"
        enablePrivacyShield = true
        enableAdBlocker = true
        enableFooterHider = true
        selectedDns = dohResolvers[0]
        isCompiling = false
        compilationStep = 0
        logs.clear()
    }
}
