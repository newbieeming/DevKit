package com.newbieeming.devkit.feature.miccontrol

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.core.ui.rememberOverlayPermissionAction
import com.newbieeming.devkit.feature.miccontrol.navigation.MIC_CONTROL_ROUTE
import com.newbieeming.devkit.feature.miccontrol.navigation.micControlScreen

class MicControlEntry : FeatureEntry {
    override val featureId = "mic_control"
    override val priority = 10

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        val viewModel: MicControlViewModel = hiltViewModel()
        val config by viewModel.config.collectAsState()
        val isRunning by viewModel.isServiceRunning.collectAsState()
        val toggleOverlay = rememberOverlayPermissionAction {
            viewModel.toggleOverlay(config)
        }

        FeatureTileScaffold(
            icon = Icons.Default.Mic,
            title = stringResource(R.string.mic_control_title),
            description = stringResource(R.string.mic_control_description),
            modifier = modifier,
            actionIcon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            actionContentDescription = stringResource(
                if (isRunning) R.string.stop_mic_overlay else R.string.start_mic_overlay,
            ),
            onActionClick = {
                if (isRunning) viewModel.toggleOverlay(config) else toggleOverlay()
            },
            requiredPermissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
            onClick = { onNavigate(MIC_CONTROL_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.micControlScreen(onNavigateUp = { navController.navigateUp() })
    }
}
