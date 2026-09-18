# Packora Template Module ProGuard / R8 Rules

# 1. Keep all JavascriptInterfaces used in WebView (AutofillBridge, NotificationBridge, etc.)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# 2. Main WebAPK Container Activity
-keep class com.maheswara660.packora.template.MainActivity { *; }
-keep class com.maheswara660.packora.template.MainActivity$* { *; }

# 3. Keep WebView client classes & WebChromeClient handlers
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }

# 4. AndroidX WebView Compat (WebSettingsCompat, WebViewFeature) — used via reflection
-keep class androidx.webkit.** { *; }
-dontwarn androidx.webkit.**

# 5. Credential Manager & Google Play Services Auth synchronization
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class com.google.android.gms.auth.** { *; }
-dontwarn com.google.android.gms.auth.**

# 6. FileProvider & Core Components
-keep class androidx.core.content.FileProvider { *; }

# 7. Preserve stack trace line numbers for crash debugging
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile
