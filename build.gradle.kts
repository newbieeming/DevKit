// Top-level build file — 仅声明插件，不在此处配置子模块
// 子模块通过 build-logic 中的约定插件统一管理构建配置
plugins {
    alias(libs.plugins.android.application)       apply false
    alias(libs.plugins.android.library)           apply false
    alias(libs.plugins.kotlin.android)            apply false
    alias(libs.plugins.kotlin.compose)            apply false
    alias(libs.plugins.kotlin.jvm)                apply false
    alias(libs.plugins.kotlin.serialization)      apply false
    alias(libs.plugins.hilt)                      apply false
    alias(libs.plugins.ksp)                       apply false
    alias(libs.plugins.room)                      apply false
}
