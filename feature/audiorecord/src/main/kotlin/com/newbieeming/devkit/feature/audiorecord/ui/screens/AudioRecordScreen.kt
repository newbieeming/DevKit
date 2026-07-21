package com.newbieeming.devkit.feature.audiorecord.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newbieeming.devkit.feature.audiorecord.presentation.RecordingEvent
import com.newbieeming.devkit.feature.audiorecord.presentation.RecordingState
import com.newbieeming.devkit.feature.audiorecord.presentation.RecordingViewModel
import com.newbieeming.devkit.feature.audiorecord.ui.components.ConfigDialog
import com.newbieeming.devkit.feature.audiorecord.ui.components.RecordingButton
import com.newbieeming.devkit.feature.audiorecord.ui.components.RecordingList
import com.newbieeming.devkit.feature.audiorecord.ui.components.Waveform
import com.newbieeming.devkit.feature.audiorecord.ui.components.recordingParams
import com.newbieeming.devkit.feature.audiorecord.R

@Composable
fun AudioRecordScreen(
    onNavigateUp: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val messageTemplates = rememberRecordingMessageTemplates()
    var showConfig by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel, messageTemplates) {
        viewModel.events.collect { event ->
            if (event is RecordingEvent.Message) {
                snackbar.showSnackbar(event.resolve(messageTemplates))
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (maxWidth >= 600.dp) {
                Row(modifier = Modifier.fillMaxSize()) {
                    RecordingPanel(state, onNavigateUp, Modifier.weight(.65f))
                    Spacer(Modifier.width(10.dp))
                    RecordingsPanel(state, viewModel, { showConfig = true }, Modifier.weight(.35f))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    RecordingPanel(state, onNavigateUp, Modifier.weight(1f))
                    Spacer(Modifier.height(10.dp))
                    RecordingsPanel(state, viewModel, { showConfig = true }, Modifier.weight(1f))
                }
            }
        }
    }
    if (showConfig) ConfigDialog(state.config, { showConfig = false }) { config -> viewModel.updateConfig(config); showConfig = false }
}

@Composable
private fun rememberRecordingMessageTemplates(): Map<Int, String> {
    val recordingFailed = stringResource(R.string.recording_failed)
    val recordingSaved = stringResource(R.string.recording_saved)
    val deleteFailed = stringResource(R.string.delete_recording_failed)
    val renameFailed = stringResource(R.string.rename_recording_failed)
    val playbackInitializationFailed = stringResource(R.string.audio_playback_initialization_failed)
    val playbackFailed = stringResource(R.string.playback_failed)
    return remember(
        recordingFailed,
        recordingSaved,
        deleteFailed,
        renameFailed,
        playbackInitializationFailed,
        playbackFailed,
    ) {
        mapOf(
            R.string.recording_failed to recordingFailed,
            R.string.recording_saved to recordingSaved,
            R.string.delete_recording_failed to deleteFailed,
            R.string.rename_recording_failed to renameFailed,
            R.string.audio_playback_initialization_failed to playbackInitializationFailed,
            R.string.playback_failed to playbackFailed,
        )
    }
}

private fun RecordingEvent.Message.resolve(templates: Map<Int, String>): String {
    val template = templates.getValue(messageRes)
    return formatArg?.let { arg -> template.format(arg) } ?: template
}

@Composable
private fun RecordingPanel(state: RecordingState, onNavigateUp: () -> Unit, modifier: Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Waveform(state.channelLevels, state.config.waveformChannelCount, state.isRecording)
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(recordingParams(state.config), modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(formatDuration(state.recordingDurationMs), modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), style = MaterialTheme.typography.headlineMedium, color = if (state.isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecordingsPanel(
    state: RecordingState,
    viewModel: RecordingViewModel,
    onSettings: () -> Unit,
    modifier: Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.recording_list), style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            RecordingList(state.recordings, state.currentPlayingId, viewModel::togglePlayback, viewModel::delete, viewModel::rename, Modifier.weight(1f))
            RecordingButton(state.isRecording, onClick = { if (state.isRecording) viewModel.stopRecording() else viewModel.startRecording() })
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Settings, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.configure_parameters)) }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    return "%02d:%02d.%03d".format(seconds / 60, seconds % 60, durationMs % 1000)
}
