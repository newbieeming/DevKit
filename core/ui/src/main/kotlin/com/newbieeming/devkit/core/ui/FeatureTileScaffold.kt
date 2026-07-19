package com.newbieeming.devkit.core.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.newbieeming.devkit.core.designsystem.theme.DevKitColors

/**
 * 检查单个权限是否已授予，对特殊权限使用正确的检测方式。
 */
private fun isPermissionGranted(context: Context, permission: String): Boolean {
    return when (permission) {
        // 悬浮窗权限
        Manifest.permission.SYSTEM_ALERT_WINDOW -> Settings.canDrawOverlays(context)
        // 安装未知来源应用
        Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else true
        }
        // 通知权限 (Android 13+)
        Manifest.permission.POST_NOTIFICATIONS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        }
        // 常规权限
        else -> ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * 权限 → 中文显示名映射。
 * 未收录的权限自动取最后一段（如 android.permission.FOO → FOO）。
 * 受版本限制的权限仅在对应 API 及以上才加入，避免 lint 警告。
 */
private val PERMISSION_DISPLAY_NAMES: Map<String, String> = buildMap {
    // 存储
    put(Manifest.permission.READ_EXTERNAL_STORAGE, "读取存储")
    put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入存储")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        put(Manifest.permission.READ_MEDIA_IMAGES, "读取图片")
        put(Manifest.permission.READ_MEDIA_VIDEO, "读取视频")
        put(Manifest.permission.READ_MEDIA_AUDIO, "读取音频")
        put(Manifest.permission.POST_NOTIFICATIONS, "通知")
    }
    // 相机 / 麦克风
    put(Manifest.permission.CAMERA, "相机")
    put(Manifest.permission.RECORD_AUDIO, "麦克风")
    // 位置
    put(Manifest.permission.ACCESS_FINE_LOCATION, "精确定位")
    put(Manifest.permission.ACCESS_COARSE_LOCATION, "粗略定位")
    // 电话 / 短信
    put(Manifest.permission.READ_PHONE_STATE, "读取电话状态")
    put(Manifest.permission.CALL_PHONE, "拨打电话")
    put(Manifest.permission.READ_CONTACTS, "读取通讯录")
    put(Manifest.permission.SEND_SMS, "发送短信")
    // 特殊权限
    put(Manifest.permission.SYSTEM_ALERT_WINDOW, "悬浮窗")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        put(Manifest.permission.REQUEST_INSTALL_PACKAGES, "安装应用")
    }
}

private fun permissionDisplayName(permission: String): String =
    PERMISSION_DISPLAY_NAMES[permission]
        ?: permission.substringAfterLast('.')

/**
 * 所有 Feature 磁贴的通用容器。
 *
 * **宽度**：由调用方（LazyVerticalStaggeredGrid 列宽）决定，内部 fillMaxWidth 填满。
 * **高度**：自适应内容，不固定——这正是瀑布流需要的变高特性。
 * 通过 [defaultMinSize] 保证最小高度，避免内容极少时卡片过矮。
 *
 * @param icon        表情/文字图标，后续可扩展为 ImageVector
 * @param title       功能名称
 * @param description 一两句说明（多行时卡片自动变高）
 * @param badge       右上角状态徽章（可选）
 * @param requiredPermissions 需要的权限名称列表，组件会在每次 resume 时自动检查授权状态
 * @param onClick     点击回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatureTileScaffold(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    requiredPermissions: List<String> = emptyList(),
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionStates by remember {
        mutableStateOf(requiredPermissions.associateWith { isPermissionGranted(context, it) })
    }

    // 每次页面 resume 时重新检查权限状态（从设置页返回等场景）
    LaunchedEffect(lifecycleOwner, requiredPermissions) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            permissionStates =
                requiredPermissions.associateWith { isPermissionGranted(context, it) }
        }
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            // 图标行：左侧 emoji，右侧可选徽章
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = icon, fontSize = 28.sp)
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // description 不限行数——内容多时卡片自然变高，形成瀑布流效果
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 权限状态展示
            if (permissionStates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "前置权限: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    permissionStates.forEach { (permName, isGranted) ->
                        val bgColor =
                            if (isGranted) DevKitColors.Green400.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer
                        val textColor =
                            if (isGranted) DevKitColors.Green400 else MaterialTheme.colorScheme.onErrorContainer
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(bgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${permissionDisplayName(permName)} ${if (isGranted) "✓" else "✕"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
