pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DevKit"

include(":app")

// ── Core modules ──────────────────────────────────────────────────────────────
include(":core:common")       // 公共工具/扩展/协程调度器
include(":core:model")        // 纯 Kotlin 数据模型（无 Android 依赖）
include(":core:domain")       // 用例层 Use Cases（纯 Kotlin）
include(":core:data")         // Repository 接口与实现
include(":core:database")     // Room 本地数据库
include(":core:datastore")    // DataStore 偏好持久化
include(":core:network")      // 网络层（NTP、HTTP）
include(":core:permissions")  // 权限请求与状态封装
include(":core:service")      // 后台 Service / 悬浮窗基础封装
include(":core:designsystem") // 主题、颜色、字体、通用设计令牌
include(":core:ui")           // 可复用 Compose 组件（波形图、悬浮卡片等）
include(":core:testing")      // 测试辅助工具与假数据

// ── Feature modules ───────────────────────────────────────────────────────────
include(":feature:miccontrol")   // 麦克风控制（悬浮图标开关）
include(":feature:audiorecord")  // 音频录制（实时波形 + 文件列表）
include(":feature:appmanager")   // 应用管理（信息/卸载/权限跳转）
include(":feature:networkspeed") // 网速悬浮显示
include(":feature:deviceinfo")   // 设备/车机信息（版本、算力等）
include(":feature:timesync")     // 时间显示（可配置 NTP 服务器）
