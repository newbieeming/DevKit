package com.newbieeming.devkit.feature.appmanager.model

import android.graphics.Bitmap

enum class AppFilter {
    ALL,
    SYSTEM,
    THIRD_PARTY,
}

data class InstalledAppSummary(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isUpdatedSystemApp: Boolean,
    val icon: Bitmap?,
) {
    val canRequestUninstall: Boolean
        get() = !isSystemApp || isUpdatedSystemApp
}

data class AppInstallSource(
    val installingPackageName: String?,
    val initiatingPackageName: String?,
    val originatingPackageName: String?,
    val packageSource: Int?,
)

data class FactoryAppInfo(
    val versionName: String,
    val versionCode: Long,
    val apkSizeBytes: Long,
    val apkPaths: List<String>,
)

data class InstalledAppDetail(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isUpdatedSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val apkSizeBytes: Long,
    val apkPaths: List<String>,
    val installSource: AppInstallSource,
    val metaData: Map<String, String>,
    val factoryInfo: FactoryAppInfo?,
    val icon: Bitmap?,
) {
    val canRequestUninstall: Boolean
        get() = !isSystemApp || isUpdatedSystemApp
}
