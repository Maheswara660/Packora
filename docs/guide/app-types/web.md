# Default Web App Architecture

The **Web** architecture (`PackoraAppType.WEB`) is the default and most versatile target in Packora. It transforms any responsive website or Progressive Web App (PWA) into an independent, production-grade Android WebAPK.

---

## Key Capabilities

- **Isolated Window & Process**: Runs with its own dedicated Android `taskAffinity`, window title, icon, and lifecycle independent of any installed browser.
- **50+ Vector Stealth Privacy Shield**: Protects Canvas 2D pixel readback, WebGL GPU parameters, AudioContext curves, and blocks WebRTC local ICE leaks.
- **Built-in Ad & Tracker Blocker**: High-speed local request filtering against bundled blocklists with automatic cosmetic container collapsing.
- **Encrypted DNS-over-HTTPS (DoH)**: Native resolution through 9 privacy resolvers (Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, System, Custom).
- **150+ Multilingual Smart Footer Hider**: Two-stage CSS and DOM suppression to eliminate sticky web footers without impacting bottom navigation bars.
- **Deterministic RSA-3072 Keystore**: Cryptographic signing identity generated specifically for the package ID for lifetime conflict-free in-place updates.
- **Android 15 16KB Page Alignment**: Ensures full compatibility with modern 16KB kernel memory pages.

---

## Configuration Options

When building a Web application, you can configure:
- **Target URL**: Any secure HTTPS or HTTP endpoint. Auto-paste from clipboard and favicon extraction are built-in.
- **Package Identity**: Custom application name, package ID (`com.example.app`), `versionName`, and `versionCode`.
- **Runtime Toggles**:
  - `Stealth Privacy Shield`: Anti-fingerprinting protection.
  - `Ad & Tracker Blocker`: Request and cosmetic ad blocker.
  - `Encrypted DNS`: Select resolver or custom DoH endpoint.
  - `Hide Web Footer`: Multilingual footer suppressor.
  - `Desktop Mode`: Desktop viewport and desktop Chrome User-Agent.
  - `Force Dark Mode`: Algorithmic darkening for sites lacking dark mode.
  - `Pinch Zoom`: Multi-touch zoom controls.
  - `Allow Text Copying`: Override CSS user-select restrictions.
- **Custom Download Location**: Select a dedicated subfolder on device storage for all downloaded files.
