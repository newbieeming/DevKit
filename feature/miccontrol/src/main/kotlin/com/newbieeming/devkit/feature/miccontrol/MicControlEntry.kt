package com.newbieeming.devkit.feature.miccontrol

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold

/**
 * 麦克风控制 Feature 入口
 *
 * 磁贴点击直接启动悬浮窗。
 *
 */
class MicControlEntry : FeatureEntry {
    override val featureId = "mic_control"
    override val priority = 10   // 悬浮控制类优先展示

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        val viewModel: MicControlViewModel = hiltViewModel()
        val context = LocalContext.current
        val isServiceRunning by viewModel.isServiceRunningFlow.collectAsState()

        FeatureTileScaffold(
            icon = "🎙️",
            title = "MIC悬浮按钮",
            description = "控制系统全局麦克风静音(可拖拽)",
            modifier = modifier,
            badge = if (isServiceRunning) "已启用" else "未启用",
            requiredPermissions = listOf(
                Manifest.permission.SYSTEM_ALERT_WINDOW
            ),
            onClick = {
                if (Settings.canDrawOverlays(context)) {
                    viewModel.toggleOverlay()
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri()
                    )
                    context.startActivity(intent)
                }
            },
        )
    }
}
