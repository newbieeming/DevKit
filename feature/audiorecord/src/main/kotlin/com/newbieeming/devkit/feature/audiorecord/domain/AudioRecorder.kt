package com.newbieeming.devkit.feature.audiorecord.domain

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import androidx.annotation.RequiresPermission
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class AudioRecorder @Inject constructor() {
    @Volatile private var recording = false
    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun startRecording(config: RecordingConfig, outputFile: File): Flow<RecordingData> =
        flow {
            val bufferSize = AudioRecord.getMinBufferSize(config.sampleRate, config.channelConfig, config.audioFormat)
            check(bufferSize > 0) { "Unsupported recording configuration: $bufferSize" }
            val recorder = createAudioRecord(config, bufferSize)
            val bytesPerSample = when (config.audioFormat) {
                AudioFormat.ENCODING_PCM_8BIT -> 1
                AudioFormat.ENCODING_PCM_32BIT, AudioFormat.ENCODING_PCM_FLOAT -> 4
                else -> 2
            }
            val buffer = ByteArray(bufferSize)
            audioRecord = recorder
            recording = true
            try {
                recorder.startRecording()
                FileOutputStream(outputFile).use { output ->
                    while (recording) {
                        val readBytes = recorder.read(buffer, 0, minOf(buffer.size, 512 * bytesPerSample))
                        if (readBytes <= 0) {
                            delay(5.milliseconds)
                            continue
                        }
                        output.write(buffer, 0, readBytes)
                        emit(
                            RecordingData(
                                channelLevels = channelLevels(
                                    buffer,
                                    readBytes,
                                    bytesPerSample,
                                    config.audioFormat,
                                    config.waveformChannelCount,
                                ),
                            ),
                        )
                    }
                }
            } finally {
                release(recorder)
            }
        }.flowOn(Dispatchers.IO)

    fun stop() {
        recording = false
        audioRecord?.let(::release)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun createAudioRecord(config: RecordingConfig, bufferSize: Int): AudioRecord =
        AudioRecord(config.audioSource, config.sampleRate, config.channelConfig, config.audioFormat, bufferSize)
            .also { recorder ->
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    recorder.release()
                    error("AudioRecord initialization failed")
                }
            }

    private fun release(recorder: AudioRecord) {
        runCatching { recorder.stop() }
        runCatching { recorder.release() }
        if (audioRecord === recorder) audioRecord = null
    }

    private fun channelLevels(
        data: ByteArray,
        bytesRead: Int,
        bytesPerSample: Int,
        audioFormat: Int,
        channelCount: Int,
    ): List<Float> {
        val samples = bytesRead / bytesPerSample
        val channels = channelCount.coerceIn(1, 8)
        if (samples == 0) return List(channels) { 0f }
        val byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        return List(channels) { channel ->
            var peak = 0f
            var sum = 0f
            var count = 0
            for (index in channel until samples step channels) {
                val value = when (audioFormat) {
                    AudioFormat.ENCODING_PCM_8BIT -> data[index].toFloat() / 128f
                    AudioFormat.ENCODING_PCM_32BIT -> byteBuffer.getInt(index * bytesPerSample).toFloat() / Int.MAX_VALUE
                    AudioFormat.ENCODING_PCM_FLOAT -> byteBuffer.getFloat(index * bytesPerSample)
                    else -> byteBuffer.getShort(index * bytesPerSample).toFloat() / 32768f
                }.coerceIn(-1f, 1f)
                val amplitude = abs(value)
                peak = maxOf(peak, amplitude)
                sum += amplitude
                count++
            }
            ((if (count == 0) 0f else sum / count) * .6f + peak * .4f).coerceIn(0f, 1f)
        }
    }
}

data class RecordingData(val channelLevels: List<Float>)
