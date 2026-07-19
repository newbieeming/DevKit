plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.newbieeming.devkit.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.datastore.preferences)
    implementation(libs.kotlin.serialization.json)
}
