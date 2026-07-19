plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
}

android {
    namespace = "com.newbieeming.devkit.core.common"
}

dependencies {
    // 协程（调度器对外暴露）
    api(libs.kotlinx.coroutines.android)
    // 日志
    api(libs.timber)
}
