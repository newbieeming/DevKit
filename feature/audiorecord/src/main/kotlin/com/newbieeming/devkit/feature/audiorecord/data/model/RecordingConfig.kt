package com.newbieeming.devkit.feature.audiorecord.data.model

import android.media.AudioFormat
import android.media.MediaRecorder

data class RecordingConfig(
    val audioSource: Int = MediaRecorder.AudioSource.MIC,
    val sampleRate: Int = 16_000,
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val waveformChannelCount: Int = 1,
)

data class RecordingItem(
    val id: String,
    val name: String,
    val filePath: String,
    val timestamp: Long,
    val fileSize: Long,
)
