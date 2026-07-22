package com.newbieeming.devkit.feature.timesync.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.timesync.ui.TimeSyncScreen

const val TIME_SYNC_ROUTE = "time_sync"

fun NavGraphBuilder.timeSyncScreen(onNavigateUp: () -> Unit) {
    composable(route = TIME_SYNC_ROUTE) {
        TimeSyncScreen(onNavigateUp = onNavigateUp)
    }
}
