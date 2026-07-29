package com.newbieeming.devkit.feature.appmanager.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.core.common.system.PackageChangeMonitor
import com.newbieeming.devkit.feature.appmanager.data.InstalledAppsDataSource
import com.newbieeming.devkit.feature.appmanager.model.AppFilter
import com.newbieeming.devkit.feature.appmanager.model.InstalledAppSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppManagerViewModel @Inject constructor(
    private val dataSource: InstalledAppsDataSource,
    private val packageChangeMonitor: PackageChangeMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(AppManagerUiState(isLoading = true))
    val state = _state.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            packageChangeMonitor.observePackageChanges().collect {
                refreshJob?.join()
                refresh()
            }
        }
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = it.apps.isEmpty(), loadFailed = false) }
            try {
                val apps = dataSource.loadInstalledApps()
                _state.update { state ->
                    state.copy(
                        apps = apps,
                        selectedPackages = state.selectedPackages.intersect(
                            apps.asSequence()
                                .filter(InstalledAppSummary::canRequestUninstall)
                                .map(InstalledAppSummary::packageName)
                                .toSet(),
                        ),
                        isLoading = false,
                    ).withFilteredApps()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false, loadFailed = true) }
            }
        }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query).withFilteredApps() }
    }

    fun setFilter(filter: AppFilter) {
        _state.update { it.copy(filter = filter).withFilteredApps() }
    }

    fun toggleSelection(packageName: String) {
        _state.update { state ->
            val app = state.apps.firstOrNull { it.packageName == packageName }
            if (app?.canRequestUninstall != true) return@update state
            val selected = state.selectedPackages.toMutableSet().apply {
                if (!add(packageName)) remove(packageName)
            }
            state.copy(selectedPackages = selected)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedPackages = emptySet()) }
    }

    private fun AppManagerUiState.withFilteredApps(): AppManagerUiState {
        val normalizedQuery = query.trim()
        return copy(
            filteredApps = apps.filter { app ->
                val matchesFilter = when (filter) {
                    AppFilter.ALL -> true
                    AppFilter.SYSTEM -> app.isSystemApp
                    AppFilter.THIRD_PARTY -> !app.isSystemApp
                }
                val matchesQuery = normalizedQuery.isEmpty() ||
                    app.label.contains(normalizedQuery, ignoreCase = true) ||
                    app.packageName.contains(normalizedQuery, ignoreCase = true)
                matchesFilter && matchesQuery
            },
        )
    }
}

data class AppManagerUiState(
    val apps: List<InstalledAppSummary> = emptyList(),
    val filteredApps: List<InstalledAppSummary> = emptyList(),
    val query: String = "",
    val filter: AppFilter = AppFilter.ALL,
    val selectedPackages: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
)
