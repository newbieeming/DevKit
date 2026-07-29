package com.newbieeming.devkit.feature.appmanager.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.feature.appmanager.data.InstalledAppsDataSource
import com.newbieeming.devkit.feature.appmanager.model.InstalledAppDetail
import com.newbieeming.devkit.feature.appmanager.navigation.PACKAGE_NAME_ARGUMENT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataSource: InstalledAppsDataSource,
) : ViewModel() {
    private val packageName = requireNotNull(savedStateHandle.get<String>(PACKAGE_NAME_ARGUMENT))
    private val _state = MutableStateFlow(AppDetailUiState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AppDetailUiState(isLoading = true)
            try {
                _state.value = AppDetailUiState(
                    app = dataSource.loadAppDetail(packageName),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = AppDetailUiState(loadFailed = true)
            }
        }
    }
}

data class AppDetailUiState(
    val app: InstalledAppDetail? = null,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
)
