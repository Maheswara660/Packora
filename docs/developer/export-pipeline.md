# Export Pipeline

The export pipeline turns a `PackoraApp` model into an installable, standalone signed Android APK. It lives in `app/src/main/java/com/maheswara660/packora/builder/`.

## Key classes

| File | Role |
| --- | --- |
| `ApkBuilder.kt` | Orchestrates APK assembly, ZIP streaming, configuration embedding, and signature dispatch. |
| `ApkTemplate.kt` | Extracts and caches the pre-built `webview_shell.apk` template from Android assets. |
| `AxmlRebuilder.kt` | Modifies the binary `AndroidManifest.xml` (AXML format) to rewrite package names, app name labels, screen orientation, and hardware acceleration flags. |
| `ArscRebuilder.kt` | Modifies the binary resource table (`resources.arsc`) to replace the application title string in the global string pool without requiring full AAPT compilation. |
| `JarSigner.kt` | Signs the APK with V1 (JAR signing), V2 (APK Signature Scheme v2), and V3 schemes using `android-apksig` or custom BouncyCastle keystore engines. |
| `PerAppSigningIdentity.kt` | Generates unique, reproducible keystores per application identity for clean updates and installation safety. |
| `AppLogger.kt` | Unified logging utility capturing pipeline stages and debugging metrics. |

## The Pipeline Flow

```text
PackoraApp (Studio Model)
  │
  ├─ 1. Validation & Staging
  │    ├─ Validate URL, package name, and version code
  │    └─ Extract template APK from assets/template/webview_shell.apk
  │
  ├─ 2. Configuration Serialization
  │    └─ Build JSON object containing:
  │         - appName, packageName, targetUrl, versionCode, versionName
  │         - isDesktopMode, forceDarkMode, enableZoom, allowCopying
  │         - enableWebFooter, hideWebFooter
  │         - privacyConfig (fingerprint disguise, WebGL/Canvas/WebRTC masking)
  │         - adBlockConfig (trackers, cosmetic filtering, hosts rules)
  │         - networkConfig (DoH provider, custom endpoint, strict DoH, ECH)
  │
  ├─ 3. Binary Mutation (Zip Stream)
  │    ├─ Inject assets/app_config.json
  │    ├─ Patch AndroidManifest.xml (AxmlRebuilder)
  │    ├─ Patch resources.arsc (ArscRebuilder)
  │    └─ Scale and inject launcher icon across mipmap/drawable densities
  │
  ├─ 4. Digital Signature (JarSigner)
  │    ├─ Compute SHA-256 digests of ZIP entries and manifest
  │    ├─ Apply V1 JAR signature block (META-INF/*.SF, *.RSA)
  │    ├─ Apply V2 / V3 APK Signing Block (magic 0x71065442)
  │    └─ Output aligned, signed APK to destination storage
  │
  └─ 5. Completion
       └─ Register output with File Manager & Build History
```

## Binary Mutation Architecture

Instead of running heavy command-line AAPT or Gradle compilations on the mobile device, Packora performs **in-place binary reconstruction**:

- **AXML Processing**: `AxmlRebuilder` walks the binary XML tree, identifies string pool entries, and safely rewrites UTF-8/UTF-16 string constants while updating table offsets.
- **ARSC Processing**: `ArscRebuilder` parses the Android compiled resource table header, locates the global string pool chunk, and substitutes the app name string directly into the compiled table.
- **Icon Replacement**: The user-selected icon is rendered into high-quality Bitmaps, compressed to WebP/PNG, and inserted into all standard density buckets:
  - `res/mipmap-mdpi/ic_launcher.png`
  - `res/mipmap-hdpi/ic_launcher.png`
  - `res/mipmap-xhdpi/ic_launcher.png`
  - `res/mipmap-xxhdpi/ic_launcher.png`
  - `res/mipmap-xxxhdpi/ic_launcher.png`

## Signature Schemes

Packora supports flexible, hardened signing:
1. **Default Packora Identity** — Fast, pre-configured signing for immediate testing and personal use.
2. **Per-App Identity** (`PerAppSigningIdentity`) — Automatically provisions a deterministic private key derived from the app's package name.
3. **Custom Keystore** — Supports PKCS12 (`.p12` / `.keystore`) keys with custom passwords, aliases, and certificate validity periods up to 25+ years.
