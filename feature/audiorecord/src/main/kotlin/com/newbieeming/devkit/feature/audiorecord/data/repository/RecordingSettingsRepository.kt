package com.newbieeming.devkit.feature.audiorecord.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.newbieeming.devkit.feature.audiorecord.data.model.RecordingConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.audioRecordDataStore by preferencesDataStore(name = "audio_record_settings")

@Singleton
class RecordingSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val recordingConfig = context.audioRecordDataStore.data.map { preferences ->
        RecordingConfig(
            audioSource = preferences[AUDIO_SOURCE] ?: RecordingConfig().audioSource,
            sampleRate = preferences[SAMPLE_RATE] ?: RecordingConfig().sampleRate,
            channelConfig = preferences[CHANNEL_CONFIG] ?: RecordingConfig().channelConfig,
            audioFormat = preferences[AUDIO_FORMAT] ?: RecordingConfig().audioFormat,
            waveformChannelCount = (preferences[WAVEFORM_CHANNELS] ?: 1).coerceIn(1, 8),
        )
    }

    suspend fun save(config: RecordingConfig) {
        context.audioRecordDataStore.edit { preferences ->
            preferences[AUDIO_SOURCE] = config.audioSource
            preferences[SAMPLE_RATE] = config.sampleRate
            preferences[CHANNEL_CONFIG] = config.channelConfig
            preferences[AUDIO_FORMAT] = config.audioFormat
            preferences[WAVEFORM_CHANNELS] = config.waveformChannelCount.coerceIn(1, 8)
        }
    }

    private companion object {
        val AUDIO_SOURCE = intPreferencesKey("audio_source")
        val SAMPLE_RATE = intPreferencesKey("sample_rate")
        val CHANNEL_CONFIG = intPreferencesKey("channel_config")
        val AUDIO_FORMAT = intPreferencesKey("audio_format")
        val WAVEFORM_CHANNELS = intPreferencesKey("waveform_channels")
    }
}
