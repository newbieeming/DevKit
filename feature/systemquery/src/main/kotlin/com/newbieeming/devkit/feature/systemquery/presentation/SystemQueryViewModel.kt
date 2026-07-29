package com.newbieeming.devkit.feature.systemquery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.devkit.feature.systemquery.data.QueryHistoryRepository
import com.newbieeming.devkit.feature.systemquery.data.SystemValueDataSource
import com.newbieeming.devkit.feature.systemquery.model.QueryScope
import com.newbieeming.devkit.feature.systemquery.model.QuerySection
import com.newbieeming.devkit.feature.systemquery.model.ValueSource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.PatternSyntaxException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SystemQueryViewModel @Inject constructor(
    private val dataSource: SystemValueDataSource,
    private val historyRepository: QueryHistoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SystemQueryUiState())
    val state = _state.asStateFlow()
    private var queryJob: Job? = null

    init {
        viewModelScope.launch {
            historyRepository.history.collect { history ->
                _state.update { state -> state.copy(history = history) }
            }
        }
    }

    fun setQuery(query: String) {
        _state.update {
            it.copy(
                query = query.take(MAX_QUERY_LENGTH),
                error = null,
            )
        }
    }

    fun selectScope(scope: QueryScope) {
        _state.update { it.copy(scope = scope) }
    }

    fun selectHistory(query: String) {
        setQuery(query)
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clear()
        }
    }

    fun query() {
        val pattern = _state.value.query.trim()
        val scope = _state.value.scope
        if (pattern.isEmpty()) {
            _state.update { it.copy(error = QueryError.EMPTY_QUERY) }
            return
        }

        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    query = pattern,
                    isLoading = true,
                    hasSearched = true,
                    sections = emptyList(),
                    unavailableSources = emptySet(),
                    error = null,
                )
            }
            try {
                val result = dataSource.query(pattern, scope)
                historyRepository.remember(pattern)
                _state.update {
                    it.copy(
                        isLoading = false,
                        sections = result.sections,
                        unavailableSources = result.unavailableSources,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: PatternSyntaxException) {
                _state.update {
                    it.copy(isLoading = false, error = QueryError.INVALID_REGEX)
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = QueryError.QUERY_FAILED)
                }
            }
        }
    }

    private companion object {
        const val MAX_QUERY_LENGTH = 256
    }
}

data class SystemQueryUiState(
    val query: String = "",
    val scope: QueryScope = QueryScope.ALL,
    val sections: List<QuerySection> = emptyList(),
    val unavailableSources: Set<ValueSource> = emptySet(),
    val history: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: QueryError? = null,
)

enum class QueryError {
    EMPTY_QUERY,
    INVALID_REGEX,
    QUERY_FAILED,
}
