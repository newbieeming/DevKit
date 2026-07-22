package com.newbieeming.devkit.feature.networkspeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newbieeming.devkit.core.model.NetworkSpeedSnapshot
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.resolveOverlayColors
import java.util.Locale

@Composable
fun NetworkSpeedOverlayContent(
    snapshot: NetworkSpeedSnapshot,
    config: OverlayConfig,
    options: NetworkOverlayOptions,
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
            horizontalArrangement = Arrangement.spacedBy((8f * scale).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (config.showIcon) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = stringResource(R.string.network_speed_title),
                    modifier = Modifier.size((32f * scale).coerceIn(18f, 52f).dp),
                    tint = colors.icon,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                options.displayMode.rows(snapshot).forEach { row ->
                    SpeedRow(
                        row = row,
                        indicatorStyle = options.indicatorStyle,
                        fontSizeSp = (15f * scale).coerceIn(10f, 25f),
                        iconColor = colors.icon,
                        textColor = colors.text,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedRow(
    row: SpeedRowData,
    indicatorStyle: NetworkIndicatorStyle,
    fontSizeSp: Float,
    iconColor: Color,
    textColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeedIndicator(style = indicatorStyle, direction = row.direction, color = iconColor)
        Text(row.formattedSpeed, fontSize = fontSizeSp.sp, maxLines = 1, color = textColor)
    }
}

@Composable
private fun SpeedIndicator(
    style: NetworkIndicatorStyle,
    direction: SpeedDirection,
    color: Color,
) {
    when (style) {
        NetworkIndicatorStyle.ARROW -> Icon(
            imageVector = direction.icon,
            contentDescription = stringResource(direction.contentDescriptionRes),
            modifier = Modifier.size(18.dp),
            tint = color,
        )
        NetworkIndicatorStyle.DOT -> Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        NetworkIndicatorStyle.NONE -> Unit
    }
}

private data class SpeedRowData(
    val direction: SpeedDirection,
    val formattedSpeed: String,
)

private enum class SpeedDirection(
    val icon: ImageVector,
    val contentDescriptionRes: Int,
) {
    DOWNLOAD(Icons.Default.ArrowDownward, R.string.download_speed),
    UPLOAD(Icons.Default.ArrowUpward, R.string.upload_speed),
}

private fun NetworkDisplayMode.rows(snapshot: NetworkSpeedSnapshot): List<SpeedRowData> {
    val download = SpeedRowData(SpeedDirection.DOWNLOAD, formatSpeed(snapshot.rxBytesPerSec))
    val upload = SpeedRowData(SpeedDirection.UPLOAD, formatSpeed(snapshot.txBytesPerSec))
    return when (this) {
        NetworkDisplayMode.DOWNLOAD -> listOf(download)
        NetworkDisplayMode.UPLOAD -> listOf(upload)
        NetworkDisplayMode.BOTH -> listOf(download, upload)
    }
}

private fun formatSpeed(bytesPerSecond: Long): String = when {
    bytesPerSecond < 1_024L -> "$bytesPerSecond B/s"
    bytesPerSecond < 1_048_576L -> String.format(Locale.getDefault(), "%.1f KB/s", bytesPerSecond / 1_024.0)
    else -> String.format(Locale.getDefault(), "%.1f MB/s", bytesPerSecond / 1_048_576.0)
}
