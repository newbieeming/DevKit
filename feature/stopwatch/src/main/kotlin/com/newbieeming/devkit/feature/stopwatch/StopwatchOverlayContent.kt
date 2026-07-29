package com.newbieeming.devkit.feature.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.resolveOverlayColors

@Composable
fun StopwatchOverlayContent(
    state: StopwatchState,
    config: OverlayConfig,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = resolveOverlayColors(config)
    val scale = (config.sizeDp / 200f).coerceIn(0.5f, 1.5f)
    val drawsBackground = config.showBackground &&
        config.backgroundColor != OverlayColorChoice.TRANSPARENT
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape((16f * scale).coerceAtLeast(8f).dp),
        color = colors.background,
        tonalElevation = if (drawsBackground) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(if (drawsBackground) (8f * scale).dp else 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (config.showIcon) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = stringResource(R.string.stopwatch),
                    modifier = Modifier
                        .padding(start = (4f * scale).dp)
                        .size((28f * scale).coerceIn(18f, 42f).dp),
                    tint = colors.icon,
                )
            }
            Text(
                text = state.elapsedMillis.toStopwatchText(),
                modifier = Modifier.padding(horizontal = (8f * scale).dp),
                color = colors.text,
                fontFamily = FontFamily.Monospace,
                fontSize = (22f * scale).coerceIn(13f, 34f).sp,
                maxLines = 1,
            )
            IconButton(
                onClick = if (state.status == StopwatchStatus.RUNNING) onPause else onStartOrResume,
                modifier = Modifier.size((40f * scale).coerceIn(32f, 52f).dp),
            ) {
                Icon(
                    imageVector = if (state.status == StopwatchStatus.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = stringResource(
                        if (state.status == StopwatchStatus.RUNNING) {
                            R.string.pause_stopwatch
                        } else {
                            R.string.start_stopwatch
                        },
                    ),
                    tint = colors.icon,
                )
            }
            IconButton(
                onClick = onReset,
                enabled = state.elapsedMillis > 0L || state.status != StopwatchStatus.RESET,
                modifier = Modifier.size((40f * scale).coerceIn(32f, 52f).dp),
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = stringResource(R.string.reset_stopwatch),
                    tint = colors.icon,
                )
            }
        }
    }
}
