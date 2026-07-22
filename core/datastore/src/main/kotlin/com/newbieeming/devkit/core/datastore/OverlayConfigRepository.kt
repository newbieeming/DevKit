package com.newbieeming.devkit.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.overlayConfigDataStore by preferencesDataStore(name = "overlay_config")

@Singleton
class OverlayConfigRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observe(featureId: String, defaults: OverlayConfig): Flow<OverlayConfig> =
        context.overlayConfigDataStore.data
            .catch { error ->
                if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
            }
            .map { preferences ->
                OverlayConfig(
                    sizeDp = preferences[intKey(featureId, "size_dp")] ?: defaults.sizeDp,
                    startX = preferences[intKey(featureId, "start_x")] ?: defaults.startX,
                    startY = preferences[intKey(featureId, "start_y")] ?: defaults.startY,
                    showIcon = preferences[booleanKey(featureId, "show_icon")] ?: defaults.showIcon,
                    showBackground = preferences[booleanKey(featureId, "show_background")]
                        ?: defaults.showBackground,
                    iconColor = preferences[colorKey(featureId, "icon_color")]
                        .toColorChoice(defaults.iconColor),
                    backgroundColor = preferences[colorKey(featureId, "background_color")]
                        .toColorChoice(defaults.backgroundColor),
                    textColor = preferences[colorKey(featureId, "text_color")]
                        .toColorChoice(defaults.textColor),
                )
            }

    fun observeOptions(
        featureId: String,
        defaults: Map<String, String>,
    ): Flow<Map<String, String>> = context.overlayConfigDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
        }
        .map { preferences ->
            defaults.mapValues { (name, default) ->
                preferences[optionKey(featureId, name)] ?: default
            }
        }

    suspend fun save(featureId: String, config: OverlayConfig) {
        context.overlayConfigDataStore.edit { preferences ->
            preferences[intKey(featureId, "size_dp")] = config.sizeDp
            preferences[intKey(featureId, "start_x")] = config.startX
            preferences[intKey(featureId, "start_y")] = config.startY
            preferences[booleanKey(featureId, "show_icon")] = config.showIcon
            preferences[booleanKey(featureId, "show_background")] = config.showBackground
            preferences[colorKey(featureId, "icon_color")] = config.iconColor.name
            preferences[colorKey(featureId, "background_color")] = config.backgroundColor.name
            preferences[colorKey(featureId, "text_color")] = config.textColor.name
        }
    }

    suspend fun saveOptions(featureId: String, options: Map<String, String>) {
        context.overlayConfigDataStore.edit { preferences ->
            options.forEach { (name, value) ->
                preferences[optionKey(featureId, name)] = value
            }
        }
    }

    private fun intKey(featureId: String, name: String) = intPreferencesKey("${featureId}_$name")

    private fun booleanKey(featureId: String, name: String) = booleanPreferencesKey("${featureId}_$name")

    private fun colorKey(featureId: String, name: String) = stringPreferencesKey("${featureId}_$name")

    private fun optionKey(featureId: String, name: String) =
        stringPreferencesKey("${featureId}_option_$name")

    private fun String?.toColorChoice(default: OverlayColorChoice): OverlayColorChoice =
        this?.let { value -> OverlayColorChoice.entries.firstOrNull { it.name == value } } ?: default
}
