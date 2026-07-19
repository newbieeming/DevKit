package com.newbieeming.devkit.feature.timesync.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val TIME_SYNC_ROUTE = "time_sync"

fun NavController.navigateToTimeSync() = navigate(TIME_SYNC_ROUTE)

fun NavGraphBuilder.timeSyncScreen() {
    composable(route = TIME_SYNC_ROUTE) {
        // TODO: TimeSyncScreen()
    }
}
