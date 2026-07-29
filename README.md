# DevKit

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="96" alt="DevKit Logo" />
</p>

<p align="center">
  一款面向 Android 设备的开发者工具箱，集成麦克风控制、音频录制、应用管理、网速监控、设备信息查看、时间显示、悬浮秒表及系统值查询等常用功能。
</p>

<p align="center">
  <img alt="Min SDK" src="https://img.shields.io/badge/Min_SDK-24-blue" />
  <img alt="Target SDK" src="https://img.shields.io/badge/Target_SDK-36-blue" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin" />
  <img alt="Compose" src="https://img.shields.io/badge/Jetpack_Compose-BOM_2026.02-4285F4?logo=jetpackcompose" />
  <img alt="License" src="https://img.shields.io/badge/License-MIT-green" />
</p>

---

## 功能列表

| 功能 | 模块 | 说明 |
|---|---|---|
| 🎙️ **麦克风控制** | `feature:miccontrol` | 悬浮图标一键切换系统麦克风开 / 关 |
| 🎵 **音频录制** | `feature:audiorecord` | PCM 录音、实时波形与本地录音文件管理 |
| 📱 **应用管理** | `feature:appmanager` | 搜索与筛选已安装应用，查看 APK / 安装 / 清单信息并发起卸载 |
| 📶 **网速悬浮显示** | `feature:networkspeed` | 常驻悬浮卡片实时展示上下行速率 |
| 🖥️ **设备信息** | `feature:deviceinfo` | 查看系统版本、平台算力、厂商扩展属性 |
| 🕐 **时间同步** | `feature:timesync` | 通过可配置悬浮窗实时展示设备当前时间 |
| ⏱️ **秒表** | `feature:stopwatch` | 通过可移动悬浮窗计时，并支持开始、暂停和归零 |
| 🔎 **系统值查询** | `feature:systemquery` | 使用正则表达式查询系统属性及 Settings 中的值，并记忆查询历史 |

---

## 架构设计

项目参考 [Now in Android](https://github.com/android/nowinandroid) 采用**多模块 + 清洁架构**。`app`、`core`、`feature` 是根目录下的同级模块；以下为逻辑职责分层：

```
┌──────────────────────────────────┐
│              :app                │  ← 组装层：导航图 + DI 入口
├──────────────────────────────────┤
│  :feature:*  （7 个功能模块）     │  ← 功能层：UI / ViewModel / Navigation
├──────────────────────────────────┤
│  :core:*     （6 个基础模块）     │  ← 基础设施层：通用能力 / UI 组件
├──────────────────────────────────┤
│  build-logic/convention          │  ← 构建逻辑层：Gradle 约定插件
└──────────────────────────────────┘
```

### 模块与依赖图

```
DevKit/
├── app/             :app，应用组装层
├── core/            :core:*，基础设施与通用能力
├── feature/         :feature:*，独立用户功能
└── build-logic/     Gradle 约定插件

:app ───────────────► :feature:* ───────────────► :core:*
  └──────────────────────────────────────────────► :core:*
```

> **feature 模块之间零直接依赖**。app 负责根导航图与 Feature 组装；每个 Feature 注册自己的页面目的地。Feature 专属的数据与业务逻辑保留在 Feature 内；确有多个消费者的契约再提取到合适的 core 模块。

---

## 模块说明

### 构建逻辑 `build-logic/`

| 约定插件 ID | 用途 |
|---|---|
| `devkit.android.application` | App 模块基础配置（compileSdk、minSdk…） |
| `devkit.android.application.compose` | App 模块 + Compose 编译器 |
| `devkit.android.library` | Android Library 基础配置 |
| `devkit.android.library.compose` | Android Library + Compose 编译器 |
| `devkit.android.hilt` | 注入 Hilt + KSP 插件 |
| `devkit.jvm.library` | 纯 Kotlin JVM 模块（不依赖 Android SDK） |

> 全局 SDK 版本、Java 兼容性只需修改 `build-logic/convention/src/main/kotlin/DevKitBuildConfig.kt` 一处。

---

### Core 层

| 模块 | 插件 | 职责 |
|---|---|---|
| `:core:model` | `jvm.library` | 多个模块共用的纯 Kotlin 悬浮窗配置与网速快照模型 |
| `:core:common` | `android.library` + `hilt` | 协程调度器 DI（`@IoDispatcher` 等）、应用包变化 Flow、安全的 `getprop` 系统属性读取、扩展函数与 Timber 日志初始化 |
| `:core:datastore` | `android.library` + `hilt` | DataStore Preferences，存储多个 Feature 共用的悬浮窗配置 |
| `:core:permissions` | `android.library` | 权限常量（`DevKitPermissions`）+ `PermissionState` 状态机封装 |
| `:core:designsystem` | `android.library.compose` | Material3 设计令牌、颜色与字体排版，通过 `api` 对外暴露 Compose BOM |
| `:core:ui` | `android.library.compose` | 可复用 Compose 组件、`FeatureEntry`、权限请求 UI 与悬浮窗生命周期封装 |

---

### Feature 层

每个 feature 模块均遵循相同内部结构；可根据复杂度增加 `data/` 与 `domain/`：

```
feature:xxx/
└── src/main/kotlin/com/newbieeming/devkit/feature/xxx/
    ├── XxxEntry.kt                    ← 仪表盘入口与进入前逻辑
    ├── navigation/                    ← 路由常量 + NavGraphBuilder 扩展
    ├── presentation/                  ← ViewModel、UiState、一次性 UI 事件
    ├── data/、domain/                 ← Feature 专属数据与领域实现（按需）
    └── ui/                            ← Screen 与可复用组件
```

| 模块 | 说明 | 关键权限 |
|---|---|---|
| `:feature:miccontrol` | 可拖拽麦克风悬浮控制，支持卡片快捷启停与大小/初始位置/图标配置 | `MODIFY_AUDIO_SETTINGS` `SYSTEM_ALERT_WINDOW` |
| `:feature:audiorecord` | PCM 录音、实时波形、播放、重命名与删除。兼容原 SoundCapture，文件保存在 `/sdcard/SoundCapture` | `RECORD_AUDIO`；Android 11+ 还需“所有文件访问” |
| `:feature:appmanager` | 已安装应用列表（搜索及系统/三方过滤）、APK 与清单详情、系统卸载 / 卸载更新 | `QUERY_ALL_PACKAGES` `REQUEST_DELETE_PACKAGES` |
| `:feature:networkspeed` | 每秒计算设备上下行速率，通过可配置悬浮窗实时显示 | `SYSTEM_ALERT_WINDOW` `ACCESS_NETWORK_STATE` |
| `:feature:deviceinfo` | 展示 `DeviceInfo`（型号、Android 版本、CPU ABI、RAM、厂商属性）。车机扩展属性通过 `customProperties: Map<String, String>` 注入 | — |
| `:feature:timesync` | 根据系统 12/24 小时制实时显示当前时间，支持悬浮窗配置 | `SYSTEM_ALERT_WINDOW` |
| `:feature:stopwatch` | 可暂停、继续和归零的悬浮秒表，支持状态、颜色与大小配置 | `SYSTEM_ALERT_WINDOW` |
| `:feature:systemquery` | 按正则查询系统属性以及 Global、System、Secure Settings，可选择全部或指定来源并保存查询历史 | — |

### Feature 入口、导航与权限

每个仪表盘功能实现 `:core:ui` 的 `FeatureEntry`。Feature 通过 Hilt 的 `@IntoSet` 提供自身入口，app 注入 `Set<FeatureEntry>` 后按优先级展示功能卡，并调用 `registerNavigation` 将 Feature 自己的目的地加入根 `NavHost`。

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object XxxEntryModule {
    @Provides
    @IntoSet
    fun provideEntry(): FeatureEntry = XxxEntry()
}
```

- `:app` 只负责展示功能卡和执行路由导航，不承载 Feature 专属业务、存储或权限策略。
- Feature 自己在 Manifest 声明最小权限，并在入口/UI 中决定何时申请；共享申请/检查能力复用 `DevKitPermissionManager` 与 `rememberFeaturePermissionRequest`。
- 路由常量和 `NavGraphBuilder` 扩展位于 Feature 自身的 `navigation/` 目录；`FeatureEntry.Tile` 不声明默认参数，避免跨模块 Compose 接口问题。

---

## 技术栈

| 类别 | 库 / 框架 |
|---|---|
| UI | Jetpack Compose + Material3 |
| 主题 | Android 12+ 动态配色，其他版本使用紫色兜底主题 |
| 导航 | Navigation Compose |
| DI | Hilt + KSP |
| 持久化 | DataStore Preferences |
| 序列化 | Kotlin Serialization |
| 异步 | Kotlin Coroutines + Flow |
| 日志 | Timber |
| 构建 | Gradle Version Catalog + Convention Plugins |

---

## 快速开始

### 环境要求

- Android Studio Meerkat (2024.3) 或更高版本
- JDK 11+
- Android SDK 36

### 克隆并运行

```bash
git clone https://github.com/newbieeming/DevKit.git
cd DevKit
```

用 Android Studio 打开项目根目录，等待 Gradle 同步完成后，选择 `app` Run Configuration 直接运行。

### 悬浮窗权限

首次使用 **麦克风控制**、**网速悬浮显示**、**时间悬浮显示** 或 **秒表** 时，应用会引导用户跳转系统设置开启“显示在其他应用上层”权限（`SYSTEM_ALERT_WINDOW`）。

---

## 新增功能模块

1. 在 `feature/` 目录下新建模块目录及 `build.gradle.kts`（复制任意现有 feature 的构建文件）
2. 在 `settings.gradle.kts` 末尾追加 `include(":feature:yourfeature")`
3. 在 `app/build.gradle.kts` 的 `dependencies` 中追加 `implementation(project(":feature:yourfeature"))`
4. 实现 `FeatureEntry`，在该 Feature 内通过 Hilt `@IntoSet` 绑定入口，并在 `registerNavigation` 注册自身的 `NavGraphBuilder` 扩展

app 已自动聚合 `FeatureEntry`；其余 Feature 对新功能**零感知**，无需修改。

---

## 项目规范

- **数据流方向**：单向，UI → ViewModel → Repository / 数据源
- **跨模块通信**：Feature 模块禁止直接互相依赖；已有多个消费者时再将共享契约放入合适的 core 模块
- **悬浮 Service**：继承 `:core:ui` 的 `AbstractOverlayService`，在各自 Feature 的 `AndroidManifest.xml` 中声明，`app` Manifest 通过合并接入
- **权限声明**：所需权限在所属 Feature 的 Manifest 中最小化声明；Feature 自己发起申请，通用检查与申请能力由 `:core:permissions` / `:core:ui` 提供
- **版本管理**：所有依赖版本集中在 `gradle/libs.versions.toml`，禁止在模块 build 文件中写死版本号
- **线程模型**：文件、音频、网络等阻塞 I/O 必须在 `Dispatchers.IO` 执行；Flow 生产端应在实现附近设置调度器，并在取消/失败时释放资源

---

## 多语言

应用默认语言为英语，并提供简体中文翻译。

- 所有用户可见文案必须放在所属模块的 `src/main/res/values/strings.xml`，默认值为英语。
- 同一资源 ID 必须同步写入 `src/main/res/values-zh/strings.xml`，提供简体中文翻译。
- 禁止在 Kotlin 中硬编码标题、描述、按钮、弹窗、错误提示、Toast/Snackbar、权限名称或 `contentDescription` 等用户可见文案；Compose 使用 `stringResource(R.string.xxx)` 读取。
- ViewModel 发送本地化一次性消息时传递资源 ID 和必要参数，由 UI 在 Compose 阶段解析，不让 ViewModel 持有 `Context`。

---

## License

```
MIT License

Copyright (c) 2026 newbieeming

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```
