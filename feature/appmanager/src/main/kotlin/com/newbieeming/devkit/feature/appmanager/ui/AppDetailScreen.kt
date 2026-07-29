package com.newbieeming.devkit.feature.appmanager.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newbieeming.devkit.feature.appmanager.R
import com.newbieeming.devkit.feature.appmanager.model.AppInstallSource
import com.newbieeming.devkit.feature.appmanager.model.InstalledAppDetail
import com.newbieeming.devkit.feature.appmanager.presentation.AppDetailViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val uninstallLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onNavigateUp()
        } else {
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.app?.label ?: stringResource(R.string.app_details_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_app_details),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(180.dp),
                )
            }

            state.loadFailed -> DetailError(
                contentPadding = padding,
                onNavigateUp = onNavigateUp,
                onRetry = viewModel::refresh,
            )

            state.app != null -> AppDetailContent(
                app = requireNotNull(state.app),
                contentPadding = padding,
                onOpenSettings = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${requireNotNull(state.app).packageName}"),
                        ),
                    )
                },
                onUninstall = {
                    uninstallLauncher.launch(
                        requestUninstallIntent(requireNotNull(state.app).packageName),
                    )
                },
            )
        }
    }
}

@Composable
private fun AppDetailContent(
    app: InstalledAppDetail,
    contentPadding: PaddingValues,
    onOpenSettings: () -> Unit,
    onUninstall: () -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 320.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 20.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        item {
            OverviewCard(app, onOpenSettings, onUninstall)
        }
        item {
            InfoCard(
                title = stringResource(R.string.version_information),
                icon = Icons.Default.Info,
            ) {
                InfoRow(stringResource(R.string.package_name), app.packageName)
                InfoRow(
                    stringResource(R.string.version_name),
                    app.versionName.ifEmpty { stringResource(R.string.not_available) },
                )
                InfoRow(stringResource(R.string.version_code), app.versionCode.toString())
                app.factoryInfo?.let { factoryInfo ->
                    InfoRow(
                        stringResource(R.string.factory_version_name),
                        factoryInfo.versionName.ifEmpty {
                            stringResource(R.string.not_available)
                        },
                    )
                    InfoRow(
                        stringResource(R.string.factory_version_code),
                        factoryInfo.versionCode.toString(),
                    )
                }
                InfoRow(
                    stringResource(R.string.application_type),
                    stringResource(
                        when {
                            app.isUpdatedSystemApp -> R.string.updated_system_app
                            app.isSystemApp -> R.string.system_app
                            else -> R.string.third_party_app
                        },
                    ),
                )
            }
        }
        item {
            InfoCard(
                title = stringResource(R.string.installation_information),
                icon = Icons.Default.Folder,
            ) {
                InfoRow(stringResource(R.string.apk_total_size), formatBytes(app.apkSizeBytes))
                InfoRow(
                    stringResource(R.string.first_installed),
                    formatDateTime(app.firstInstallTime),
                )
                InfoRow(
                    stringResource(R.string.last_updated),
                    formatDateTime(app.lastUpdateTime),
                )
            }
        }
        item {
            InfoCard(
                title = stringResource(R.string.apk_paths),
                icon = Icons.Default.Folder,
            ) {
                Text(
                    text = stringResource(R.string.current_apk_paths),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (app.apkPaths.isEmpty()) {
                    Text(stringResource(R.string.no_apk_paths))
                } else {
                    ApkPathRows(app.apkPaths)
                }
                if (app.isUpdatedSystemApp) {
                    Text(
                        text = stringResource(R.string.factory_apk_paths),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    val factoryInfo = app.factoryInfo
                    if (factoryInfo == null) {
                        Text(
                            text = stringResource(R.string.factory_apk_paths_unavailable),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        InfoRow(
                            stringResource(R.string.factory_apk_total_size),
                            formatBytes(factoryInfo.apkSizeBytes),
                        )
                        ApkPathRows(factoryInfo.apkPaths)
                    }
                }
            }
        }
        item {
            InstallSourceCard(app.installSource)
        }
        item {
            InfoCard(
                title = stringResource(R.string.manifest_meta_data),
                icon = Icons.Default.Info,
            ) {
                if (app.metaData.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_manifest_meta_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    app.metaData.forEach { (key, value) ->
                        InfoRow(key, value)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    app: InstalledAppDetail,
    onOpenSettings: () -> Unit,
    onUninstall: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.large),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.system_app_settings))
                }
                if (app.canRequestUninstall) {
                    OutlinedButton(
                        onClick = onUninstall,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(
                                if (app.isUpdatedSystemApp) {
                                    R.string.uninstall_updates
                                } else {
                                    R.string.uninstall_app
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApkPathRows(paths: List<String>) {
    paths.forEachIndexed { index, path ->
        InfoRow(
            label = if (index == 0) {
                stringResource(R.string.base_apk)
            } else {
                stringResource(R.string.split_apk_number, index)
            },
            value = path,
        )
    }
}

@Composable
private fun InstallSourceCard(source: AppInstallSource) {
    InfoCard(
        title = stringResource(R.string.install_source),
        icon = Icons.Default.Info,
    ) {
        InfoRow(
            stringResource(R.string.installing_package),
            source.installingPackageName.displayValue(),
        )
        InfoRow(
            stringResource(R.string.initiating_package),
            source.initiatingPackageName.displayValue(),
        )
        InfoRow(
            stringResource(R.string.originating_package),
            source.originatingPackageName.displayValue(),
        )
        source.packageSource?.let { packageSource ->
            InfoRow(
                stringResource(R.string.package_source),
                packageSourceName(packageSource),
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DetailError(
    contentPadding: PaddingValues,
    onNavigateUp: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.app_details_load_failed))
            Spacer(Modifier.height(8.dp))
            Row {
                TextButton(onClick = onNavigateUp) {
                    Text(stringResource(R.string.navigate_back))
                }
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun String?.displayValue(): String =
    this?.takeUnless(String::isBlank) ?: stringResource(R.string.not_available)

@Composable
private fun packageSourceName(source: Int): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return source.toString()
    }
    return stringResource(
        when (source) {
            PackageInstaller.PACKAGE_SOURCE_STORE -> R.string.package_source_store
            PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE -> R.string.package_source_local_file
            PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE ->
                R.string.package_source_downloaded_file
            PackageInstaller.PACKAGE_SOURCE_OTHER -> R.string.package_source_other
            else -> R.string.package_source_unspecified
        },
    )
}

private fun formatDateTime(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(timestamp))

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val group = (ln(bytes.toDouble()) / ln(1024.0))
        .toInt()
        .coerceIn(0, units.lastIndex)
    val value = bytes / 1024.0.pow(group.toDouble())
    return String.format(Locale.getDefault(), "%.1f %s", value, units[group])
}
