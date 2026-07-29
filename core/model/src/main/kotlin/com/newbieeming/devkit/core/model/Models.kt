package com.newbieeming.devkit.core.model

/**
 * 实时网速快照
 */
data class NetworkSpeedSnapshot(
    val rxBytesPerSec: Long,   // 下行字节/秒
    val txBytesPerSec: Long,   // 上行字节/秒
    val timestampMs: Long,
)

/** 悬浮窗颜色语义。DYNAMIC 会根据颜色用途映射到当前 Material 动态主题。 */
enum class OverlayColorChoice {
    DYNAMIC,
    PRIMARY,
    SECONDARY,
    TERTIARY,
    BLACK,
    WHITE,
    TRANSPARENT,
}

/** 通用悬浮窗显示配置。位置单位为 px，尺寸单位为 dp。 */
data class OverlayConfig(
    val sizeDp: Int,
    val startX: Int,
    val startY: Int,
    val showIcon: Boolean = true,
    val showBackground: Boolean = true,
    val iconColor: OverlayColorChoice = OverlayColorChoice.DYNAMIC,
    val backgroundColor: OverlayColorChoice = OverlayColorChoice.DYNAMIC,
    val textColor: OverlayColorChoice = OverlayColorChoice.DYNAMIC,
)
