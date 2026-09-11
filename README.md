<p align="center">
  <a href="https://github.com/maheswara660/Packora">
    <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="200" height="200" alt="Packora Logo">
  </a>
</p>

<h1 align="center">Packora v2.4.0</h1>

<p align="center">
  <b>Transforming Any Web Application into High-Performance Native Standalone Android WebAPKs — Completely On-Device.</b>
</p>

<p align="center">
  <a href="https://github.com/maheswara660/Packora/releases"><img src="https://img.shields.io/badge/Release-v2.4.0-00A86B?style=for-the-badge&logo=android" alt="Version 2.4.0"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin" alt="Kotlin"></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose" alt="Jetpack Compose"></a>
  <a href="https://developer.android.com/about/versions/15"><img src="https://img.shields.io/badge/Target_SDK-35_(Android_15)-3DDC84?style=for-the-badge&logo=android" alt="Android SDK 35"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPLv3-green?style=for-the-badge" alt="License"></a>
</p>

---

## 📌 Table of Contents
- [About Packora](#-about-packora)
- [Key Features & Capabilities](#-key-features--capabilities)
- [Technology Stack](#-technology-stack)
- [System Requirements & Build Instructions](#-system-requirements--build-instructions)
- [Contributing](#-contributing)
- [License & Credits](#-license--credits)

---

## 📝 About Packora

**Packora** is an advanced, privacy-focused Android application that compiles any web application or website into a native, standalone Android APK file **directly on your device**. 

Unlike web wrappers or cloud-based compilers, Packora operates **100% offline without external servers, desktop tools, or complex IDEs**. It features a native in-house `:template` compilation module, custom binary **AXML** and **ARSC** string pool parsers, **V2/V3 APK signing**, **16KB ELF page alignment for Android 15+**, **Crisp High-Resolution Icon Engine**, **Smooth 0–100% Compiling Progress Engine**, **Smart History Auto-Versioning**, and a **Full Factory Reset** for complete form control.

---

## ✨ Key Features & Capabilities

### ⚡ Smooth Compiling Engine & Multi-ABI Architecture
* **Smooth 0–100% Countdown**: Replaced jumpy progress steps (`0 -> 20 -> 60 -> 80 -> 100`) with a smooth, granular step-by-step counter (`0, 1, 2, 3... 100`) for a perfect real-time compilation feel.
* **Universal & Multiple ABI Split Support**: Generates per-architecture split APKs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) along with a single universal installer APK compatible with all Android devices.
* **16KB Page Alignment for Android 15+**: Integrated native `ElfAligner16k` logic for embedded `.so` libraries satisfying upcoming Android 15 kernel requirements.

### 🌐 Standalone WebAPK Runtime & Seamless Browsing
* **Respected Site-Native Dark Mode**: Respects the website's native dark/light theme by default (`forceDarkMode` default `Off`), with an optional toggle to force algorithmic darkening.
* **Smart Website Footer Hider**: Enabled by default (`hideWebFooter` default `On`) with an intuitive 1-tap Bento card toggle. Intelligently detects and hides site informational footers (copyright notices, legal policy links, terms, privacy, security) that make web apps look like websites, while preserving web app bottom navigation bars and chat input docks.
* **Multi-Layered Ad Blocker & Fallbacks**: Intercepts popunder redirects, tracking networks, and gambling URLs (`isAdOrGamblingUrl`) in real time. Dynamic CSS injection and `MutationObserver` collapse empty ad space without hiding web application modals (Credly, Forage, Eduskills, etc.).
* **Google OAuth & Device Account Sync**: Integrates `AccountManager` and Google Play Services Auth synchronization. WebAPKs for Google AI Studio, Google Skills, Google Play Academy, and Google Stitch detect logged-in device accounts for seamless 1-tap Google SSO.
* **Persistent Session Cookie Disk Sync**: Calls `CookieManager.getInstance().flush()` across page events and lifecycle pauses (`onPageFinished`, `onPause`, `onStop`), ensuring login credentials persist reliably across app restarts.
* **Multi-Touch Zoom Control**: Optional `enableZoom` toggle for multi-touch pinch-to-zoom support (default `Off` to retain native app feel).

### 🎨 Quick-Toggles Bento Grid & Customization
* **3-Tier Bento Dashboard Layout**: Organized dashboard structure featuring **Website Details Hero Card**, a 5-card **Quick Toggles Bento Grid** (Desktop Mode, Force Dark Mode, Enable Zoom, Allow Text Copying, Hide Web Footer), and 3 **Feature Option Cards** (Package Identity & Versioning, Storage Folder, Custom Signing Keystore).
* **Inline Pop-Under Expansion Cards**: All 3 Feature Option Cards (Package Identity, Storage Folder, Keystore) expand inline directly beneath the card using smooth `AnimatedVisibility` — no Modal BottomSheets. Tap once to reveal fields, tap again to collapse.
* **Extended Custom Keystore Fields**: Custom Signing Keystore card includes full PKCS12 certificate identity fields — Store Password, Key Alias, Key Password, Common Name (Author), Organization, Organizational Unit, and Validity Years — for production-grade certificate control.
* **Full Factory Reset**: A single confirmation action resets all form inputs, Quick Toggle states, Feature Card details (Package, Storage, Keystore), and restores all `SharedPreferences` to defaults.
* **Icon Color Eyedropper & Auto-Matched Background**: Extracts dominant corner colors automatically from web icons and provides an interactive Custom Hex Eyedropper in `IconZoomerBottomSheet`.
* **22+ Material 3 Color Accents**: Expanded `AppColorAccent` with 22 vibrant color accents featuring a scrollable selection dialog capped at `300.dp` max height to keep the theme dialog compact.
* **Redesigned History Screen**: Search icon in header opens an expandable search bar directly under the header with smooth animations and toggles the top icon to an 'X' button. History cards save and display actual app icon bitmaps for 1-tap config re-use.

---

## 🛠️ Technology Stack

| Component | Library / Framework | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.0.0` | Asynchronous coroutines & native build pipelines. |
| **UI Framework** | Jetpack Compose | `2024.12.01 (BOM)` | Modern Material 3 Bento UI architecture. |
| **Template Engine** | In-House `:template` | `v2.4.0` | Native WebAPK shell asset generator. |
| **Binary Engine** | Custom AXML & ARSC | `v2.4.0` | Binary manifest & AAPT2 string pool rebuilder. |
| **Signing Engine** | `apksig` & Keystore | `8.3.0` | V2/V3 APK signing & PKCS12 / JKS custom key injection. |
| **Page Alignment** | `ElfAligner16k` | `v2.4.0` | Native `.so` 16KB page alignment for Android 15+ kernels. |

---

## 🚀 System Requirements & Build Instructions

### System Requirements
* **Operating System**: Android 8.0 (Oreo) or higher (Min SDK 24, Target SDK 35).
* **Supported Architectures**: Universal APK & ABI splits (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).

### 🏗️ Building from Source

Ensure you have **Android Studio Ladybug** (or later) and **JDK 17** configured:

```bash
# 1. Clone the repository
git clone https://github.com/maheswara660/Packora.git
cd Packora

# 2. Build Packora release APKs (compiles :template and produces Universal + ABI splits)
./gradlew :app:assembleRelease
```

The compiled release APKs will be saved at:
`app/build/outputs/apk/release/`

---

## 🤝 Contributing

Contributions, feature requests, and bug reports are welcome!
1. Fork the repository on GitHub.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📜 License & Credits

Packora is open-source software licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.

* **Created & Maintained by**: [Maheswara660](https://github.com/maheswara660)
* **Donations & Support**: [Maheswara660](https://ko-fi.com/maheswara660)

---

<p align="center">
  <b>Packora v2.4.0 — Unlocking Web-to-APK Limits.</b><br>
  Made with ❤️ for the Android Community
</p>
