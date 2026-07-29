package com.newbieeming.devkit.feature.deviceinfo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.feature.deviceinfo.data.DeviceInfoCollector
import com.newbieeming.devkit.feature.deviceinfo.model.BatteryDetails
import com.newbieeming.devkit.feature.deviceinfo.model.CapabilityDetails
import com.newbieeming.devkit.feature.deviceinfo.model.CpuDetails
import com.newbieeming.devkit.feature.deviceinfo.model.DeviceIdentity
import com.newbieeming.devkit.feature.deviceinfo.model.DisplayDetails
import com.newbieeming.devkit.feature.deviceinfo.model.MemoryDetails
import com.newbieeming.devkit.feature.deviceinfo.model.NetworkDetails
import com.newbieeming.devkit.feature.deviceinfo.model.SerialAccess
import com.newbieeming.devkit.feature.deviceinfo.model.StorageDetails
import com.newbieeming.devkit.feature.deviceinfo.model.SystemDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val collector: DeviceInfoCollector,
) : ViewModel() {
    private val _state = MutableStateFlow(DeviceInfoUiState(isLoading = true))
    val state = _state.asStateFlow()

    private var refreshJob: Job? = null

    fun refresh() {
        if (refreshJob?.isActive == true) return
        _state.value = DeviceInfoUiState(isLoading = true)
        refreshJob = viewModelScope.launch {
            val primary = try {
                collector.collectPrimary()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false, loadFailed = true) }
                return@launch
            }

            _state.update {
                it.copy(
                    identity = primary.identity,
                    system = primary.system,
                    memory = primary.memory,
                    display = primary.display,
                )
            }

            coroutineScope {
                launch { loadSerialNumber() }
                launch { loadCpu() }
                launch { loadStorage() }
                launch { loadNetwork() }
                launch { loadBattery() }
                launch { loadCapabilities() }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadSerialNumber() {
        try {
            val result = collector.collectSerialNumber()
            _state.update { state ->
                state.copy(
                    identity = state.identity?.copy(
                        serialNumber = result.serialNumber,
                        serialAccess = result.serialAccess,
                    ),
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _state.update { state ->
                state.copy(
                    identity = state.identity?.copy(
                        serialNumber = null,
                        serialAccess = SerialAccess.UNAVAILABLE,
                    ),
                )
            }
        }
    }

    private suspend fun loadCpu() {
        loadSection(
            loader = collector::collectCpuDetails,
            update = { state, value -> state.copy(cpu = value) },
        )
    }

    private suspend fun loadStorage() {
        loadSection(
            loader = collector::collectStorageDetails,
            update = { state, value -> state.copy(storage = value) },
        )
    }

    private suspend fun loadNetwork() {
        loadSection(
            loader = collector::collectNetworkDetails,
            update = { state, value -> state.copy(network = value) },
        )
    }

    private suspend fun loadBattery() {
        loadSection(
            loader = collector::collectBatteryDetails,
            update = { state, value -> state.copy(battery = value) },
        )
    }

    private suspend fun loadCapabilities() {
        loadSection(
            loader = collector::collectCapabilityDetails,
            update = { state, value -> state.copy(capabilities = value) },
        )
    }

    private suspend fun <T> loadSection(
        loader: suspend () -> T,
        update: (DeviceInfoUiState, T) -> DeviceInfoUiState,
    ) {
        try {
            val value = loader()
            _state.update { state -> update(state, value) }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // The remaining sections still load independently.
        }
    }
}

data class DeviceInfoUiState(
    val identity: DeviceIdentity? = null,
    val system: SystemDetails? = null,
    val cpu: CpuDetails? = null,
    val memory: MemoryDetails? = null,
    val storage: List<StorageDetails>? = null,
    val display: DisplayDetails? = null,
    val network: NetworkDetails? = null,
    val battery: BatteryDetails? = null,
    val capabilities: CapabilityDetails? = null,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
)
