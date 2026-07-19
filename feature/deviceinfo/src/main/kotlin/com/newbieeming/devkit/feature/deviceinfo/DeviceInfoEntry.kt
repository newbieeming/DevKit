package com.newbieeming.devkit.feature.deviceinfo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.deviceinfo.navigation.DEVICE_INFO_ROUTE

/**
 * 设备信息 Feature 入口
 *
 * 磁贴点击导航到详情页（版本、算力、厂商扩展属性等）。
 * TODO: @Provides @IntoSet 后移除 FeatureRegistries 中的手动注册
 */
class DeviceInfoEntry : FeatureEntry {
    override val featureId = "device_info"
    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        FeatureTileScaffold(
            icon = "🖥️",
            title = "设备信息",
            description = "系统版本、平台算力、厂商属性",
            modifier = modifier,
            onClick = { onNavigate(DEVICE_INFO_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.composable(route = DEVICE_INFO_ROUTE) {
            // TODO: DeviceInfoScreen(navController)
            androidx.compose.material3.Text("设备信息 — 待实现")
        }
    }
}
