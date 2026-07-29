package com.newbieeming.devkit.feature.systemquery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newbieeming.devkit.feature.systemquery.R
import com.newbieeming.devkit.feature.systemquery.model.QueryScope
import com.newbieeming.devkit.feature.systemquery.model.QuerySection
import com.newbieeming.devkit.feature.systemquery.model.SystemValue
import com.newbieeming.devkit.feature.systemquery.model.ValueSource
import com.newbieeming.devkit.feature.systemquery.presentation.QueryError
import com.newbieeming.devkit.feature.systemquery.presentation.SystemQueryUiState
import com.newbieeming.devkit.feature.systemquery.presentation.SystemQueryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemQueryScreen(
    onNavigateUp: () -> Unit,
    viewModel: SystemQueryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.system_query_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            QueryContent(
                state = state,
                onQueryChange = viewModel::setQuery,
                onScopeSelected = viewModel::selectScope,
                onSearch = viewModel::query,
                onHistorySelected = viewModel::selectHistory,
                onClearHistory = viewModel::clearHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1_100.dp),
            )
        }
    }
}

@Composable
private fun QueryContent(
    state: SystemQueryUiState,
    onQueryChange: (String) -> Unit,
    onScopeSelected: (QueryScope) -> Unit,
    onSearch: () -> Unit,
    onHistorySelected: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourceLabels = mapOf(
        ValueSource.SYSTEM_PROPERTIES to stringResource(R.string.source_system_properties),
        ValueSource.SETTINGS_GLOBAL to stringResource(R.string.source_settings_global),
        ValueSource.SETTINGS_SYSTEM to stringResource(R.string.source_settings_system),
        ValueSource.SETTINGS_SECURE to stringResource(R.string.source_settings_secure),
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "query_controls") {
            QueryControls(
                state = state,
                onQueryChange = onQueryChange,
                onScopeSelected = onScopeSelected,
                onSearch = onSearch,
            )
        }

        if (state.history.isNotEmpty()) {
            item(key = "history") {
                QueryHistory(
                    history = state.history,
                    onSelected = onHistorySelected,
                    onClear = onClearHistory,
                )
            }
        }

        if (state.isLoading) {
            item(key = "loading") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        state.error?.let { error ->
            item(key = "error") {
                MessageCard(message = queryErrorText(error))
            }
        }

        if (state.unavailableSources.isNotEmpty()) {
            item(key = "unavailable_sources") {
                MessageCard(
                    message = stringResource(
                        R.string.query_sources_unavailable,
                        state.unavailableSources.joinToString { source ->
                            sourceLabels.getValue(source)
                        },
                    ),
                )
            }
        }

        if (
            state.hasSearched &&
            !state.isLoading &&
            state.error == null &&
            state.sections.isEmpty()
        ) {
            item(key = "no_results") {
                MessageCard(message = stringResource(R.string.no_query_results))
            }
        }

        state.sections.forEach { section ->
            item(key = "header_${section.source.name}") {
                ResultSectionHeader(section)
            }
            items(
                items = section.values,
                key = { value -> "${section.source.name}:${value.key}" },
            ) { value ->
                SystemValueRow(value)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryControls(
    state: SystemQueryUiState,
    onQueryChange: (String) -> Unit,
    onScopeSelected: (QueryScope) -> Unit,
    onSearch: () -> Unit,
) {
    val queryFocusRequester = remember { FocusRequester() }
    var queryFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = state.query,
                selection = TextRange(state.query.length),
            ),
        )
    }

    LaunchedEffect(state.query) {
        if (state.query != queryFieldValue.text) {
            queryFieldValue = TextFieldValue(
                text = state.query,
                selection = TextRange(state.query.length),
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = queryFieldValue,
                onValueChange = { value ->
                    queryFieldValue = value
                    onQueryChange(value.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(queryFocusRequester),
                label = { Text(stringResource(R.string.regex_query)) },
                supportingText = { Text(stringResource(R.string.regex_query_hint)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                queryFieldValue = TextFieldValue("")
                                onQueryChange("")
                                queryFocusRequester.requestFocus()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_query),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            )

            Text(
                text = stringResource(R.string.quick_input),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                shortcutValues().forEach { shortcut ->
                    SuggestionChip(
                        onClick = {
                            val selection = queryFieldValue.selection
                            val updatedText = queryFieldValue.text.replaceRange(
                                startIndex = selection.min,
                                endIndex = selection.max,
                                replacement = shortcut,
                            )
                            val cursorPosition = selection.min + shortcut.length
                            queryFieldValue = TextFieldValue(
                                text = updatedText,
                                selection = TextRange(cursorPosition),
                            )
                            onQueryChange(updatedText)
                            queryFocusRequester.requestFocus()
                        },
                        label = { Text(shortcut, fontFamily = FontFamily.Monospace) },
                    )
                }
            }

            QueryScopeDropdown(
                selected = state.scope,
                onSelected = onScopeSelected,
            )

            Button(
                onClick = onSearch,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Text(
                    text = stringResource(R.string.run_query),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryScopeDropdown(
    selected: QueryScope,
    onSelected: (QueryScope) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = scopeLabel(selected),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                ),
            readOnly = true,
            label = { Text(stringResource(R.string.query_scope)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            QueryScope.entries.forEach { scope ->
                DropdownMenuItem(
                    text = { Text(scopeLabel(scope)) },
                    onClick = {
                        onSelected(scope)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun QueryHistory(
    history: List<String>,
    onSelected: (String) -> Unit,
    onClear: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.query_history),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                )
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = stringResource(R.string.clear_query_history),
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                history.forEach { query ->
                    SuggestionChip(
                        onClick = { onSelected(query) },
                        label = {
                            Text(
                                text = query,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = FontFamily.Monospace,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultSectionHeader(section: QuerySection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = sourceLabel(section.source),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.query_result_count, section.values.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SystemValueRow(value: SystemValue) {
    Column {
        ListItem(
            headlineContent = {
                SelectionContainer {
                    Text(
                        text = value.key,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            supportingContent = {
                SelectionContainer {
                    Text(
                        text = value.value.ifEmpty { stringResource(R.string.empty_value) },
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun MessageCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun queryErrorText(error: QueryError): String = stringResource(
    when (error) {
        QueryError.EMPTY_QUERY -> R.string.query_error_empty
        QueryError.INVALID_REGEX -> R.string.query_error_invalid_regex
        QueryError.QUERY_FAILED -> R.string.query_error_failed
    },
)

@Composable
private fun scopeLabel(scope: QueryScope): String = stringResource(
    when (scope) {
        QueryScope.ALL -> R.string.query_scope_all
        QueryScope.SYSTEM_PROPERTIES -> R.string.source_system_properties
        QueryScope.SETTINGS_GLOBAL -> R.string.source_settings_global
        QueryScope.SETTINGS_SYSTEM -> R.string.source_settings_system
        QueryScope.SETTINGS_SECURE -> R.string.source_settings_secure
    },
)

@Composable
private fun sourceLabel(source: ValueSource): String = stringResource(
    when (source) {
        ValueSource.SYSTEM_PROPERTIES -> R.string.source_system_properties
        ValueSource.SETTINGS_GLOBAL -> R.string.source_settings_global
        ValueSource.SETTINGS_SYSTEM -> R.string.source_settings_system
        ValueSource.SETTINGS_SECURE -> R.string.source_settings_secure
    },
)

@Composable
private fun shortcutValues(): List<String> = listOf(
    stringResource(R.string.query_shortcut_persist),
    stringResource(R.string.query_shortcut_ro),
    stringResource(R.string.query_shortcut_system),
    stringResource(R.string.query_shortcut_dot),
    stringResource(R.string.query_shortcut_or),
    stringResource(R.string.query_shortcut_underscore),
)
