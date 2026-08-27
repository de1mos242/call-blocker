plugins {
    id("com.android.application")
}

android {
    namespace = "com.de1mos.callblocker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.de1mos.callblocker"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.13.0")
}
