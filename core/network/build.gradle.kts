plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.newbieeming.devkit.core.network"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.okhttp)
    implementation(libs.kotlin.serialization.json)
}
