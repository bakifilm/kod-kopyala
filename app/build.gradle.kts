plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Sürüm bilgisi GitHub Actions'tan gelir; yerelde derlerken varsayılanlar kullanılır.
val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
val tagName = System.getenv("GITHUB_REF_NAME")?.takeIf { it.startsWith("v") }?.removePrefix("v")

// İmza bilgileri GitHub Secrets'tan gelir. Yoksa debug anahtarıyla imzalanır.
val keystorePath = System.getenv("KEYSTORE_PATH")
val hasReleaseKey = !keystorePath.isNullOrBlank() && file(keystorePath).exists()

android {
    namespace = "work.baki.kodkopyala"
    compileSdk = 35

    defaultConfig {
        applicationId = "work.baki.kodkopyala"
        minSdk = 26
        targetSdk = 35
        versionCode = runNumber
        versionName = tagName ?: "0.1.$runNumber"
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = if (hasReleaseKey) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}
