<p align="center">
  <a href="https://github.com/maheswara660/Packora">
    <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="160" height="160" alt="Packora Logo" style="border-radius: 36px;">
  </a>
</p>

<h1 align="center">Packora v4.1.0</h1>

<p align="center">
  <b>High-Performance Standalone Android WebAPK Compiler — Completely On-Device & Offline.</b>
</p>

<p align="center">
  <a href="https://github.com/maheswara660/Packora/releases/latest"><img src="https://img.shields.io/badge/Release-v4.1.0-00A86B?style=for-the-badge&logo=android&logoColor=white" alt="Version 4.1.0"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 2.2.10"></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose Material 3"></a>
  <a href="https://developer.android.com/about/versions/15"><img src="https://img.shields.io/badge/Target_SDK-35_(Android_15)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android SDK 35"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPLv3-22C55E?style=for-the-badge" alt="License GPLv3"></a>
  <a href="https://github.com/sponsors/maheswara660"><img src="https://img.shields.io/badge/Sponsor-GitHub-EA4AAA?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Sponsor"></a>
  <a href="https://ko-fi.com/maheswara660"><img src="https://img.shields.io/badge/Sponsor-Ko--fi-FF5E5B?style=for-the-badge&logo=kofi&logoColor=white" alt="Ko-fi Sponsor"></a>
</p>

> [!NOTE]
> **Zero Telemetry • 100% Offline • No Cloud Dependencies**  
> Packora runs an entire Android compilation and packaging pipeline right on your phone or tablet. It takes any Progressive Web App (PWA) or responsive website and outputs a standalone, production-ready Android APK with zero external servers or developer tools required.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Screenshots](#-screenshots)
- [Architecture & How It Works](#-architecture--how-it-works)
- [What's New in v4.1.0](#-whats-new-in-v410)
- [Key Features & Capabilities](#-key-features--capabilities)
  - [Dashboard & Bento Grid Customization](#-dashboard--bento-grid-customization)
  - [Dedicated Updates Hub](#-dedicated-updates-hub)
  - [My Apps Management Hub](#-my-apps-management-hub)
  - [Dynamic App Icons & Theming](#-dynamic-app-icons--theming)
  - [Standalone WebAPK Runtime](#-standalone-webapk-runtime)
  - [Binary Engine & APK Signing](#-binary-engine--apk-signing)
  - [Build History & Smart Versioning](#-build-history--smart-versioning)
  - [Settings & Dynamic Theming](#-settings--dynamic-theming)
- [Permissions & Security Model](#-permissions--security-model)
- [Technology Stack](#-technology-stack)
- [Repository Structure](#-repository-structure)
- [Building from Source](#-building-from-source)
- [Contributing](#-contributing)
- [License & Acknowledgments](#-license--acknowledgments)

---

## 💡 Overview

**Packora** bridges the gap between modern web applications and native Android experiences. While standard browsers offer simple "Add to Home Screen" shortcuts that stay bound to browser windows, tabs, and URL bars, Packora builds an **independent native APK** that:

- Runs in its own dedicated Android window with independent task affinity.
- Features custom app icons, package IDs, and versioning.
- Auto-injects ad-blockers, smart website footer hiding, and persistent session cookie sync.
- Supports native Google SSO via Android Account Manager and Google Play Services Auth.
- Embeds 16KB ELF page alignment for smooth compatibility with Android 15+ kernels.
- Signs packages with APK Signature Scheme v2 & v3 using built-in or custom PKCS12 / JKS certificates.

---

## 📱 Screenshots

<div align="center">

| Build Dashboard | URL Icon Extractor | Icon Canvas Editor | Updates Hub |
| :---: | :---: | :---: | :---: |
| <img src="assets/build_screen.png" width="220" alt="Build Dashboard" /> | <img src="assets/url_icon_selection_menu.png" width="220" alt="URL Icon Extractor" /> | <img src="assets/icon_editor_menu.png" width="220" alt="Icon Canvas Editor" /> | <img src="assets/updates_screen.png" width="220" alt="Updates Hub" /> |

| My Apps Manager | Build History | Settings & Automation | Dynamic App Icons |
| :---: | :---: | :---: | :---: |
| <img src="assets/my_apps_screen.png" width="220" alt="My Apps Manager" /> | <img src="assets/history_screen.png" width="220" alt="Build History" /> | <img src="assets/settings_screen.png" width="220" alt="Settings & Automation" /> | <img src="assets/packora_app_icon_selection_menu.png" width="220" alt="Dynamic App Icons" /> |

</div>

---

## ⚙️ Architecture & How It Works

```mermaid
graph TD
    A[User Inputs PWA / Web URL] --> B[Asset Extractor & Metadata Harvester]
    B --> C[Packora Android Shell Template APK]
    C --> D[ZIP Central Directory Injector]
    D --> E[Custom Binary AXML Manifest Rebuilder]
    E --> F[AAPT2 Binary ARSC String Pool Modifier]
    F --> G[ElfAligner16k: 16KB Native Page Alignment for Android 15+]
    G --> H[apksig: APK Signature Scheme v2 & v3 Signing]
    H --> I[Standalone Signed WebAPK Installer]
```

1. **Asset & Metadata Harvesting**: Fetches high-resolution icons (HTML apple-touch, manifest, clearbit, duckduckgo, unavatar) and extracts dominant corner colors.
2. **Binary Modification Without AAPT**: In-house binary parsers rewrite `AndroidManifest.xml` (AXML) and `resources.arsc` directly in byte buffers, avoiding bulky command-line toolchains.
3. **Android 15+ 16KB Page Alignment**: Re-aligns all native ELF binaries (`.so` files) within ZIP archives to 16,384-byte boundaries.
4. **V2/V3 APK Signature**: Generates RFC-compliant cryptographic signatures on-device using ECDSA or RSA certificates.

---

## 🚀 What's New in v4.1.0

- **Sorting Consistency & Isolated Card Reordering**:
  - **My Apps & History Screens**: Resolved list sorting bugs where unsorted lists were displayed; apps and build records now reliably order by Name (A–Z), Name (Z–A), Newest, and Oldest.
  - **Updates Screen Sorting**: Fixed sort ordering and query filtering to immediately and accurately rearrange eligible update cards.
  - **Isolated Card Animations**: Integrated Jetpack Compose item placement animations (`Modifier.animateItem()`) across My Apps, Build History, and Updates screens so changing sort options smoothly and exclusively reorders the individual cards in place without causing the screen to flash, jump, or reload.
  - **Instant 1-Tap Selection**: Updated selection bottom sheets to immediately update state on tap with 1-tap select and apply.
- **Updates Screen UX & Safe Batch APK Deletion**:
  - **Card-Level Updating**: Individual app updates and batch "Update All" operations compile and update at the card level without full-screen spinners or disruptive view reloads.
  - **Safe Batch APK Deletion**: Resolved an issue where "Delete APK after install" deleted queued update APKs during "Update All" before they could be installed; APKs are now strictly preserved until the installed package version code matches or exceeds the newly built APK's version code.
- **Streamlined Update Installation Modes**:
  - Reduced Update Installation Mode in Settings to 2 straightforward options: **Manual** (standard package installer prompt) and **Auto-Prompt** (automatic installer prompt upon compilation completion).
- **UI Refresh: Authentic iOS Switch & Dot Pulse Loader**:
  - **PackoraIosSwitch**: Ported custom iOS-style toggle switch with smooth 51×31dp track, 27dp sliding thumb, and spring animations, now used as the default switch throughout the app.
  - **PackoraDotLoader**: Ported custom 8-dot circular pulsing loader across all loading and compiling states in the app.
- **Template Compatibility & Web Freedom**:
  - **Clean External Links**: Fixed `shouldOverrideUrlLoading` so non-domain-family links load freely inside the app's WebView when `openExternalLinks` is false, rather than forcibly kicking users out to an external browser.
  - **Eliminated Destructive DOM Modifications**: Neutralized `injectInteractionAndScrollUnfreezer` which was aggressively resetting `body.style.position = 'static'` and removing overlay elements, restoring full functionality to SPAs, modals, menus, drawers, and video overlays.
  - **Safe Footer Detection**: Reverted to safe v2.4.0 footer detection rules without global stylesheet overrides, preserving bottom app navigation bars and tab bars.

---

## ✨ Key Features & Capabilities

### 🎨 Dashboard & Bento Grid Customization
- **Website Details Hero Card**: Interactive URL input with clipboard auto-paste, Web icon badge, and quick clearing.
- **5-Card Quick Toggles Bento Grid**:
  - 🖥️ **Desktop Mode**: Renders sites with a full desktop viewport and Chrome desktop User-Agent.
  - 🌙 **Force Dark Mode**: Enables algorithmic darkening for sites without native dark themes.
  - 🔍 **Pinch Zoom**: Toggles multi-touch zoom controls.
  - 📋 **Allow Text Copying**: Overrides CSS user-select locks to permit text selection.
  - 🛡️ **Hide Web Footer**: Intelligently detects and hides site legal/copyright footers while keeping web app navigation intact.
- **Inline Pop-Under Feature Cards**:
  - 🆔 **Package Identity & Versioning**: Custom package names and automatic version incrementing (`versionCode` + `versionName`).
  - 📁 **Storage Folder**: Choose destination folders using Android's Storage Access Framework (SAF).
  - 🔑 **Custom Signing Keystore**: Generate custom PKCS12 certificates (Common Name, Organization, Unit, Validity Years, Key Password).
- **Icon Zoomer & Color Eyedropper**:
  - Auto-extracts dominant background corner colors with 1-tap palette auto-matching.
  - 31+ curated color fill options or custom hex input.
  - Steppers (`-` / `+`) and discrete slider for icon scaling.

### 🔄 Dedicated Updates Hub
- **Dedicated Navigation**: Independent Updates tab in the bottom navigation bar (`Icons.Outlined.SystemUpdate`) separating pending updates from installed apps.
- **Streamlined Update Modes**: Supports **Manual** (standard package installer prompt) and **Auto-Prompt** (automatic installer launch upon compile completion).
- **Dual-Action Ergonomics**: Compiled cards display side-by-side **Update** and **Install** actions.
- **Sequential Batch Compilation**: Top banner enables 1-tap sequential updates across all installed apps with safe post-install APK deletion.
- **Search & Auto-Detection**: Instant query filtering and real-time detection of installed WebAPKs with available update builds.

### 📱 My Apps Management Hub
- **Installed App Tracking**: Scans and displays WebAPKs generated by Packora on your device without cluttered updates banners.
- **Streamlined Action System**: Cards feature strictly two actions: **Open** (`FilledTonalButton`) and **Uninstall** (`FilledTonalButton` with error-tonal confirmation sheet).
- **Instant Search & Sort**: Filter installed applications instantly by name, package ID, or installation date with smooth card-level animations.

### 🎨 Dynamic App Icons & Theming
- **12 Dynamic Launcher Icons**: Switch between 12 distinct launcher app icons (Original Classic Blue, Cyber Lime, Ruby Blaze, Ocean Teal, Frost White, Neon Indigo, Deep Sapphire, Electric Azure, Emerald Green, Royal Violet, Amber Sunset, and Stealth Onyx) via Android manifest activity aliases.
- **Settings Icon Picker**: Modal bottom sheet featuring 68dp high-resolution previews, active indicators, and the Original icon listed first.
- **12 Custom Icon-Matching Themes**: Complete dark/light/AMOLED color schemes precisely tailored to match every launcher icon style.
- **12 Curated Color Accents**: Named after the launcher icons with exact hex color palettes.

### 🌐 Standalone WebAPK Runtime
- **Site-Native Dark Mode**: Automatically honors website dark/light styling without distortion.
- **Ad & Gambling Redirect Blocker**: Real-time interceptor blocking intrusive popunders, ad tracking scripts, and gambling domains.
- **Whitespace Collapsing**: Uses dynamic CSS and `MutationObserver` to collapse empty ad slots to zero height.
- **Google OAuth & Device Account Sync**: Automatic discovery of logged-in Google Accounts via `AccountManager` for 1-tap Google Sign-In.
- **Persistent Session Cookie Disk Sync**: Proactive cookie flushing ensuring login states persist across device restarts.
- **HTML5 File Picker & WebRTC**: Supports camera, microphone, geolocation, and multi-file document uploads.

### ⚡ Binary Engine & APK Signing
- **100% Offline Compilation**: Operates without AAPT, AAPT2, or external Java runtimes.
- **16KB Page Alignment**: Guarantees Android 15 kernel compatibility via `ElfAligner16k`.
- **Dual Signature Scheme**: Generates APK Signature Scheme v2 and v3 signatures compliant with Google Play and Android package verifiers.

### 📜 Build History & Smart Versioning
- **Streamlined History Cards**: Cards feature strictly two actions: **Reuse Config** (`FilledTonalButton`) and **Remove** (`FilledTonalButton` with error-tonal confirmation sheet).
- **1-Per-Row Grid**: Beautiful history cards displaying actual app icons, build timestamps, versions, package names, and output paths.
- **Config Auto-Matching**: Entering a previously built URL automatically populates earlier settings and increments the version string.

### ⚙️ Settings & Dynamic Theming
- **Auto-Delete APKs Toggle & iOS Switch**: Custom iOS-style toggle switch (`PackoraIosSwitch`) in Settings to automatically delete APK files after successful installation, preserving device storage while safely retaining queued or uninstalled APKs in `Downloads/Packora`.
- **Custom Pulse Dot Loader**: Embedded 8-dot pulsing canvas loader (`PackoraDotLoader`) providing sleek, consistent loading states across the app.
- **Material You Dynamic Theming**: Adapts to system wallpaper colors or selects from **12 icon-matched themes and color accents**.
- **Browser Engine Selector**: Switch between System Default WebView, Chrome Engine, or Custom Tab runtimes.
- **Full Factory Reset**: 1-tap wipe to restore all settings and form inputs to default values.

---

## 🔒 Permissions & Security Model

Packora adheres to strict privacy standards. It contains **no third-party tracking SDKs**, **no analytics**, and **no network calls** other than loading the target website you specify.

| Permission | Purpose in Packora |
| :--- | :--- |
| `INTERNET` | Loading web applications and downloading target web favicons. |
| `ACCESS_NETWORK_STATE` | Detecting online/offline connectivity to display offline fallback screens. |
| `QUERY_ALL_PACKAGES` | Inspecting installed WebAPKs for update status and management in My Apps. |
| `REQUEST_INSTALL_PACKAGES` | Triggering native Android package installation for compiled APKs. |
| `REQUEST_DELETE_PACKAGES` | Triggering native Android package uninstallation from My Apps bottom sheet. |
| `POST_NOTIFICATIONS` | Delivering status bar notifications for build completion and WebAPK push events. |
| `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` | Selecting custom launcher icons and files from device storage. |
| `CAMERA` / `RECORD_AUDIO` | Delegated to WebAPK runtime for HTML5 WebRTC video calls and audio capture. |
| `ACCESS_FINE_LOCATION` | Delegated to WebAPK runtime for map services and geolocation. |

---

## 🛠️ Technology Stack

| Component | Library / Framework | Version | Details |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.2.10` | Coroutines, Flow, modern functional syntax |
| **UI Toolkit** | Jetpack Compose | `2026.02.01 (BOM)` | Material Design 3, Navigation, Animations |
| **Android SDK** | Android SDK | `API 35 (15)` | Min SDK: 24 (Android 7.0+), Compile: 35 |
| **Signing Engine** | Android `apksig` | `8.3.0` | Cryptographic V2 / V3 signature generation |
| **Page Alignment** | In-House `ElfAligner16k` | `v4.1.0` | 16KB ELF boundary alignment |
| **Binary Engine** | In-House `AxmlRebuilder` & `ArscRebuilder` | `v4.1.0` | Low-level byte-level binary manifest rewriter |
| **Template Engine** | In-House `:template` Shell | `v4.1.0` | High-performance standalone WebAPK wrapper |

---

## 📂 Repository Structure

```text
Packora/
├── app/                                 # Primary Packora application module
│   ├── src/main/java/.../packora/
│   │   ├── MainActivity.kt              # App entry point & navigation host
│   │   ├── builder/                     # Binary compiler engine
│   │   │   ├── ApkBuilder.kt            # Compilation pipeline orchestrator
│   │   │   ├── AxmlRebuilder.kt         # Binary AndroidManifest.xml rebuilder
│   │   │   ├── ArscRebuilder.kt         # Binary resources.arsc rebuilder
│   │   │   ├── ElfAligner16k.kt         # 16KB ELF boundary aligner
│   │   │   ├── JarSigner.kt             # apksig V2/V3 cryptographic signing
│   │   │   └── ZipAligner.kt            # 4-byte ZIP entry alignment
│   │   ├── manager/                     # Persistent managers
│   │   │   ├── AppIconManager.kt        # Dynamic launcher app icons manager
│   │   │   ├── BuildHistoryManager.kt   # History tracking & auto-versioning
│   │   │   └── PackoraPreferencesManager.kt # User settings & accents
│   │   └── ui/                          # Jetpack Compose UI screens
│   │       ├── BuildScreen.kt           # Dashboard & Bento Builder UI
│   │       ├── UpdatesScreen.kt         # Dedicated WebAPKs updates hub
│   │       ├── MyAppsScreen.kt          # Installed WebAPKs manager & search
│   │       ├── HistoryScreen.kt         # Build history grid
│   │       ├── SettingsScreen.kt        # App configuration & accents
│   │       ├── AboutScreen.kt           # App info, credits & links
│   │       └── components/              # Shared Compose UI components
│   │           └── PackoraCustomSwitch.kt # Ported animated custom toggle switch
│   └── proguard-rules.pro               # App module R8/ProGuard configuration
├── template/                            # Embedded WebAPK shell source module
│   ├── src/main/java/.../template/
│   │   └── MainActivity.kt              # Standalone WebAPK activity window
│   └── proguard-rules.pro               # Shell R8/ProGuard configuration
├── .github/                             # Workflows & community templates
│   ├── workflows/build.yml              # CI/CD multi-architecture release builder
│   └── ISSUE_TEMPLATE/                  # GitHub issue templates
├── CHANGELOG.md                         # Detailed version changelog
└── README.md                            # Project documentation
```


---

## 🏗️ Building from Source

### Prerequisites
- **JDK 17** or higher configured (`JAVA_HOME`).
- **Android Studio Ladybug (2024.2+)** or command-line Android SDK.
- **Android SDK Platform 35** and **Build-Tools 35.0.0**.

### Quick Build Instructions

```bash
# 1. Clone the repository
git clone https://github.com/maheswara660/Packora.git
cd Packora

# 2. Compile debug APK
./gradlew :app:assembleDebug

# 3. Compile full release APKs (Universal + ABI splits)
./gradlew :app:assembleRelease
```

Compiled APK files will be located at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/`

---

## 🤝 Contributing

Contributions are warmly welcome! Whether fixing a bug, suggesting a feature, or optimizing the binary engine:

1. **Fork** the repository on GitHub.
2. **Create a branch** for your feature:
   ```bash
   git checkout -b feature/my-new-feature
   ```
3. **Commit** your changes:
   ```bash
   git commit -m "feat: Add support for custom WebChromeClient geolocation"
   ```
4. **Push** to your fork:
   ```bash
   git push origin feature/my-new-feature
   ```
5. **Open a Pull Request** with a detailed explanation of your changes.

Please make sure your code adheres to Kotlin coding conventions and passes `./gradlew :app:assembleDebug`.

---

## 📜 License & Acknowledgments

Packora is free and open-source software licensed under the **[GNU General Public License v3.0](LICENSE)**.

- **Author & Lead Developer**: [Maheswara660](https://github.com/maheswara660)
- **Support the Project**:
  - [Buy me a coffee on Ko-fi](https://ko-fi.com/maheswara660)
  - [GitHub Sponsors](https://github.com/sponsors/maheswara660)

---

<p align="center">
  <b>Packora v4.1.0 — Unlocking Web-to-APK Limits.</b><br>
  Built with ❤️ for the Android open-source community.
</p>
