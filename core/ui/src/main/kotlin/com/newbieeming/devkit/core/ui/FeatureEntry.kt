package com.newbieeming.devkit.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

/**
 * Feature 入口契约
 *
 * 每个功能模块实现此接口，向主界面提供：
 *   1. 一个磁贴（Tile）— 展示在 FlowRow 仪表盘中
 *   2. 可选的二级导航目的地（registerNavigation）
 *
 * ⚠️  重要：Tile 方法不声明默认参数值。
 *    Kotlin 接口中带默认值的 @Composable 方法跨模块实现时，
 *    Kotlin 生成的 DefaultImpls + Compose 注入的 Composer 参数会导致 AbstractMethodError。
 *    调用方（MainActivity）始终显式传入所有参数。
 *
 * ──────────────────────────────────────────────────────────────
 * 自动发现机制（AGP 9.x 兼容后启用）：
 *   每个 feature 模块通过 Hilt @Multibindings 将自身注入到 Set<FeatureEntry>：
 *
 *   @Module @InstallIn(SingletonComponent::class)
 *   object XxxFeatureModule {
 *       @Provides @IntoSet
 *       fun provideEntry(): FeatureEntry = XxxEntry()
 *   }
 *
 *   在 MainActivity 中通过 @Inject Set<FeatureEntry> 接收，无需手动声明。
 *
 * 临时方案（当前）：
 *   在 :app 模块的 FeatureRegistries 中手动注册，待 Hilt 恢复后直接删除该文件。
 * ──────────────────────────────────────────────────────────────
 */
interface FeatureEntry {

    /** 功能唯一标识符，也用作二级路由前缀，需保证全局唯一 */
    val featureId: String

    /**
     * 磁贴展示优先级：数值越小越靠前。
     * 不设置则为 [Int.MAX_VALUE]（按注册顺序排在末尾）。
     */
    val priority: Int get() = Int.MAX_VALUE

    /**
     * 在主界面 FlowRow 中渲染的磁贴 Composable。
     *
     * 注意：不声明默认参数值（防止跨模块 AbstractMethodError）。
     * 调用方应始终显式传入所有参数。
     *
     * @param modifier    由调用方传入尺寸/间距等约束
     * @param onNavigate  导航回调；直接执行的功能传入空 lambda
     */
    @Composable
    fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit)

    /**
     * 向应用 NavHost 注册本功能的二级界面（可选）。
     * 不需要独立页面的功能不必重写此方法。
     *
     * 此方法非 @Composable，跨模块默认实现安全。
     */
    fun registerNavigation(builder: NavGraphBuilder, navController: NavController) = Unit
}
