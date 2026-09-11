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
        versionCode = 24
        versionName = "2.4.0"
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
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.webkit:webkit:1.12.0")
    implementation("androidx.browser:browser:1.8.0")
}
