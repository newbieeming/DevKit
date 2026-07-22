package com.newbieeming.devkit.feature.timesync.ui

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
import com.newbieeming.devkit.feature.timesync.R
import com.newbieeming.devkit.feature.timesync.TimeFormatOption
import com.newbieeming.devkit.feature.timesync.TimeSyncViewModel

@Composable
fun TimeSyncScreen(
    onNavigateUp: () -> Unit,
    viewModel: TimeSyncViewModel = hiltViewModel(),
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
        title = stringResource(R.string.time_sync_title),
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
                title = stringResource(R.string.time_format),
                selected = draftOptions.format,
                options = listOf(
                    OverlaySettingOption(TimeFormatOption.SYSTEM, stringResource(R.string.time_format_system)),
                    OverlaySettingOption(TimeFormatOption.HOUR_24_WITH_SECONDS, stringResource(R.string.time_format_24_seconds)),
                    OverlaySettingOption(TimeFormatOption.HOUR_24, stringResource(R.string.time_format_24)),
                    OverlaySettingOption(TimeFormatOption.HOUR_12_WITH_SECONDS, stringResource(R.string.time_format_12_seconds)),
                    OverlaySettingOption(TimeFormatOption.HOUR_12, stringResource(R.string.time_format_12)),
                ),
                onSelected = { value -> draftOptions = draftOptions.copy(format = value) },
            )
        },
    )
}
