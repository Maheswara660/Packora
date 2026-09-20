# Changelog

All notable changes to the **Packora** project will be documented in this file.

## [3.2.1] - 2026-09-20
### Fixed & Enhanced
- **Universal Scheme Hijacking Elimination**:
  - **Removed Open-Ended Scheme Filters**: Removed standalone `<data android:scheme="https" />` and `<data android:scheme="http" />` intent filters from `AxmlRebuilder` and `ApkBuilder`. Compiled WebAPKs no longer advertise themselves as universal device-wide web browsers, completely eliminating unwanted app suggestions and chooser prompts when clicking links in Chrome or third-party apps.
  - **Strict Scheme-Host Pairing**: Deep link intent filters strictly pair both scheme and host together (`<data android:scheme="https" android:host="domain.com" />`), ensuring deep link intents only trigger for the exact domain the WebAPK was built for.
- **Cross-App WebAPK Isolation**:
  - **No Cross-App Launching**: Excluded Packora packages and WebAPK template activities from external intent resolution in `tryLaunchInInstalledNativeApp`. Compiled apps will never bounce between each other or launch other Packora apps.
  - **Intra-Domain Link Locking**: Enforced that intra-domain links, subdomains, query routes, and auth flows unconditionally open directly inside the active app's WebView without querying external native handlers.
- **Auth, Login & Sign Up Sandboxing**:
  - **Seamless Authentication**: Comprehensive SSO and identity provider routes (Google, GitHub, Apple, Microsoft, Twitter/X, Discord, Auth0, Okta, Supabase, Firebase, Cognito, Keycloak, etc.) load directly inside the app WebView without triggering external app delegations.
  - **Multi-Window OAuth Dialogue Safety**: Redesigned WebChromeClient `onCreateWindow` to route same-domain `target="_blank"` navigations directly into the primary WebView, while wrapping external OAuth popups in a managed dialog with a visible Close button header, progress bar, `setCanceledOnTouchOutside(true)`, and automatic dismissal on authentication completion.
- **Touch Freeze & Black Screen Fixes**:
  - **Touch Unresponsive State Fixed**: Eliminated unmanaged fullscreen transparent dialogs from `onCreateWindow` that previously trapped touch focus and froze user interaction.
  - **Black Screen GPU Compositing Resolved**: Replaced `binding.webView.setBackgroundColor(Color.TRANSPARENT)` with solid theme background colors (`#FFFFFF` in light mode, `#121212` in dark mode, or dynamic webpage theme color), preventing hardware acceleration buffer failures and transparent-canvas black screens.
  - **Manifest Task Isolation**: Configured `android:taskAffinity=""` on the template Activity to ensure every compiled WebAPK maintains a dedicated, isolated task stack.

## [3.2.0] - 2026-09-19
### Added & Enhanced
- **Unified My Apps & Updates Hub**:
  - **Seamless Hub Integration**: Merged the Updates screen into My Apps with an intelligent `Updates Available` summary banner, eliminating screen redundancy and establishing a clean 4-tab bottom navigation bar (Build, My Apps, History, Settings).
  - **Updates Available Banner**: Pinned to the top of My Apps displaying the total count of eligible updates with a 1-tap `UPDATE ALL` action and real-time background compilation status.
- **F-Droid & Aurora Store Style Sequential Install Queue**:
  - **Sequential Background Pipeline**: Tapping `UPDATE ALL` compiles all eligible apps in the background, then triggers the system package installer for one app at a time.
  - **Automated Progression Receiver**: Listens for system `ACTION_PACKAGE_REPLACED` and `ACTION_PACKAGE_ADDED` broadcasts, automatically prompting the installer for the next queued update the instant the previous app installation completes.
  - **Resume Fallback & Skip Control**: Verifies installed package version codes on app resume and provides a `SKIP` button if the user opts out of installing a specific app.
  - **Collision-Proof Output Naming**: Formats update APK files as `<package_name>_v<versionCode>.apk`, preventing file overwrite conflicts across separate builds.
- **Markdown-Formatted In-App Release Notes**:
  - **Rich Changelog Rendering**: Replaced raw markdown plain text inside the `App Update Ready!` bottom sheet with a dedicated `MarkdownText` renderer supporting markdown headers (`#`, `##`, `###`), bullet lists, bold text (`**`), italics (`*`), inline code pills (`` `code` ``), and horizontal rules (`---`).
- **One-Time "What's New" Changelog Bottom Sheet**:
  - **Automated Version Prompt**: Automatically displays a modal bottom sheet highlighting the latest release's features on initial launch following an update or install.
  - **Single Source of Truth**: Dynamically pulls release highlights from a shared `packoraReleases` registry without code duplication, completing with an `UNDERSTOOD` confirmation button.
- **WebAPK Shell Launch & Blackscreen Fix**:
  - **Isolated Task Affinity**: Configured `android:launchMode="singleTask"` and `alwaysRetainTaskState="true"` in the `:template` manifest to prevent WebAPKs from attaching to the package installer task or opening into other application windows.
  - **Lifecycle Surface Management**: Added `webView.onResume()` and `webView.onPause()` to resume Chromium hardware rasterizers and JavaScript execution upon app reopen, resolving black screen freezes.
  - **Clean Splash Removal**: Removed unneeded `splashScreen.setOnExitAnimationListener` intercepts on Android 12+ that caused black screen splash hangs.
  - **Renderer Crash Recovery**: Added `onRenderProcessGone` handler in `WebViewClient` to recover from Chromium terminations.
- **GitHub Sponsors Integration**:
  - Added dedicated **Sponsor on GitHub** action tile (`https://github.com/sponsors/maheswara660`) in the About screen alongside Ko-fi.

## [3.1.1] - 2026-09-19
### Added & Enhanced
- **Dedicated Compiled Updates Ready Section in My Apps**:
  - **Instant 1-Tap Installation**: Added a dedicated `Compiled Updates Ready` section pinned at the top of My Apps displaying pre-compiled update WebAPKs with version progression chips (`v<installed> ➔ v<ready>`), compile configuration badges, and direct `INSTALL UPDATE` actions without requiring any recompilation.
- **Non-Intrusive Silent Batch Compilation in Updates Screen**:
  - **Uninterrupted Workflow**: Updated "Update All" to compile all eligible apps sequentially in the background without auto-launching repetitive system package installer prompt dialogs after each build.
  - **Completion Notification**: Displays a batch completion toast when all compiles finish, allowing users to review and install updates on their own schedule.
- **Smart Pre-Compiled Update Detection**:
  - Automatically identifies whether an update WebAPK is already compiled and present in local storage, rendering an `Installed ➔ Ready` version chip, a direct green `INSTALL UPDATE` button, and an optional recompile icon button.
- **Clean Single Update Loader in Settings**:
  - Removed duplicate `CircularProgressIndicator` from the left icon badge in the "Check for Updates" preference tile, permanently restoring the static system update icon and displaying a single progress spinner on the far right while polling GitHub releases.
- **Template WebAPK Shell Polish**:
  - Cleaned up experimental Picture-in-Picture and freeform window handlers from the `:template` module for lighter, more stable standalone WebAPK builds.

## [3.1.0] - 2026-09-19
### Added & Enhanced
- **Floating Window & Freeform Windowing Engine for Generated WebAPKs**:
  - **Freeform Multi-Window Support**: Added `enterFloatingWindowMode()` utilizing Android's native `WINDOWING_MODE_FREEFORM` (`ActivityOptions.setWindowingMode(5)`) with Picture-in-Picture fallback, allowing compiled WebAPKs to run as movable, resizable floating pop-up windows directly over games and other apps.
  - **OEM Pop-Up View & Pen Window Parity**: Configured `MULTIWINDOW_LAUNCHER` category, Samsung Multi-Window metadata (`com.samsung.android.sdk.multiwindow.penwindow.enable`, `enableInstanceForAll`), and default window dimensions (`600dp x 800dp`, `gravity="top|end"`). WebAPKs now appear in Samsung One UI "Open in pop-up view", Xiaomi HyperOS floating windows, ColorOS/OxygenOS mini windows, and stock Android freeform task launchers.
- **Seamless Google Sign-In & Native Account Chooser Integration**:
  - **403 Disallowed Useragent Fix**: Suppressed the `X-Requested-With` header on authentication endpoints using `WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, emptySet())`, preventing Google OAuth from blocking in-app WebAPK logins.
  - **Clean Desktop/Mobile Chrome User-Agent**: Configured an authentic, un-hijacked modern Chrome User-Agent string adhering to Google identity platform security criteria.
  - **Non-Blocking Button Interception**: Removed event cancellations (`preventDefault` / `stopPropagation`) on Google SSO buttons in injected script handlers.
  - **Native Modal Account Chooser Dialog**: Implemented custom popup `Dialog` windowing for multi-window OAuth redirects (`onCreateWindow`) styled with a 170-alpha darkened scrim backdrop, rendering Google's "Choose an account to continue to..." dialog as an authentic centered modal over the WebAPK.
- **Passwordless Email Magic Link Clipboard Sync & Manual Input**:
  - **Automatic Clipboard Detection**: Built an automatic clipboard token scanner on app resume (`onResume`) that detects magic login URLs (from services like Notion, Slack, Substack, Medium, etc.) copied from external email clients and navigates directly without manual re-typing.
  - **Direct Magic Link Paste Dialog**: Added a dedicated manual paste dialog with real-time URL validation to instantly route magic links directly into the active session.
- **Dedicated In-App Update Bottom Sheet**:
  - **Converted Update Dialog**: Transformed the generic app generation dialog into a dedicated `App Update Ready!` installation bottom sheet on the Updates screen.
  - **Update Version Highlighting**: Displays previous vs. target version progression (`v<oldVersion> ➔ v<newVersion>`), build details, and a prominent green `INSTALL UPDATE` primary action button.
- **Updates Screen Card Design Parity**:
  - Standardized `UpdateAppCard` to match `MyAppsScreen` and `HistoryScreen` cards 1:1, featuring elevated `20.dp` rounded surfaces, tonal borders, `54.dp` launcher icon container with primary container tinting, version chips, and compilation badges.
- **Settings Screen Real-Time Update Checker Spinner**:
  - Replaced static "Checking" text on the update preference tile with a sleek Material 3 `CircularProgressIndicator` (20.dp) for smooth visual feedback while polling remote GitHub releases.

## [3.0.0] - 2026-09-19
### Added & Enhanced
- **My Apps Management Hub with Build History Parity**:
  - **Material 3 Delete BottomSheet Menu**: Replaced the system `AlertDialog` uninstallation prompt with a dedicated, height-fitting `ModalBottomSheet` menu featuring a 56.dp circular error header badge, application launcher icon preview, package name, version chip (`v<versionName> (<versionCode>)`), data deletion warning notice card, and styled `CANCEL` / `UNINSTALL` action buttons.
  - **Real-Time App Search**: Added search icon to the top bar toggling an animated `OutlinedTextField` search bar for filtering installed WebAPKs by title or package name in real time.
  - **Sort BottomSheet Menu**: Added sort action icon opening a modal selection sheet to sort installed apps by *Recently Installed*, *App Name (A–Z)*, and *Updates Available First*.
  - **Manual Refresh Scanner**: Added refresh icon button to trigger an on-demand re-scan of installed packages with live update checking.
  - **Instant Action Buttons on App Cards**: Integrated **Reuse Config** (`Icons.Outlined.AutoMode`) and **Install APK** (`Icons.Outlined.InstallMobile`) directly onto installed app cards for seamless rebuilds and direct installations.
- **Icon Zoomer BottomSheet Stepper Controls & Discrete Track Points**:
  - **Precise Stepper Buttons**: Added circular `FilledTonalIconButton` controls on both sides of the zoom scale slider (`-` on left, `+` on right) allowing users to decrease/increase zoom scale by exactly 1 point (1% / `0.01f`), clamped at 40% minimum and 200% maximum.
  - **10-Point Discrete Steps on Slider Track**: Added `steps = 15` to the `Slider`, dividing the 40%–200% range into 16 intervals and rendering tactile stop indicator dots at every 10 points (50%, 60%, 70% ... 190%) along the track.
  - **Styled Value Badge**: Enclosed the real-time zoom percentage (`${(scaleFactor * 100).roundToInt()}%`) inside a primary-tinted rounded badge.
- **Ergonomic Bottom Navbar with Text Labels**:
  - Restored clear text labels below navigation bar icons with an optimized 66.dp container height, delivering a balanced and easily accessible navigation experience.
- **Harmonized Circular Icon Badges Across All Screens**:
  - Standardized modern 40.dp circular icon container badges (`CircleShape`, `MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)`) across the Dashboard (URL web icon, Build & Config section), all Settings screen preference tiles, and About screen action cards for complete visual harmony.
- **Android Permissions & Package Management Expansion**:
  - Added native permissions to `AndroidManifest.xml`: `REQUEST_DELETE_PACKAGES`, `QUERY_ALL_PACKAGES`, `READ_MEDIA_IMAGES`, and `POST_NOTIFICATIONS` ensuring uninstallation intents, media picking, and push notifications work reliably on Android 13 through Android 15.
- **Dynamic Package ID & Update Pipeline Enhancements**:
  - Resolved duplicate package ID generation, refined automatic package assignment from URLs, and ensured app update compilations inherit exact original configurations.

## [2.4.0] - 2026-09-11
### Added & Enhanced
- **Smooth 0–100% Compiling Progress Engine**: Replaced jumpy discrete progress steps (`0 -> 20 -> 60 -> 80 -> 100`) with a smooth, granular step-by-step counter (`0, 1, 2, 3... 100`) for a perfect real-time compilation feel.
- **Multiple ABI Splits & Universal Installer**: Configured Android Gradle splits to produce individual CPU architecture APKs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) alongside the universal installer APK.
- **Enhanced Ad-Blocker Fallbacks & Real-Time Cleanup**: Added `DOMContentLoaded` and periodic interval cleanup fallbacks to purge dynamic ad scripts. Expanded ad network domain blacklist (`monetag`, `admaven`, `hilltopads`, `juicyads`, `clickadu`, `popmyads`, etc.) and refined CSS selectors so web app modals (Credly, Forage, Eduskills) render without touch freezing or screen dimming.
- **Discovered & Respected Native Website Dark Mode**: Disabled forced algorithmic darkening by default (`forceDarkMode` flag, default `Off`). WebAPKs now respect the website's native theme styling by default, while allowing users to enable forced dark mode if desired.
- **Fixed Eduskills & Flexbox Vertical Stretching**: Corrected WebView layout constraints and window inset padding in `:template` to prevent 100vh flexbox containers and page layouts from stretching vertically on sites like Eduskills Academy.
- **Persistent Login Sessions & Disk Sync**: Added explicit calls to `CookieManager.getInstance().flush()` during `onPageFinished()`, `onPause()`, `onStop()`, and cookie modifications, ensuring login credentials and session tokens persist reliably across app restarts.
- **Google OAuth & Device Google Accounts Sync**: Integrated `AccountManager` account picker and Google Play Services Auth sync in `:template`. WebAPKs for Google AI Studio, Google Skills, Google Play Academy, and Google Stitch now detect logged-in Google Accounts on device for direct 1-tap Google SSO sign-in.
- **Enable Zoom Toggle Control**: Added `enableZoom` configuration support (default `Off` to maintain native app feel, optional toggle in builder dashboard for full multi-touch zoom).
- **Smart Website Footer Hider Default Enabled**: Updated `hideWebFooter` configuration to default **ON (`true`)** across template runtime and builder settings. Removed the `Switch` component from the Bento card, converting it into a 1-tap click-to-toggle card matching Desktop Mode, Force Dark, Enable Zoom, and Text Copying. Intelligently detects and hides site informational footers (copyright notices, legal policy links, terms, privacy, security, cookies, contact) while preserving web app tab bars and bottom navigation docks.
- **Redesigned 3-Tier Dashboard Bento Layout**: Organized dashboard options into **Website Details Hero Card**, 5-card **Quick Toggles Bento Grid** (Desktop Mode, Force Dark Mode, Enable Zoom, Allow Text Copying, Hide Web Footer), and 3 clickable **Feature Option Cards** (Package Identity, Storage Folder, Keystore).
- **Inline Expandable Feature Cards (Pop-Under Design)**: Replaced Modal BottomSheets for Package Identity, Storage Folder, and Custom Signing Keystore with smooth `AnimatedVisibility` inline expansion cards that pop open directly beneath each Feature Card — consistent with how the Storage Folder card previously worked.
- **Extended Custom Keystore Generation Fields**: Expanded the Signing Keystore card with 5 additional PKCS12 certificate fields — **Common Name (Author)**, **Organization**, **Organizational Unit**, **Validity Years**, and a separate **Key Password** — for complete, production-grade certificate identity control.
- **Full Factory Reset**: Reset Details button now wipes everything — all form inputs (URL, App Name, Package, Version), all Quick Toggle states (Desktop Mode, Force Dark, Zoom, Text Copy, Hide Footer), all Feature Card details (Package Identity, Storage Folder, Keystore), and restores all `SharedPreferences` to default values in a single confirmation action.
- **22+ Color Accents Palette & Compact Scrollable Menu**: Expanded `AppColorAccent` to 22 Material 3 color accents. Redesigned the Accent menu with a scrollable selection container capped at `300.dp` max height while keeping the Theme menu compact and non-scrollable.
- **Redesigned Expandable History Search Bar**: Search icon in the header toggles an expandable search bar directly *under* the header using smooth `AnimatedVisibility`, replacing the top search icon with an 'X' button to close/clear search.

### Maintenance
- **ProGuard Coverage Expansion**: Added explicit keep rules for `JarSigner`, `ZipAligner`, `ZipUtils`, `AppLogger`, `ElfAligner16k$*` data classes, and a `keepclassmembers` rule for all data class constructors in the Packora package. Template module updated with `MainActivity$*` wildcard and `androidx.webkit.**` keep rules for `WebSettingsCompat` reflection calls.
- **Build Workflow Updates**: Updated `BUILD_TOOLS_VERSION` from `34.0.0` → `35.0.0` to match `compileSdk = 35`, and broadened unsigned APK upload glob from `*-unsigned.apk` to `*.apk` for all release variants.

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
