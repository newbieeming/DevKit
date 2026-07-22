package com.newbieeming.devkit.feature.timesync

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
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
import com.newbieeming.devkit.feature.timesync.navigation.TIME_SYNC_ROUTE
import com.newbieeming.devkit.feature.timesync.navigation.timeSyncScreen

class TimeSyncEntry : FeatureEntry {
    override val featureId = "time_sync"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        val viewModel: TimeSyncViewModel = hiltViewModel()
        val config by viewModel.config.collectAsState()
        val options by viewModel.options.collectAsState()
        val isRunning by viewModel.isServiceRunning.collectAsState()
        val toggleOverlay = rememberOverlayPermissionAction {
            viewModel.toggleOverlay(config, options)
        }

        FeatureTileScaffold(
            icon = Icons.Default.Schedule,
            title = stringResource(R.string.time_sync_title),
            description = stringResource(R.string.time_sync_description),
            modifier = modifier,
            actionIcon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            actionContentDescription = stringResource(
                if (isRunning) R.string.stop_time_overlay else R.string.start_time_overlay,
            ),
            onActionClick = {
                if (isRunning) viewModel.toggleOverlay(config, options) else toggleOverlay()
            },
            requiredPermissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
            onClick = { onNavigate(TIME_SYNC_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.timeSyncScreen(onNavigateUp = { navController.navigateUp() })
    }
}
