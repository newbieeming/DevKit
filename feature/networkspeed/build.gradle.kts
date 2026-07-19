plugins {
    alias(libs.plugins.devkit.android.library.compose)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.feature.networkspeed"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:service"))
    implementation(libs.hilt.navigation.compose)
}
