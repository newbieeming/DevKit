plugins {
    alias(libs.plugins.devkit.android.library.compose)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.feature.appmanager"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlinx.coroutines.android)
}
