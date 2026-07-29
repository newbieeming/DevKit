package com.newbieeming.devkit.feature.systemquery.data

import android.content.Context
import android.net.Uri
import android.provider.Settings
import com.newbieeming.devkit.core.common.di.IoDispatcher
import com.newbieeming.devkit.feature.systemquery.model.QueryResult
import com.newbieeming.devkit.feature.systemquery.model.QueryScope
import com.newbieeming.devkit.feature.systemquery.model.QuerySection
import com.newbieeming.devkit.feature.systemquery.model.SystemValue
import com.newbieeming.devkit.feature.systemquery.model.ValueSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.regex.PatternSyntaxException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class SystemValueDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    @Throws(PatternSyntaxException::class)
    suspend fun query(pattern: String, scope: QueryScope): QueryResult =
        withContext(ioDispatcher) {
            val regex = Regex(pattern)
            val sections = mutableListOf<QuerySection>()
            val unavailableSources = mutableSetOf<ValueSource>()

            scope.sources.forEach { source ->
                try {
                    val values = when (source) {
                        ValueSource.SYSTEM_PROPERTIES -> readSystemProperties()
                        ValueSource.SETTINGS_GLOBAL -> readSettings(Settings.Global.CONTENT_URI)
                        ValueSource.SETTINGS_SYSTEM -> readSettings(Settings.System.CONTENT_URI)
                        ValueSource.SETTINGS_SECURE -> readSettings(Settings.Secure.CONTENT_URI)
                    }.filterByKey(regex)
                    if (values.isNotEmpty()) {
                        sections += QuerySection(source = source, values = values)
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    unavailableSources += source
                }
            }

            QueryResult(
                sections = sections,
                unavailableSources = unavailableSources,
            )
        }

    private fun readSystemProperties(): List<SystemValue> {
        val process = try {
            ProcessBuilder(GETPROP_COMMAND)
                .redirectErrorStream(true)
                .start()
        } catch (error: IOException) {
            throw SystemQueryException(error)
        }

        return try {
            val values = process.inputStream.bufferedReader().useLines { lines ->
                lines.mapNotNull(::parseGetpropLine).toList()
            }
            if (process.waitFor() != 0) {
                throw SystemQueryException()
            }
            values
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw SystemQueryException(error)
        } catch (error: IOException) {
            throw SystemQueryException(error)
        } finally {
            process.destroy()
        }
    }

    private fun readSettings(uri: Uri): List<SystemValue> {
        val values = mutableListOf<SystemValue>()
        context.contentResolver.query(
            uri,
            SETTINGS_PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(SETTINGS_NAME_COLUMN)
            val valueIndex = cursor.getColumnIndexOrThrow(SETTINGS_VALUE_COLUMN)
            while (cursor.moveToNext()) {
                val key = cursor.getString(nameIndex) ?: continue
                values += SystemValue(
                    key = key,
                    value = cursor.getString(valueIndex).orEmpty(),
                )
            }
        } ?: throw SystemQueryException()
        return values
    }

    private companion object {
        const val GETPROP_COMMAND = "getprop"
        const val SETTINGS_NAME_COLUMN = "name"
        const val SETTINGS_VALUE_COLUMN = "value"
        val SETTINGS_PROJECTION = arrayOf(SETTINGS_NAME_COLUMN, SETTINGS_VALUE_COLUMN)
    }
}

internal fun parseGetpropLine(line: String): SystemValue? {
    val match = GETPROP_LINE_PATTERN.matchEntire(line) ?: return null
    return SystemValue(
        key = match.groupValues[1],
        value = match.groupValues[2],
    )
}

internal fun List<SystemValue>.filterByKey(regex: Regex): List<SystemValue> =
    asSequence()
        .filter { value -> regex.containsMatchIn(value.key) }
        .sortedBy(SystemValue::key)
        .toList()

private val GETPROP_LINE_PATTERN = Regex("""^\[([^]]+)]\s*:\s*\[(.*)]$""")

private class SystemQueryException(cause: Throwable? = null) : Exception(cause)
