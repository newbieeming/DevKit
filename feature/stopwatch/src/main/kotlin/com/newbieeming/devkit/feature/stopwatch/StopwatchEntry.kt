package com.newbieeming.devkit.feature.stopwatch

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
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
import com.newbieeming.devkit.feature.stopwatch.navigation.STOPWATCH_ROUTE
import com.newbieeming.devkit.feature.stopwatch.navigation.stopwatchScreen

class StopwatchEntry : FeatureEntry {
    override val featureId = "stopwatch"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        val viewModel: StopwatchViewModel = hiltViewModel()
        val config by viewModel.config.collectAsState()
        val isRunning by viewModel.isServiceRunning.collectAsState()
        val toggleOverlay = rememberOverlayPermissionAction {
            viewModel.toggleOverlay(config)
        }

        FeatureTileScaffold(
            icon = Icons.Default.Timer,
            title = stringResource(R.string.stopwatch_title),
            description = stringResource(R.string.stopwatch_description),
            modifier = modifier,
            actionIcon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            actionContentDescription = stringResource(
                if (isRunning) R.string.stop_stopwatch_overlay else R.string.start_stopwatch_overlay,
            ),
            onActionClick = {
                if (isRunning) viewModel.toggleOverlay(config) else toggleOverlay()
            },
            requiredPermissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
            onClick = { onNavigate(STOPWATCH_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.stopwatchScreen(onNavigateUp = { navController.navigateUp() })
    }
}
