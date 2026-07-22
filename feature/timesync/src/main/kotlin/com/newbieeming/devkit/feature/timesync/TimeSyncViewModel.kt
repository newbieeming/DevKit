package com.newbieeming.devkit.feature.timesync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.core.datastore.OverlayConfigRepository
import com.newbieeming.devkit.core.model.OverlayConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TimeSyncViewModel @Inject constructor(
    application: Application,
    private val configRepository: OverlayConfigRepository,
) : AndroidViewModel(application) {
    val config = configRepository.observe(FEATURE_ID, TIME_OVERLAY_DEFAULTS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TIME_OVERLAY_DEFAULTS)
    val options = configRepository.observeOptions(FEATURE_ID, TimeOverlayOptions.preferenceDefaults)
        .map(TimeOverlayOptions::fromPreferences)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeOverlayOptions())
    val isServiceRunning = TimeOverlayService.isServiceRunning

    fun saveConfig(config: OverlayConfig, options: TimeOverlayOptions) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            configRepository.saveOptions(FEATURE_ID, options.toPreferences())
            if (isServiceRunning.value) TimeOverlayService.update(getApplication(), config, options)
        }
    }

    fun toggleOverlay(config: OverlayConfig, options: TimeOverlayOptions) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            configRepository.saveOptions(FEATURE_ID, options.toPreferences())
            val context = getApplication<Application>()
            if (isServiceRunning.value) {
                TimeOverlayService.stop(context)
            } else {
                TimeOverlayService.start(context, config, options)
            }
        }
    }

    private companion object {
        const val FEATURE_ID = "time_sync"
    }
}

internal val TIME_OVERLAY_DEFAULTS = OverlayConfig(
    sizeDp = 180,
    startX = 100,
    startY = 460,
)
