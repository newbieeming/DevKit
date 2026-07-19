plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.core.service"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:permissions"))
}
