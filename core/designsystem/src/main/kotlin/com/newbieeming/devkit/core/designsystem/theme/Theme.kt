package com.newbieeming.devkit.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 主色调
private val DarkColorScheme = darkColorScheme(
    primary   = DevKitColors.Blue400,
    secondary = DevKitColors.Cyan300,
    surface   = DevKitColors.Gray900,
    background = DevKitColors.Gray950,
)

private val LightColorScheme = lightColorScheme(
    primary   = DevKitColors.Blue600,
    secondary = DevKitColors.Cyan600,
)

@Composable
fun DevKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),   // 跟随系统昼夜模式
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = DevKitTypography,
        content     = content,
    )
}
