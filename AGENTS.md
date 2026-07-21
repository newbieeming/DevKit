# DevKit：AI 协作规范

修改本仓库前，必须按以下顺序阅读：

1. 本文件 `AGENTS.md`：约束与实现规则。
2. `README.md`：项目定位、模块清单、技术栈与整体架构。
3. 当前需求涉及模块的 `build.gradle.kts`、Manifest、入口类与现有实现：以代码的当前行为为准。

项目面向车机与 Android 设备，使用 Kotlin、Jetpack Compose、Material 3、Navigation Compose、Hilt、Coroutines/Flow 与 Gradle Version Catalog。

## 1. 模块结构与依赖边界

```
DevKit/
├── app/             :app，应用组装层
├── core/            :core:*，基础设施与通用能力
├── feature/         :feature:*，独立用户功能
└── build-logic/     Gradle convention plugin
```

README 中的四层表示逻辑职责。下面使用箭头表示允许的主要依赖方向，同样不表示模块层级：

```
:app ─────────► :feature:* ─────────► :core:*
  └─────────────────────────────────► :core:*

build-logic/convention ──────────────► 各模块的 Gradle 构建配置
```

实际依赖规则如下：

- `:app` 可以依赖 `:feature:*` 与必要的 `:core:*`，但只负责组装，不实现 Feature 业务。
- `:feature:*` 可以依赖 `:core:*`；Feature 之间**禁止直接依赖**。
- `:core:*` 禁止依赖 `:feature:*` 或 `:app`。
- 跨 Feature 协作通过 `:core:domain` 中的 Repository 契约或其他合适的 core 契约完成。
- 所有依赖版本集中在 `gradle/libs.versions.toml`；使用现有 convention plugin，禁止在模块 Gradle 文件中硬编码版本号。

### Core 模块职责

新增或调整代码时按职责放置；只有可被多个功能复用的能力才进入 core。

| 模块 | 应放内容 |
|---|---|
| `:core:model` | 无 Android 依赖的共享数据模型 |
| `:core:domain` | Repository 接口、UseCase、跨 Feature 的业务契约 |
| `:core:common` | 调度器限定符、通用扩展、日志等基础工具 |
| `:core:database` / `:core:datastore` / `:core:network` | Room、DataStore、网络等通用数据源能力 |
| `:core:data` | 聚合通用数据源的 Repository 实现 |
| `:core:permissions` | 权限常量、授权状态及通用权限判断 |
| `:core:service` | 悬浮窗/后台 Service 的通用基类和生命周期封装 |
| `:core:designsystem` | Material 3 设计令牌、基础主题与排版 |
| `:core:ui` | 跨 Feature 复用的 Compose 组件、`FeatureEntry`、通用权限请求 UI 逻辑 |
| `:core:testing` | Fake、测试调度器与测试辅助能力 |

Feature 专属的数据模型、Repository、页面和逻辑保留在该 Feature 内；不要因为“可能以后复用”而提前搬到 core。已有至少两个消费者，或确实属于系统级统一规则时，才抽取到 core。

## 2. Feature 的实现与接入方式

每个 Feature 的推荐结构如下，可按实际复杂度增加 `data/`、`domain/`、`presentation/`、`ui/components/`：

```
feature/xxx/src/main/kotlin/.../feature/xxx/
├── XxxEntry.kt                 仪表盘入口与 Feature 自有的进入前逻辑
├── navigation/                 路由常量和 NavGraphBuilder 扩展
├── presentation/               ViewModel、UiState、一次性 UI 事件
├── data/                       Feature 专属数据源与 Repository
├── domain/                     Feature 专属领域实现
└── ui/                         Screen 与可复用组件
```

- 仪表盘功能实现 `core:ui` 的 `FeatureEntry`，使用 `FeatureTileScaffold` 显示图标、标题、描述、状态与权限。
- `FeatureEntry.Tile` 不得定义默认参数值；跨模块 Compose 接口的默认参数可能导致 `AbstractMethodError`。
- Feature 在 `registerNavigation` 中注册自身的导航目的地；路由常量和 `NavGraphBuilder` 扩展放在该 Feature 的 `navigation/`。
- `:app` 仅持有根 `NavHost` 并调用 `entry.registerNavigation(...)`，仪表盘点击仅执行路由导航。不要把任一 Feature 的页面判断、业务逻辑或权限代码写进 `MainActivity`。
- 新增 Feature 时：在 `settings.gradle.kts` include 模块、在 `app/build.gradle.kts` 接入模块、提供 `FeatureEntry` 的 Hilt 绑定，并注册该 Feature 的导航目的地。以现有 `AudioRecordEntry` 与 `AudioRecordEntryModule` 为参考。

## 3. 权限、主题与 UI 设计

- 权限在所属 Feature 的 Manifest 最小化声明。Feature 自己决定需要哪些权限、何时申请、授权成功后是否进入页面。
- 共享权限判断与申请流程使用 `DevKitPermissionManager`、`DevKitPermissions` 和 `rememberFeaturePermissionRequest`；通用逻辑放 core，Feature 专属策略留在 Feature。禁止将 Feature 权限流程写入 `MainActivity`。
- 使用 Material 3 组件、语义正确的 `ImageVector` 图标和 `MaterialTheme.colorScheme`。应用整体使用动态配色，无法使用动态配色时使用紫色兜底主题；禁止 Feature 另设固定蓝灰色或固定颜色体系。
- `FeatureTileScaffold` 的文案应说明“用户能做什么”，而不是堆砌内部实现或不准确地承诺未完成能力。标题简洁明确，描述使用一至两句用户视角说明。
- 大小、布局与内容密度应根据可用宽度自适应，不能只按横/竖屏分支。为卡片和控件设置合理下限，避免宽屏上出现大量过窄卡片。

## 4. 状态、并发与资源释放

- 保持单向数据流：Composable UI → ViewModel 事件 → UseCase/Repository/数据源 → `UiState` 或一次性事件 → UI。
- ViewModel 不得持有 `Activity`、`View` 或 Compose 状态；UI 只渲染状态并转发用户事件。
- 文件扫描、创建、删除、重命名、音频读写、播放、网络请求及其他阻塞操作必须在 `Dispatchers.IO` 执行。
- 对 Flow 生产端中的阻塞 I/O，应在实现附近明确指定调度器（例如 `.flowOn(Dispatchers.IO)`），不要只依赖远端调用者，避免主线程卡顿、ANR 与线程饥饿提示。
- 资源（`AudioRecord`、`AudioTrack`、文件流、Service 等）必须在 `finally` 或等效生命周期处理中释放；停止、取消和失败路径都要安全。
- 只有 UI 状态更新与 Compose 渲染可以留在主线程。

## 5. 多语言：强制要求

默认语言为英语，必须同时支持简体中文。

- 禁止新增用户可见的 Kotlin 字符串硬编码。
- 用户可见文本包括：功能卡标题/描述、按钮、弹窗、字段标签、空状态、错误状态、Snackbar、Toast、权限名称、状态徽标及无障碍 `contentDescription`。
- 每条用户可见文本都必须放在**所属模块**的 `src/main/res/values/strings.xml` 中，默认值使用英语。
- 同一次修改必须在该模块的 `src/main/res/values-zh/strings.xml` 中提供相同资源 ID 的简体中文翻译。
- Compose 中使用 `stringResource(R.string.xxx)`；不要用 `LocalContext.current.getString()` 在 `LaunchedEffect` 等副作用中查询资源。
- ViewModel 的本地化一次性事件应携带字符串资源 ID 和范围明确的格式化参数，由 UI 在 Compose 阶段解析；不要为了读取文案让 ViewModel 持有 `Context`。
- 资源名必须稳定且具有业务语义，例如 `recording_saved`；禁止 `text_1` 一类无语义命名。修改已有文案时必须同步更新英语和中文。
- 路由、文件名、技术常量、日志、仅供开发者查看的异常诊断不是 UI 文案，除非会展示给用户，否则保持英文。

## 6. 修改与验证

- 先用 `rg` 定位代码与资源，编辑使用 `apply_patch`。不要覆盖或格式化与当前需求无关的工作区改动。
- 删除、重命名或覆盖文件前先确认目标与影响范围；禁止使用破坏性 Git 命令清理用户改动。
- 修改资源后校验 XML 格式；修改代码后至少执行 `git diff --check`。
- 本地工具链可用时，执行最小范围的 Gradle 编译/测试任务；若工具链不可用，应明确说明未能完成的验证及原因。
- 交付时说明：改动的模块、关键设计决策、已执行的验证，以及仍受环境限制未验证的部分。
