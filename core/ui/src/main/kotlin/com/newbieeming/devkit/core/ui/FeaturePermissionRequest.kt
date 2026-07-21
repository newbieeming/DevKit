package com.newbieeming.devkit.core.ui

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
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
    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (DevKitPermissionManager.isGranted(context, DevKitPermissions.ALL_FILES_ACCESS)) {
            latestOnGranted()
        } else {
            Toast.makeText(context, context.getString(R.string.all_files_access_required), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, context.getString(R.string.permissions_required), Toast.LENGTH_SHORT).show()
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
