package com.newbieeming.devkit.feature.networkspeed

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
class NetworkSpeedViewModel @Inject constructor(
    application: Application,
    private val configRepository: OverlayConfigRepository,
) : AndroidViewModel(application) {
    val config = configRepository.observe(FEATURE_ID, NETWORK_OVERLAY_DEFAULTS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NETWORK_OVERLAY_DEFAULTS)
    val options = configRepository.observeOptions(FEATURE_ID, NetworkOverlayOptions.preferenceDefaults)
        .map(NetworkOverlayOptions::fromPreferences)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkOverlayOptions())
    val isServiceRunning = NetworkSpeedService.isServiceRunning

    fun saveConfig(config: OverlayConfig, options: NetworkOverlayOptions) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            configRepository.saveOptions(FEATURE_ID, options.toPreferences())
            if (isServiceRunning.value) NetworkSpeedService.update(getApplication(), config, options)
        }
    }

    fun toggleOverlay(config: OverlayConfig, options: NetworkOverlayOptions) {
        viewModelScope.launch {
            configRepository.save(FEATURE_ID, config)
            configRepository.saveOptions(FEATURE_ID, options.toPreferences())
            val context = getApplication<Application>()
            if (isServiceRunning.value) {
                NetworkSpeedService.stop(context)
            } else {
                NetworkSpeedService.start(context, config, options)
            }
        }
    }

    private companion object {
        const val FEATURE_ID = "network_speed"
    }
}

internal val NETWORK_OVERLAY_DEFAULTS = OverlayConfig(
    sizeDp = 180,
    startX = 100,
    startY = 320,
)
