package com.newbieeming.devkit.feature.deviceinfo.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.newbieeming.devkit.feature.deviceinfo.R
import com.newbieeming.devkit.feature.deviceinfo.model.BatteryDetails
import com.newbieeming.devkit.feature.deviceinfo.model.BatteryHealth
import com.newbieeming.devkit.feature.deviceinfo.model.BatteryStatus
import com.newbieeming.devkit.feature.deviceinfo.model.CapabilityDetails
import com.newbieeming.devkit.feature.deviceinfo.model.CpuDetails
import com.newbieeming.devkit.feature.deviceinfo.model.DeviceIdentity
import com.newbieeming.devkit.feature.deviceinfo.model.DeviceType
import com.newbieeming.devkit.feature.deviceinfo.model.DisplayDetails
import com.newbieeming.devkit.feature.deviceinfo.model.MemoryDetails
import com.newbieeming.devkit.feature.deviceinfo.model.NetworkDetails
import com.newbieeming.devkit.feature.deviceinfo.model.NetworkStatus
import com.newbieeming.devkit.feature.deviceinfo.model.NetworkTransport
import com.newbieeming.devkit.feature.deviceinfo.model.PowerSource
import com.newbieeming.devkit.feature.deviceinfo.model.SerialAccess
import com.newbieeming.devkit.feature.deviceinfo.model.StorageDetails
import com.newbieeming.devkit.feature.deviceinfo.model.StorageKind
import com.newbieeming.devkit.feature.deviceinfo.model.SystemDetails
import com.newbieeming.devkit.feature.deviceinfo.model.ThermalStatus
import com.newbieeming.devkit.feature.deviceinfo.presentation.DeviceInfoUiState
import com.newbieeming.devkit.feature.deviceinfo.presentation.DeviceInfoViewModel
import java.text.DecimalFormat
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    onNavigateUp: () -> Unit,
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    val hasPrimaryInfo = state.identity != null &&
        state.system != null &&
        state.memory != null &&
        state.display != null
    if (state.isLoading || (!state.loadFailed && !hasPrimaryInfo)) {
        LoadingScreen(modifier = Modifier.fillMaxSize())
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_info_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_device_info),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            hasPrimaryInfo -> {
                DeviceInfoContent(
                    state = state,
                    contentPadding = padding,
                )
            }
            state.loadFailed -> ErrorContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onRetry = viewModel::refresh,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LoadingIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(180.dp),
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoContent(
    state: DeviceInfoUiState,
    contentPadding: PaddingValues,
) {
    val identity = requireNotNull(state.identity)
    val system = requireNotNull(state.system)
    val memory = requireNotNull(state.memory)
    val display = requireNotNull(state.display)
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
        item(span = StaggeredGridItemSpan.FullLine) {
            OverviewCard(
                identity = identity,
                system = system,
                memory = memory,
                network = state.network,
                cpu = state.cpu,
            )
        }
        item {
            IdentityCard(identity)
        }
        item {
            DisplayCard(display)
        }
        item {
            SystemCard(system)
        }
        item {
            state.cpu?.let { CpuCard(it) } ?: SectionPlaceholderCard(
                title = stringResource(R.string.processor_section),
                icon = Icons.Default.DeveloperBoard,
                isLoading = state.isLoading,
            )
        }
        item {
            MemoryCard(memory)
        }
        state.storage?.let { storageItems ->
            if (storageItems.isEmpty()) {
                item {
                    SectionPlaceholderCard(
                        title = stringResource(R.string.internal_storage_section),
                        icon = Icons.Default.Storage,
                        isLoading = false,
                    )
                }
            } else {
                storageItems.forEachIndexed { index, storage ->
                    item(key = "storage_${storage.kind}_$index") {
                        StorageCard(storage)
                    }
                }
            }
        } ?: item {
            SectionPlaceholderCard(
                title = stringResource(R.string.internal_storage_section),
                icon = Icons.Default.Storage,
                isLoading = state.isLoading,
            )
        }
        item {
            state.network?.let { NetworkCard(it) } ?: SectionPlaceholderCard(
                title = stringResource(R.string.network_section),
                icon = Icons.Default.NetworkCheck,
                isLoading = state.isLoading,
            )
        }
        item {
            state.battery?.let { BatteryCard(it) } ?: SectionPlaceholderCard(
                title = stringResource(R.string.power_section),
                icon = Icons.Default.BatteryFull,
                isLoading = state.isLoading,
            )
        }
        item {
            state.capabilities?.let { CapabilitiesCard(it) } ?: SectionPlaceholderCard(
                title = stringResource(R.string.capabilities_section),
                icon = Icons.Default.DevicesOther,
                isLoading = state.isLoading,
            )
        }
    }
}

@Composable
private fun OverviewCard(
    identity: DeviceIdentity,
    system: SystemDetails,
    memory: MemoryDetails,
    network: NetworkDetails?,
    cpu: CpuDetails?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.DevicesOther,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = identity.model.displayValue(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = identity.manufacturer.displayValue(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                network?.let { ConnectionChip(it.status) }
            }
            Spacer(Modifier.height(18.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val useRow = maxWidth >= 620.dp
                if (useRow) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryMetric(
                            label = stringResource(R.string.android_version),
                            value = androidVersion(system.androidRelease, system.apiLevel),
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetric(
                            label = stringResource(R.string.total_memory),
                            value = formatBytes(memory.totalBytes),
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetric(
                            label = stringResource(R.string.cpu_cores),
                            value = cpu?.let {
                                stringResource(R.string.core_count_value, it.onlineCoreCount, it.coreCount)
                            } ?: stringResource(R.string.loading),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryMetric(
                            label = stringResource(R.string.android_version),
                            value = androidVersion(system.androidRelease, system.apiLevel),
                        )
                        SummaryMetric(
                            label = stringResource(R.string.total_memory),
                            value = formatBytes(memory.totalBytes),
                        )
                        SummaryMetric(
                            label = stringResource(R.string.cpu_cores),
                            value = cpu?.let {
                                stringResource(R.string.core_count_value, it.onlineCoreCount, it.coreCount)
                            } ?: stringResource(R.string.loading),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun SystemCard(system: SystemDetails) {
    InfoCard(
        title = stringResource(R.string.system_section),
        icon = Icons.Default.Android,
    ) {
        InfoRow(R.string.android_version, androidVersion(system.androidRelease, system.apiLevel))
        InfoRow(R.string.security_patch, system.securityPatch.displayValue())
        InfoRow(R.string.kernel_version, system.kernelVersion.displayValue())
        InfoRow(R.string.build_number, system.buildNumber.displayValue())
        InfoRow(R.string.build_id, system.buildId.displayValue())
        InfoRow(R.string.build_type, system.buildType.displayValue())
        InfoRow(
            R.string.app_version,
            stringResource(R.string.app_version_value, system.appVersionName.displayValue(), system.appVersionCode),
        )
        InfoRow(
            R.string.minimum_supported_android,
            androidVersion(androidReleaseForApi(system.minSdk), system.minSdk),
        )
        InfoRow(R.string.target_android_api, stringResource(R.string.api_level_value, system.targetSdk))
        InfoRow(R.string.device_uptime, formatUptime(system.uptimeMillis))
        InfoRow(R.string.java_vm_version, system.javaVmVersion.displayValue())
        InfoRow(R.string.bootloader, system.bootloader.displayValue())
        InfoRow(R.string.baseband_version, system.baseband.displayValue())
    }
}

@Composable
private fun CpuCard(cpu: CpuDetails) {
    InfoCard(
        title = stringResource(R.string.processor_section),
        icon = Icons.Default.DeveloperBoard,
    ) {
        InfoRow(R.string.cpu_model, cpu.model.displayValue())
        if (cpu.socManufacturer != null || cpu.socModel != null) {
            InfoRow(
                R.string.soc,
                listOfNotNull(cpu.socManufacturer, cpu.socModel).joinToString(" ").displayValue(),
            )
        }
        InfoRow(R.string.cpu_architecture, cpu.architecture.displayValue())
        InfoRow(
            R.string.cpu_cores,
            stringResource(R.string.core_count_value, cpu.onlineCoreCount, cpu.coreCount),
        )
        InfoRow(
            R.string.cpu_frequency_range,
            frequencyRange(cpu.minFrequencyKhz, cpu.maxFrequencyKhz),
        )
        InfoRow(R.string.cpu_current_frequency, formatFrequency(cpu.currentFrequencyKhz))
        InfoRow(R.string.cpu_clusters, formatClusters(cpu.clusterMaxFrequenciesKhz))
        InfoRow(R.string.aggregate_peak_frequency, formatFrequency(cpu.aggregateMaxFrequencyKhz))
        SupportingText(R.string.aggregate_frequency_explanation)
        InfoRow(R.string.supported_abis, cpu.supportedAbis.joinToString(", ").displayValue())
        InfoRow(R.string.instruction_features, cpu.features.joinToString(" ").displayValue())
    }
}

@Composable
private fun MemoryCard(memory: MemoryDetails) {
    val usedBytes = (memory.totalBytes - memory.availableBytes).coerceAtLeast(0L)
    InfoCard(
        title = stringResource(R.string.memory_section),
        icon = Icons.Default.Memory,
    ) {
        UsageIndicator(usedBytes, memory.totalBytes)
        InfoRow(R.string.total_memory, formatBytes(memory.totalBytes))
        InfoRow(R.string.memory_used, formatBytes(usedBytes))
        InfoRow(R.string.memory_available, formatBytes(memory.availableBytes))
        InfoRow(R.string.memory_format, memory.ramType ?: stringResource(R.string.not_reported_by_android))
        InfoRow(R.string.low_memory_threshold, formatBytes(memory.thresholdBytes))
        InfoRow(
            R.string.low_memory_state,
            stringResource(if (memory.isLowMemory) R.string.yes else R.string.no),
        )
        InfoRow(
            R.string.swap_usage,
            if (memory.swapTotalBytes != null && memory.swapFreeBytes != null) {
                val usedSwap = (memory.swapTotalBytes - memory.swapFreeBytes).coerceAtLeast(0L)
                stringResource(
                    R.string.used_of_total_value,
                    formatBytes(usedSwap),
                    formatBytes(memory.swapTotalBytes),
                )
            } else {
                stringResource(R.string.not_available)
            },
        )
        InfoRow(R.string.app_heap_limit, formatBytes(memory.appHeapLimitBytes))
    }
}

@Composable
private fun StorageCard(storage: StorageDetails) {
    val usedBytes = (storage.totalBytes - storage.availableBytes).coerceAtLeast(0L)
    val titleRes = when (storage.kind) {
        StorageKind.INTERNAL -> R.string.internal_storage_section
        StorageKind.SHARED -> R.string.shared_storage_section
        StorageKind.REMOVABLE -> R.string.removable_storage_section
    }
    InfoCard(
        title = stringResource(titleRes),
        icon = Icons.Default.Storage,
    ) {
        UsageIndicator(usedBytes, storage.totalBytes)
        InfoRow(R.string.storage_total, formatBytes(storage.totalBytes))
        InfoRow(R.string.storage_used, formatBytes(usedBytes))
        InfoRow(R.string.storage_available, formatBytes(storage.availableBytes))
        InfoRow(R.string.file_system, storage.fileSystem.displayValue())
        InfoRow(
            R.string.removable,
            stringResource(if (storage.isRemovable) R.string.yes else R.string.no),
        )
    }
}

@Composable
private fun DisplayCard(display: DisplayDetails) {
    InfoCard(
        title = stringResource(R.string.display_section),
        icon = Icons.Default.Monitor,
    ) {
        InfoRow(
            R.string.resolution,
            stringResource(R.string.resolution_value, display.widthPixels, display.heightPixels),
        )
        InfoRow(R.string.density_dpi, stringResource(R.string.dpi_value, display.densityDpi))
        InfoRow(R.string.logical_density, DecimalFormat("0.##").format(display.density))
        InfoRow(R.string.scaled_density, DecimalFormat("0.##").format(display.scaledDensity))
        InfoRow(
            R.string.physical_dpi,
            stringResource(R.string.physical_dpi_value, display.xDpi, display.yDpi),
        )
        InfoRow(
            R.string.refresh_rate,
            display.refreshRateHz?.let { stringResource(R.string.refresh_rate_value, it) }
                ?: stringResource(R.string.not_available),
        )
    }
}

@Composable
private fun NetworkCard(network: NetworkDetails) {
    InfoCard(
        title = stringResource(R.string.network_section),
        icon = Icons.Default.NetworkCheck,
    ) {
        InfoRow(R.string.network_status, networkStatusLabel(network.status))
        InfoRow(
            R.string.network_transport,
            formatNetworkTransports(network.transports),
        )
        InfoRow(R.string.ip_addresses, network.ipAddresses.joinToString("\n").displayValue())
        InfoRow(
            R.string.internet_validated,
            stringResource(if (network.isValidated) R.string.yes else R.string.no),
        )
        InfoRow(
            R.string.metered_network,
            stringResource(if (network.isMetered) R.string.yes else R.string.no),
        )
        InfoRow(R.string.network_interface, network.interfaceName.displayValue())
        InfoRow(R.string.gateway, network.gatewayAddresses.joinToString("\n").displayValue())
        InfoRow(R.string.dns_servers, network.dnsServers.joinToString("\n").displayValue())
        InfoRow(R.string.private_dns, network.privateDnsServer.displayValue())
    }
}

@Composable
private fun BatteryCard(battery: BatteryDetails) {
    InfoCard(
        title = stringResource(R.string.power_section),
        icon = Icons.Default.BatteryFull,
    ) {
        InfoRow(
            R.string.battery_level,
            (battery.levelPercent ?: battery.capacityPercent)?.let {
                stringResource(R.string.percent_value, it)
            }
                ?: stringResource(R.string.not_available),
        )
        InfoRow(R.string.battery_status, batteryStatusLabel(battery.status))
        InfoRow(R.string.battery_health, batteryHealthLabel(battery.health))
        InfoRow(
            R.string.power_source,
            formatPowerSources(battery.powerSources),
        )
        InfoRow(
            R.string.battery_temperature,
            battery.temperatureCelsius?.let { stringResource(R.string.temperature_value, it) }
                ?: stringResource(R.string.not_available),
        )
        InfoRow(
            R.string.battery_voltage,
            battery.voltageMillivolts?.let { stringResource(R.string.voltage_value, it) }
                ?: stringResource(R.string.not_available),
        )
        InfoRow(R.string.thermal_status, thermalStatusLabel(battery.thermalStatus))
    }
}

@Composable
private fun IdentityCard(identity: DeviceIdentity) {
    InfoCard(
        title = stringResource(R.string.device_identity_section),
        icon = Icons.Default.Info,
    ) {
        InfoRow(R.string.model, identity.model.displayValue())
        InfoRow(R.string.manufacturer, identity.manufacturer.displayValue())
        InfoRow(R.string.brand, identity.brand.displayValue())
        InfoRow(
            R.string.serial_number,
            when (identity.serialAccess) {
                SerialAccess.LOADING -> stringResource(R.string.loading)
                SerialAccess.AVAILABLE -> identity.serialNumber.displayValue()
                SerialAccess.RESTRICTED -> stringResource(R.string.restricted_by_android)
                SerialAccess.UNAVAILABLE -> stringResource(R.string.not_available)
            },
        )
        InfoRow(R.string.android_id, identity.androidId.displayValue())
        InfoRow(R.string.product, identity.product.displayValue())
        InfoRow(R.string.device, identity.device.displayValue())
        InfoRow(R.string.board, identity.board.displayValue())
        InfoRow(R.string.hardware, identity.hardware.displayValue())
        SupportingText(R.string.identifier_privacy_explanation)
    }
}

@Composable
private fun CapabilitiesCard(capabilities: CapabilityDetails) {
    InfoCard(
        title = stringResource(R.string.capabilities_section),
        icon = Icons.Default.DevicesOther,
    ) {
        InfoRow(
            R.string.device_type,
            formatDeviceTypes(capabilities.deviceTypes),
        )
        InfoRow(R.string.camera_count, capabilities.cameraCount.toString())
        InfoRow(R.string.sensor_count, capabilities.sensorCount.toString())
        InfoRow(
            R.string.bluetooth,
            stringResource(if (capabilities.hasBluetooth) R.string.supported else R.string.not_supported),
        )
        InfoRow(
            R.string.nfc,
            stringResource(if (capabilities.hasNfc) R.string.supported else R.string.not_supported),
        )
        InfoRow(
            R.string.gps,
            stringResource(if (capabilities.hasGps) R.string.supported else R.string.not_supported),
        )
        InfoRow(R.string.sensors, capabilities.sensorNames.joinToString("\n").displayValue())
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun SectionPlaceholderCard(
    title: String,
    icon: ImageVector,
    isLoading: Boolean,
) {
    InfoCard(title = title, icon = icon) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(
                text = stringResource(if (isLoading) R.string.loading else R.string.not_available),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoRow(@StringRes labelRes: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SelectionContainer(modifier = Modifier.weight(0.6f)) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun UsageIndicator(usedBytes: Long, totalBytes: Long) {
    val fraction = if (totalBytes > 0) {
        (usedBytes.toDouble() / totalBytes).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.used_of_total_value, formatBytes(usedBytes), formatBytes(totalBytes)),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.percent_value, (fraction * 100).roundToInt()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
    }
}

@Composable
private fun SupportingText(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ConnectionChip(status: NetworkStatus) {
    val connected = status == NetworkStatus.CONNECTED
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (connected) Icons.Default.NetworkCheck else Icons.Default.WarningAmber,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = networkStatusLabel(status),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ErrorContent(modifier: Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.device_info_load_failed),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun String?.displayValue(): String =
    this?.takeUnless { it.isBlank() || it.equals("unknown", ignoreCase = true) }
        ?: stringResource(R.string.not_available)

@Composable
private fun formatBytes(bytes: Long): String {
    if (bytes < 0L) return stringResource(R.string.not_available)
    val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    val pattern = if (value >= 100 || unitIndex == 0) "0" else "0.#"
    return "${DecimalFormat(pattern).format(value)} ${units[unitIndex]}"
}

@Composable
private fun formatFrequency(frequencyKhz: Long?): String {
    if (frequencyKhz == null || frequencyKhz <= 0L) return stringResource(R.string.not_available)
    return if (frequencyKhz >= 1_000_000L) {
        stringResource(R.string.frequency_ghz_value, frequencyKhz / 1_000_000.0)
    } else {
        stringResource(R.string.frequency_mhz_value, frequencyKhz / 1_000.0)
    }
}

@Composable
private fun frequencyRange(minKhz: Long?, maxKhz: Long?): String {
    if (minKhz == null && maxKhz == null) return stringResource(R.string.not_available)
    return stringResource(
        R.string.range_value,
        formatFrequency(minKhz),
        formatFrequency(maxKhz),
    )
}

@Composable
private fun formatClusters(clusters: Map<Long, Int>): String {
    if (clusters.isEmpty()) return stringResource(R.string.not_available)
    val values = mutableListOf<String>()
    for ((frequency, cores) in clusters) {
        values += stringResource(R.string.cluster_value, cores, formatFrequency(frequency))
    }
    return values.joinToString(" + ")
}

@Composable
private fun formatNetworkTransports(transports: Set<NetworkTransport>): String {
    if (transports.isEmpty()) return stringResource(R.string.not_available)
    val labels = mutableListOf<String>()
    for (transport in transports) labels += networkTransportLabel(transport)
    return labels.joinToString(", ")
}

@Composable
private fun formatPowerSources(sources: Set<PowerSource>): String {
    if (sources.isEmpty()) return stringResource(R.string.on_battery)
    val labels = mutableListOf<String>()
    for (source in sources) labels += powerSourceLabel(source)
    return labels.joinToString(", ")
}

@Composable
private fun formatDeviceTypes(types: Set<DeviceType>): String {
    if (types.isEmpty()) return stringResource(R.string.not_available)
    val labels = mutableListOf<String>()
    for (type in types) labels += deviceTypeLabel(type)
    return labels.joinToString(", ")
}

@Composable
private fun androidVersion(release: String, api: Int): String =
    stringResource(R.string.android_version_value, release.displayValue(), api)

private fun androidReleaseForApi(api: Int): String = when (api) {
    24 -> "7.0"
    25 -> "7.1"
    26 -> "8.0"
    27 -> "8.1"
    28 -> "9"
    29 -> "10"
    30 -> "11"
    31 -> "12"
    32 -> "12L"
    33 -> "13"
    34 -> "14"
    35 -> "15"
    36 -> "16"
    else -> api.toString()
}

@Composable
private fun formatUptime(uptimeMillis: Long): String {
    val totalMinutes = uptimeMillis / 60_000L
    val days = totalMinutes / (24 * 60)
    val hours = totalMinutes / 60 % 24
    val minutes = totalMinutes % 60
    return stringResource(R.string.uptime_value, days, hours, minutes)
}

@Composable
private fun networkStatusLabel(status: NetworkStatus): String = stringResource(
    when (status) {
        NetworkStatus.CONNECTED -> R.string.connected
        NetworkStatus.DISCONNECTED -> R.string.disconnected
    },
)

@Composable
private fun networkTransportLabel(transport: NetworkTransport): String = stringResource(
    when (transport) {
        NetworkTransport.WIFI -> R.string.transport_wifi
        NetworkTransport.ETHERNET -> R.string.transport_ethernet
        NetworkTransport.CELLULAR -> R.string.transport_cellular
        NetworkTransport.VPN -> R.string.transport_vpn
        NetworkTransport.BLUETOOTH -> R.string.transport_bluetooth
        NetworkTransport.USB -> R.string.transport_usb
        NetworkTransport.OTHER -> R.string.transport_other
    },
)

@Composable
private fun batteryStatusLabel(status: BatteryStatus): String = stringResource(
    when (status) {
        BatteryStatus.CHARGING -> R.string.battery_charging
        BatteryStatus.DISCHARGING -> R.string.battery_discharging
        BatteryStatus.FULL -> R.string.battery_full
        BatteryStatus.NOT_CHARGING -> R.string.battery_not_charging
        BatteryStatus.UNKNOWN -> R.string.unknown
    },
)

@Composable
private fun batteryHealthLabel(health: BatteryHealth): String = stringResource(
    when (health) {
        BatteryHealth.GOOD -> R.string.battery_health_good
        BatteryHealth.OVERHEAT -> R.string.battery_health_overheat
        BatteryHealth.DEAD -> R.string.battery_health_dead
        BatteryHealth.OVERVOLTAGE -> R.string.battery_health_overvoltage
        BatteryHealth.COLD -> R.string.battery_health_cold
        BatteryHealth.FAILURE -> R.string.battery_health_failure
        BatteryHealth.UNKNOWN -> R.string.unknown
    },
)

@Composable
private fun powerSourceLabel(source: PowerSource): String = stringResource(
    when (source) {
        PowerSource.AC -> R.string.power_ac
        PowerSource.USB -> R.string.power_usb
        PowerSource.WIRELESS -> R.string.power_wireless
        PowerSource.DOCK -> R.string.power_dock
    },
)

@Composable
private fun thermalStatusLabel(status: ThermalStatus): String = stringResource(
    when (status) {
        ThermalStatus.NONE -> R.string.thermal_none
        ThermalStatus.LIGHT -> R.string.thermal_light
        ThermalStatus.MODERATE -> R.string.thermal_moderate
        ThermalStatus.SEVERE -> R.string.thermal_severe
        ThermalStatus.CRITICAL -> R.string.thermal_critical
        ThermalStatus.EMERGENCY -> R.string.thermal_emergency
        ThermalStatus.SHUTDOWN -> R.string.thermal_shutdown
        ThermalStatus.UNAVAILABLE -> R.string.not_available
    },
)

@Composable
private fun deviceTypeLabel(type: DeviceType): String = stringResource(
    when (type) {
        DeviceType.AUTOMOTIVE -> R.string.device_type_automotive
        DeviceType.TELEVISION -> R.string.device_type_television
        DeviceType.TABLET -> R.string.device_type_tablet
        DeviceType.PHONE -> R.string.device_type_phone
        DeviceType.EMBEDDED -> R.string.device_type_embedded
    },
)
