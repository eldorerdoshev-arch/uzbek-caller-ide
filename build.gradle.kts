plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "uz.uzbekcallerid"
    compileSdk = 35
    defaultConfig {
        applicationId = "uz.uzbekcallerid"
        minSdk = 26
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
}
