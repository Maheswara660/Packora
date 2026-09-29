# In-App Packora App Info Inspector

Packora provides an in-app architectural details sheet (`PackoraAppInfoBottomSheetDialog`) accessible directly by tapping any card in **My Apps** or **Build History**.

---

## What the Inspector Displays

- **App Identity**: App Name, Package Name, Version Name, and Version Code.
- **Architecture Target Badge**: Visual indicator showing whether the app was compiled as a Default Web App, Offline HTML Pack, Frontend SPA, Multi-Web Hub, or Media Streamer.
- **Stealth Privacy Shield**: Vector-by-vector breakdown of anti-fingerprinting defenses (Canvas 2D, WebGL GPU, AudioContext, DOM ClientRects, WebRTC local IP, Exit Cache Hygiene).
- **Ad & Tracker Defense**: Status of local request interceptors and cosmetic collapsing.
- **Encrypted DNS Provider**: Active DNS-over-HTTPS resolver.
- **Downloads Destination**: Custom storage subdirectory configured for file downloads.

---

## Quick Actions

- **Launch App**: Directly launches the target WebAPK without returning to the Android home screen or app drawer.
- **System App Info**: Opens native Android application settings (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`) to manage OS-level permissions, notifications, and battery optimization.
- **UNDERSTOOD**: Clean dismissal action consistent with all Packora bottom sheets.
