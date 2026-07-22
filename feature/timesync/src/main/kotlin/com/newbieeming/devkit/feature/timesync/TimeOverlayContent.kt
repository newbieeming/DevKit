package com.newbieeming.devkit.feature.timesync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.resolveOverlayColors

@Composable
fun TimeOverlayContent(
    time: String,
    config: OverlayConfig,
    modifier: Modifier = Modifier,
) {
    val colors = resolveOverlayColors(config)
    val scale = (config.sizeDp / 180f).coerceIn(0.5f, 1.8f)
    val drawsBackground = config.showBackground && config.backgroundColor != OverlayColorChoice.TRANSPARENT
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape((16f * scale).coerceAtLeast(8f).dp),
        color = colors.background,
        tonalElevation = if (drawsBackground) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(if (drawsBackground) (12f * scale).dp else 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (config.showIcon) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = stringResource(R.string.current_time),
                    modifier = Modifier
                        .padding(end = (8f * scale).dp)
                        .size((32f * scale).coerceIn(18f, 52f).dp),
                    tint = colors.icon,
                )
            }
            Text(
                text = time,
                fontSize = (23f * scale).coerceIn(12f, 40f).sp,
                maxLines = 1,
                color = colors.text,
            )
        }
    }
}
