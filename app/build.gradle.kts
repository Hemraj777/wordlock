plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.wordlock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wordlock"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("wordlock.keystore")
            storePassword = "wordlock123"
            keyAlias = "wordlock"
            keyPassword = "wordlock123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val ks = file("wordlock.keystore")
            if (ks.exists()) {
                signingConfig = signingConfigs.getByName("release")
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}
