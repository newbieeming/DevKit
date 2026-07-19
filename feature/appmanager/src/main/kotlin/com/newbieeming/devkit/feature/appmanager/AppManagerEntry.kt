package com.newbieeming.devkit.feature.appmanager

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.appmanager.navigation.APP_MANAGER_ROUTE

/**
 * 应用管理 Feature 入口
 *
 * 磁贴点击导航到应用列表页（信息 / 卸载 / 权限跳转）。
 * TODO: @Provides @IntoSet 后移除 FeatureRegistries 中的手动注册
 */
class AppManagerEntry : FeatureEntry {
    override val featureId = "app_manager"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        FeatureTileScaffold(
            icon = "📱",
            title = "应用管理",
            description = "查看信息、卸载、跳转权限设置",
            modifier = modifier,
            onClick = { onNavigate(APP_MANAGER_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.composable(route = APP_MANAGER_ROUTE) {
            // TODO: AppManagerScreen(navController)
            androidx.compose.material3.Text("应用管理 — 待实现")
        }
    }
}
