package com.newbieeming.devkit.feature.timesync

enum class TimeFormatOption(val pattern: String?) {
    SYSTEM(null),
    HOUR_24_WITH_SECONDS("HH:mm:ss"),
    HOUR_24("HH:mm"),
    HOUR_12_WITH_SECONDS("hh:mm:ss a"),
    HOUR_12("hh:mm a"),
    ;

    fun resolvePattern(systemUses24Hour: Boolean): String = pattern ?: if (systemUses24Hour) {
        "HH:mm:ss"
    } else {
        "hh:mm:ss a"
    }
}

data class TimeOverlayOptions(
    val format: TimeFormatOption = TimeFormatOption.SYSTEM,
) {
    fun toPreferences(): Map<String, String> = mapOf(FORMAT_KEY to format.name)

    companion object {
        const val FORMAT_KEY = "time_format"
        val preferenceDefaults = TimeOverlayOptions().toPreferences()

        fun fromPreferences(values: Map<String, String>): TimeOverlayOptions =
            TimeOverlayOptions(format = values[FORMAT_KEY].toFormat())

        fun fromIntentValue(value: String?): TimeOverlayOptions =
            TimeOverlayOptions(format = value.toFormat())

        private fun String?.toFormat(): TimeFormatOption =
            TimeFormatOption.entries.firstOrNull { it.name == this } ?: TimeFormatOption.SYSTEM
    }
}
