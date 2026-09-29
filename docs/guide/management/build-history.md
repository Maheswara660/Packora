# Build History & Config Reuse

Every WebAPK compiled by Packora is recorded in **Build History** along with its complete, fine-grained 16-parameter configuration snapshot.

---

## 16-Parameter State Preservation

When a build finishes, the following configuration parameters are saved into `BuildHistoryManager`:
- **App Target Architecture**: `WEB`, `HTML`, `FRONTEND`, `MULTI_WEB`, `MEDIA`.
- **Target URL & Package Name**: Full origin URL and unique package identifier.
- **Version Metadata**: `versionCode` and `versionName`.
- **Storage & Downloads Folder**: Output directory and custom download destination.
- **Multi-Web Tabs**: Secondary URLs configured for tabbed navigation.
- **SPA Routing Fallback**: Client-side history navigation status.
- **Keep Screen On**: Video and display awake policy.
- **Stealth Privacy Shield**: Individual vector toggles (Canvas 2D, WebGL, AudioContext, DOM ClientRects, WebRTC local IP, Clear on Exit).
- **Ad & Tracker Blocker**: Local domain filtering and cosmetic container collapsing.
- **Encrypted DNS**: Resolver endpoint, strict transport mode, and ECH.

---

## 1-Tap "Reuse Config"

- **Action**: Tapping **"REUSE CONFIG"** on any history card instantly restores 100% of the saved options back into **Build Studio**.
- **Efficiency**: Avoids repetitive manual entry of complex URLs, package names, custom directories, and privacy flags when re-compiling or creating variants.
