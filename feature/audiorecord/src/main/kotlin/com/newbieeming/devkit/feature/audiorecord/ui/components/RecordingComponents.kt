package com.newbieeming.devkit.feature.audiorecord.ui.components

import android.media.AudioFormat
import android.media.MediaRecorder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.newbieeming.devkit.feature.audiorecord.data.model.AudioFormatExt
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingConfig
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingItem
import com.newbieeming.devkit.feature.audiorecord.data.repository.RecordingRepository
import com.newbieeming.devkit.feature.audiorecord.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Waveform(
    levels: List<Float>,
    channelCount: Int,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
    val channels = channelCount.coerceIn(1, 8)
    val samples = remember { List(8) { mutableStateListOf<Float>() } }
    val wasRecording = remember { mutableStateOf(isRecording) }
    val waveformColor = MaterialTheme.colorScheme.primary
    LaunchedEffect(isRecording, channels) {
        // 与原 SoundCapture 一致：开始一次新录音时清空；停止后保留最后波形。
        if (isRecording && !wasRecording.value) samples.forEach { it.clear() }
        wasRecording.value = isRecording
    }
    LaunchedEffect(levels, isRecording, channels) {
        if (!isRecording || levels.isEmpty()) return@LaunchedEffect
        repeat(channels) { index ->
            samples[index].add(levels.getOrElse(index) { 0f }.coerceIn(0f, 1f))
            if (samples[index].size > 500) samples[index].removeAt(0)
        }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val laneHeight = size.height / channels
        repeat(channels) { channel ->
            val channelSamples = samples[channel]
            drawWaveformLane(
                values = channelSamples,
                centerY = laneHeight * (channel + .5f),
                maxHalfHeight = laneHeight * .45f,
                color = waveformColor,
                showBaseline = isRecording && channelSamples.isNotEmpty(),
            )
        }
    }
}

private fun DrawScope.drawWaveformLane(
    values: List<Float>,
    centerY: Float,
    maxHalfHeight: Float,
    color: Color,
    showBaseline: Boolean,
) {
    val maxBars = (size.width / BAR_SPACING_PX).toInt().coerceAtLeast(1)
    val firstSample = (values.size - maxBars).coerceAtLeast(0)
    val visibleSampleCount = values.size - firstSample
    val startX = (size.width - visibleSampleCount * BAR_SPACING_PX).coerceAtLeast(0f)

    if (showBaseline) {
        drawLine(
            color = color.copy(alpha = BASELINE_ALPHA),
            start = Offset(startX, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = WAVEFORM_STROKE_WIDTH,
        )
    }

    for (sampleIndex in firstSample until values.size) {
        val visibleIndex = sampleIndex - firstSample
        val halfHeight = maxHalfHeight * values[sampleIndex]
        val x = startX + visibleIndex * BAR_SPACING_PX + BAR_CENTER_OFFSET_PX
        drawLine(
            color = color,
            start = Offset(x, centerY - halfHeight),
            end = Offset(x, centerY + halfHeight),
            strokeWidth = WAVEFORM_STROKE_WIDTH,
        )
    }
}

private const val BAR_SPACING_PX = 2f
private const val BAR_CENTER_OFFSET_PX = 0.5f
private const val WAVEFORM_STROKE_WIDTH = 1f
private const val BASELINE_ALPHA = 0.4f

@Composable
fun RecordingButton(isRecording: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors(),
    ) {
        Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(if (isRecording) R.string.stop_recording else R.string.start_recording))
    }
}

@Composable
fun RecordingList(
    recordings: List<RecordingItem>,
    playingId: String?,
    onPlay: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    onRename: (RecordingItem, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (recordings.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_recordings), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(recordings, key = RecordingItem::id) { recording ->
                RecordingListItem(recording, recording.id == playingId, onPlay, onDelete, onRename)
            }
        }
    }
}

@Composable
private fun RecordingListItem(
    recording: RecordingItem,
    isPlaying: Boolean,
    onPlay: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    onRename: (RecordingItem, String) -> Unit,
) {
    var renaming by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(recording.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                Text("${formatSize(recording.fileSize)}  ${formatDate(recording.timestamp)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onPlay(recording) }, modifier = Modifier.size(36.dp)) {
                Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = stringResource(if (isPlaying) R.string.stop_playback else R.string.play), tint = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { renaming = true }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.rename)) }
            IconButton(onClick = { onDelete(recording) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error) }
        }
    }
    if (renaming) RenameDialog(recording.name, onDismiss = { renaming = false }) { name -> onRename(recording, name); renaming = false }
}

@Composable
private fun RenameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_recording)) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.file_name)) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
fun ConfigDialog(config: RecordingConfig, onDismiss: () -> Unit, onConfirm: (RecordingConfig) -> Unit) {
    var source by remember(config) { mutableStateOf(config.audioSource) }
    var rate by remember(config) { mutableStateOf(config.sampleRate) }
    var inputChannels by remember(config) { mutableStateOf(config.channelConfig) }
    var format by remember(config) { mutableStateOf(config.audioFormat) }
    var waveformChannels by remember(config) { mutableStateOf(config.waveformChannelCount) }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recording_parameters)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ConfigField(stringResource(R.string.audio_source), source, audioSources()) { source = it }
                ConfigField(stringResource(R.string.sample_rate), rate, listOf(8000, 16000, 22050, 44100, 48000).associateWith { "$it Hz" }) { rate = it }
                ConfigField(stringResource(R.string.input_channels), inputChannels, inputChannelConfigs) { inputChannels = it }
                ConfigField(stringResource(R.string.encoding_format), format, audioFormats) { format = it }
                ConfigField(stringResource(R.string.waveform_channels), waveformChannels, (1..8).associateWith { it.toString() }) { waveformChannels = it }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(RecordingConfig(source, rate, inputChannels, format, waveformChannels)) }) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private fun audioSources(): Map<Int, String> = buildMap {
    put(MediaRecorder.AudioSource.DEFAULT, "DEFAULT")
    put(MediaRecorder.AudioSource.MIC, "MIC")
    put(MediaRecorder.AudioSource.VOICE_UPLINK, "VOICE_UPLINK")
    put(MediaRecorder.AudioSource.VOICE_DOWNLINK, "VOICE_DOWNLINK")
    put(MediaRecorder.AudioSource.VOICE_CALL, "VOICE_CALL")
    put(MediaRecorder.AudioSource.CAMCORDER, "CAMCORDER")
    put(MediaRecorder.AudioSource.VOICE_RECOGNITION, "VOICE_RECOGNITION")
    put(MediaRecorder.AudioSource.VOICE_COMMUNICATION, "VOICE_COMMUNICATION")
    put(MediaRecorder.AudioSource.REMOTE_SUBMIX, "REMOTE_SUBMIX")
    put(MediaRecorder.AudioSource.UNPROCESSED, "UNPROCESSED")
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        put(MediaRecorder.AudioSource.VOICE_PERFORMANCE, "VOICE_PERFORMANCE")
    }
}

private val inputChannelConfigs = linkedMapOf(
    AudioFormat.CHANNEL_IN_DEFAULT to "CHANNEL_IN_DEFAULT",
    AudioFormat.CHANNEL_IN_MONO to "CHANNEL_IN_MONO",
    AudioFormat.CHANNEL_IN_STEREO to "CHANNEL_IN_STEREO",
    AudioFormat.CHANNEL_IN_LEFT to "CHANNEL_IN_LEFT",
    AudioFormat.CHANNEL_IN_RIGHT to "CHANNEL_IN_RIGHT",
    AudioFormat.CHANNEL_IN_BACK to "CHANNEL_IN_BACK",
    AudioFormatExt.CHANNEL_IN_BACK_LEFT to "CHANNEL_IN_BACK_LEFT",
    AudioFormatExt.CHANNEL_IN_BACK_RIGHT to "CHANNEL_IN_BACK_RIGHT",
    AudioFormatExt.CHANNEL_IN_CENTER to "CHANNEL_IN_CENTER",
    AudioFormatExt.CHANNEL_IN_LOW_FREQUENCY to "CHANNEL_IN_LOW_FREQUENCY",
    AudioFormatExt.CHANNEL_IN_TOP_LEFT to "CHANNEL_IN_TOP_LEFT",
    AudioFormatExt.CHANNEL_IN_TOP_RIGHT to "CHANNEL_IN_TOP_RIGHT",
    AudioFormatExt.CHANNEL_IN_2POINT0POINT2 to "CHANNEL_IN_2POINT0POINT2",
    AudioFormatExt.CHANNEL_IN_2POINT1POINT2 to "CHANNEL_IN_2POINT1POINT2",
    AudioFormatExt.CHANNEL_IN_3POINT0POINT2 to "CHANNEL_IN_3POINT0POINT2",
    AudioFormatExt.CHANNEL_IN_3POINT1POINT2 to "CHANNEL_IN_3POINT1POINT2",
    AudioFormatExt.CHANNEL_IN_5POINT1 to "CHANNEL_IN_5POINT1",
    AudioFormatExt.CHANNEL_IN_FRONT_BACK to "CHANNEL_IN_FRONT_BACK",
)

private val audioFormats = linkedMapOf(
    AudioFormat.ENCODING_PCM_8BIT to "PCM 8 bit",
    AudioFormat.ENCODING_PCM_16BIT to "PCM 16 bit",
    AudioFormat.ENCODING_PCM_32BIT to "PCM 32 bit",
    AudioFormat.ENCODING_PCM_FLOAT to "PCM Float",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigField(label: String, selected: Int, options: Map<Int, String>, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = options[selected] ?: stringResource(R.string.unknown),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { (value, text) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            onSelect(value)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

fun recordingParams(config: RecordingConfig): String = listOf(
    RecordingRepository.sampleRateToken(config.sampleRate),
    RecordingRepository.audioFormatToken(config.audioFormat),
    RecordingRepository.channelConfigToken(config.channelConfig),
    RecordingRepository.audioSourceToken(config.audioSource),
    RecordingRepository.channelCountToken(config.waveformChannelCount),
).joinToString(" · ")

private fun formatDate(timestamp: Long) = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
private fun formatSize(bytes: Long) = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1fK", bytes / 1024.0)
    else -> String.format(Locale.getDefault(), "%.1fM", bytes / (1024.0 * 1024))
}
