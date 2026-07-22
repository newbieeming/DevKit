package com.newbieeming.devkit.core.ui.overlay

import android.content.Intent
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig

fun Intent.putOverlayConfig(config: OverlayConfig): Intent = apply {
    putExtra(EXTRA_SIZE_DP, config.sizeDp)
    putExtra(EXTRA_START_X, config.startX)
    putExtra(EXTRA_START_Y, config.startY)
    putExtra(EXTRA_SHOW_ICON, config.showIcon)
    putExtra(EXTRA_SHOW_BACKGROUND, config.showBackground)
    putExtra(EXTRA_ICON_COLOR, config.iconColor.name)
    putExtra(EXTRA_BACKGROUND_COLOR, config.backgroundColor.name)
    putExtra(EXTRA_TEXT_COLOR, config.textColor.name)
}

internal fun Intent?.overlayConfigOr(defaults: OverlayConfig): OverlayConfig = OverlayConfig(
    sizeDp = this?.getIntExtra(EXTRA_SIZE_DP, defaults.sizeDp) ?: defaults.sizeDp,
    startX = this?.getIntExtra(EXTRA_START_X, defaults.startX) ?: defaults.startX,
    startY = this?.getIntExtra(EXTRA_START_Y, defaults.startY) ?: defaults.startY,
    showIcon = this?.getBooleanExtra(EXTRA_SHOW_ICON, defaults.showIcon) ?: defaults.showIcon,
    showBackground = this?.getBooleanExtra(EXTRA_SHOW_BACKGROUND, defaults.showBackground)
        ?: defaults.showBackground,
    iconColor = this.colorChoiceOr(EXTRA_ICON_COLOR, defaults.iconColor),
    backgroundColor = this.colorChoiceOr(EXTRA_BACKGROUND_COLOR, defaults.backgroundColor),
    textColor = this.colorChoiceOr(EXTRA_TEXT_COLOR, defaults.textColor),
)

private fun Intent?.colorChoiceOr(
    key: String,
    default: OverlayColorChoice,
): OverlayColorChoice {
    val value = this?.getStringExtra(key) ?: return default
    return OverlayColorChoice.entries.firstOrNull { it.name == value } ?: default
}

private const val EXTRA_SIZE_DP = "devkit.overlay.SIZE_DP"
private const val EXTRA_START_X = "devkit.overlay.START_X"
private const val EXTRA_START_Y = "devkit.overlay.START_Y"
private const val EXTRA_SHOW_ICON = "devkit.overlay.SHOW_ICON"
private const val EXTRA_SHOW_BACKGROUND = "devkit.overlay.SHOW_BACKGROUND"
private const val EXTRA_ICON_COLOR = "devkit.overlay.ICON_COLOR"
private const val EXTRA_BACKGROUND_COLOR = "devkit.overlay.BACKGROUND_COLOR"
private const val EXTRA_TEXT_COLOR = "devkit.overlay.TEXT_COLOR"
