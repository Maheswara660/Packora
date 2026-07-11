# Changelog

All notable changes to the **Packora** project will be documented in this file.

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
