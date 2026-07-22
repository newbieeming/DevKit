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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.newbieeming.devkit.core.designsystem.theme.DevKitColors
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.resolveOverlayColors

@Composable
fun OverlayContent(
    isMuted: Boolean,
    config: OverlayConfig,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = resolveOverlayColors(config)
    val scale = (config.sizeDp / 64f).coerceIn(0.75f, 2.5f)
    val iconSize = (42f * scale).coerceIn(24f, 112f)
    val indicatorSize = (iconSize * 0.25f).coerceIn(7f, 24f)
    val drawsBackground = config.showBackground && config.backgroundColor != OverlayColorChoice.TRANSPARENT
    val backgroundShape = RoundedCornerShape((16f * scale).coerceIn(10f, 32f).dp)
    val backgroundClip = if (drawsBackground) Modifier.clip(backgroundShape) else Modifier
    Box(
        modifier = modifier
            .then(backgroundClip)
            .background(color = colors.background, shape = backgroundShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(if (drawsBackground) (10f * scale).dp else 0.dp)
                .size(if (config.showIcon) iconSize.dp else (16f * scale).dp),
            contentAlignment = Alignment.Center,
        ) {
            if (config.showIcon) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = stringResource(
                        if (isMuted) R.string.mic_muted else R.string.mic_active,
                    ),
                    tint = colors.icon,
                    modifier = Modifier.size(iconSize.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 1.dp, bottom = 1.dp)
                        .size(indicatorSize.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.Gray else DevKitColors.Green400),
                )
            }
        }
    }
}
