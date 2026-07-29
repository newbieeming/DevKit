package com.newbieeming.devkit.feature.deviceinfo.data

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.view.Display
import com.newbieeming.devkit.core.common.di.IoDispatcher
import com.newbieeming.devkit.core.common.system.SystemPropertyReader
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
import com.newbieeming.devkit.feature.deviceinfo.model.PrimaryDeviceInfo
import com.newbieeming.devkit.feature.deviceinfo.model.SerialAccess
import com.newbieeming.devkit.feature.deviceinfo.model.SerialNumberResult
import com.newbieeming.devkit.feature.deviceinfo.model.StorageDetails
import com.newbieeming.devkit.feature.deviceinfo.model.StorageKind
import com.newbieeming.devkit.feature.deviceinfo.model.SystemDetails
import com.newbieeming.devkit.feature.deviceinfo.model.ThermalStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoCollector @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val systemPropertyReader: SystemPropertyReader,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun collectPrimary(): PrimaryDeviceInfo = withContext(ioDispatcher) {
        PrimaryDeviceInfo(
            identity = collectIdentity(
                serialNumber = null,
                serialAccess = SerialAccess.LOADING,
            ),
            system = collectSystem(),
            memory = collectMemory(),
            display = collectDisplay(),
        )
    }

    suspend fun collectSerialNumber(): SerialNumberResult = withContext(ioDispatcher) {
        val (serialNumber, serialAccess) = readSerialNumber()
        SerialNumberResult(serialNumber = serialNumber, serialAccess = serialAccess)
    }

    suspend fun collectCpuDetails(): CpuDetails = withContext(ioDispatcher) {
        collectCpu()
    }

    suspend fun collectStorageDetails(): List<StorageDetails> = withContext(ioDispatcher) {
        collectStorage()
    }

    suspend fun collectNetworkDetails(): NetworkDetails = withContext(ioDispatcher) {
        collectNetwork()
    }

    suspend fun collectBatteryDetails(): BatteryDetails = withContext(ioDispatcher) {
        collectBattery()
    }

    suspend fun collectCapabilityDetails(): CapabilityDetails = withContext(ioDispatcher) {
        collectCapabilities()
    }

    // These identifiers are shown locally on an explicit diagnostics screen and are never persisted.
    @SuppressLint("HardwareIds")
    private fun collectIdentity(
        serialNumber: String?,
        serialAccess: SerialAccess,
    ): DeviceIdentity = DeviceIdentity(
        manufacturer = Build.MANUFACTURER.orUnknown(),
        brand = Build.BRAND.orUnknown(),
        model = Build.MODEL.orUnknown(),
        product = Build.PRODUCT.orUnknown(),
        device = Build.DEVICE.orUnknown(),
        board = Build.BOARD.orUnknown(),
        hardware = Build.HARDWARE.orUnknown(),
        serialNumber = serialNumber,
        serialAccess = serialAccess,
        androidId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()?.takeUnless { it.isBlank() },
    )

    @Suppress("DEPRECATION")
    @SuppressLint("HardwareIds", "MissingPermission")
    private suspend fun readSerialNumber(): Pair<String?, SerialAccess> {
        val platformResult = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Build.SERIAL.validSerialNumber() to SerialAccess.UNAVAILABLE
        } else {
            try {
                Build.getSerial().validSerialNumber() to SerialAccess.UNAVAILABLE
            } catch (_: SecurityException) {
                null to SerialAccess.RESTRICTED
            }
        }

        platformResult.first?.let { serial ->
            return serial to SerialAccess.AVAILABLE
        }

        val propertySerial = systemPropertyReader
            .getFirst(SERIAL_NUMBER_PROPERTIES)
            .validSerialNumber()
        return if (propertySerial != null) {
            propertySerial to SerialAccess.AVAILABLE
        } else {
            null to platformResult.second
        }
    }

    @Suppress("DEPRECATION")
    private fun collectSystem(): SystemDetails {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appInfo = context.applicationInfo
        return SystemDetails(
            androidRelease = Build.VERSION.RELEASE.orUnknown(),
            apiLevel = Build.VERSION.SDK_INT,
            codename = Build.VERSION.CODENAME.takeUnless { it.isBlank() || it == "REL" },
            securityPatch = Build.VERSION.SECURITY_PATCH.takeUnless { it.isBlank() },
            buildId = Build.ID.orUnknown(),
            buildNumber = Build.DISPLAY.orUnknown(),
            buildType = Build.TYPE.orUnknown(),
            bootloader = Build.BOOTLOADER.takeUnless { it.isBlank() || it == Build.UNKNOWN },
            baseband = Build.getRadioVersion()?.takeUnless { it.isBlank() || it == Build.UNKNOWN },
            kernelVersion = readFirstLine("/proc/sys/kernel/osrelease")
                ?: System.getProperty("os.version").orUnknown(),
            javaVmVersion = System.getProperty("java.vm.version")?.takeUnless { it.isBlank() },
            appVersionName = packageInfo.versionName.orUnknown(),
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            },
            minSdk = appInfo.minSdkVersion,
            targetSdk = appInfo.targetSdkVersion,
            uptimeMillis = SystemClock.elapsedRealtime(),
        )
    }

    private fun collectCpu(): CpuDetails {
        val presentCoreCount = parseCpuRange(readFirstLine("/sys/devices/system/cpu/present"))
        val onlineCoreCount = parseCpuRange(readFirstLine("/sys/devices/system/cpu/online"))
        val coreCount = (presentCoreCount ?: Runtime.getRuntime().availableProcessors()).coerceAtLeast(1)
        val onlineCount = (onlineCoreCount ?: Runtime.getRuntime().availableProcessors())
            .coerceIn(1, coreCount)

        val minFrequencies = mutableListOf<Long>()
        val maxFrequencies = mutableListOf<Long>()
        val currentFrequencies = mutableListOf<Long>()
        repeat(coreCount) { core ->
            readFrequency(core, "cpuinfo_min_freq", "scaling_min_freq")?.let(minFrequencies::add)
            readFrequency(core, "cpuinfo_max_freq", "scaling_max_freq")?.let(maxFrequencies::add)
            readFrequency(core, "scaling_cur_freq", "cpuinfo_cur_freq")?.let(currentFrequencies::add)
        }

        val cpuInfo = readText("/proc/cpuinfo")
        val properties = cpuInfo.lineSequence()
            .mapNotNull { line ->
                val delimiter = line.indexOf(':')
                if (delimiter <= 0) null else {
                    line.substring(0, delimiter).trim().lowercase() to line.substring(delimiter + 1).trim()
                }
            }
            .filter { it.second.isNotBlank() }
            .toList()
        val model = listOf("model name", "hardware", "processor")
            .firstNotNullOfOrNull { key -> properties.firstOrNull { it.first == key }?.second }
        val features = listOf("features", "flags")
            .firstNotNullOfOrNull { key -> properties.firstOrNull { it.first == key }?.second }
            ?.split(Regex("\\s+"))
            ?.filter(String::isNotBlank)
            ?.distinct()
            .orEmpty()

        return CpuDetails(
            model = model,
            socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MANUFACTURER.takeUnless { it.isBlank() || it == Build.UNKNOWN }
            } else {
                null
            },
            socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL.takeUnless { it.isBlank() || it == Build.UNKNOWN }
            } else {
                null
            },
            architecture = System.getProperty("os.arch")?.takeUnless { it.isBlank() }
                ?: Build.SUPPORTED_ABIS.firstOrNull().orUnknown(),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            coreCount = coreCount,
            onlineCoreCount = onlineCount,
            minFrequencyKhz = minFrequencies.minOrNull(),
            maxFrequencyKhz = maxFrequencies.maxOrNull(),
            currentFrequencyKhz = currentFrequencies.takeIf { it.isNotEmpty() }?.average()?.toLong(),
            aggregateMaxFrequencyKhz = maxFrequencies.takeIf { it.isNotEmpty() }?.sum(),
            clusterMaxFrequenciesKhz = maxFrequencies.groupingBy { it }.eachCount().toSortedMap(),
            features = features,
        )
    }

    private fun readFrequency(core: Int, vararg names: String): Long? = names.firstNotNullOfOrNull { name ->
        readFirstLine("/sys/devices/system/cpu/cpu$core/cpufreq/$name")?.toLongOrNull()
    }?.takeIf { it > 0L }

    private fun collectMemory(): MemoryDetails {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val info = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        val memInfo = readKeyValueFile("/proc/meminfo")
        return MemoryDetails(
            totalBytes = info.totalMem,
            availableBytes = info.availMem,
            thresholdBytes = info.threshold,
            isLowMemory = info.lowMemory,
            swapTotalBytes = memInfo["SwapTotal"]?.kilobytesToBytes(),
            swapFreeBytes = memInfo["SwapFree"]?.kilobytesToBytes(),
            appHeapLimitBytes = activityManager.memoryClass.toLong() * BYTES_PER_MIB,
            ramType = null,
        )
    }

    private fun collectStorage(): List<StorageDetails> {
        val items = mutableListOf<StorageDetails>()
        addStorage(items, Environment.getDataDirectory(), StorageKind.INTERNAL, isRemovable = false)

        @Suppress("DEPRECATION")
        val sharedDirectory = Environment.getExternalStorageDirectory()
        addStorage(items, sharedDirectory, StorageKind.SHARED, Environment.isExternalStorageRemovable())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val storageManager = context.getSystemService(android.os.storage.StorageManager::class.java)
            storageManager.storageVolumes
                .asSequence()
                .filter { !it.isPrimary && it.directory != null }
                .forEach { volume ->
                    addStorage(
                        target = items,
                        directory = volume.directory!!,
                        kind = if (volume.isRemovable) StorageKind.REMOVABLE else StorageKind.SHARED,
                        isRemovable = volume.isRemovable,
                    )
                }
        }
        return items
    }

    private fun addStorage(
        target: MutableList<StorageDetails>,
        directory: File,
        kind: StorageKind,
        isRemovable: Boolean,
    ) {
        runCatching {
            val stats = StatFs(directory.absolutePath)
            val total = stats.totalBytes
            val available = stats.availableBytes
            val duplicate = target.any {
                it.kind == kind && it.totalBytes == total && it.availableBytes == available
            }
            if (!duplicate) {
                target += StorageDetails(
                    kind = kind,
                    totalBytes = total,
                    availableBytes = available,
                    fileSystem = findFileSystem(directory),
                    isRemovable = isRemovable,
                )
            }
        }
    }

    private fun findFileSystem(directory: File): String? {
        val absolutePath = runCatching { directory.canonicalPath }.getOrDefault(directory.absolutePath)
        return readText("/proc/mounts").lineSequence()
            .mapNotNull { line ->
                val columns = line.split(' ')
                if (columns.size < 3) null else {
                    val mountPoint = columns[1]
                        .replace("\\040", " ")
                        .replace("\\011", "\t")
                    if (absolutePath == mountPoint || absolutePath.startsWith("$mountPoint/")) {
                        mountPoint to columns[2]
                    } else {
                        null
                    }
                }
            }
            .maxByOrNull { it.first.length }
            ?.second
    }

    private fun collectDisplay(): DisplayDetails {
        val metrics = context.resources.displayMetrics
        val display = context.getSystemService(DisplayManager::class.java)
            .getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display?.mode
        return DisplayDetails(
            widthPixels = mode?.physicalWidth ?: metrics.widthPixels,
            heightPixels = mode?.physicalHeight ?: metrics.heightPixels,
            densityDpi = metrics.densityDpi,
            density = metrics.density,
            scaledDensity = metrics.density * context.resources.configuration.fontScale,
            xDpi = metrics.xdpi,
            yDpi = metrics.ydpi,
            refreshRateHz = mode?.refreshRate?.takeIf { it > 0f },
        )
    }

    private fun collectNetwork(): NetworkDetails {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val network = manager.activeNetwork
        val capabilities = network?.let(manager::getNetworkCapabilities)
        val linkProperties = network?.let(manager::getLinkProperties)
        val transports = buildSet {
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) add(NetworkTransport.WIFI)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) add(NetworkTransport.ETHERNET)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) add(NetworkTransport.CELLULAR)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) add(NetworkTransport.VPN)
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true
            ) {
                add(NetworkTransport.BLUETOOTH)
            }
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_USB) == true
            ) {
                add(NetworkTransport.USB)
            }
            if (capabilities != null && isEmpty()) add(NetworkTransport.OTHER)
        }
        val linkAddresses = linkProperties?.linkAddresses
            ?.map { "${it.address.hostAddress}/${it.prefixLength}" }
            .orEmpty()
        val fallbackAddresses = if (linkAddresses.isEmpty()) localIpAddresses() else emptyList()
        return NetworkDetails(
            status = if (network != null && capabilities != null) {
                NetworkStatus.CONNECTED
            } else {
                NetworkStatus.DISCONNECTED
            },
            transports = transports,
            isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true,
            isMetered = manager.isActiveNetworkMetered,
            interfaceName = linkProperties?.interfaceName,
            ipAddresses = linkAddresses.ifEmpty { fallbackAddresses },
            gatewayAddresses = linkProperties?.routes
                ?.mapNotNull { it.gateway?.hostAddress }
                ?.distinct()
                .orEmpty(),
            dnsServers = linkProperties?.dnsServers
                ?.mapNotNull { it.hostAddress }
                ?.distinct()
                .orEmpty(),
            privateDnsServer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                linkProperties?.privateDnsServerName?.takeUnless { it.isBlank() }
            } else {
                null
            },
        )
    }

    private fun localIpAddresses(): List<String> = runCatching {
        NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            .asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filter { !it.isLoopbackAddress }
            .mapNotNull { it.hostAddress?.substringBefore('%') }
            .distinct()
            .toList()
    }.getOrDefault(emptyList())

    private fun collectBattery(): BatteryDetails {
        val manager = context.getSystemService(BatteryManager::class.java)
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        return BatteryDetails(
            levelPercent = if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else null,
            status = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
                BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
                BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
                else -> BatteryStatus.UNKNOWN
            },
            health = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
                BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVERVOLTAGE
                BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.FAILURE
                else -> BatteryHealth.UNKNOWN
            },
            powerSources = buildSet {
                if (plugged and BatteryManager.BATTERY_PLUGGED_AC != 0) add(PowerSource.AC)
                if (plugged and BatteryManager.BATTERY_PLUGGED_USB != 0) add(PowerSource.USB)
                if (plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0) add(PowerSource.WIRELESS)
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    plugged and BatteryManager.BATTERY_PLUGGED_DOCK != 0
                ) {
                    add(PowerSource.DOCK)
                }
            },
            temperatureCelsius = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                ?.takeUnless { it == Int.MIN_VALUE }
                ?.div(10f),
            voltageMillivolts = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                ?.takeIf { it >= 0 },
            capacityPercent = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                .takeIf { it in 0..100 },
            thermalStatus = collectThermalStatus(),
        )
    }

    private fun collectThermalStatus(): ThermalStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalStatus.UNAVAILABLE
        return when (context.getSystemService(PowerManager::class.java).currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.EMERGENCY
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.SHUTDOWN
            else -> ThermalStatus.UNAVAILABLE
        }
    }

    private fun collectCapabilities(): CapabilityDetails {
        val packageManager = context.packageManager
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val sensorNames = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL)
            .map { it.name }
            .filter(String::isNotBlank)
            .distinct()
            .sorted()
        val types = buildSet {
            when {
                packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE) -> add(DeviceType.AUTOMOTIVE)
                packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) -> add(DeviceType.TELEVISION)
                context.resources.configuration.smallestScreenWidthDp >= 600 -> add(DeviceType.TABLET)
                else -> add(DeviceType.PHONE)
            }
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_EMBEDDED)) add(DeviceType.EMBEDDED)
        }
        return CapabilityDetails(
            deviceTypes = types,
            cameraCount = runCatching {
                context.getSystemService(CameraManager::class.java).cameraIdList.size
            }.getOrDefault(0),
            sensorCount = sensorNames.size,
            sensorNames = sensorNames,
            hasBluetooth = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            hasNfc = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC),
            hasGps = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
        )
    }

    private fun readKeyValueFile(path: String): Map<String, Long> = readText(path)
        .lineSequence()
        .mapNotNull { line ->
            val delimiter = line.indexOf(':')
            if (delimiter <= 0) return@mapNotNull null
            val value = Regex("\\d+").find(line.substring(delimiter + 1))?.value?.toLongOrNull()
            value?.let { line.substring(0, delimiter).trim() to it }
        }
        .toMap()

    private fun readFirstLine(path: String): String? = runCatching {
        File(path).bufferedReader().use { it.readLine()?.trim() }
    }.getOrNull()?.takeUnless { it.isBlank() }

    private fun readText(path: String): String = runCatching { File(path).readText() }.getOrDefault("")

    private fun parseCpuRange(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        val count = value.split(',').sumOf { range ->
            val bounds = range.trim().split('-')
            when (bounds.size) {
                1 -> 1
                2 -> (bounds[1].toIntOrNull() ?: return null) -
                    (bounds[0].toIntOrNull() ?: return null) + 1
                else -> return null
            }
        }
        return count.takeIf { it > 0 }
    }

    private fun String?.orUnknown(): String = this?.takeUnless { it.isBlank() || it == Build.UNKNOWN }.orEmpty()

    private fun String?.validSerialNumber(): String? = this
        ?.trim()
        ?.takeUnless {
            it.isBlank() ||
                it.equals(Build.UNKNOWN, ignoreCase = true) ||
                it.equals("null", ignoreCase = true) ||
                it.equals("n/a", ignoreCase = true)
        }

    private fun Long.kilobytesToBytes(): Long = this * 1024L

    private companion object {
        const val BYTES_PER_MIB = 1024L * 1024L
        val SERIAL_NUMBER_PROPERTIES = listOf(
            "ro.serialno",
            "ro.boot.serialno",
            "ro.boot.hardware.serialno",
            "ro.vendor.serialno",
            "sys.serialnumber",
            "persist.sys.serialno",
            "ril.serialnumber",
        )
    }
}
