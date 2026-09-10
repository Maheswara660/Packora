# Changelog

All notable changes to the **Packora** project will be documented in this file.

## [2.3.0] - 2026-09-10
### Added & Enhanced
- **Ad & Gambling Redirect Link Blocker**: Injected real-time URL interceptor (`isAdOrGamblingUrl`) in WebAPK shell (`shouldOverrideUrlLoading` & `onCreateWindow`) to automatically block ad popunder redirects, tracking URLs, and gambling networks (Parimatch, 1xBet, Bet365, PopAds, PopCash, Adsterra, PropellerAds, ExoClick, DoubleClick, etc.).
- **In-App Ad Blocker & Whitespace Collapsing**: Implemented `injectAdBlockerAndSpaceCollapsing` with dynamic CSS injection (`display: none !important`, `height: 0 !important`) and JavaScript `MutationObserver` to hide ad slots (`ins.adsbygoogle`, `iframe[src*="doubleclick"]`, `.ad-container`, `.ad-wrapper`, `.sponsored-content`) and collapse empty white space to zero height in real time.
- **Installed Native App & WebAPK Deep Linking**: Built `tryLaunchInInstalledNativeApp` supporting `intent://` URIs and standard `http(s)` links to automatically resolve and route links to installed native Android apps (YouTube, LinkedIn, Instagram, Twitter, Spotify) or other Packora WebAPKs on device.
- **Native Web-to-Android System Bar Notifications**: Created `NotificationBridge` JavaScript interface (`showNotification`) to forward web app push notifications directly to high-priority Android status bar notifications with notification channels, custom titles, and launch intents.
- **Crisp High-Resolution Icon Engine**: Optimized icon scaling and adaptive foreground density calculations (`xxxhdpi` up to `432x432 px`), reading native dimensions directly from template ZIP entries (`outWidth`/`outHeight`) before replacement to eliminate launcher icon blurriness on modern high-DPI Android displays.
- **Interactive Icon Zoom & 31+ Background Color Palette**: Added a 31+ background color fill palette with miniature colored circle badges and scale factor controls in `IconZoomerBottomSheet` for customizing app icon backgrounds.
- **Multi-Source Icon Selector & 2x2 Control Matrix**: Integrated a scrollable multi-source icon selector sheet (fetching from HTML tags, Apple touch icons, Clearbit, DuckDuckGo, Google, Yandex, and Unavatar APIs) and reorganized dashboard icon action controls into an interactive 2x2 grid (`Retry Icon`, `Zoom & Fill`, `Icon Sources`, `Remove Icon`).
- **Universal Single APK Architecture**: Disabled multi-ABI splits (`splits.abi.isEnable = false`) in favor of a single universal installer APK compatible across all Android hardware architectures (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).
- **Material Design 3 Offline & Connection Error Screen**: Replaced legacy error overlay in WebAPK shell with a modern Material 3 `CardView` container (`24dp` rounded corners, `#1E1E1E` surface background, `#2E2E2E` stroke border), bold `Connection Offline` title, and a green pill `RETRY RELOAD` button.
- **Direct App Launch Experience**: Removed initial splash loader overlay (`loaderOverlay`) so generated WebAPKs launch directly into the standalone WebView container.
- **HTML5 File Uploads**: Enabled native HTML5 `<input type="file">` file uploads connected to system document picker (`Intent.ACTION_GET_CONTENT`) for uploading single or multiple files and media inside WebAPKs.
- **Material 3 BottomSheet Standardization**: Standardized all Modal BottomSheets across Packora with centered headers, icon badges, surface containers (`#1E1E1E`), and 48dp/50dp rounded action buttons.
- **Streamlined UI & Input Placeholders**: Updated home screen input field hints to display generic standard examples (`com.example.myapp` and `1.0.0`) while preserving automatic functional compilation fallbacks (`com.maheswara660.packora.<appname>` and `1.0.0`).
- **Clean Navigation & Privacy**: Removed legacy Cloudflare scripts from template shell while preserving native cookie management, WebRTC permissions, ProGuard keep rules for bridges, and Android Password Manager Autofill integrations.

## [2.2.0] - 2026-08-26
### Added & Enhanced
- **Modern BottomSheet Selection Dialogs**: Redesigned selection menus for App Theme, Color Accent, and Browser Engine with glassmorphic cards, glowing active selection outlines, checkmark badges (`CheckCircle`), and bold `CANCEL` / `OK` action buttons.
- **AboutScreen Architecture Change**: Rebuilt `AboutScreen.kt` featuring dynamic app icon extraction (`packageManager.getApplicationIcon`), `DeveloperCard` (`Developed by Maheswara660`), grouped `AboutActionItem` cards with adaptive corner rounding, and community footer.
- **Standalone App Window (`SYSTEM_DEFAULT` Engine)**: Generated WebAPKs load target URLs directly inside their own standalone activity window, combining mobile Chrome User-Agent header, system cookie sync, and Android Password Manager Autofill (`IMPORTANT_FOR_AUTOFILL_YES`).
- **Smart Build History & Auto-Versioning**: Automatically matches target URLs against previous build history on entry/autofill and auto-increments `versionCode` (`+1`) and `versionName` (`1.0.0` ➔ `1.0.1`), resolving `INSTALL_FAILED_UPDATE_INCOMPATIBLE` package update conflicts.
- **Optimized Gesture Navigation Inset Padding**: Dynamic bottom padding calculation reduces empty bottom space to `4.dp` when gesture navigation mode is enabled, while retaining full `navigationBars` protection for 3-button navigation mode.
- **WebRTC Camera, Microphone, Geolocation & Biometrics Permissions**:
  - Injected `CAMERA`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `VIBRATE`, and `USE_BIOMETRIC` permissions.
  - Implemented `WebChromeClient.onPermissionRequest` and `onGeolocationPermissionsShowPrompt` for WebRTC video/audio streams and maps.
- **Navigation BackStack & System BackHandler**: Integrated Jetpack Compose `BackHandler` and backstack array (`navigationStack`), providing seamless system back gesture navigation between `DASHBOARD`, `SETTINGS`, `HISTORY`, and `ABOUT`.
- **Package Installer Extra Flags**: Added `EXTRA_NOT_UNKNOWN_SOURCE`, `EXTRA_ALLOW_REPLACE`, and `EXTRA_INSTALLER_PACKAGE_NAME` to APK installation intents for smoother package updates.

## [2.1.0] - 2026-08-14
### Added & Enhanced
- **Dynamic Status Bar & Navigation Bar Contrast**: System status bar and navigation bar background colors dynamically sync with the web app header/theme color in real-time. Status bar font and system navigation buttons automatically adapt between **BLACK** (for light backgrounds) and **WHITE** (for dark backgrounds).
- **Algorithmic Dark Mode**: Added `WebSettingsCompat.setAlgorithmicDarkeningAllowed` support to WebAPK shell, allowing web pages to render in Dark Mode matching system appearance.
- **Android Password Manager & Autofill Integration**: Enabled `importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES` and added an `AutofillBridge` JavaScript interface to trigger system Password Managers (Google Password Manager, Bitwarden, 1Password) on web login forms.
- **Stale Icon Reset**: Fixed an issue where changing the input URL retained the previously fetched app icon. Now URL edits immediately clear cached icons and fetch fresh favicons.
- **High-Precision Icon Scaling**: Rewrote bitmap scaling in `ApkTemplate.kt` using direct `Matrix` transformations with anti-aliasing, filtering, and dithering for razor-sharp launcher icons.
- **Automated Template Build Pipeline**: Updated `:app` Gradle build configuration to automatically assemble `:template:assembleRelease` and refresh `webview_shell.apk` in assets before every build.

## [2.0.0] - 2026-07-31
### Added & Enhanced
- **Native In-House Template Module (`:template`)**: Replaced external static shell binaries with an integrated Gradle `:template` application module. The WebAPK shell is now compiled alongside Packora, ensuring 100% binary consistency and easy future extensibility.
- **16KB Page Alignment for Android 15+**: Integrated native ELF 16KB page alignment logic for `.so` shared libraries embedded inside generated WebAPKs to satisfy upcoming Android 15 kernel constraints.
- **AAPTS2 Resource Obfuscation Support**: Engineered `ArscRebuilder` to parse `.arsc` string pools and type chunks, allowing Packora to discover and replace all 25+ obfuscated launcher icon resource paths (`res/9w.png`, `res/3z.png`, etc.) generated by AAPT2 resource optimization.
- **Automatic Favicon Fetcher**: Built `fetchPremiumIcon` to automatically scrape high-resolution site icons and favicons (HTML link tags, DuckDuckGo icon API, Google Favicon API, Apple Touch icons) as soon as the user enters a URL.
- **Android Mascot Default Branding**: Updated fallback launcher icons to feature the official green Android mascot head on a clean white background (`#FFFFFF`).
- **HTML5 File Uploads & Storage Access**:
  - Embedded `WebChromeClient.onShowFileChooser` connected to native Android document picker (`Intent.ACTION_GET_CONTENT`), allowing users to upload files and media from `<input type="file">` controls in generated WebAPKs.
  - Added full storage/media permissions suite (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`).
- **DownloadManager Completion Notifications**:
  - Integrated system `DownloadManager` in WebAPK runtime with `VISIBILITY_VISIBLE_NOTIFY_COMPLETED`.
  - Added runtime `POST_NOTIFICATIONS` permission request on Android 13+ so users receive system status bar notifications when file downloads start and complete.
- **Desktop Mode & Responsive Viewport**:
  - Implemented responsive desktop viewport injection (`width=1024, user-scalable=yes`) in WebAPK shell without top-left zoom offset locks.
  - Added a Desktop Mode toggle in the main dashboard UI to force WebAPKs to load sites in Desktop Mode.
- **System Theme Integration**: Dynamically syncs WebAPK status bar and navigation bar icon contrast with system Light/Dark appearance.
- **UI Architecture Evolution & Bento UI**:
  - Streamlined Packora UI into a unified dashboard, completely removing obsolete welcome/instruction onboarding screens.
  - Settings are cleanly grouped into distinct Material Elevated Cards (App Identity, Configuration, Security) for much better readability.
  - Primary "GENERATE APP" action and its progress indicator are placed in a sticky bottom bar, keeping it always accessible without scrolling.

### Changed
- **Package Name Fallback**: Changed default package name generation pattern from `com.maheswara660.<app-name>` to `com.maheswara660.packora.<app-name>` to clearly identify apps generated by Packora.

## [1.1.0] - 2026-07-11
### Added
- **Custom Download Folder Options**: Utilize a native Android Folder Picker to specify precisely where the generated WebAPK should save downloads instead of the internal, scoped app data directory.
- **Improved UI Flow**: Reordered the app creation form for a more intuitive build sequence (e.g. prioritizing Custom Download Folder and grouping related Advanced Settings).
- **Download Notifications Support**: WebAPKs are now automatically injected with the `POST_NOTIFICATIONS` permission so the user receives a status bar notification when a download finishes on modern Android devices.

### Changed
- **Smart App Icon Preview**: The icon size has been increased to 72dp for better visibility. The preview also now defaults to the Android Mascot icon if an image hasn't been uploaded or fetched.
- **Icon Proportion Balancing**: Modified the icon packaging engine to implement an 85% safe zone. This effectively shrinks the visual size of generated app icons inside the Android launcher by 15-20%, resolving the issue of over-scaled icons.

## [1.0.0] - 2026-06-30
### Added
- **Initial Release**: Launching Packora as a powerful, on-device Web-to-APK builder.
- **Native AXML Rebuilder**: Modify package names, permissions, and app names directly via binary AXML manipulation without external tools.
- **Dynamic APK Signing**: Integrated robust V2/V3 APK Signature Scheme via `apksig` allowing custom on-the-fly Keystore generation.
- **Web App Templating**: Seamlessly wrap any URL into a high-performance WebAPK shell with customizable icons.
- **Offline Compilation**: Build fully functioning Android applications completely offline, directly on the device.
- **Clean Architecture**: Built using clean ViewModels, Jetpack Compose UI, and optimized ProGuard integration for minimal footprint.
