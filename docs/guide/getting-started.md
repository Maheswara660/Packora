# Getting Started with Packora

This guide walks you through compiling your very first standalone, hardened WebAPK directly on your Android device using Packora Studio.

---

## 1. Installation & Requirements

- **Supported OS**: Android 7.0 (Nougat, API 24) through Android 15 (Vanilla Ice Cream, API 35) and beyond.
- **Hardware Architecture**: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.
- **Root Required**: No. Packora operates entirely in user space without requiring root or unlocked bootloaders. (Optional Root/Shizuku modes are available in Settings for unattended silent updates).

Download the latest signed release APK from [GitHub Releases](https://github.com/Maheswara660/Packora/releases) and install it on your device.

---

## 2. Launching Packora Studio

When you launch Packora, the **My Apps** dashboard presents:
- A search bar for filtering your generated WebAPKs.
- Category filters to organize apps into work, media, tools, and social categories.
- A floating action button (**＋**) to initiate a new app build.
- Quick navigation to **Updates**, **History**, and **Settings**.

---

## 3. Creating Your First WebAPK

Tap the **＋ (Create)** button to open the **Build Studio**.

### Step A: Choose an Architecture
Select the target architecture matching your source:
1. **Web App (URL / PWA)**: Ideal for existing websites and cloud applications (e.g. `https://linear.app`, `https://notion.so`).
2. **Offline HTML5**: Ideal for offline games, interactive calculators, documentation bundles, or zip archives containing `index.html`.
3. **Frontend SPA**: Ideal for client-routed single-page apps (React, Vue, Svelte, Angular).
4. **Multi-Web Hub**: Ideal for portals combining multiple URLs with a native bottom navigation bar.
5. **Media Stream**: Ideal for HLS/DASH video streams, live audio, or radio stations.

### Step B: Configure App Identity
- **App Name**: Enter the title that will appear in the Android system launcher and splash screen.
- **Package Name**: Packora suggests a reverse-domain identifier (e.g., `com.company.myapp`). You can customize this freely or generate a random clean identifier.
- **App Icon**: Select an image from your device gallery, enter an icon URL, or let Packora scrape the high-resolution vector favicon directly from the target website.

### Step C: Customize Privacy & Network
- **Stealth Privacy Shield**: Toggle ON to intercept 50+ fingerprinting vectors (Canvas, WebGL, AudioContext, Battery, WebRTC).
- **Ad & Tracker Blocker**: Toggle ON to load the 70,000+ EasyList rule engine.
- **Encrypted DNS**: Pick from 9 zero-logging DoH providers (Cloudflare, AdGuard, Quad9, Google, Mullvad, etc.) or enter a custom DoH endpoint.

### Step D: Compile the WebAPK
Tap the **Build WebAPK** button at the bottom.
Packora's on-device binary compiler will:
1. Extract and clone the pre-compiled template APK.
2. Binary-patch `AndroidManifest.xml` (AXML) with the custom package name, permissions, and launcher activities.
3. Re-index and patch `resources.arsc` with custom strings and colors.
4. Replace the launcher icon mipmaps with adaptive icon layers.
5. Inject the serialized `app_config.json` containing runtime policies.
6. Align all zip entries and native binaries to 16KB page boundaries.
7. Generate an isolated RSA-3072 keystore and sign the package using APK Signature Scheme v1 and v2.

The entire process takes **between 1 and 4 seconds**.

---

## 4. Installing and Testing

Once compilation completes, Packora prompts you to install the APK:
- Tap **Install APK** to invoke the Android package installer.
- Once installed, the WebAPK appears on your launcher just like any native application.
- Launch the app to experience native splash animations, hardware-accelerated rendering, and privacy isolation.

---

## 5. Managing Updates & Build History

- **Build History**: Access the **History** tab to review all previously compiled APKs, inspect SHA-256 hashes, verify file sizes, or share artifacts with other devices.
- **Updates Hub**: The **Updates** screen automatically monitors remote endpoints for manifest changes, allowing you to update your WebAPKs with one tap.

---

## Next Steps

- Learn more about the [5 App Architectures](/guide/app-types/).
- Configure [Encrypted DNS](/guide/security-privacy/encrypted-dns) and [Ad Blocking](/guide/security-privacy/ad-blocking).
- Explore [Deterministic Keystores](/developer/deterministic-keys).
