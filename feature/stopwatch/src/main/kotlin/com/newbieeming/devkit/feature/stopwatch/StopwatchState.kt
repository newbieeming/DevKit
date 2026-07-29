package com.newbieeming.devkit.feature.stopwatch

enum class StopwatchStatus {
    RUNNING,
    PAUSED,
    RESET,
}

data class StopwatchState(
    val elapsedMillis: Long = 0L,
    val status: StopwatchStatus = StopwatchStatus.RESET,
)

internal fun Long.toStopwatchText(): String {
    val totalHundredths = coerceAtLeast(0L) / 10L
    val hundredths = totalHundredths % 100L
    val totalSeconds = totalHundredths / 100L
    val seconds = totalSeconds % 60L
    val totalMinutes = totalSeconds / 60L
    val minutes = totalMinutes % 60L
    val hours = totalMinutes / 60L
    return if (hours > 0L) {
        "%d:%02d:%02d:%02d".format(hours, minutes, seconds, hundredths)
    } else {
        "%02d:%02d:%02d".format(minutes, seconds, hundredths)
    }
}
