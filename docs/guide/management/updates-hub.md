# Updates Hub

The **Updates Hub** in Packora provides an independent, dedicated workspace for managing and compiling in-place updates for all your installed WebAPKs.

---

## 4 Update Installation Modes

Packora gives you fine-grained control over how compiled updates are handed off to Android's package installer:

1. **Auto-Prompt** (*Default*): Automatically triggers the system package installer prompt immediately after compiling an update.
2. **Automate "Update All" Only**: Prompts for single updates, but performs unattended background package sessions when executing batch updates.
3. **Automate All Updates**: Fully automates both single and batch installations using Android's `PackageInstaller` session API (Android 12+ API 31+).
4. **Manual Only**: Compiles update APKs into your storage folder and leaves installation to manual user action.

---

## Batch Sequential Compilation ("Update All")

- **Top Banner Action**: When multiple installed WebAPKs have newer versions or configuration changes, the top banner offers a 1-tap **"UPDATE ALL"** action.
- **Sequential Queue**: Compiles apps one by one in the background, displaying real-time progress indicators and pulsing `PackoraDotLoader` animations.
- **Auto-Cleanup**: When enabled in Settings, temporary APK files are automatically deleted after successful installation to save device storage.
