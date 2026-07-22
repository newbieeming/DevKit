package com.newbieeming.devkit.feature.miccontrol

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
class MicControlViewModel @Inject constructor(
    application: Application,
    private val configRepository: OverlayConfigRepository,
) : AndroidViewModel(application) {
    val config = configRepository.observe(FEATURE_ID, MIC_OVERLAY_DEFAULTS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MIC_OVERLAY_DEFAULTS)
    val isServiceRunning = MicControlService.isServiceRunning

    fun saveConfig(config: OverlayConfig) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            if (isServiceRunning.value) MicControlService.update(getApplication(), config)
        }
    }

    fun toggleOverlay(config: OverlayConfig) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            val context = getApplication<Application>()
            if (isServiceRunning.value) {
                MicControlService.stop(context)
            } else {
                MicControlService.start(context, config)
            }
        }
    }

    private companion object {
        const val FEATURE_ID = "mic_control"
    }
}

internal val MIC_OVERLAY_DEFAULTS = OverlayConfig(
    sizeDp = 64,
    startX = 100,
    startY = 200,
)
