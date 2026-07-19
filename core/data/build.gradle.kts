plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))
}
