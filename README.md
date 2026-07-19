# DevKit

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="96" alt="DevKit Logo" />
</p>

<p align="center">
  一款面向车机 / Android 设备的开发者工具箱，集成麦克风控制、音频录制、应用管理、网速监控、设备信息查看及时间同步等常用功能。
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
| 🎵 **音频录制** | `feature:audiorecord` | 实时波形可视化，录音文件本地管理 |
| 📱 **应用管理** | `feature:appmanager` | 查看应用信息、跳转卸载 / 权限设置页 |
| 📶 **网速悬浮显示** | `feature:networkspeed` | 常驻悬浮卡片实时展示上下行速率 |
| 🖥️ **设备信息** | `feature:deviceinfo` | 查看系统版本、平台算力、厂商扩展属性 |
| 🕐 **时间同步** | `feature:timesync` | 可配置 NTP 服务器，精准对时并展示当前时间 |

---

## 架构设计

项目参考 [Now in Android](https://github.com/android/nowinandroid) 采用**多模块 + 清洁架构**，分为四层：

```
┌──────────────────────────────────┐
│              :app                │  ← 组装层：导航图 + DI 入口
├──────────────────────────────────┤
│  :feature:*  （6 个功能模块）     │  ← 功能层：UI / ViewModel / Navigation
├──────────────────────────────────┤
│  :core:*     （12 个基础模块）    │  ← 基础设施层：数据 / 领域 / UI组件
├──────────────────────────────────┤
│  build-logic/convention          │  ← 构建逻辑层：Gradle 约定插件
└──────────────────────────────────┘
```

### 模块依赖图

```
:app
 └── :feature:miccontrol
 └── :feature:audiorecord
 └── :feature:appmanager
 └── :feature:networkspeed
 └── :feature:deviceinfo
 └── :feature:timesync
       │
       ├── :core:ui ──────────── :core:designsystem
       ├── :core:data ─────────┬─ :core:database
       │                       ├─ :core:datastore
       │                       └─ :core:network
       ├── :core:domain ──────── :core:model
       ├── :core:permissions
       └── :core:service ─────── :core:common
```

> **feature 模块之间零直接依赖**，跨 feature 的通信通过 `:core:domain` 的 Repository 接口完成，导航跳转由 `:app` 层统一调度。

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
| `:core:model` | `jvm.library` | 纯 Kotlin 数据类（`AppInfo`、`RecordingFile`、`DeviceInfo`…），无 Android 依赖，可脱离设备单测 |
| `:core:domain` | `jvm.library` | Repository **接口** + `UseCase`/`FlowUseCase` 基类，定义业务契约 |
| `:core:common` | `android.library` + `hilt` | 协程调度器 DI（`@IoDispatcher` 等）、扩展函数、Timber 日志初始化 |
| `:core:database` | `android.library` + `hilt` | Room 数据库，管理录音元数据等本地持久化，Schema 导出至 `schemas/` 目录 |
| `:core:datastore` | `android.library` + `hilt` | DataStore Preferences，存储用户配置（NTP 地址、悬浮窗开关…） |
| `:core:network` | `android.library` + `hilt` | OkHttp + Kotlin 序列化，封装 NTP 同步与 HTTP 请求 |
| `:core:data` | `android.library` + `hilt` | Repository **实现**，聚合 database / datastore / network，对 feature 屏蔽数据来源 |
| `:core:permissions` | `android.library` | 权限常量（`DevKitPermissions`）+ `PermissionState` 状态机封装 |
| `:core:service` | `android.library` + `hilt` | `BaseOverlayService` 悬浮窗 Service 基类，封装 `WindowManager` 生命周期 |
| `:core:designsystem` | `android.library.compose` | Material3 主题（深色车机风格）、颜色令牌、字体排版，通过 `api` 对外暴露 Compose BOM |
| `:core:ui` | `android.library.compose` | 基于 `:core:designsystem` 的可复用组件（波形图、悬浮卡片、加载状态…） |
| `:core:testing` | `android.library` | 测试用 Fake Repository、协程测试调度器，供 feature 模块的单元测试复用 |

---

### Feature 层

每个 feature 模块均遵循相同内部结构：

```
feature:xxx/
└── src/main/kotlin/com/newbieeming/devkit/feature/xxx/
    ├── navigation/    XxxNavigation.kt  ← 路由常量 + NavGraphBuilder 扩展
    ├── ui/            XxxScreen.kt      ← Composable 页面（待实现）
    └── XxxViewModel.kt                 ← 状态管理（待实现）
```

| 模块 | 说明 | 关键权限 |
|---|---|---|
| `:feature:miccontrol` | 悬浮麦克风图标，长按拖动，点击开关录音设备。依赖 `BaseOverlayService` | `RECORD_AUDIO` `SYSTEM_ALERT_WINDOW` |
| `:feature:audiorecord` | 录音 + 实时振幅波形（`WaveformSample` 值类型）。录音文件存入 Room，支持播放 / 分享 / 删除 | `RECORD_AUDIO` `FOREGROUND_SERVICE_MICROPHONE` |
| `:feature:appmanager` | 已安装应用列表（支持系统/用户应用过滤），跳转系统应用详情 / 卸载 / 权限页 | `QUERY_ALL_PACKAGES` `REQUEST_INSTALL_PACKAGES` |
| `:feature:networkspeed` | 常驻悬浮卡片，每秒刷新 `NetworkSpeedSnapshot`（上/下行字节速率）。依赖 `BaseOverlayService` | `SYSTEM_ALERT_WINDOW` `ACCESS_NETWORK_STATE` |
| `:feature:deviceinfo` | 展示 `DeviceInfo`（型号、Android 版本、CPU ABI、RAM、厂商属性）。车机扩展属性通过 `customProperties: Map<String, String>` 注入 | — |
| `:feature:timesync` | NTP 对时（可配置服务器地址与同步间隔），时间实时展示 | `ACCESS_NETWORK_STATE` |

---

## 技术栈

| 类别 | 库 / 框架 |
|---|---|
| UI | Jetpack Compose + Material3 |
| 导航 | Navigation Compose |
| DI | Hilt + KSP |
| 数据库 | Room |
| 持久化 | DataStore Preferences |
| 网络 | OkHttp + Kotlin Serialization |
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

首次使用 **麦克风控制** 或 **网速悬浮显示** 时，应用会引导用户跳转系统设置开启"显示在其他应用上层"权限（`SYSTEM_ALERT_WINDOW`）。

---

## 新增功能模块

1. 在 `feature/` 目录下新建模块目录及 `build.gradle.kts`（复制任意现有 feature 的构建文件）
2. 在 `settings.gradle.kts` 末尾追加 `include(":feature:yourfeature")`
3. 在 `app/build.gradle.kts` 的 `dependencies` 中追加 `implementation(project(":feature:yourfeature"))`
4. 在 `app/.../navigation/DevKitNavHost.kt` 中调用新模块的 `NavGraphBuilder` 扩展函数

其余所有模块对新功能**零感知**，无需修改。

---

## 项目规范

- **数据流方向**：单向，UI → ViewModel → UseCase → Repository → 数据源
- **跨模块通信**：通过 `:core:domain` 的 Repository 接口，禁止 feature 模块直接互相依赖
- **悬浮 Service**：继承 `BaseOverlayService`，在各自 feature 模块的 `AndroidManifest.xml` 中声明，`app/Manifest` 通过 Manifest Merge 合并
- **权限声明**：所需权限在各 feature/app 的 Manifest 中最小化声明，运行时申请由 `:core:permissions` 统一封装
- **版本管理**：所有依赖版本集中在 `gradle/libs.versions.toml`，禁止在模块 build 文件中写死版本号

---

## License

```
MIT License

Copyright (c) 2026 newbieeming

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```
