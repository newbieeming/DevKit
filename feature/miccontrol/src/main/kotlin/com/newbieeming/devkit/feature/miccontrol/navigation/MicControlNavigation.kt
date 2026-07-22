package com.newbieeming.devkit.feature.miccontrol.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.miccontrol.ui.MicControlScreen

const val MIC_CONTROL_ROUTE = "mic_control"

fun NavGraphBuilder.micControlScreen(onNavigateUp: () -> Unit) {
    composable(route = MIC_CONTROL_ROUTE) {
        MicControlScreen(onNavigateUp = onNavigateUp)
    }
}
