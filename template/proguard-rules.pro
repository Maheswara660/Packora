# Packora Template Module ProGuard / R8 Rules

# Keep JavascriptInterfaces used in WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView client classes
-keep class * extends android.webkit.WebViewClient
-keep class * extends android.webkit.WebChromeClient

# Preserve stack trace line numbers for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
