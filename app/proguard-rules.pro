# Packora App Module ProGuard / R8 Rules

# 1. Serialization & History Management Rules
-keep class com.maheswara660.packora.builder.JarSigner { *; }
-keep class com.maheswara660.packora.builder.JarSigner$SigningSchemeOptions { *; }
-keep class com.maheswara660.packora.manager.** { *; }
-keep class com.maheswara660.packora.ui.components.** { *; }
-keep class com.maheswara660.packora.ui.InstalledPackoraApp { *; }
-keep class com.maheswara660.packora.ui.PendingInstallTask { *; }
-keep class com.maheswara660.packora.ui.PendingUpdateApp { *; }
-keep class com.maheswara660.packora.ui.AppSortMode { *; }
-keep class com.maheswara660.packora.ui.SortMode { *; }
-keep class com.maheswara660.packora.ui.ReleaseItem { *; }
-keep class com.maheswara660.packora.ui.Screen { *; }
-keep class com.maheswara660.packora.receiver.** { *; }
-keep class com.maheswara660.packora.installer.** { *; }

# Packora Subpackages & Engine Modules
-keep class com.maheswara660.packora.model.** { *; }
-keep class com.maheswara660.packora.adblock.** { *; }
-keep class com.maheswara660.packora.crypto.** { *; }
-keep class com.maheswara660.packora.dns.** { *; }
-keep class com.maheswara660.packora.privacy.** { *; }
-keep class com.maheswara660.packora.scraper.** { *; }
-keep class com.maheswara660.packora.analyzer.** { *; }
-keep class com.maheswara660.packora.extension.** { *; }
-keep class com.maheswara660.packora.builder.PerAppSigningIdentity { *; }
-keep class com.maheswara660.packora.builder.PerAppSigningIdentity$* { *; }

# Standard Gson rules
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# 2. APK Signature Scheme (apksig) Rules
-keep class com.android.apksig.** { *; }
-dontwarn com.android.apksig.**

# 3. Cryptography & Compression Libraries
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn org.apache.commons.compress.**

# 4. Networking & DNS-over-HTTPS (OkHttp) Rules
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# 3. Binary Rebuilders & WebAPK Generator Engine
-keep class com.maheswara660.packora.builder.ApkBuilder { *; }
-keep class com.maheswara660.packora.builder.ApkTemplate { *; }
-keep class com.maheswara660.packora.builder.AxmlRebuilder { *; }
-keep class com.maheswara660.packora.builder.ArscRebuilder { *; }
-keep class com.maheswara660.packora.builder.ElfAligner16k { *; }
-keep class com.maheswara660.packora.builder.ElfAligner16k$* { *; }
-keep class com.maheswara660.packora.builder.ZipAligner { *; }
-keep class com.maheswara660.packora.builder.ZipUtils { *; }
-keep class com.maheswara660.packora.builder.AppLogger { *; }

# 4. Data classes & Models — preserve field names for serialization/reflection safety
-keepclassmembers class com.maheswara660.packora.** {
    public <init>(...);
}

# 5. Android Core, FileProvider & Jetpack Compose
-keep class androidx.core.content.FileProvider { *; }
-keepattributes SourceFile,LineNumberTable,*Annotation*,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Coroutines rules to prevent minification issues
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
