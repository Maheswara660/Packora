# Packora App Module ProGuard / R8 Rules

# 1. Gson & Serialization Rules
-keep class com.maheswara660.packora.builder.JarSigner$SigningSchemeOptions { *; }
-keep class com.maheswara660.packora.manager.HistoryItem { *; }
-keep class com.maheswara660.packora.manager.PackoraPreferencesManager { *; }

# Standard Gson rules
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# 2. APK Signature Scheme (apksig) Rules
-keep class com.android.apksig.** { *; }
-dontwarn com.android.apksig.**

# 3. Binary Rebuilders & Builder Engine
-keep class com.maheswara660.packora.builder.ApkBuilder { *; }
-keep class com.maheswara660.packora.builder.ApkTemplate { *; }
-keep class com.maheswara660.packora.builder.AxmlRebuilder { *; }
-keep class com.maheswara660.packora.builder.ArscRebuilder { *; }
-keep class com.maheswara660.packora.builder.ElfAligner16k { *; }

# 4. Android Core & Jetpack Compose
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Coroutines rules to prevent minification issues
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
