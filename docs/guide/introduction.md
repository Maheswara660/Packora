# Introduction to Packora

**Packora** is a next-generation Android WebAPK Studio designed to transform modern websites, Progressive Web Apps (PWAs), single-page applications (SPAs), offline HTML bundles, and media streams into production-grade, hardened native Android applications directly on your Android device.

No developer machine, Android Studio, Gradle daemon, or cloud build server is required. Packora handles the entire build, asset generation, binary patching, cryptographic signing, and 16KB ELF alignment on-device in under 5 seconds.

---

## Why Packora?

Most web-to-app wrappers simply point a generic Android WebView at a URL. This results in slow load times, glaring privacy leaks (fingerprinting, IP exposure via WebRTC), intrusive ads, and broken Android 15 compatibility.

Packora reimagines WebAPK generation from first principles:

```
┌────────────────────────────────────────────────────────┐
│                      PACKORA STUDIO                     │
├─────────────────┬───────────────────┬──────────────────┤
│ 5 ARCHITECTURES │  STEALTH PRIVACY  │ NETWORK ENCRYPTION│
│ • Web (PWA/URL) │ • 50+ Vectors     │ • 9 DoH Resolvers │
│ • Offline HTML5 │ • Canvas & WebGL  │ • EasyList Filter │
│ • Frontend SPA  │ • AudioContext    │ • Zero Logging    │
│ • Multi-Web Hub │ • WebRTC Shield   │ • Custom Proxies  │
│ • Media Stream  │ • Device Spoofing │ • Cache Isolation │
├─────────────────┴───────────────────┴──────────────────┤
│             ON-DEVICE BINARY COMPILER ENGINE           │
│  AXML Bytecode Patcher • ARSC String Table Rebuilder    │
│  Deterministic RSA-3072 Keystores • 16KB Page Alignment │
└────────────────────────────────────────────────────────┘
```

---

## Key Pillars

### 1. Five Specialized Architectures
Packora provides 5 purpose-built architectures tuned for specific web workloads:
- **Web App (URL / PWA)**: Direct online web applications with service workers, pull-to-refresh, custom user-agents, and background audio.
- **Offline HTML5 Bundle**: Self-contained web apps running locally without internet access, bundled directly into the APK assets.
- **Frontend SPA**: Optimized for React, Vue, Svelte, Vite, and Angular applications with client-side SPA hash/history routing fallback.
- **Multi-Web Tabbed Hub**: Multi-domain applications with native bottom navigation bars or drawer tabs allowing unified browsing across multiple endpoints.
- **Immersive Media**: Video streams, live IPTV, and audio players with picture-in-picture (PiP), screen keep-awake, and orientation lock.

### 2. Stealth Privacy Shield (50+ Vectors)
Modern web trackers fingerprint devices through hardware quirks, rendering differences, and device APIs. Packora's built-in stealth engine intercepts and sanitizes:
- **Canvas 2D & WebGL**: Subtly mutates pixel buffers and spoof GPU vendor (`ANGLE / Apple / Mali`) to prevent browser fingerprinting.
- **AudioContext**: Adds sub-perceptual noise to audio frequency data to disrupt acoustic fingerprinting.
- **WebRTC**: Clamps ICE candidates to prevent private and public IP leaks behind VPNs or proxies.
- **Hardware & System**: Spoofs `navigator.hardwareConcurrency`, `navigator.deviceMemory`, battery status, and screen geometry.

### 3. Encrypted DNS & Built-In Ad Blocking
- **DNS-over-HTTPS (DoH)**: Resolves hostnames over HTTPS to eliminate ISP sniffing and DNS hijacking. Includes 9 built-in privacy resolvers: Cloudflare, Google, AdGuard, Quad9, Mullvad, Control D, DNS.SB, CleanBrowsing, and OpenDNS.
- **EasyList Engine**: Blocks telemetry, analytics, and advertising scripts before network packets leave the device, drastically saving data and accelerating page loads.

### 4. Deterministic RSA-3072 Keystores
Every WebAPK is signed using an isolated, cryptographically secure RSA-3072 keystore derived deterministically from the package identifier. This guarantees that successive builds of the same package share the exact same signature for smooth in-place updates, while ensuring no two different apps share the same cryptographic key.

### 5. 16KB ELF Page Alignment
Android 15 introduces 16KB memory page support. Standard APK builders produce native libraries aligned to 4KB boundaries, resulting in crashes on modern devices. Packora's internal binary zip engine strictly aligns all native binaries and resources to 16KB boundaries.

---

## App Navigation & Layout

Packora's interface is structured into four primary hubs:
- **My Apps (Studio)**: View, launch, search, export, and manage your installed and generated WebAPKs.
- **Build Studio**: Create or reconfigure WebAPKs with real-time package name validation, icon customization, and privacy toggles.
- **Updates Hub**: Check for remote releases, view changelogs, verify SHA-256 hashes, and perform batch updates.
- **Build History**: Inspect previously compiled APK artifacts, review signing metadata, and share outputs.
- **Settings**: Configure studio defaults, install modes (Root, Shizuku, Intent), and preferred DNS resolvers.

---

## Next Steps

- Follow the [Getting Started Guide](/guide/getting-started) to build your first WebAPK in under 60 seconds.
- Discover all [5 Supported Architectures](/guide/app-types/).
- Explore the [Stealth Privacy Shield](/guide/security-privacy/stealth-privacy).
- Read about the [Binary Engine](/developer/binary-engine) under Developer Docs.
