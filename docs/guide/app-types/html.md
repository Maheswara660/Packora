# Offline HTML Pack Architecture

The **HTML** architecture (`PackoraAppType.HTML`) packages static web assets directly into the compiled APK binary, enabling fully functional offline web apps that run without an active internet connection.

---

## How It Works

1. **Asset Bundling**: Web assets (HTML, CSS, JS, images, fonts, web manifests) are packaged into the APK's `assets/www/` directory.
2. **Local Protocol Resolution**: The application loads the entrypoint directly from `file:///android_asset/www/index.html`.
3. **Local Access Security**: The WebView runtime safely configures `allowFileAccess = true`, `allowContentAccess = true`, and allows local DOM storage and IndexedDB persistence.
4. **Zero Network Latency**: Pages and assets load instantaneously from on-device flash storage.

---

## Best For

- Interactive documentation & offline reference manuals.
- Offline tools, engineering calculators, and utility apps.
- HTML5 games and interactive graphic presentations.
- Disaster-readiness and survival guides requiring 100% offline availability.

---

## Configuration

- **Asset Folder**: Select the directory containing your static build using Android's Storage Access Framework (SAF).
- **Entrypoint**: By default, `index.html` at the root of the folder serves as the launch screen.
- **Privacy & Storage**: Stealth privacy protection and local storage isolation remain fully active.
