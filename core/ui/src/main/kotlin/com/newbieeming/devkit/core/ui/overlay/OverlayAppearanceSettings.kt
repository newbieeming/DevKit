package com.newbieeming.devkit.core.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.R

@Composable
internal fun OverlayAppearanceSettings(
    config: OverlayConfig,
    onConfigChange: (OverlayConfig) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        OverlaySizeSetting(config, onConfigChange)
        OverlayPositionSettings(config, onConfigChange)
        OverlayVisibilitySettings(config, onConfigChange)
        OverlayColorSettings(config, onConfigChange)
    }
}

@Composable
private fun OverlaySizeSetting(config: OverlayConfig, onConfigChange: (OverlayConfig) -> Unit) {
    OverlaySliderSetting(
        title = stringResource(R.string.overlay_size),
        valueLabel = stringResource(R.string.overlay_size_value, config.sizeDp),
        value = config.sizeDp.toFloat(),
        range = MIN_SIZE_DP.toFloat()..MAX_SIZE_DP.toFloat(),
        onValueChange = { value -> onConfigChange(config.copy(sizeDp = value.toInt())) },
    )
}

@Composable
private fun OverlayPositionSettings(config: OverlayConfig, onConfigChange: (OverlayConfig) -> Unit) {
    Text(text = stringResource(R.string.overlay_start_position), style = MaterialTheme.typography.titleMedium)
    OverlaySliderSetting(
        title = stringResource(R.string.overlay_start_x),
        valueLabel = stringResource(R.string.overlay_position_value, config.startX),
        value = config.startX.toFloat(),
        range = POSITION_RANGE,
        onValueChange = { value -> onConfigChange(config.copy(startX = value.toInt())) },
    )
    OverlaySliderSetting(
        title = stringResource(R.string.overlay_start_y),
        valueLabel = stringResource(R.string.overlay_position_value, config.startY),
        value = config.startY.toFloat(),
        range = POSITION_RANGE,
        onValueChange = { value -> onConfigChange(config.copy(startY = value.toInt())) },
    )
}

@Composable
private fun OverlayVisibilitySettings(config: OverlayConfig, onConfigChange: (OverlayConfig) -> Unit) {
    OverlaySwitchSetting(
        title = stringResource(R.string.overlay_show_icon),
        description = stringResource(R.string.overlay_show_icon_description),
        checked = config.showIcon,
        onCheckedChange = { checked -> onConfigChange(config.copy(showIcon = checked)) },
    )
    OverlaySwitchSetting(
        title = stringResource(R.string.overlay_show_background),
        description = stringResource(R.string.overlay_show_background_description),
        checked = config.showBackground,
        onCheckedChange = { checked -> onConfigChange(config.copy(showBackground = checked)) },
    )
}

@Composable
private fun OverlayColorSettings(config: OverlayConfig, onConfigChange: (OverlayConfig) -> Unit) {
    val colors = overlayColorOptions()
    OverlayDropdownSetting(
        title = stringResource(R.string.overlay_icon_color),
        selected = config.iconColor,
        options = colors,
        onSelected = { color -> onConfigChange(config.copy(iconColor = color)) },
        enabled = config.showIcon,
    )
    OverlayDropdownSetting(
        title = stringResource(R.string.overlay_background_color),
        selected = config.backgroundColor,
        options = colors + OverlaySettingOption(
            OverlayColorChoice.TRANSPARENT,
            stringResource(R.string.overlay_color_transparent),
        ),
        onSelected = { color -> onConfigChange(config.copy(backgroundColor = color)) },
        enabled = config.showBackground,
    )
    OverlayDropdownSetting(
        title = stringResource(R.string.overlay_text_color),
        selected = config.textColor,
        options = colors,
        onSelected = { color -> onConfigChange(config.copy(textColor = color)) },
    )
}

@Composable
private fun overlayColorOptions(): List<OverlaySettingOption<OverlayColorChoice>> = listOf(
    OverlaySettingOption(OverlayColorChoice.DYNAMIC, stringResource(R.string.overlay_color_dynamic)),
    OverlaySettingOption(OverlayColorChoice.PRIMARY, stringResource(R.string.overlay_color_primary)),
    OverlaySettingOption(OverlayColorChoice.SECONDARY, stringResource(R.string.overlay_color_secondary)),
    OverlaySettingOption(OverlayColorChoice.TERTIARY, stringResource(R.string.overlay_color_tertiary)),
    OverlaySettingOption(OverlayColorChoice.BLACK, stringResource(R.string.overlay_color_black)),
    OverlaySettingOption(OverlayColorChoice.WHITE, stringResource(R.string.overlay_color_white)),
)

private const val MIN_SIZE_DP = 48
private const val MAX_SIZE_DP = 240
private val POSITION_RANGE = 0f..2000f
