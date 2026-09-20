plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.maheswara660.packora"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.maheswara660.packora"
        minSdk = 24
        targetSdk = 35
        versionCode = 35
        versionName = "3.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

// ── Template APK Automation ─────────────────────────────────────────────────
// Automatically builds :template natively and copies the APK to assets before every
// :app compilation. Single Gradle daemon execution prevents dual compiler lock errors.

val copyTemplateApk by tasks.registering(Copy::class) {
    description = "Copies the built template APK into app assets"
    group = "build"
    dependsOn(":template:assembleRelease")
    mustRunAfter(":template:assembleRelease")
    from(rootProject.file("template/build/outputs/apk/release/template-release-unsigned.apk"))
    into(layout.projectDirectory.dir("src/main/assets/template"))
    rename { "webview_shell.apk" }
}

// Hook into all preBuild, lint, and asset tasks so the template is always fresh and dependencies are clean
tasks.matching { 
    it.name.startsWith("pre") || 
    it.name.contains("Lint") || 
    it.name.contains("Asset") || 
    it.name.contains("Manifest") 
}.configureEach {
    dependsOn(copyTemplateApk)
}



kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.android.tools.build:apksig:8.3.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}