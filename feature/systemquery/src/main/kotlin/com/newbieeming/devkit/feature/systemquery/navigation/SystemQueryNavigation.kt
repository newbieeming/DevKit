package com.newbieeming.devkit.feature.systemquery.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.systemquery.ui.SystemQueryScreen

const val SYSTEM_QUERY_ROUTE = "system_query"

fun NavGraphBuilder.systemQueryScreen(onNavigateUp: () -> Unit) {
    composable(route = SYSTEM_QUERY_ROUTE) {
        SystemQueryScreen(onNavigateUp = onNavigateUp)
    }
}
