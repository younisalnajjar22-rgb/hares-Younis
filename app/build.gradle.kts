plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.harasyounis"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.harasyounis"
        minSdk = 24
        targetSdk = 35
        versionCode = 11
        versionName = "1.1"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    }
}
