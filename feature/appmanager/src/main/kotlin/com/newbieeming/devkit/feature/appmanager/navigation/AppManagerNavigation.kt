package com.newbieeming.devkit.feature.appmanager.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.appmanager.ui.AppDetailScreen
import com.newbieeming.devkit.feature.appmanager.ui.AppManagerScreen

const val APP_MANAGER_ROUTE = "app_manager"
const val APP_MANAGER_DETAIL_ROUTE = "app_manager/detail"
const val PACKAGE_NAME_ARGUMENT = "packageName"
private const val APP_MANAGER_DETAIL_PATTERN =
    "$APP_MANAGER_DETAIL_ROUTE/{$PACKAGE_NAME_ARGUMENT}"

fun NavController.navigateToAppManager() = navigate(APP_MANAGER_ROUTE)

fun NavController.navigateToAppDetail(packageName: String) {
    navigate("$APP_MANAGER_DETAIL_ROUTE/${Uri.encode(packageName)}")
}

fun NavGraphBuilder.appManagerGraph(navController: NavController) {
    composable(route = APP_MANAGER_ROUTE) {
        AppManagerScreen(
            onNavigateUp = navController::navigateUp,
            onOpenApp = navController::navigateToAppDetail,
        )
    }
    composable(
        route = APP_MANAGER_DETAIL_PATTERN,
        arguments = listOf(
            navArgument(PACKAGE_NAME_ARGUMENT) { type = NavType.StringType },
        ),
    ) {
        AppDetailScreen(onNavigateUp = navController::navigateUp)
    }
}
