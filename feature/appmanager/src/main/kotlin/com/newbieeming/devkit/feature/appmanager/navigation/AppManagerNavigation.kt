package com.newbieeming.devkit.feature.appmanager.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val APP_MANAGER_ROUTE = "app_manager"

fun NavController.navigateToAppManager() = navigate(APP_MANAGER_ROUTE)

fun NavGraphBuilder.appManagerScreen() {
    composable(route = APP_MANAGER_ROUTE) {
        // TODO: AppManagerScreen()
    }
}
