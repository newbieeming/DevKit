package com.newbieeming.devkit.feature.deviceinfo.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val DEVICE_INFO_ROUTE = "device_info"

fun NavController.navigateToDeviceInfo() = navigate(DEVICE_INFO_ROUTE)

fun NavGraphBuilder.deviceInfoScreen() {
    composable(route = DEVICE_INFO_ROUTE) {
        // TODO: DeviceInfoScreen()
    }
}
