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
    implementation("androidx.activity:activity:1.13.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
}
