<p align="center">
  <img src="assets/app_icon.webp" width="180" height="180" alt="Packora Logo">
</p>

<h1 align="center">Packora v2.0.0</h1>

<p align="center">
  <b>The ultimate on-device Web-to-APK engine for Android.</b><br>
  Built with Jetpack Compose, native in-house template compilation, binary AXML manipulation, and modern Bento UI architecture.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-2.0.0-blue?style=for-the-badge" alt="Version 2.0.0">
  <img src="https://img.shields.io/badge/Language-Kotlin%202.0.0-blue?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/Android-Sdk%2035-3DDC84?style=for-the-badge&logo=android" alt="Android">
  <img src="https://img.shields.io/badge/License-GPLv3-green?style=for-the-badge" alt="License">
</p>

---

## 📝 About Packora
Packora is a powerful, completely offline Android utility that enables you to convert any website into a standalone native Android application (APK) directly on your device. No cloud servers, no PC, no complex IDE setups. 

With **v2.0.0**, Packora undergoes a complete architectural overhaul—featuring an in-house Gradle template module (`:template`), 16KB page-aligned ELF native libraries for Android 15+ compatibility, automatic high-res URL favicon fetching, custom signing keystores, and a streamlined Bento Dashboard.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Core Language** | Kotlin | `2.0.0` | Modern, safe, and asynchronous. |
| **UI Framework** | Jetpack Compose | `2024.12.01 (BOM)` | Declarative Bento grid interface. |
| **Template Engine** | In-House Android Template | `v2.0.0` | Native `:template` module with embedded WebView shell. |
| **Binary Engine** | Custom AXML Parser | `v2.0.0` | Direct binary `AndroidManifest.xml` & `.arsc` string manipulation. |
| **Security Layer**| apksig & Custom Keystore | `v2.0.0` | V2/V3 APK signing supporting custom PKCS12 / JKS keystores. |
| **Page Alignment**| 16KB Page Aligner | `v2.0.0` | Ensures native `.so` binaries align to 16KB pages for Android 15+. |

---

## ✨ Key Features in v2.0.0

### ⚡ Complete On-Device WebAPK Engine
*   **100% Offline Compilation**: Generate signed APKs instantly on your device without cloud dependencies.
*   **16KB Memory Page Alignment**: Realigns native `.so` libraries inside generated APKs to meet Android 15+ kernel memory constraints.
*   **Custom Storage Routing**: Choose where generated WebAPKs are stored on your device.

### 🎨 Smart Branding & Icon Fetching
*   **Auto Favicon Downloader**: Input any URL and Packora automatically fetches the highest-resolution site favicon/icon.
*   **Android Mascot Icon**: Default fallback icon features the Android mascot head on a white background.
*   **Adaptive Launcher Icons**: Generates full multi-density adaptive PNG/WEBP launcher icon suites for clean launcher grid displays.

### 🌐 Advanced WebView Runtime
*   **Desktop Mode Toggle**: Seamlessly view desktop websites with standard responsive viewport scaling.
*   **System Theme Matching**: Status and navigation bar colors seamlessly sync with Android System Dark/Light appearance.
*   **Native File Downloads & Uploads**: Embedded Chrome FileChooser and DownloadManager integration.

---

## 📸 Interactive UI Gallery

<table align="center">
  <tr>
    <td align="center"><b>Simple Dashboard</b></td>
    <td align="center"><b>Advanced Settings</b></td>
  </tr>
  <tr>
    <td><img src="assets/simple_menu.png" width="260" style="border-radius: 14px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);"></td>
    <td><img src="assets/advanced_settings.png" width="260" style="border-radius: 14px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);"></td>
  </tr>
</table>

<table align="center">
  <tr>
    <td align="center"><b>Custom Keystore Setup</b></td>
    <td align="center"><b>Storage Location Picker</b></td>
  </tr>
  <tr>
    <td><img src="assets/custom_keystore.png" width="260" style="border-radius: 14px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);"></td>
    <td><img src="assets/storage_location.png" width="260" style="border-radius: 14px; box-shadow: 0 4px 8px rgba(0,0,0,0.2);"></td>
  </tr>
</table>

---

## 🚀 Installation & Requirements

### System Requirements
* **OS**: Android 8.0 (Oreo) or higher (Min SDK 24, Target SDK 35).
* **Architecture**: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`.

---

## 🏗️ Build from Source

Ensure you have **Android Studio Ladybug** (or later) and **JDK 17/21** configured.

```bash
# Clone the repository
git clone https://github.com/maheswara660/Packora.git
cd Packora

# Build template shell APK
./gradlew :template:assembleRelease

# Copy template shell to app assets
cp template/build/outputs/apk/release/template-release-unsigned.apk app/src/main/assets/template/webview_shell.apk

# Build Packora release variant
./gradlew :app:assembleRelease
```

---

## 📜 License
Packora is open-source software licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <b>Packora v2.0.0 — Unlocking Web-to-APK limits.</b><br>
  Made with ❤️ in India
</p>
