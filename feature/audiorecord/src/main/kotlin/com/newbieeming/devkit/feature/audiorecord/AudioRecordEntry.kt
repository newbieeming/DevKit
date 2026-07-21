package com.newbieeming.devkit.feature.audiorecord

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.core.ui.rememberFeaturePermissionRequest
import com.newbieeming.devkit.core.permissions.DevKitPermissions
import com.newbieeming.devkit.feature.audiorecord.navigation.AUDIO_RECORD_ROUTE
import com.newbieeming.devkit.feature.audiorecord.navigation.audioRecordScreen

/**
 * 音频录制 Feature 入口
 *
 * 磁贴点击后由自身申请麦克风与存储权限，授权成功才导航到录制详情页。
 */
class AudioRecordEntry : FeatureEntry {
    override val featureId = "audio_record"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        val runtimePermissions = audioRecordRuntimePermissions()
        val requestAndNavigate = rememberFeaturePermissionRequest(
            runtimePermissions = runtimePermissions,
            requiresAllFilesAccess = true,
            onGranted = { onNavigate(AUDIO_RECORD_ROUTE) },
        )

        FeatureTileScaffold(
            icon = Icons.Default.GraphicEq,
            title = stringResource(R.string.audio_record_title),
            description = stringResource(R.string.audio_record_description),
            modifier = modifier,
            requiredPermissions = audioRecordDisplayedPermissions(),
            onClick = requestAndNavigate,
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.audioRecordScreen(onNavigateUp = { navController.navigateUp() })
    }
}

private fun audioRecordRuntimePermissions(): List<String> = buildList {
    add(DevKitPermissions.RECORD_AUDIO)
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}

private fun audioRecordDisplayedPermissions(): List<String> = buildList {
    add(DevKitPermissions.RECORD_AUDIO)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        add(DevKitPermissions.ALL_FILES_ACCESS)
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
