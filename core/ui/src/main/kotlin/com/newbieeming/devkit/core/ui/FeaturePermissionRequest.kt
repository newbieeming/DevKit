package com.newbieeming.devkit.core.ui

import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.newbieeming.devkit.core.permissions.DevKitPermissionManager
import com.newbieeming.devkit.core.permissions.DevKitPermissions

/**
 * 返回一个可直接作为磁贴点击事件使用的权限请求回调。
 *
 * feature 只声明所需普通权限及是否需要所有文件访问；申请流程与系统设置页跳转
 * 由此处统一处理，所有权限满足后才调用 [onGranted]。
 */
@Composable
fun rememberFeaturePermissionRequest(
    runtimePermissions: List<String>,
    requiresAllFilesAccess: Boolean = false,
    onGranted: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val latestOnGranted by rememberUpdatedState(onGranted)
    val allFilesAccessRequiredMessage = stringResource(R.string.all_files_access_required)
    val permissionsRequiredMessage = stringResource(R.string.permissions_required)
    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (DevKitPermissionManager.isGranted(context, DevKitPermissions.ALL_FILES_ACCESS)) {
            latestOnGranted()
        } else {
            Toast.makeText(context, allFilesAccessRequiredMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun continueWhenGranted() {
        if (
            requiresAllFilesAccess &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !DevKitPermissionManager.isGranted(context, DevKitPermissions.ALL_FILES_ACCESS)
        ) {
            allFilesAccessLauncher.launch(DevKitPermissionManager.allFilesAccessIntent(context))
        } else {
            latestOnGranted()
        }
    }

    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        if (runtimePermissions.all { DevKitPermissionManager.isGranted(context, it) }) {
            continueWhenGranted()
        } else {
            Toast.makeText(context, permissionsRequiredMessage, Toast.LENGTH_SHORT).show()
        }
    }

    return remember(context, runtimePermissions, requiresAllFilesAccess) {
        {
            val missingPermissions = runtimePermissions.filterNot {
                DevKitPermissionManager.isGranted(context, it)
            }
            if (missingPermissions.isEmpty()) {
                continueWhenGranted()
            } else {
                runtimePermissionLauncher.launch(missingPermissions.toTypedArray())
            }
        }
    }
}

/** 请求悬浮窗权限，并在授权成功后执行 [onGranted]。 */
@Composable
fun rememberOverlayPermissionAction(onGranted: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val latestOnGranted by rememberUpdatedState(onGranted)
    val deniedMessage = stringResource(R.string.overlay_permission_required)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (DevKitPermissionManager.isGranted(context, DevKitPermissions.SYSTEM_ALERT)) {
            latestOnGranted()
        } else {
            Toast.makeText(context, deniedMessage, Toast.LENGTH_SHORT).show()
        }
    }
    return remember(context, launcher) {
        {
            if (DevKitPermissionManager.isGranted(context, DevKitPermissions.SYSTEM_ALERT)) {
                latestOnGranted()
            } else {
                launcher.launch(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
        }
    }
}
