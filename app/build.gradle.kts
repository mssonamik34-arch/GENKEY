plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val genkeyBuildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "com.genkey.keyboard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.genkey.keyboard"
        minSdk = 24
        targetSdk = 34
        versionCode = genkeyBuildNumber
        versionName = "1.0.$genkeyBuildNumber"
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.3")
}
