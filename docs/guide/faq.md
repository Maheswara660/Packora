# Frequently Asked Questions (FAQ)

Everything you need to know about Packora, its binary engine, privacy shield, and on-device WebAPK compilation.

---

## General

### What is Packora?
Packora is an open-source Android WebAPK Studio that compiles websites, PWAs, single-page web applications, HTML5 bundles, and media streams into fully native, standalone, and privacy-hardened Android APKs directly on your phone.

### Is Packora free and open source?
Yes. Packora is licensed under the [GNU General Public License v3.0 (GPL-3.0)](https://github.com/Maheswara660/Packora/blob/main/LICENSE). The source code is completely open, auditable, and free forever.

### Does Packora require a PC, Node.js, or Android Studio?
No. Packora runs entirely on your Android smartphone or tablet. The binary patching, asset injection, AXML manipulation, resource rebuilding, 16KB ELF alignment, and cryptographic signing are all executed locally in user space on your device.

### Which Android versions are supported?
Packora supports Android 7.0 (Nougat, API 24) through Android 15+ (Vanilla Ice Cream, API 35). It is fully compatible with both 4KB and 16KB memory page kernel architectures.

---

## Architectures & Capabilities

### What are the 5 supported app architectures?
1. **Web App (URL / PWA)**: Direct online web applications with service worker support, cache storage, pull-to-refresh, and custom user-agents.
2. **Offline HTML5**: Fully offline bundles packed directly into the APK assets, running with zero internet requirement.
3. **Frontend SPA**: Tailored for React, Vue, Svelte, and Angular applications with automated client-side routing fallback.
4. **Multi-Web Tabbed Hub**: Multi-domain applications featuring a native bottom navigation bar or tabbed switcher.
5. **Immersive Media**: Audio/video players with picture-in-picture (PiP), lock-screen controls, and screen wake lock.

### How does Packora compare to standard browser shortcuts or Chrome WebAPKs?
Standard browser shortcuts require the host browser to be running, share cookies and browsing history with the parent browser, display browser toolbars, and leak device fingerprints. Packora WebAPKs:
- Run in their own sandboxed data directory with isolated cookies, local storage, and IndexedDB.
- Intercept 50+ fingerprinting vectors via the Stealth Privacy Shield.
- Filter ads and telemetry natively using 70,000+ EasyList rules.
- Support encrypted DNS-over-HTTPS (DoH).
- Install as true native APKs that survive browser uninstalls and can be shared with friends.

---

## Security & Privacy

### What is the Stealth Privacy Shield?
The Stealth Privacy Shield is Packora's proprietary anti-fingerprinting system. It injects hardened JavaScript hooks prior to page load to randomize or spoof over 50 tracking vectors:
- **Canvas 2D**: Randomizes subtle noise in `toDataURL` and `getImageData` pixel values.
- **WebGL**: Spoofs unmasked vendor and renderer strings (e.g. simulating Apple GPU or Mali).
- **AudioContext**: Adds micro-jitter to frequency and audio analysis buffers.
- **WebRTC**: Drops ICE candidate IP discoveries to prevent real IP exposure behind VPNs.
- **System Attributes**: Standardizes screen dimensions, battery status, `hardwareConcurrency`, and `deviceMemory`.

### What DNS providers are supported?
Packora integrates 9 encrypted DNS-over-HTTPS (DoH) providers:
- **Cloudflare** (`https://cloudflare-dns.com/dns-query`)
- **Google** (`https://dns.google/dns-query`)
- **AdGuard** (`https://dns.adguard-dns.com/dns-query`)
- **Quad9** (`https://dns.quad9.net/dns-query`)
- **Mullvad** (`https://dns.mullvad.net/dns-query`)
- **Control D** (`https://freedns.controld.com/p0`)
- **DNS.SB** (`https://doh.dns.sb/dns-query`)
- **CleanBrowsing** (`https://doh.cleanbrowsing.org/doh/family-filter/`)
- **OpenDNS** (`https://doh.opendns.com/dns-query`)
- **Custom DoH**: You can specify any valid HTTPS DNS-over-HTTPS URL.

### How does the ad blocker work?
Packora's internal ad blocking engine matches web resource requests against an optimized compiled list of 70,000+ EasyList and EasyPrivacy domain rules directly inside the Android WebView `shouldInterceptRequest` pipeline. Blocked requests are terminated before network packets leave the device, cutting latency and conserving data.

---

## Binary Engine & Keystores

### How does on-device signing work?
Packora generates an isolated RSA-3072 keystore deterministically derived from the target application's unique package name. This ensures:
1. **Seamless In-Place Updates**: Subsequent builds of the same package name produce the identical cryptographic signature, allowing seamless updates without uninstalling.
2. **Cross-App Isolation**: Different WebAPKs receive distinct keystores, preventing security boundary bleed.
3. **Full Compliance**: Signs using both APK Signature Scheme v1 (JAR signing) and v2 (Whole-APK signing block).

### What is 16KB Page Alignment?
Starting with Android 15, devices can use memory page sizes of 16KB (instead of the legacy 4KB) for significant memory and CPU performance gains. Native shared libraries (`.so`) that are not 16KB page-aligned will fail to load or crash on modern kernels. Packora's internal zip engine aligns all zip local headers and uncompressed entries to strict 16,384-byte boundaries.

---

## Troubleshooting & Updates

### Can I update an already installed WebAPK?
Yes! Use the **Updates Hub** in Packora to scan for new versions or re-open the app in **Build Studio**, increase the version code, and tap **Build WebAPK**. The new APK installs seamlessly over the existing one without clearing your app data or cookies.

### Where can I report bugs or contribute?
Packora is hosted on GitHub at [github.com/Maheswara660/Packora](https://github.com/Maheswara660/Packora). Feel free to open issues, submit pull requests, or join discussions.
