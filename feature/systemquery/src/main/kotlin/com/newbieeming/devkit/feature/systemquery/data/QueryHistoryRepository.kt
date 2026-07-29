package com.newbieeming.devkit.feature.systemquery.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.systemQueryDataStore by preferencesDataStore(name = "system_query_history")

@Singleton
class QueryHistoryRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val history: Flow<List<String>> = context.systemQueryDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            preferences[HISTORY_KEY]
                .orEmpty()
                .lineSequence()
                .filter(String::isNotBlank)
                .map(Uri::decode)
                .toList()
        }

    suspend fun remember(query: String) {
        context.systemQueryDataStore.edit { preferences ->
            val previous = preferences[HISTORY_KEY]
                .orEmpty()
                .lineSequence()
                .filter(String::isNotBlank)
                .map(Uri::decode)
                .toList()
            val updated = buildList {
                add(query)
                addAll(previous.filterNot { it == query })
            }.take(MAX_HISTORY_COUNT)
            preferences[HISTORY_KEY] = updated.joinToString("\n", transform = Uri::encode)
        }
    }

    suspend fun clear() {
        context.systemQueryDataStore.edit { preferences ->
            preferences.remove(HISTORY_KEY)
        }
    }

    private companion object {
        val HISTORY_KEY = stringPreferencesKey("queries")
        const val MAX_HISTORY_COUNT = 20
    }
}
