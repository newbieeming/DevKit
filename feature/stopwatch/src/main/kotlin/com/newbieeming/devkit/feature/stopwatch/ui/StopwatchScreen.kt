package com.newbieeming.devkit.feature.stopwatch.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.OverlayConfigurationScreen
import com.newbieeming.devkit.core.ui.rememberOverlayPermissionAction
import com.newbieeming.devkit.feature.stopwatch.R
import com.newbieeming.devkit.feature.stopwatch.StopwatchState
import com.newbieeming.devkit.feature.stopwatch.StopwatchStatus
import com.newbieeming.devkit.feature.stopwatch.StopwatchViewModel
import com.newbieeming.devkit.feature.stopwatch.toStopwatchText

@Composable
fun StopwatchScreen(
    onNavigateUp: () -> Unit,
    viewModel: StopwatchViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    var pendingConfig by remember(config) { mutableStateOf(config) }
    val toggleOverlay = rememberOverlayPermissionAction {
        viewModel.toggleOverlay(pendingConfig)
    }
    val startTimer = rememberOverlayPermissionAction {
        viewModel.startOrResume(config)
    }

    OverlayConfigurationScreen(
        title = stringResource(R.string.stopwatch_title),
        config = config,
        isRunning = isServiceRunning,
        onNavigateUp = onNavigateUp,
        onSave = viewModel::saveConfig,
        onToggle = { draft: OverlayConfig ->
            pendingConfig = draft
            if (isServiceRunning) viewModel.toggleOverlay(draft) else toggleOverlay()
        },
        additionalContent = {
            StopwatchStatusControls(
                state = timerState,
                onStartOrResume = {
                    if (isServiceRunning) viewModel.startOrResume(config) else startTimer()
                },
                onPause = viewModel::pause,
                onReset = viewModel::reset,
            )
        },
    )
}

@Composable
private fun StopwatchStatusControls(
    state: StopwatchState,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.stopwatch_status),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(
                when (state.status) {
                    StopwatchStatus.RUNNING -> R.string.stopwatch_status_running
                    StopwatchStatus.PAUSED -> R.string.stopwatch_status_paused
                    StopwatchStatus.RESET -> R.string.stopwatch_status_reset
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = state.elapsedMillis.toStopwatchText(),
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.headlineMedium,
        )
        FilledTonalButton(
            onClick = if (state.status == StopwatchStatus.RUNNING) onPause else onStartOrResume,
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.material3.Icon(
                imageVector = if (state.status == StopwatchStatus.RUNNING) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = null,
            )
            Text(
                text = stringResource(
                    if (state.status == StopwatchStatus.RUNNING) {
                        R.string.pause_stopwatch
                    } else {
                        R.string.start_stopwatch
                    },
                ),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onReset,
            enabled = state.elapsedMillis > 0L || state.status != StopwatchStatus.RESET,
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.stop_and_reset_stopwatch),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
