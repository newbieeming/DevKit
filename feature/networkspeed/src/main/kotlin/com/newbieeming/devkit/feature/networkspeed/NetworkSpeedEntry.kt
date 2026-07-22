package com.newbieeming.devkit.feature.networkspeed

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
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
import com.newbieeming.devkit.feature.networkspeed.navigation.NETWORK_SPEED_ROUTE
import com.newbieeming.devkit.feature.networkspeed.navigation.networkSpeedScreen

class NetworkSpeedEntry : FeatureEntry {
    override val featureId = "network_speed"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        val viewModel: NetworkSpeedViewModel = hiltViewModel()
        val config by viewModel.config.collectAsState()
        val options by viewModel.options.collectAsState()
        val isRunning by viewModel.isServiceRunning.collectAsState()
        val toggleOverlay = rememberOverlayPermissionAction {
            viewModel.toggleOverlay(config, options)
        }

        FeatureTileScaffold(
            icon = Icons.Default.NetworkCheck,
            title = stringResource(R.string.network_speed_title),
            description = stringResource(R.string.network_speed_description),
            modifier = modifier,
            actionIcon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            actionContentDescription = stringResource(
                if (isRunning) R.string.stop_network_overlay else R.string.start_network_overlay,
            ),
            onActionClick = {
                if (isRunning) viewModel.toggleOverlay(config, options) else toggleOverlay()
            },
            requiredPermissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
            onClick = { onNavigate(NETWORK_SPEED_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.networkSpeedScreen(onNavigateUp = { navController.navigateUp() })
    }
}
