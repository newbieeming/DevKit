package com.newbieeming.devkit.feature.stopwatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.core.datastore.OverlayConfigRepository
import com.newbieeming.devkit.core.model.OverlayConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    application: Application,
    private val configRepository: OverlayConfigRepository,
) : AndroidViewModel(application) {
    val config = configRepository.observe(FEATURE_ID, STOPWATCH_OVERLAY_DEFAULTS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), STOPWATCH_OVERLAY_DEFAULTS)
    val isServiceRunning = StopwatchOverlayService.isServiceRunning
    val timerState = StopwatchOverlayService.timerState

    fun saveConfig(config: OverlayConfig) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            if (isServiceRunning.value) {
                StopwatchOverlayService.update(getApplication(), config)
            }
        }
    }

    fun toggleOverlay(config: OverlayConfig) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            val context = getApplication<Application>()
            if (isServiceRunning.value) {
                StopwatchOverlayService.stop(context)
            } else {
                StopwatchOverlayService.start(context, config)
            }
        }
    }

    fun startOrResume(config: OverlayConfig) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            if (isServiceRunning.value) {
                StopwatchOverlayService.startOrResume(context)
            } else {
                configRepository.save(FEATURE_ID, config)
                StopwatchOverlayService.start(context, config)
            }
        }
    }

    fun pause() {
        if (isServiceRunning.value) {
            StopwatchOverlayService.pause(getApplication())
        }
    }

    fun reset() {
        if (isServiceRunning.value) {
            StopwatchOverlayService.reset(getApplication())
        }
    }

    private companion object {
        const val FEATURE_ID = "stopwatch"
    }
}
