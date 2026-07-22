package com.newbieeming.devkit.feature.networkspeed.ui

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
import com.newbieeming.devkit.core.ui.overlay.OverlayDropdownSetting
import com.newbieeming.devkit.core.ui.overlay.OverlaySettingOption
import com.newbieeming.devkit.core.ui.rememberOverlayPermissionAction
import com.newbieeming.devkit.feature.networkspeed.NetworkDisplayMode
import com.newbieeming.devkit.feature.networkspeed.NetworkIndicatorStyle
import com.newbieeming.devkit.feature.networkspeed.NetworkOverlayOptions
import com.newbieeming.devkit.feature.networkspeed.NetworkSpeedViewModel
import com.newbieeming.devkit.feature.networkspeed.R

@Composable
fun NetworkSpeedScreen(
    onNavigateUp: () -> Unit,
    viewModel: NetworkSpeedViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsState()
    val options by viewModel.options.collectAsState()
    val isRunning by viewModel.isServiceRunning.collectAsState()
    var pendingConfig by remember(config) { mutableStateOf(config) }
    var pendingOptions by remember(options) { mutableStateOf(options) }
    var draftOptions by remember(options) { mutableStateOf(options) }
    val toggleOverlay = rememberOverlayPermissionAction {
        viewModel.toggleOverlay(pendingConfig, pendingOptions)
    }

    OverlayConfigurationScreen(
        title = stringResource(R.string.network_speed_title),
        config = config,
        isRunning = isRunning,
        onNavigateUp = onNavigateUp,
        onSave = { draft -> viewModel.saveConfig(draft, draftOptions) },
        onToggle = { draft: OverlayConfig ->
            pendingConfig = draft
            pendingOptions = draftOptions
            if (isRunning) viewModel.toggleOverlay(draft, draftOptions) else toggleOverlay()
        },
        additionalContent = {
            OverlayDropdownSetting(
                title = stringResource(R.string.network_display_content),
                selected = draftOptions.displayMode,
                options = listOf(
                    OverlaySettingOption(NetworkDisplayMode.DOWNLOAD, stringResource(R.string.network_display_download)),
                    OverlaySettingOption(NetworkDisplayMode.UPLOAD, stringResource(R.string.network_display_upload)),
                    OverlaySettingOption(NetworkDisplayMode.BOTH, stringResource(R.string.network_display_both)),
                ),
                onSelected = { value -> draftOptions = draftOptions.copy(displayMode = value) },
            )
            OverlayDropdownSetting(
                title = stringResource(R.string.network_indicator_style),
                selected = draftOptions.indicatorStyle,
                options = listOf(
                    OverlaySettingOption(NetworkIndicatorStyle.ARROW, stringResource(R.string.network_indicator_arrow)),
                    OverlaySettingOption(NetworkIndicatorStyle.DOT, stringResource(R.string.network_indicator_dot)),
                    OverlaySettingOption(NetworkIndicatorStyle.NONE, stringResource(R.string.network_indicator_none)),
                ),
                onSelected = { value -> draftOptions = draftOptions.copy(indicatorStyle = value) },
            )
        },
    )
}
