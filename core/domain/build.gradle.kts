plugins {
    alias(libs.plugins.devkit.jvm.library)
}

// 纯 Kotlin 模块，Use Case 不依赖任何 Android/框架细节
dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.android)
}
