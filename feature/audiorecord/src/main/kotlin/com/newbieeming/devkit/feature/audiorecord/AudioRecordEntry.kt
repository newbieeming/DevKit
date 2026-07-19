package com.newbieeming.devkit.feature.audiorecord

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.audiorecord.navigation.AUDIO_RECORD_ROUTE

/**
 * 音频录制 Feature 入口
 *
 * 磁贴点击导航到录制详情页（实时波形 + 文件列表）。
 * TODO: @Provides @IntoSet 后移除 FeatureRegistries 中的手动注册
 */
class AudioRecordEntry : FeatureEntry {
    override val featureId = "audio_record"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        FeatureTileScaffold(
            icon = "🎵",
            title = "音频录制",
            description = "实时波形可视化，本地文件管理",
            modifier = modifier,
            onClick = { onNavigate(AUDIO_RECORD_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.composable(route = AUDIO_RECORD_ROUTE) {
            // TODO: AudioRecordScreen(navController)
            androidx.compose.material3.Text("音频录制 — 待实现")
        }
    }
}
