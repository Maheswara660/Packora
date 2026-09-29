/**
 * Packora Kotlin Multiplatform Web Runtime v5.4.0
 * Compiled from kmp-web/src/jsMain/kotlin/com/maheswara660/packora/web/
 * Package: com.maheswara660.packora.web
 */
(function (root, factory) {
    var exp = factory();
    if (typeof module === 'object' && module && module.exports) {
        module.exports = exp;
    }
    if (typeof define === 'function' && define.amd) {
        define([], function() { return exp; });
    }
    var g = (typeof window !== 'undefined') ? window : (typeof globalThis !== 'undefined') ? globalThis : (typeof self !== 'undefined') ? self : root;
    g.PackoraKMP = exp;
}(typeof self !== 'undefined' ? self : this, function () {
    'use strict';

    var VERSION = "5.4.0";

    var TARGETS = [
        { id: "web", title: "Default Web App", badge: "Universal PWA", description: "Full standalone WebAPK with independent task affinity and custom keystores." },
        { id: "html", title: "Offline HTML Pack", badge: "100% Offline", description: "Self-contained local web assets bundled into assets/www/ with zero network dependency." },
        { id: "frontend", title: "Frontend SPA", badge: "SPA Router", description: "Single Page App with client-side history routing fallback for React, Vue, Vite, Nuxt." },
        { id: "multi-web", title: "Multi-Web Hub", badge: "Tabbed Workspace", description: "Aggregates multiple destinations with an interactive native dark pill navigation bar." },
        { id: "media", title: "Media Streamer", badge: "Background Audio", description: "Video and audio player with auto-keep-screen-on and uninterrupted background playback." }
    ];

    var DOH_RESOLVERS = [
        { id: "cloudflare", name: "Cloudflare DNS", endpoint: "https://cloudflare-dns.com/dns-query", features: "Ultra-fast global Anycast (1.1.1.1)" },
        { id: "google", name: "Google Public DNS", endpoint: "https://dns.google/dns-query", features: "High-capacity worldwide infrastructure (8.8.8.8)" },
        { id: "adguard", name: "AdGuard DNS", endpoint: "https://dns.adguard-dns.com/dns-query", features: "Built-in ad & tracker blocking resolver" },
        { id: "quad9", name: "Quad9 DNS", endpoint: "https://dns.quad9.net/dns-query", features: "Swiss privacy-first security & threat protection" },
        { id: "mullvad", name: "Mullvad DoH", endpoint: "https://doh.mullvad.net/dns-query", features: "Strict zero-logging audited privacy resolver" },
        { id: "controld", name: "Control D", endpoint: "https://freedns.controld.com/p0", features: "High-performance privacy resolver with zero telemetry" },
        { id: "dnssb", name: "DNS.SB", endpoint: "https://doh.dns.sb/dns-query", features: "European privacy-first non-censored DNS resolver" },
        { id: "cleanbrowsing", name: "CleanBrowsing Security", endpoint: "https://doh.cleanbrowsing.org/doh/security-filter/", features: "Malware, phishing & malicious domain blocker" },
        { id: "opendns", name: "OpenDNS", endpoint: "https://doh.opendns.com/dns-query", features: "Cisco Anycast recursive DNS network" }
    ];

    var PRIVACY_VECTORS = [
        { category: "Canvas 2D", vectorName: "toDataURL & getImageData", attackSurface: "Pixel hashing to identify browser graphics stack", mitigation: "Injects microscopic, deterministic pixel noise to randomize tracking hashes without visual degradation." },
        { category: "WebGL", vectorName: "UNMASKED_RENDERER_WEBGL", attackSurface: "GPU vendor, renderer strings & extensions profiling", mitigation: "Spoofs GPU strings to generic high-end Adreno/Mali profiles and masks shader precision." },
        { category: "AudioContext", vectorName: "OscillatorNode & AnalyserNode", attackSurface: "Acoustic frequency response & FFT fingerprinting", mitigation: "Applies sub-audible jitter (+/- 0.0001) to buffer frequencies, breaking audio fingerprint curves." },
        { category: "DOM Micro-Geometry", vectorName: "getClientRects & getBoundingClientRect", attackSurface: "Subpixel font rendering & display scaling telemetry", mitigation: "Injects microscopic fractional floating-point jitter to subpixel coordinate readbacks." },
        { category: "WebRTC", vectorName: "RTCPeerConnection ICE Candidates", attackSurface: "Local intranet IP address & network topology leakage", mitigation: "Blocks host-type ICE candidate generation, preventing local private IP leakages." },
        { category: "Battery API", vectorName: "navigator.getBattery()", attackSurface: "Battery charging level & discharge time fingerprinting", mitigation: "Spoofs charging status to 100% constant, preventing timing-based session correlation." },
        { category: "Device Memory", vectorName: "navigator.deviceMemory", attackSurface: "RAM capacity device categorization", mitigation: "Normalizes memory reporting to standard 8GB profiles across all devices." },
        { category: "Hardware Concurrency", vectorName: "navigator.hardwareConcurrency", attackSurface: "CPU core count correlation", mitigation: "Clamps CPU core reports to standard 8-core mobile baseline." },
        { category: "Data Hygiene", vectorName: "Storage & Cache Eviction", attackSurface: "Persistent cross-session tracking via IndexedDB/Cookies", mitigation: "Performs comprehensive cache, DOM storage, and cookie wiping upon application exit when enabled." }
    ];

    function getPipeline(targetId, url, appName, pkgName) {
        var steps = [
            { stepNumber: 1, title: "Harvesting Web Metadata", detail: "Inspecting " + url + " for PWA manifest, high-res Apple touch icons, and theme colors...", durationMs: 350 },
            { stepNumber: 2, title: "Extracting Template APK", detail: "Staging webview_shell.apk base container (Android 15 API 35 ready)...", durationMs: 250 },
            { stepNumber: 3, title: "In-House Binary AXML Patching", detail: "Rewriting binary AndroidManifest.xml: package=" + pkgName + ", target=" + targetId + ", permissions configured...", durationMs: 400 },
            { stepNumber: 4, title: "In-House Binary ARSC Rebuilding", detail: "Updating compiled resources.arsc string pools: appName=\"" + appName + "\"...", durationMs: 300 }
        ];

        if (targetId === "html") {
            steps.push({ stepNumber: 5, title: "Packaging Offline Assets", detail: "Bundling local web assets recursively into assets/www/ with file access policies...", durationMs: 350 });
        }

        steps.push(
            { stepNumber: 6, title: "ELF 16KB Page Boundary Alignment", detail: "Re-aligning all native shared libraries (*.so) to 16,384-byte boundaries for Android 15+ kernels...", durationMs: 400 },
            { stepNumber: 7, title: "Generating Deterministic Keystore", detail: "Computing isolated RSA-3072 cryptographic keypair for package " + pkgName + "...", durationMs: 450 },
            { stepNumber: 8, title: "APK Signature Scheme v2 & v3", detail: "Generating RFC-compliant cryptographic signatures via apksig engine...", durationMs: 350 },
            { stepNumber: 9, title: "WebAPK Generation Complete", detail: "Standalone WebAPK compiled successfully! Ready for in-place updates.", durationMs: 200 }
        );

        return steps;
    }

    console.log("Packora Kotlin Multiplatform Engine v" + VERSION + " active.");

    return {
        version: VERSION,
        getVersion: function () { return VERSION; },
        getTargets: function () { return TARGETS; },
        getResolvers: function () { return DOH_RESOLVERS; },
        getPrivacyVectors: function () { return PRIVACY_VECTORS; },
        getPipeline: getPipeline
    };
}));
