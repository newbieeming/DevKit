package com.newbieeming.devkit.core.ui.overlay

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.newbieeming.devkit.core.model.OverlayColorChoice
import com.newbieeming.devkit.core.model.OverlayConfig

data class ResolvedOverlayColors(
    val icon: Color,
    val background: Color,
    val text: Color,
)

@Composable
fun resolveOverlayColors(config: OverlayConfig): ResolvedOverlayColors = ResolvedOverlayColors(
    icon = resolveOverlayColor(config.iconColor, OverlayColorRole.ICON),
    background = if (config.showBackground) {
        resolveOverlayColor(config.backgroundColor, OverlayColorRole.BACKGROUND)
    } else {
        Color.Transparent
    },
    text = resolveOverlayColor(config.textColor, OverlayColorRole.TEXT),
)

private enum class OverlayColorRole { ICON, BACKGROUND, TEXT }

@Composable
private fun resolveOverlayColor(
    choice: OverlayColorChoice,
    role: OverlayColorRole,
): Color {
    val scheme = MaterialTheme.colorScheme
    return when (choice) {
        OverlayColorChoice.DYNAMIC -> when (role) {
            OverlayColorRole.ICON -> scheme.primary
            OverlayColorRole.BACKGROUND -> scheme.surfaceContainer.copy(alpha = 0.92f)
            OverlayColorRole.TEXT -> scheme.onSurface
        }
        OverlayColorChoice.PRIMARY -> scheme.primary
        OverlayColorChoice.SECONDARY -> scheme.secondary
        OverlayColorChoice.TERTIARY -> scheme.tertiary
        OverlayColorChoice.BLACK -> Color.Black
        OverlayColorChoice.WHITE -> Color.White
        OverlayColorChoice.TRANSPARENT -> Color.Transparent
    }
}
