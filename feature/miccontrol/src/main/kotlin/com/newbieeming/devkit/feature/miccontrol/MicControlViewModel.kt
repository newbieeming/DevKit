package com.newbieeming.devkit.feature.miccontrol

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import com.newbieeming.devkit.core.designsystem.theme.DevKitColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MicControlViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // Expose the global service running state from the Service companion
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

@Composable
fun OverlayContent(
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (isMuted) "Mic Muted" else "Mic Active",
            tint = if (isMuted) Color.Gray else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )

        // Status indicator dot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 6.dp, end = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isMuted) Color.Gray else DevKitColors.Green400)
        )
    }
}
