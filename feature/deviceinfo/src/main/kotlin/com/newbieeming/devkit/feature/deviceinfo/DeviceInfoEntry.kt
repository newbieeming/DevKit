package com.newbieeming.devkit.feature.deviceinfo

import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.deviceinfo.navigation.DEVICE_INFO_ROUTE
import com.newbieeming.devkit.feature.deviceinfo.navigation.deviceInfoScreen

class DeviceInfoEntry : FeatureEntry {
    override val featureId = "device_info"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        FeatureTileScaffold(
            icon = Icons.Default.Memory,
            title = stringResource(R.string.device_info_title),
            description = stringResource(R.string.device_info_description),
            modifier = modifier,
            onClick = { onNavigate(DEVICE_INFO_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.deviceInfoScreen(onNavigateUp = { navController.navigateUp() })
    }
}
