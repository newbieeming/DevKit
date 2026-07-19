plugins {
    alias(libs.plugins.devkit.android.library)
    alias(libs.plugins.devkit.android.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.newbieeming.devkit.core.database"
}

// Room schema 导出目录（便于迁移版本管理）
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
