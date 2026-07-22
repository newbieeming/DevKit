package com.newbieeming.devkit.feature.networkspeed

enum class NetworkDisplayMode {
    DOWNLOAD,
    UPLOAD,
    BOTH,
}

enum class NetworkIndicatorStyle {
    ARROW,
    DOT,
    NONE,
}

data class NetworkOverlayOptions(
    val displayMode: NetworkDisplayMode = NetworkDisplayMode.BOTH,
    val indicatorStyle: NetworkIndicatorStyle = NetworkIndicatorStyle.ARROW,
) {
    fun toPreferences(): Map<String, String> = mapOf(
        DISPLAY_MODE_KEY to displayMode.name,
        INDICATOR_STYLE_KEY to indicatorStyle.name,
    )

    companion object {
        const val DISPLAY_MODE_KEY = "display_mode"
        const val INDICATOR_STYLE_KEY = "indicator_style"

        val preferenceDefaults = NetworkOverlayOptions().toPreferences()

        fun fromPreferences(values: Map<String, String>): NetworkOverlayOptions = NetworkOverlayOptions(
            displayMode = values[DISPLAY_MODE_KEY].toEnumOr(NetworkDisplayMode.BOTH),
            indicatorStyle = values[INDICATOR_STYLE_KEY].toEnumOr(NetworkIndicatorStyle.ARROW),
        )

        fun fromIntentValue(displayMode: String?, indicatorStyle: String?): NetworkOverlayOptions =
            NetworkOverlayOptions(
                displayMode = displayMode.toEnumOr(NetworkDisplayMode.BOTH),
                indicatorStyle = indicatorStyle.toEnumOr(NetworkIndicatorStyle.ARROW),
            )

        private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
            enumValues<T>().firstOrNull { it.name == this } ?: default
    }
}
