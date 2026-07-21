package com.newbieeming.devkit.core.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.newbieeming.devkit.core.permissions.DevKitPermissionManager

/**
 * 权限 → 本地化显示名映射。
 * 未收录的权限自动取最后一段（如 android.permission.FOO → FOO）。
 * 受版本限制的权限仅在对应 API 及以上才加入，避免 lint 警告。
 */
@Composable
private fun permissionDisplayNames(): Map<String, String> = buildMap {
    // 存储
    put(Manifest.permission.READ_EXTERNAL_STORAGE, stringResource(R.string.permission_read_storage))
    put(Manifest.permission.WRITE_EXTERNAL_STORAGE, stringResource(R.string.permission_write_storage))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        put(Manifest.permission.MANAGE_EXTERNAL_STORAGE, stringResource(R.string.permission_all_files_access))
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        put(Manifest.permission.READ_MEDIA_IMAGES, stringResource(R.string.permission_read_images))
        put(Manifest.permission.READ_MEDIA_VIDEO, stringResource(R.string.permission_read_video))
        put(Manifest.permission.READ_MEDIA_AUDIO, stringResource(R.string.permission_read_audio))
        put(Manifest.permission.POST_NOTIFICATIONS, stringResource(R.string.permission_notifications))
    }
    // 相机 / 麦克风
    put(Manifest.permission.CAMERA, stringResource(R.string.permission_camera))
    put(Manifest.permission.RECORD_AUDIO, stringResource(R.string.permission_microphone))
    // 位置
    put(Manifest.permission.ACCESS_FINE_LOCATION, stringResource(R.string.permission_precise_location))
    put(Manifest.permission.ACCESS_COARSE_LOCATION, stringResource(R.string.permission_approximate_location))
    // 电话 / 短信
    put(Manifest.permission.READ_PHONE_STATE, stringResource(R.string.permission_read_phone_state))
    put(Manifest.permission.CALL_PHONE, stringResource(R.string.permission_call_phone))
    put(Manifest.permission.READ_CONTACTS, stringResource(R.string.permission_read_contacts))
    put(Manifest.permission.SEND_SMS, stringResource(R.string.permission_send_sms))
    // 特殊权限
    put(Manifest.permission.SYSTEM_ALERT_WINDOW, stringResource(R.string.permission_overlay))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        put(Manifest.permission.REQUEST_INSTALL_PACKAGES, stringResource(R.string.permission_install_apps))
    }
}

private fun permissionDisplayName(permission: String, displayNames: Map<String, String>): String =
    displayNames[permission]
        ?: permission.substringAfterLast('.')

/**
 * 所有 Feature 磁贴的通用容器。
 *
 * 使用 Material 3 容器、色彩角色和圆角形状：图标、文字层级、状态徽章及权限标签
 * 均会随应用的动态配色自动适配。
 *
 * @param icon        Material 图标
 * @param title       功能名称
 * @param description 一两句说明（多行时卡片自动变高）
 * @param badge       右上角状态徽章（可选）
 * @param requiredPermissions 需要的权限名称列表，组件会在每次 resume 时自动检查授权状态
 * @param onClick     点击回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatureTileScaffold(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    requiredPermissions: List<String> = emptyList(),
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val permissionDisplayNames = permissionDisplayNames()
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionStates by remember {
        mutableStateOf(requiredPermissions.associateWith { DevKitPermissionManager.isGranted(context, it) })
    }

    // 每次页面 resume 时重新检查权限状态（从设置页返回等场景）
    LaunchedEffect(lifecycleOwner, requiredPermissions) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            permissionStates =
                requiredPermissions.associateWith { DevKitPermissionManager.isGranted(context, it) }
        }
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 148.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (badge != null) {
                    FeatureBadge(badge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (permissionStates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.required_permissions),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    permissionStates.forEach { (permName, isGranted) ->
                        PermissionTag(permissionDisplayName(permName, permissionDisplayNames), isGranted)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun PermissionTag(label: String, granted: Boolean) {
    val containerColor = if (granted) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (granted) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (granted) "✓" else "!", style = MaterialTheme.typography.labelSmall)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
