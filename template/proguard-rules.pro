# Packora Template Module ProGuard / R8 Rules

# Keep JavascriptInterfaces used in WebView & Password Manager Autofill
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.maheswara660.packora.template.MainActivity$AutofillBridge { *; }
-keep class com.maheswara660.packora.template.MainActivity$NotificationBridge { *; }

# Keep WebView client classes & WebChromeClient handlers
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }

# Preserve stack trace line numbers for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
