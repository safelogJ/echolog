import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
}

configure <ApplicationExtension> {
    namespace = "com.safelogj.echolog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.safelogj.echolog"
        minSdk = 21
        targetSdk = 36
        versionCode = 21
        versionName = "2.8.6 tr+"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }


    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation(libs.lottie) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.okio)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}