package com.newbieeming.devkit.core.permissions

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** 权限状态 */
sealed interface PermissionState {
    data object Granted : PermissionState
    data class Denied(val shouldShowRationale: Boolean) : PermissionState
}

/** 统一权限常量，避免在各 Feature 散落字符串 */
object DevKitPermissions {
    const val RECORD_AUDIO    = android.Manifest.permission.RECORD_AUDIO
    const val SYSTEM_ALERT    = android.Manifest.permission.SYSTEM_ALERT_WINDOW
    const val FOREGROUND_SVC  = android.Manifest.permission.FOREGROUND_SERVICE
    const val FOREGROUND_MICROPHONE = "android.permission.FOREGROUND_SERVICE_MICROPHONE"
    const val REQUEST_INSTALL = android.Manifest.permission.REQUEST_INSTALL_PACKAGES
    const val WRITE_SETTINGS  = android.Manifest.permission.WRITE_SETTINGS
}
