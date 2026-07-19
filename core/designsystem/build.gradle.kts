plugins {
    alias(libs.plugins.devkit.android.library.compose)
}

android {
    namespace = "com.newbieeming.devkit.core.designsystem"
}

dependencies {
    // Compose BOM 统一版本
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
