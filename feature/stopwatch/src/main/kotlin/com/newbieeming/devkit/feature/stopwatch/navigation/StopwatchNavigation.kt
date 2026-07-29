package com.newbieeming.devkit.feature.stopwatch.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.stopwatch.ui.StopwatchScreen

const val STOPWATCH_ROUTE = "stopwatch"

fun NavGraphBuilder.stopwatchScreen(onNavigateUp: () -> Unit) {
    composable(route = STOPWATCH_ROUTE) {
        StopwatchScreen(onNavigateUp = onNavigateUp)
    }
}
