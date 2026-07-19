package com.newbieeming.devkit.feature.networkspeed.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val NETWORK_SPEED_ROUTE = "network_speed"

fun NavController.navigateToNetworkSpeed() = navigate(NETWORK_SPEED_ROUTE)

fun NavGraphBuilder.networkSpeedScreen() {
    composable(route = NETWORK_SPEED_ROUTE) {
        // TODO: NetworkSpeedScreen()
    }
}
