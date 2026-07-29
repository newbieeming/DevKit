package com.newbieeming.devkit.feature.deviceinfo.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.deviceinfo.ui.DeviceInfoScreen

const val DEVICE_INFO_ROUTE = "device_info"

fun NavGraphBuilder.deviceInfoScreen(onNavigateUp: () -> Unit) {
    composable(route = DEVICE_INFO_ROUTE) {
        DeviceInfoScreen(onNavigateUp = onNavigateUp)
    }
}
