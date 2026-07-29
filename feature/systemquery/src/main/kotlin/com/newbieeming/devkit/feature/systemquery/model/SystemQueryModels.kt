package com.newbieeming.devkit.feature.systemquery.model

enum class QueryScope {
    ALL,
    SYSTEM_PROPERTIES,
    SETTINGS_GLOBAL,
    SETTINGS_SYSTEM,
    SETTINGS_SECURE,
    ;

    val sources: List<ValueSource>
        get() = when (this) {
            ALL -> ValueSource.entries
            SYSTEM_PROPERTIES -> listOf(ValueSource.SYSTEM_PROPERTIES)
            SETTINGS_GLOBAL -> listOf(ValueSource.SETTINGS_GLOBAL)
            SETTINGS_SYSTEM -> listOf(ValueSource.SETTINGS_SYSTEM)
            SETTINGS_SECURE -> listOf(ValueSource.SETTINGS_SECURE)
        }
}

enum class ValueSource {
    SYSTEM_PROPERTIES,
    SETTINGS_GLOBAL,
    SETTINGS_SYSTEM,
    SETTINGS_SECURE,
}

data class SystemValue(
    val key: String,
    val value: String,
)

data class QuerySection(
    val source: ValueSource,
    val values: List<SystemValue>,
)

data class QueryResult(
    val sections: List<QuerySection>,
    val unavailableSources: Set<ValueSource>,
)
