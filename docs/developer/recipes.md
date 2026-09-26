# Change Recipes

Standard workflows for developing and contributing to Packora.

## 1. Add a new setting to the WebAPK builder

Trace and update all stages:

1. **Model** (`app/.../model/PackoraApp.kt`): Add property to `PackoraWebConfig`, `PackoraPrivacyConfig`, or `PackoraNetworkConfig`.
2. **UI** (`app/.../ui/BuildScreen.kt`): Add the toggle control using `PackoraIosSwitch` or selection bottom sheet.
3. **Builder** (`app/.../builder/ApkBuilder.kt`): Add parameter and put value into `configJson`.
4. **Template** (`template/.../MainActivity.kt`): Read the key via `config?.optBoolean("key")` / `config?.optString("key")` and apply to WebView or system services.
5. **Recompile**: Rebuild template and app modules.

## 2. Update Template Activity or Shell Layout

1. Modify `template/src/main/java/com/maheswara660/packora/template/MainActivity.kt` or `template/src/main/res/layout/activity_main.xml`.
2. Recompile and stage the template APK:
   ```bash
   ./gradlew :template:assembleRelease :app:copyTemplateApk
   ```
3. Test that `:app` picks up the newly staged `webview_shell.apk`.

## 3. Add or Modify UI Components

1. Place reusable Compose components under `app/src/main/java/com/maheswara660/packora/ui/components/`.
2. Follow Packora UI design rules:
   - Use `PackoraIosSwitch` for boolean switches.
   - Use `PackoraDotLoader` for loading states.
   - For bottom sheets with selection chips, always ensure the **Apply** button triggers the update and avoids unwanted auto-dismissals.

## 4. Verify Commands

Run these Gradle commands to validate all changes:

```bash
# Rebuild the standalone template APK and stage into app assets
./gradlew :template:assembleRelease :app:copyTemplateApk

# Validate compilation of both Studio host and Template module
./gradlew :app:compileDebugKotlin :template:compileReleaseKotlin

# Run unit tests
./gradlew testDebugUnitTest
```
