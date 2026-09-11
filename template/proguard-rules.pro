# Packora Template Module ProGuard / R8 Rules

# Keep all JavascriptInterfaces used in WebView (AutofillBridge, NotificationBridge, and any future bridges)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.maheswara660.packora.template.MainActivity { *; }
-keep class com.maheswara660.packora.template.MainActivity$* { *; }

# Keep WebView client classes & WebChromeClient handlers
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }

# Keep AndroidX WebView Compat (WebSettingsCompat, WebViewFeature) — used via reflection
-keep class androidx.webkit.** { *; }
-dontwarn androidx.webkit.**

# Preserve stack trace line numbers for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
