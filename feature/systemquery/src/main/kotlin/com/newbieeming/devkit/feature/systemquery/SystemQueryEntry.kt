package com.newbieeming.devkit.feature.systemquery

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.core.ui.FeatureTileScaffold
import com.newbieeming.devkit.feature.systemquery.navigation.SYSTEM_QUERY_ROUTE
import com.newbieeming.devkit.feature.systemquery.navigation.systemQueryScreen

class SystemQueryEntry : FeatureEntry {
    override val featureId = "system_query"

    @Composable
    override fun Tile(modifier: Modifier, onNavigate: (route: String) -> Unit) {
        FeatureTileScaffold(
            icon = Icons.AutoMirrored.Filled.ManageSearch,
            title = stringResource(R.string.system_query_title),
            description = stringResource(R.string.system_query_description),
            modifier = modifier,
            onClick = { onNavigate(SYSTEM_QUERY_ROUTE) },
        )
    }

    override fun registerNavigation(builder: NavGraphBuilder, navController: NavController) {
        builder.systemQueryScreen(onNavigateUp = { navController.navigateUp() })
    }
}
