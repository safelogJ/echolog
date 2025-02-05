plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.safelogj.echolog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.safelogj.echolog"
        minSdk = 21
        targetSdk = 35
        versionCode = 4
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.vosk) {
        exclude(group = "net.java.dev.jna", "jna")
    }
    implementation(libs.netjava)
    implementation(libs.yandexsdk)
    implementation(libs.lottie)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}