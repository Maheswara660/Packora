# Packora Specific ProGuard/R8 Rules

# 1. Gson & Serialization Rules
# Keep the SigningSchemeOptions data class used for JSON saving/loading via Gson
-keep class com.maheswara660.packora.builder.JarSigner$SigningSchemeOptions { *; }

# Standard Gson rules
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# 2. APK Signature Scheme (apksig) Rules
# Suppress warnings from the apksig library since it references BouncyCastle and internal java/sun APIs
-dontwarn com.android.apksig.**

# 3. Android Core & Jetpack Compose
# Keeping line numbers for debugging stack traces in release builds.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Coroutines rules to prevent minification issues
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
