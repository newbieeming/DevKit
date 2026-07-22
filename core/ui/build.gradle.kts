plugins {
    alias(libs.plugins.devkit.android.library.compose)
}

android {
    namespace = "com.newbieeming.devkit.core.ui"
}

dependencies {
    // 璁捐绯荤粺 api 鏆撮湶锛宖eature 鍙渶寮曞叆 core:ui 鍗冲彲
    api(project(":core:designsystem"))
    api(project(":core:model"))
    implementation(project(":core:permissions"))
    implementation(libs.androidx.activity.compose)
    // 瀵艰埅锛欶eatureEntry 鐨?registerNavigation 闇€瑕?NavGraphBuilder
    api(libs.androidx.navigation.compose)
    api(libs.androidx.viewmodel.savedstate)
    api(libs.androidx.savedstate.ktx)
}
