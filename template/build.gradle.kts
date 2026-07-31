plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.maheswara660.packora.template"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.maheswara660.packora.template"
        minSdk = 24
        targetSdk = 35
        versionCode = 20
        versionName = "2.0.0"
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
        viewBinding = true
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.7.0")
}
