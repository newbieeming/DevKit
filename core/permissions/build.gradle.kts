plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.core.permissions"
}

dependencies {
    implementation(project(":core:common"))
}
