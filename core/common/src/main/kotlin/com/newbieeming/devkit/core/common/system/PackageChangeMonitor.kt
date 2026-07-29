package com.newbieeming.devkit.core.common.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class PackageChangeType {
    INSTALLED,
    UNINSTALLED,
    UPDATED,
}

data class PackageChangeEvent(
    val packageName: String,
    val type: PackageChangeType,
)

/**
 * Exposes installed-package changes as a cold Flow.
 *
 * Android emits REMOVED and ADDED with EXTRA_REPLACING around an app update, followed by
 * REPLACED. The intermediate broadcasts are ignored so consumers receive one UPDATED event.
 */
@Singleton
class PackageChangeMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun observePackageChanges(): Flow<PackageChangeEvent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val packageName = intent?.data?.schemeSpecificPart ?: return
                val event = packageChangeEvent(
                    action = intent.action,
                    packageName = packageName,
                    isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false),
                ) ?: return
                trySend(event)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme(PACKAGE_URI_SCHEME)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }.buffer()
}

internal fun packageChangeEvent(
    action: String?,
    packageName: String,
    isReplacing: Boolean,
): PackageChangeEvent? {
    if (packageName.isBlank()) return null
    val type = when (action) {
        Intent.ACTION_PACKAGE_ADDED ->
            if (isReplacing) return null else PackageChangeType.INSTALLED
        Intent.ACTION_PACKAGE_REMOVED ->
            if (isReplacing) return null else PackageChangeType.UNINSTALLED
        Intent.ACTION_PACKAGE_REPLACED -> PackageChangeType.UPDATED
        else -> return null
    }
    return PackageChangeEvent(packageName = packageName, type = type)
}

private const val PACKAGE_URI_SCHEME = "package"
