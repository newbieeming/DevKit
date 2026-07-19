package com.newbieeming.devkit.feature.timesync

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.timesync.navigation.TIME_SYNC_ROUTE

/**
 * 时间同步 Feature 入口
 *
 * 磁贴显示当前时间；点击导航到详情页配置 NTP 服务器。
 * TODO: @Provides @IntoSet 后移除 FeatureRegistries 中的手动注册
 */
class TimeSyncEntry : FeatureEntry {
    override val featureId = "time_sync"
    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        // TODO: 接入 TimeSyncViewModel 展示实时时间
        FeatureTileScaffold(
            icon = "🕐",
            title = "时间同步",
            description = "可配置 NTP 服务器，精准对时",
            modifier = modifier,
            onClick = { onNavigate(TIME_SYNC_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.composable(route = TIME_SYNC_ROUTE) {
            // TODO: TimeSyncScreen(navController)
            androidx.compose.material3.Text("时间同步 — 待实现")
        }
    }
}
