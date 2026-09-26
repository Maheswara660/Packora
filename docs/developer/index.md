# Developer Docs

This section is for developers and contributors working on Packora itself. It explains how the codebase is architected, how the on-device APK generation pipeline operates, and how the template runtime executes standalone WebAPKs.

::: tip Authoritative reference
[`README.md`](https://github.com/Maheswara660/Packora/blob/main/README.md) at the repository root outlines installation, feature matrix, and licensing under the GNU General Public License v3.0 (GPLv3).
:::

## Repository layout

| Path | Role |
| --- | --- |
| `app/` | Packora Studio host: Jetpack Compose UI, APK compilation engine, signing, scraping, and preferences. |
| `template/` | Standalone WebAPK shell template module: lightweight WebView container, download manager, custom tabs, biometric/credman, and native callbacks. Built to `app/src/main/assets/template/webview_shell.apk`. |
| `docs/` | Official documentation site powered by VitePress. |
| `assets/` | Brand assets, high-res preview graphics, and screenshots. |

## The Gradle modules

- **`:app`** — The Studio & Builder.
  - `namespace = "com.maheswara660.packora"`
  - `applicationId = "com.maheswara660.packora"`
  - `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`
  - Java 17 toolchain, Kotlin Compose compiler.
  - Orchestrates on-device binary manipulation (`AxmlRebuilder`, `ArscRebuilder`, `JarSigner`, `ApkBuilder`).
- **`:template`** — The standalone WebAPK Shell.
  - `namespace = "com.maheswara660.packora.template"`
  - `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`
  - Assembled to release APK and staged to `app/src/main/assets/template/webview_shell.apk` automatically via the `:app:copyTemplateApk` task before host compilation.

## Package structure (`app/src/main/java/com/maheswara660/packora`)

- **`builder/`** — Core APK generation pipeline: `ApkBuilder.kt`, `ApkTemplate.kt`, `AxmlRebuilder.kt`, `ArscRebuilder.kt`, `JarSigner.kt`, `PerAppSigningIdentity.kt`, `AppLogger.kt`.
- **`model/`** — Domain models and configuration: `PackoraApp.kt`, `PackoraAppType`, `PackoraWebConfig`, `PackoraNetworkConfig`, `PackoraPrivacyConfig`, `PackoraAdBlockConfig`, `PackoraSecurityConfig`, `PackoraSplashConfig`, `PackoraServerConfig`.
- **`manager/`** — System state & persistence: `PackoraPreferencesManager.kt` (settings, sort orders, update install modes, themes, directories).
- **`ui/`** — Jetpack Compose modern UI screens and design system:
  - `BuildScreen.kt` — Build Studio, app configuration, capability cards, bottom sheet menus.
  - `MyAppsScreen.kt` — Installed app list, quick launch, sorting, search, action sheets.
  - `SettingsScreen.kt` — Global preferences, update check modal, backup/restore, signing keys.
  - `HistoryScreen.kt` — Build history, output manager, APK installer.
  - `components/` — `PackoraIosSwitch`, `PackoraDotLoader`, `PackoraCard`, `PackoraDropdownMenu`.
- **`adblock/`** — AdGuard & cosmetic filtering engine, custom hosts rule parser.
- **`analyzer/`** — Web page deep analyzer, metadata/manifest extractor, favicon scraper.
- **`dns/`** — DNS-over-HTTPS (DoH) engine with Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, and Custom providers.
- **`privacy/`** — Canvas, WebGL, WebRTC IP, AudioContext, and timezone spoofing scripts.
- **`scraper/`** — Media and offline pack crawler.
- **`crypto/`** — Resource encryption, keystore generation, and integrity verifiers.

## Where to read next

- [Architecture](/developer/architecture) — Preview-vs-export execution models.
- [Export Pipeline](/developer/export-pipeline) — How `PackoraApp` transforms into a signed APK.
- [Template & Shell Architecture](/developer/shell-sync) — How the template APK is built and customized.
- [Config Field Drift](/developer/config-drift) — Keeping JSON schema synced between Studio and Template.
- [Change Recipes](/developer/recipes) — Everyday development workflows.
- [Contributing](/developer/contributing) — Contribution guidelines and standards.
