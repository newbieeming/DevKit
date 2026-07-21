package com.newbieeming.devkit.feature.audiorecord.presentation

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.feature.audiorecord.R
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingConfig
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingItem
import com.newbieeming.devkit.feature.audiorecord.data.repository.RecordingRepository
import com.newbieeming.devkit.feature.audiorecord.data.repository.RecordingSettingsRepository
import com.newbieeming.devkit.feature.audiorecord.domain.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val repository: RecordingRepository,
    private val settings: RecordingSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RecordingState())
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<RecordingEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var recordingJob: Job? = null
    private var durationJob: Job? = null
    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var outputPath: String? = null
    private var startedAt = 0L

    init {
        settings.recordingConfig.onEach { config -> _state.update { it.copy(config = config) } }.launchIn(viewModelScope)
        repository.recordings.onEach { items -> _state.update { it.copy(recordings = items) } }.launchIn(viewModelScope)
        viewModelScope.launch(Dispatchers.IO) {
            repository.loadRecordings()
        }
    }

    fun startRecording() {
        if (_state.value.isRecording) return
        val file = repository.createRecordingFile(_state.value.config)
        outputPath = file.absolutePath
        startedAt = System.currentTimeMillis()
        _state.update { it.copy(isRecording = true, channelLevels = emptyList(), recordingDurationMs = 0L) }
        recordingJob = audioRecorder.startRecording(_state.value.config, file)
            // AudioRecord.read 与 PCM 文件写入均由 AudioRecorder 在 Dispatchers.IO 执行。
            .onEach { data -> _state.update { it.copy(isRecording = true, channelLevels = data.channelLevels) } }
            .catch { error ->
                _state.update { it.copy(isRecording = false, channelLevels = emptyList()) }
                durationJob?.cancel()
                outputPath = null
                _events.tryEmit(RecordingEvent.Message(R.string.recording_failed))
            }
            .launchIn(viewModelScope)
        durationJob = viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(recordingDurationMs = System.currentTimeMillis() - startedAt) }
                delay(100.milliseconds)
            }
        }
    }

    fun stopRecording() {
        if (!_state.value.isRecording && outputPath == null) return
        audioRecorder.stop()
        recordingJob?.cancel()
        durationJob?.cancel()
        _state.update { it.copy(isRecording = false, channelLevels = emptyList(), recordingDurationMs = 0L) }
        val savedPath = outputPath
        outputPath = null
        savedPath?.let { path ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.loadRecordings()
                _events.emit(RecordingEvent.Message(R.string.recording_saved, path))
            }
        }
    }

    fun updateConfig(config: RecordingConfig) {
        _state.update { it.copy(config = config) }
        viewModelScope.launch { settings.save(config) }
    }

    fun delete(recording: RecordingItem) {
        if (_state.value.currentPlayingId == recording.id) stopPlayback()
        viewModelScope.launch(Dispatchers.IO) {
            if (!repository.deleteRecording(recording.id)) {
                _events.emit(RecordingEvent.Message(R.string.delete_recording_failed))
            }
        }
    }

    fun rename(recording: RecordingItem, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!repository.renameRecording(recording.id, name)) {
                _events.emit(RecordingEvent.Message(R.string.rename_recording_failed))
            }
        }
    }

    fun togglePlayback(recording: RecordingItem) {
        if (_state.value.currentPlayingId == recording.id) stopPlayback() else play(recording)
    }

    private fun play(recording: RecordingItem) {
        stopPlayback()
        playbackJob = viewModelScope.launch(Dispatchers.IO) {
            val config = _state.value.config
            val channelCount = config.waveformChannelCount.coerceIn(1, 8)
            val channelIndexMask = RecordingRepository.channelIndexMaskFromCount(channelCount)
            val channelMask = when (channelCount) {
                1 -> AudioFormat.CHANNEL_OUT_MONO
                else -> AudioFormat.CHANNEL_OUT_STEREO
            }
            val bufferSize = AudioTrack.getMinBufferSize(config.sampleRate, channelMask, config.audioFormat)
            if (bufferSize <= 0) {
                _events.tryEmit(RecordingEvent.Message(R.string.audio_playback_initialization_failed))
                return@launch
            }
            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(config.audioFormat)
                            .setSampleRate(config.sampleRate)
                            .setChannelIndexMask(channelIndexMask)
                            .build(),
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrElse {
                _events.tryEmit(RecordingEvent.Message(R.string.playback_failed))
                return@launch
            }
            audioTrack = track
            withContext(Dispatchers.Main) { _state.update { it.copy(currentPlayingId = recording.id) } }
            try {
                track.play()
                FileInputStream(recording.filePath).use { input ->
                    val buffer = ByteArray(bufferSize)
                    while (isActive) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        track.write(buffer, 0, read)
                    }
                }
            } catch (error: Exception) {
                _events.tryEmit(RecordingEvent.Message(R.string.playback_failed))
            } finally {
                runCatching { track.stop() }
                track.release()
                if (audioTrack === track) audioTrack = null
                withContext(Dispatchers.Main) { _state.update { it.copy(currentPlayingId = null) } }
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        audioTrack?.let { track -> runCatching { track.stop() }; track.release() }
        audioTrack = null
        _state.update { it.copy(currentPlayingId = null) }
    }

    override fun onCleared() {
        stopRecording()
        stopPlayback()
    }
}

data class RecordingState(
    val isRecording: Boolean = false,
    val config: RecordingConfig = RecordingConfig(),
    val channelLevels: List<Float> = emptyList(),
    val recordingDurationMs: Long = 0L,
    val recordings: List<RecordingItem> = emptyList(),
    val currentPlayingId: String? = null,
)

sealed interface RecordingEvent {
    data class Message(
        @StringRes val messageRes: Int,
        val formatArg: String? = null,
    ) : RecordingEvent
}
