package com.newbieeming.devkit.feature.miccontrol

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
import com.newbieeming.devkit.core.designsystem.theme.DevKitColors

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
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
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