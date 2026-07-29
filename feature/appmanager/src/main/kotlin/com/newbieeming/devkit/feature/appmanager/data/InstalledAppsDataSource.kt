package com.newbieeming.devkit.feature.appmanager.data

import android.content.Context
import android.graphics.Bitmap
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import com.newbieeming.devkit.core.common.di.IoDispatcher
import com.newbieeming.devkit.feature.appmanager.model.AppInstallSource
import com.newbieeming.devkit.feature.appmanager.model.FactoryAppInfo
import com.newbieeming.devkit.feature.appmanager.model.InstalledAppDetail
import com.newbieeming.devkit.feature.appmanager.model.InstalledAppSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val iconCache = LruCache<String, Bitmap>(ICON_CACHE_ENTRY_COUNT)
    private val iconSizePx = (context.resources.displayMetrics.density * ICON_SIZE_DP)
        .toInt()
        .coerceAtLeast(1)

    private val packageManager: PackageManager
        get() = context.packageManager

    suspend fun loadInstalledApps(): List<InstalledAppSummary> = withContext(ioDispatcher) {
        val collator = Collator.getInstance(Locale.getDefault())
        installedPackages()
            .mapNotNull(::toSummary)
            .sortedWith { first, second ->
                val labelComparison = collator.compare(first.label, second.label)
                if (labelComparison != 0) {
                    labelComparison
                } else {
                    first.packageName.compareTo(second.packageName)
                }
            }
    }

    suspend fun loadAppDetail(packageName: String): InstalledAppDetail =
        withContext(ioDispatcher) {
            val packageInfo = getPackageInfo(packageName, PackageManager.GET_META_DATA)
            val applicationInfo = requireNotNull(packageInfo.applicationInfo)
            val apkPaths = applicationInfo.apkPaths()
            val versionCode = packageInfo.longVersionCodeCompat()
            InstalledAppDetail(
                packageName = packageInfo.packageName,
                label = applicationInfo.loadLabel(packageManager).toString()
                    .ifBlank { packageInfo.packageName },
                versionName = packageInfo.versionName.orEmpty(),
                versionCode = versionCode,
                isSystemApp = applicationInfo.hasFlag(ApplicationInfo.FLAG_SYSTEM),
                isUpdatedSystemApp =
                    applicationInfo.hasFlag(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP),
                firstInstallTime = packageInfo.firstInstallTime,
                lastUpdateTime = packageInfo.lastUpdateTime,
                apkSizeBytes = apkPaths.sumOf { path ->
                    runCatching { File(path).length() }.getOrDefault(0L)
                },
                apkPaths = apkPaths,
                installSource = readInstallSource(packageName),
                metaData = applicationInfo.metaData.toDisplayMap(packageName),
                factoryInfo = if (
                    applicationInfo.hasFlag(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
                ) {
                    loadFactoryInfo(packageName, apkPaths)
                } else {
                    null
                },
                icon = loadIcon(applicationInfo, versionCode),
            )
        }

    private fun installedPackages(): List<PackageInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }

    private fun getPackageInfo(packageName: String, flags: Int): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, flags)
        }

    private fun toSummary(packageInfo: PackageInfo): InstalledAppSummary? {
        val applicationInfo = packageInfo.applicationInfo ?: return null
        return runCatching {
            InstalledAppSummary(
                packageName = packageInfo.packageName,
                label = applicationInfo.loadLabel(packageManager).toString()
                    .ifBlank { packageInfo.packageName },
                versionName = packageInfo.versionName.orEmpty(),
                versionCode = packageInfo.longVersionCodeCompat(),
                isSystemApp = applicationInfo.hasFlag(ApplicationInfo.FLAG_SYSTEM),
                isUpdatedSystemApp =
                    applicationInfo.hasFlag(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP),
                icon = loadIcon(applicationInfo, packageInfo.longVersionCodeCompat()),
            )
        }.getOrNull()
    }

    private fun loadFactoryInfo(
        packageName: String,
        currentApkPaths: List<String>,
    ): FactoryAppInfo? {
        val factoryPackage = runCatching {
            getPackageInfo(packageName, MATCH_FACTORY_ONLY_COMPAT)
        }.getOrNull() ?: return null
        val factoryPaths = factoryPackage.applicationInfo
            ?.apkPaths()
            .orEmpty()
            .filterNot(currentApkPaths::contains)
        if (factoryPaths.isEmpty()) return null
        return FactoryAppInfo(
            versionName = factoryPackage.versionName.orEmpty(),
            versionCode = factoryPackage.longVersionCodeCompat(),
            apkSizeBytes = factoryPaths.totalFileSize(),
            apkPaths = factoryPaths,
        )
    }

    private fun loadIcon(applicationInfo: ApplicationInfo, versionCode: Long): Bitmap? {
        val cacheKey = "${applicationInfo.packageName}:$versionCode"
        synchronized(iconCache) {
            iconCache.get(cacheKey)?.let { return it }
        }
        val bitmap = runCatching {
            applicationInfo.loadIcon(packageManager).toBitmap(
                width = iconSizePx,
                height = iconSizePx,
                config = Bitmap.Config.ARGB_8888,
            )
        }.getOrNull() ?: return null
        synchronized(iconCache) {
            iconCache.put(cacheKey, bitmap)
        }
        return bitmap
    }

    @Suppress("DEPRECATION")
    private fun readInstallSource(packageName: String): AppInstallSource {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return AppInstallSource(
                installingPackageName = packageManager.getInstallerPackageName(packageName),
                initiatingPackageName = null,
                originatingPackageName = null,
                packageSource = null,
            )
        }
        return runCatching {
            val sourceInfo = packageManager.getInstallSourceInfo(packageName)
            AppInstallSource(
                installingPackageName = sourceInfo.installingPackageName,
                initiatingPackageName = sourceInfo.initiatingPackageName,
                originatingPackageName = sourceInfo.originatingPackageName,
                packageSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sourceInfo.packageSource
                } else {
                    null
                },
            )
        }.getOrElse {
            AppInstallSource(null, null, null, null)
        }
    }

    @Suppress("DEPRECATION")
    private fun Bundle?.toDisplayMap(packageName: String): Map<String, String> {
        if (this == null || isEmpty) return emptyMap()
        return keySet().sorted().associateWith { key ->
            valueToText(get(key), packageName)
        }
    }

    private fun valueToText(value: Any?, packageName: String): String = when (value) {
        null -> "null"
        is Int -> resourceTextOrNumber(value, packageName)
        is Array<*> -> value.joinToString()
        is IntArray -> value.joinToString()
        is LongArray -> value.joinToString()
        is BooleanArray -> value.joinToString()
        is FloatArray -> value.joinToString()
        is DoubleArray -> value.joinToString()
        else -> value.toString()
    }

    private fun resourceTextOrNumber(value: Int, packageName: String): String =
        runCatching {
            packageManager.getResourcesForApplication(packageName).getText(value).toString()
        }.getOrElse {
            value.toString()
        }
}

private fun ApplicationInfo.hasFlag(flag: Int): Boolean = flags and flag != 0

private fun ApplicationInfo.apkPaths(): List<String> = buildList {
    sourceDir?.let(::add)
    splitSourceDirs?.let(::addAll)
}.distinct()

private fun List<String>.totalFileSize(): Long = sumOf { path ->
    runCatching { File(path).length() }.getOrDefault(0L)
}

@Suppress("DEPRECATION")
private fun PackageInfo.longVersionCodeCompat(): Long =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()

// MATCH_FACTORY_ONLY is a stable platform flag since API 24, but is hidden from the public SDK.
private const val MATCH_FACTORY_ONLY_COMPAT = 0x00200000
private const val ICON_CACHE_ENTRY_COUNT = 512
private const val ICON_SIZE_DP = 48
