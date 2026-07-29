package com.newbieeming.devkit.feature.appmanager.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.newbieeming.devkit.feature.appmanager.R
import com.newbieeming.devkit.feature.appmanager.model.AppFilter
import com.newbieeming.devkit.feature.appmanager.model.InstalledAppSummary
import com.newbieeming.devkit.feature.appmanager.presentation.AppManagerUiState
import com.newbieeming.devkit.feature.appmanager.presentation.AppManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(
    onNavigateUp: () -> Unit,
    onOpenApp: (String) -> Unit,
    viewModel: AppManagerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showUninstallConfirmation by remember { mutableStateOf(false) }
    var uninstallQueue by remember { mutableStateOf(emptyList<String>()) }
    val uninstallLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        uninstallQueue = uninstallQueue.drop(1)
        viewModel.refresh()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uninstallQueue) {
        uninstallQueue.firstOrNull()?.let { packageName ->
            uninstallLauncher.launch(requestUninstallIntent(packageName))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.selectedPackages.isEmpty()) {
                            stringResource(R.string.app_manager_title)
                        } else {
                            stringResource(
                                R.string.selected_app_count,
                                state.selectedPackages.size,
                            )
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = if (state.selectedPackages.isEmpty()) {
                            onNavigateUp
                        } else {
                            viewModel::clearSelection
                        },
                    ) {
                        Icon(
                            imageVector = if (state.selectedPackages.isEmpty()) {
                                Icons.AutoMirrored.Filled.ArrowBack
                            } else {
                                Icons.Default.Close
                            },
                            contentDescription = stringResource(
                                if (state.selectedPackages.isEmpty()) {
                                    R.string.navigate_back
                                } else {
                                    R.string.clear_selection
                                },
                            ),
                        )
                    }
                },
                actions = {
                    if (state.selectedPackages.isNotEmpty()) {
                        IconButton(onClick = { showUninstallConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.uninstall_selected_apps,
                                    state.selectedPackages.size,
                                ),
                            )
                        }
                    }
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_app_list),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (state.selectedPackages.isNotEmpty()) {
                Surface(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    ),
                    shadowElevation = 8.dp,
                ) {
                    FilledTonalButton(
                        onClick = { showUninstallConfirmation = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(
                                R.string.uninstall_selected_apps,
                                state.selectedPackages.size,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        AppManagerContent(
            state = state,
            contentPadding = padding,
            onQueryChange = viewModel::setQuery,
            onFilterChange = viewModel::setFilter,
            onToggleSelection = viewModel::toggleSelection,
            onOpenApp = onOpenApp,
            onRetry = viewModel::refresh,
        )
    }

    if (showUninstallConfirmation) {
        AlertDialog(
            onDismissRequest = { showUninstallConfirmation = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.uninstall_confirmation_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.uninstall_confirmation_message,
                        state.selectedPackages.size,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uninstallQueue = state.apps
                            .filter { it.packageName in state.selectedPackages }
                            .map(InstalledAppSummary::packageName)
                        showUninstallConfirmation = false
                        viewModel.clearSelection()
                    },
                ) {
                    Text(stringResource(R.string.continue_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppManagerContent(
    state: AppManagerUiState,
    contentPadding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AppFilter) -> Unit,
    onToggleSelection: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        SearchAndFilters(
            query = state.query,
            selectedFilter = state.filter,
            resultCount = state.filteredApps.size,
            onQueryChange = onQueryChange,
            onFilterChange = onFilterChange,
        )
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(180.dp),
                )
            }

            state.loadFailed -> MessageContent(
                message = stringResource(R.string.app_list_load_failed),
                action = stringResource(R.string.retry),
                onAction = onRetry,
            )

            state.filteredApps.isEmpty() -> MessageContent(
                message = stringResource(R.string.no_apps_found),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                items(
                    items = state.filteredApps,
                    key = InstalledAppSummary::packageName,
                ) { app ->
                    AppListItem(
                        app = app,
                        isSelected = app.packageName in state.selectedPackages,
                        onToggleSelection = { onToggleSelection(app.packageName) },
                        onOpen = { onOpenApp(app.packageName) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    query: String,
    selectedFilter: AppFilter,
    resultCount: Int,
    onQueryChange: (String) -> Unit,
    onFilterChange: (AppFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.search_apps)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            stringResource(
                                when (filter) {
                                    AppFilter.ALL -> R.string.filter_all
                                    AppFilter.SYSTEM -> R.string.filter_system
                                    AppFilter.THIRD_PARTY -> R.string.filter_third_party
                                },
                            ),
                        )
                    },
                )
            }
            Text(
                text = stringResource(R.string.app_result_count, resultCount),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AppListItem(
    app: InstalledAppSummary,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (app.icon != null) {
            Image(
                bitmap = app.icon.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
        } else {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.version_summary,
                        app.versionName.ifEmpty { stringResource(R.string.not_available) },
                        app.versionCode,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (app.isUpdatedSystemApp) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.updated_system_app)) },
                    )
                }
            }
        }
        if (app.canRequestUninstall) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
            )
        } else {
            Text(
                text = stringResource(R.string.app_not_uninstallable),
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageContent(
    message: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(message, style = MaterialTheme.typography.bodyLarge)
                if (action != null && onAction != null) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onAction) {
                        Text(action)
                    }
                }
            }
        }
    }
}

internal fun requestUninstallIntent(packageName: String): Intent =
    Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
        putExtra(Intent.EXTRA_RETURN_RESULT, true)
    }
