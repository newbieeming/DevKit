package com.newbieeming.devkit.feature.audiorecord.data.repository

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.MediaRecorder
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingConfig
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** 管理原 SoundCapture 使用的公共 PCM 目录。 */
@Singleton
class RecordingRepository @Inject constructor() {
    private val _recordings = MutableStateFlow<List<RecordingItem>>(emptyList())
    val recordings = _recordings.asStateFlow()

    private val recordingsDir: File
        get() = File(RECORDINGS_DIR_PATH).apply { mkdirs() }

    fun createRecordingFile(config: RecordingConfig): File {
        val timestamp = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(Date())
        val baseName = "${sampleRateToken(config.sampleRate)}_${channelCountToken(config.waveformChannelCount)}_" +
            "${audioSourceToken(config.audioSource)}_${channelConfigToken(config.channelConfig)}_" +
            "${audioFormatToken(config.audioFormat)}_$timestamp"
        var candidate = recordingsDir.resolve("$baseName.pcm")
        var suffix = 1
        while (candidate.exists()) {
            candidate = recordingsDir.resolve("${baseName}_$suffix.pcm")
            suffix++
        }
        return candidate
    }

    fun loadRecordings() {
        _recordings.value = recordingsDir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.extension.equals("pcm", ignoreCase = true) }
            ?.map {
                RecordingItem(
                    id = it.nameWithoutExtension,
                    name = it.nameWithoutExtension,
                    filePath = it.absolutePath,
                    timestamp = it.lastModified(),
                    fileSize = it.length(),
                )
            }
            ?.sortedByDescending(RecordingItem::timestamp)
            ?.toList()
            .orEmpty()
    }

    fun deleteRecording(id: String): Boolean = recordingsDir.resolve("$id.pcm").delete().also {
        if (it) loadRecordings()
    }

    fun renameRecording(id: String, requestedName: String): Boolean {
        val name = requestedName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (name.isBlank()) return false
        return recordingsDir.resolve("$id.pcm").renameTo(recordingsDir.resolve("$name.pcm")).also {
            if (it) loadRecordings()
        }
    }

    companion object {
        @SuppressLint("SdCardPath")
        private const val RECORDINGS_DIR_PATH = "/sdcard/SoundCapture"

        fun sampleRateToken(sampleRate: Int) = if (sampleRate % 1000 == 0) "${sampleRate / 1000}K" else "${sampleRate}HZ"
        fun channelCountToken(channelCount: Int) = "${channelCount.coerceIn(1, 8)}CH"

        /** 根据波形通道数生成 AudioTrack 使用的 channel index mask。 */
        fun channelIndexMaskFromCount(channelCount: Int): Int =
            (1 shl channelCount.coerceIn(1, 8)) - 1

        fun audioSourceToken(source: Int) = when (source) {
            MediaRecorder.AudioSource.MIC -> "MIC"
            MediaRecorder.AudioSource.CAMCORDER -> "CAMCORDER"
            MediaRecorder.AudioSource.VOICE_RECOGNITION -> "VOICE_RECOGNITION"
            MediaRecorder.AudioSource.VOICE_COMMUNICATION -> "VOICE_COMMUNICATION"
            MediaRecorder.AudioSource.UNPROCESSED -> "UNPROCESSED"
            else -> "SOURCE_$source"
        }
        fun channelConfigToken(config: Int) = when (config) {
            AudioFormat.CHANNEL_IN_MONO -> "MONO"
            AudioFormat.CHANNEL_IN_STEREO -> "STEREO"
            else -> "CHANNEL_$config"
        }
        fun audioFormatToken(format: Int) = when (format) {
            AudioFormat.ENCODING_PCM_8BIT -> "8BIT"
            AudioFormat.ENCODING_PCM_16BIT -> "16BIT"
            AudioFormat.ENCODING_PCM_32BIT -> "32BIT"
            AudioFormat.ENCODING_PCM_FLOAT -> "FLOAT"
            else -> "FORMAT_$format"
        }
    }
}
