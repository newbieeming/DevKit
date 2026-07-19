plugins {
    alias(libs.plugins.devkit.android.library.compose)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.feature.miccontrol"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:permissions"))
    implementation(project(":core:service"))
    implementation(libs.hilt.navigation.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.7")
    implementation("androidx.savedstate:savedstate-ktx:1.2.1")
}
