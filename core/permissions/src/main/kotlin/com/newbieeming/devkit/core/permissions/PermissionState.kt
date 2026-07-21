package com.newbieeming.devkit.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

/** 权限状态 */
sealed interface PermissionState {
    data object Granted : PermissionState
    data class Denied(val shouldShowRationale: Boolean) : PermissionState
}

/** 统一权限常量，避免在各 Feature 散落字符串 */
object DevKitPermissions {
    const val RECORD_AUDIO    = android.Manifest.permission.RECORD_AUDIO
    const val ALL_FILES_ACCESS = android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
    const val SYSTEM_ALERT    = android.Manifest.permission.SYSTEM_ALERT_WINDOW
    const val FOREGROUND_SVC  = android.Manifest.permission.FOREGROUND_SERVICE
    const val FOREGROUND_MICROPHONE = "android.permission.FOREGROUND_SERVICE_MICROPHONE"
    const val REQUEST_INSTALL = android.Manifest.permission.REQUEST_INSTALL_PACKAGES
    const val WRITE_SETTINGS  = android.Manifest.permission.WRITE_SETTINGS
}

/** 普通权限与系统设置型特殊权限的统一判断工具。 */
object DevKitPermissionManager {
    fun isGranted(context: Context, permission: String): Boolean = when (permission) {
        Manifest.permission.SYSTEM_ALERT_WINDOW -> Settings.canDrawOverlays(context)
        Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()
        }
        Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
        }
        Manifest.permission.POST_NOTIFICATIONS -> {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        else -> ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun allFilesAccessIntent(context: Context): Intent {
        val appSpecificIntent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )
        val fallbackIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        return if (appSpecificIntent.resolveActivity(context.packageManager) != null) {
            appSpecificIntent
        } else {
            fallbackIntent
        }
    }
}
