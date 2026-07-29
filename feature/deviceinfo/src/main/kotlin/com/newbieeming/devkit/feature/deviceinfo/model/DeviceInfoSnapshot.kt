package com.newbieeming.devkit.feature.deviceinfo.model

data class DeviceInfoSnapshot(
    val identity: DeviceIdentity,
    val system: SystemDetails,
    val cpu: CpuDetails,
    val memory: MemoryDetails,
    val storage: List<StorageDetails>,
    val display: DisplayDetails,
    val network: NetworkDetails,
    val battery: BatteryDetails,
    val capabilities: CapabilityDetails,
)

data class PrimaryDeviceInfo(
    val identity: DeviceIdentity,
    val system: SystemDetails,
    val memory: MemoryDetails,
    val display: DisplayDetails,
)

data class SerialNumberResult(
    val serialNumber: String?,
    val serialAccess: SerialAccess,
)

data class DeviceIdentity(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val product: String,
    val device: String,
    val board: String,
    val hardware: String,
    val serialNumber: String?,
    val serialAccess: SerialAccess,
    val androidId: String?,
)

enum class SerialAccess {
    LOADING,
    AVAILABLE,
    RESTRICTED,
    UNAVAILABLE,
}

data class SystemDetails(
    val androidRelease: String,
    val apiLevel: Int,
    val codename: String?,
    val securityPatch: String?,
    val buildId: String,
    val buildNumber: String,
    val buildType: String,
    val bootloader: String?,
    val baseband: String?,
    val kernelVersion: String,
    val javaVmVersion: String?,
    val appVersionName: String,
    val appVersionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val uptimeMillis: Long,
)

data class CpuDetails(
    val model: String?,
    val socManufacturer: String?,
    val socModel: String?,
    val architecture: String,
    val supportedAbis: List<String>,
    val coreCount: Int,
    val onlineCoreCount: Int,
    val minFrequencyKhz: Long?,
    val maxFrequencyKhz: Long?,
    val currentFrequencyKhz: Long?,
    val aggregateMaxFrequencyKhz: Long?,
    val clusterMaxFrequenciesKhz: Map<Long, Int>,
    val features: List<String>,
)

data class MemoryDetails(
    val totalBytes: Long,
    val availableBytes: Long,
    val thresholdBytes: Long,
    val isLowMemory: Boolean,
    val swapTotalBytes: Long?,
    val swapFreeBytes: Long?,
    val appHeapLimitBytes: Long,
    val ramType: String?,
)

data class StorageDetails(
    val kind: StorageKind,
    val totalBytes: Long,
    val availableBytes: Long,
    val fileSystem: String?,
    val isRemovable: Boolean,
)

enum class StorageKind {
    INTERNAL,
    SHARED,
    REMOVABLE,
}

data class DisplayDetails(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val density: Float,
    val scaledDensity: Float,
    val xDpi: Float,
    val yDpi: Float,
    val refreshRateHz: Float?,
)

data class NetworkDetails(
    val status: NetworkStatus,
    val transports: Set<NetworkTransport>,
    val isValidated: Boolean,
    val isMetered: Boolean,
    val interfaceName: String?,
    val ipAddresses: List<String>,
    val gatewayAddresses: List<String>,
    val dnsServers: List<String>,
    val privateDnsServer: String?,
)

enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
}

enum class NetworkTransport {
    WIFI,
    ETHERNET,
    CELLULAR,
    VPN,
    BLUETOOTH,
    USB,
    OTHER,
}

data class BatteryDetails(
    val levelPercent: Int?,
    val status: BatteryStatus,
    val health: BatteryHealth,
    val powerSources: Set<PowerSource>,
    val temperatureCelsius: Float?,
    val voltageMillivolts: Int?,
    val capacityPercent: Int?,
    val thermalStatus: ThermalStatus,
)

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN,
}

enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVERVOLTAGE,
    COLD,
    FAILURE,
    UNKNOWN,
}

enum class PowerSource {
    AC,
    USB,
    WIRELESS,
    DOCK,
}

enum class ThermalStatus {
    NONE,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
    EMERGENCY,
    SHUTDOWN,
    UNAVAILABLE,
}

data class CapabilityDetails(
    val deviceTypes: Set<DeviceType>,
    val cameraCount: Int,
    val sensorCount: Int,
    val sensorNames: List<String>,
    val hasBluetooth: Boolean,
    val hasNfc: Boolean,
    val hasGps: Boolean,
)

enum class DeviceType {
    AUTOMOTIVE,
    TELEVISION,
    TABLET,
    PHONE,
    EMBEDDED,
}
