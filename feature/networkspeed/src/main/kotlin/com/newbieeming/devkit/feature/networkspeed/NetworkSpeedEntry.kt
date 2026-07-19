package com.newbieeming.devkit.feature.networkspeed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.networkspeed.navigation.NETWORK_SPEED_ROUTE

/**
 * 网速监控 Feature 入口
 *
 * 磁贴显示实时速率；点击导航到详情页可开关悬浮显示。
 * TODO: @Provides @IntoSet 后移除 FeatureRegistries 中的手动注册
 */
class NetworkSpeedEntry : FeatureEntry {
    override val featureId = "network_speed"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        // TODO: 接入 NetworkSpeedViewModel 展示实时速率
        FeatureTileScaffold(
            icon = "📶",
            title = "网速监控",
            description = "悬浮实时显示上下行速率",
            modifier = modifier,
            // badge = "↑ 1.2 MB/s",
            onClick = { onNavigate(NETWORK_SPEED_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.composable(route = NETWORK_SPEED_ROUTE) {
            // TODO: NetworkSpeedScreen(navController)
            androidx.compose.material3.Text("网速监控 — 待实现")
        }
    }
}
