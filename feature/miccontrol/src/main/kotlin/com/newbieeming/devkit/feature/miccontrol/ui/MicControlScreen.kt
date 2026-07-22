package com.newbieeming.devkit.feature.miccontrol.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.OverlayConfigurationScreen
import com.newbieeming.devkit.core.ui.rememberOverlayPermissionAction
import com.newbieeming.devkit.feature.miccontrol.MicControlViewModel
import com.newbieeming.devkit.feature.miccontrol.R

@Composable
fun MicControlScreen(
    onNavigateUp: () -> Unit,
    viewModel: MicControlViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsState()
    val isRunning by viewModel.isServiceRunning.collectAsState()
    var pendingConfig by remember(config) { mutableStateOf(config) }
    val toggleOverlay = rememberOverlayPermissionAction {
        viewModel.toggleOverlay(pendingConfig)
    }

    OverlayConfigurationScreen(
        title = stringResource(R.string.mic_control_title),
        config = config,
        isRunning = isRunning,
        onNavigateUp = onNavigateUp,
        onSave = viewModel::saveConfig,
        onToggle = { draft: OverlayConfig ->
            pendingConfig = draft
            if (isRunning) viewModel.toggleOverlay(draft) else toggleOverlay()
        },
    )
}
