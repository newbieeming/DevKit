package com.newbieeming.devkit.core.common.system

import com.newbieeming.devkit.core.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads Android system properties through the platform `getprop` executable.
 *
 * A property name is passed to [ProcessBuilder] as a separate argument, never through a shell.
 * Property values may be device identifiers, so failures and returned values are intentionally
 * not logged.
 */
@Singleton
class SystemPropertyReader @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun get(propertyName: String): String? = withContext(ioDispatcher) {
        getBlocking(propertyName)
    }

    suspend fun getFirst(propertyNames: Iterable<String>): String? = withContext(ioDispatcher) {
        propertyNames.firstNotNullOfOrNull(::getBlocking)
    }

    private fun getBlocking(propertyName: String): String? {
        require(PROPERTY_NAME_PATTERN.matches(propertyName)) {
            "Invalid Android system property name"
        }

        val process = try {
            ProcessBuilder(GETPROP_COMMAND, propertyName)
                .redirectErrorStream(true)
                .start()
        } catch (_: IOException) {
            return null
        }

        return try {
            if (!waitForProcess(process)) {
                null
            } else if (process.exitValue() != 0) {
                null
            } else {
                process.inputStream.bufferedReader().use { reader ->
                    reader.readLine()?.trim()?.takeUnless(String::isBlank)
                }
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        } catch (_: IOException) {
            null
        } finally {
            process.destroy()
        }
    }

    private fun waitForProcess(process: Process): Boolean {
        val deadlineNanos = System.nanoTime() + PROCESS_TIMEOUT_MILLIS * NANOS_PER_MILLISECOND
        while (System.nanoTime() < deadlineNanos) {
            try {
                process.exitValue()
                return true
            } catch (_: IllegalThreadStateException) {
                Thread.sleep(PROCESS_POLL_INTERVAL_MILLIS)
            }
        }
        return false
    }

    private companion object {
        const val GETPROP_COMMAND = "getprop"
        const val PROCESS_TIMEOUT_MILLIS = 1_500L
        const val PROCESS_POLL_INTERVAL_MILLIS = 10L
        const val NANOS_PER_MILLISECOND = 1_000_000L
        val PROPERTY_NAME_PATTERN = Regex("[A-Za-z0-9_.-]{1,96}")
    }
}
