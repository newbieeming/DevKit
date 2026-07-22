package com.newbieeming.devkit.core.model

/**
 * 麦克风状态
 */
enum class MicState { ENABLED, DISABLED, UNKNOWN }

/**
 * 录音文件元数据
 */
data class RecordingFile(
    val id: Long = 0,
    val name: String,
    val path: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Long,
)

/**
 * 已安装应用信息
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
)

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

/**
 * 设备 / 车机基础信息
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val buildNumber: String,
    val boardPlatform: String,
    val cpuAbi: String,
    val totalRamMb: Long,
    val customProperties: Map<String, String> = emptyMap(), // 车机厂商扩展属性
)

/**
 * NTP / 时间同步配置
 */
data class TimeServerConfig(
    val host: String = "pool.ntp.org",
    val port: Int = 123,
    val syncIntervalMs: Long = 60_000L,
)

/**
 * 波形采样点（归一化 0f–1f）
 */
@JvmInline
value class WaveformSample(val amplitude: Float)
