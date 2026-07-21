package com.newbieeming.devkit.feature.miccontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MicControlViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    val isServiceRunningFlow = MicControlService.isServiceRunning

    fun toggleOverlay() {
        val context = getApplication<Application>()
        if (MicControlService.isServiceRunning.value) {
            MicControlService.stop(context)
        } else {
            MicControlService.start(context)
        }
    }
}
