plugins {
    alias(libs.plugins.devkit.android.library)
}

android {
    namespace = "com.newbieeming.devkit.core.testing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.junit)
    implementation(libs.hilt.android)
}
