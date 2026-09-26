# Config Field Drift

When introducing a new feature or toggle in Packora, the configuration field must stay synchronized between the Studio builder and the runtime template.

## Why Field Alignment Matters

Packora serializes settings into `assets/app_config.json` inside `ApkBuilder.kt`, and the standalone WebAPK deserializes those settings via Android's native `org.json.JSONObject` in `template/MainActivity.kt`:

1. **Serialization** in `ApkBuilder.kt`:
   ```kotlin
   val configJson = JSONObject().apply {
       put("appName", appName)
       put("packageName", packageName)
       put("targetUrl", targetUrl)
       put("isDesktopMode", isDesktopMode)
       put("allowCopying", allowCopying)
       put("isForceDarkMode", isForceDarkMode)
       put("enableZoom", enableZoom)
       // ... new configuration key
   }
   ```
2. **Deserialization** in `template/MainActivity.kt`:
   ```kotlin
   enableZoom = config?.optBoolean("enableZoom", false) ?: false
   allowCopying = config?.optBoolean("allowCopying", true) ?: true
   isForceDarkMode = config?.optBoolean("forceDarkMode", false) ?: false
   ```

Because `optBoolean` / `optString` fall back to defaults when a key is missing or misspelled, a typo in the key string will silently cause the feature to use default behavior without throwing an exception.

## The End-to-End Checklist

When adding any new setting that affects the exported WebAPK:

1. **Domain Model**:
   - Add the property to `PackoraApp`, `PackoraWebConfig`, `PackoraPrivacyConfig`, or `PackoraNetworkConfig` in `app/.../model/PackoraApp.kt`.
2. **Studio UI**:
   - Add the UI toggle or control in `BuildScreen.kt` using `PackoraIosSwitch` or selection dialogs.
3. **Builder Serialization**:
   - Update `ApkBuilder.buildApk(...)` arguments and write the key to the `configJson` object.
4. **Template Deserialization**:
   - In `template/MainActivity.kt`, read the key from `config?.optBoolean("key")` / `config?.optString("key")` and apply it to the `WebView` or Android system service.
5. **Verification**:
   - Build both `:app` and `:template` modules and verify in an emulator or test device.
