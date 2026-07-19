plugins {
    alias(libs.plugins.devkit.jvm.library)
}

// 纯 Kotlin 模块，无 Android 依赖，便于独立单测
dependencies {
    // 仅允许纯 Kotlin 依赖
    implementation(libs.kotlinx.coroutines.android)
}
