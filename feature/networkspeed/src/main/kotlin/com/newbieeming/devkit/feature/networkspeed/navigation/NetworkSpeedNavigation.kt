package com.newbieeming.devkit.feature.networkspeed.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.networkspeed.ui.NetworkSpeedScreen

const val NETWORK_SPEED_ROUTE = "network_speed"

fun NavGraphBuilder.networkSpeedScreen(onNavigateUp: () -> Unit) {
    composable(route = NETWORK_SPEED_ROUTE) {
        NetworkSpeedScreen(onNavigateUp = onNavigateUp)
    }
}
