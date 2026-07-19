package com.newbieeming.devkit.core.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newbieeming.devkit.core.designsystem.theme.DevKitColors

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
 * @param requiredPermissions 需要的权限及其授权状态（Map<权限名, 是否已授权>）
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
    requiredPermissions: Map<String, Boolean> = emptyMap(),
    onClick: () -> Unit = {},
) {
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
            if (requiredPermissions.isNotEmpty()) {
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
                    requiredPermissions.forEach { (permName, isGranted) ->
                        val bgColor = if (isGranted) DevKitColors.Green400.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer
                        val textColor = if (isGranted) DevKitColors.Green400 else MaterialTheme.colorScheme.onErrorContainer
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(bgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$permName ${if (isGranted) "✓" else "✕"}",
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
