plugins {
    alias(libs.plugins.devkit.android.library.compose)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.feature.audiorecord"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:permissions"))
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.material.icons)
}
