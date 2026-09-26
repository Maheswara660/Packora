# Architecture

The fundamental architectural principle in Packora: **the Studio host (`:app`) and the generated standalone WebAPK (`:template`) are decoupled, high-performance Android modules connected via binary instrumentation and asset configuration.**

## The Two Environments

```text
Packora Studio (:app)
  ├─ User Interface (Jetpack Compose, Midnight Theme)
  ├─ Domain Model: PackoraApp (WebConfig, PrivacyConfig, NetworkConfig, etc.)
  ├─ Extraction & Analysis (Metadata Scraper, Favicon Downloader, Manifest Parser)
  └─ Binary Instrumentation Engine:
       ├─ ApkTemplate (Locates base webview_shell.apk)
       ├─ AxmlRebuilder (Binary patches AndroidManifest.xml: package, name, permissions)
       ├─ ArscRebuilder (Binary patches resources.arsc string pools)
       ├─ Icon Density Inserter (Overlays mipmap icons across all densities)
       ├─ JSON Serializer (Injects assets/app_config.json)
       └─ JarSigner / PerAppSigningIdentity (Signs with V1, V2, and V3 schemes)
             ↓
       Standalone Signed WebAPK

Standalone WebAPK (:template / MainActivity)
  ├─ Loads assets/app_config.json via zero-overhead native JSONObject
  ├─ Configures Android WebView (DOM storage, JS interfaces, custom User-Agent)
  ├─ Applies Network Hardening (DNS-over-HTTPS, Strict DoH, ECH)
  ├─ Injects Privacy Guards (Canvas, WebGL, AudioContext, WebRTC IP masking)
  ├─ Handles Downloads, File Chooser, Biometrics, Credentials, Fullscreen, Orientation
  └─ Custom Tabs Auth Fallback & Native Error Handling
```

| Component | Studio Host (`:app`) | Generated WebAPK (`:template`) |
| --- | --- | --- |
| **Namespace** | `com.maheswara660.packora` | `com.maheswara660.packora.template` |
| **Target SDK** | Android 15 (API 35) | Android 15 (API 35) |
| **Minimum SDK** | Android 7.0 (API 24) | Android 7.0 (API 24) |
| **UI Framework** | Jetpack Compose + Material 3 | Optimized Android Views + Hardware-Accelerated WebView |
| **Configuration** | In-memory `PackoraApp` & SharedPreferences | Embedded `assets/app_config.json` |
| **Dependencies** | Studio tools, Compose, Coroutines | Ultra-lean, zero bloat, zero reflection |

## The Configuration Flow

When a user configures and builds an app in Packora:

1. **Model** — Settings are collected in `PackoraApp` and its sub-configs (`PackoraWebConfig`, `PackoraPrivacyConfig`, `PackoraNetworkConfig`, `PackoraAdBlockConfig`, `PackoraSecurityConfig`).
2. **Serialization** — `ApkBuilder.kt` compiles these settings into a structured `app_config.json`.
3. **Template Staging** — `ApkTemplate` extracts the pre-built `webview_shell.apk` from Studio assets.
4. **Binary Patching**:
   - `AxmlRebuilder` modifies package name, app name resource IDs, orientation, and hardware acceleration in binary `AndroidManifest.xml`.
   - `ArscRebuilder` updates the app title string within the compiled resource table (`resources.arsc`).
   - The app's chosen launcher icon is scaled and injected across all standard Android DPI directories (`mipmap-mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).
5. **Signing** — `JarSigner` signs the APK using either Packora's default keystore, a generated per-app identity, or a user-provided PKCS12 keystore.
6. **Execution** — The resulting WebAPK runs completely standalone without requiring Packora to remain installed on the device.

## The Template APK Module

There is exactly **one** canonical shell template: `webview_shell.apk`, located at `app/src/main/assets/template/webview_shell.apk`. It is compiled directly from the `:template` module via the Gradle task `:template:assembleRelease` and automatically staged by `:app:copyTemplateApk`.
