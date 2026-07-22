package com.newbieeming.devkit.core.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayConfigurationScreen(
    title: String,
    config: OverlayConfig,
    isRunning: Boolean,
    onNavigateUp: () -> Unit,
    onSave: (OverlayConfig) -> Unit,
    onToggle: (OverlayConfig) -> Unit,
    additionalContent: @Composable () -> Unit = {},
) {
    var draft by remember(config) { mutableStateOf(config) }
    Scaffold(
        topBar = { OverlayConfigurationTopBar(title, onNavigateUp) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OverlayAppearanceSettings(
                config = draft,
                onConfigChange = { updated -> draft = updated },
            )

            additionalContent()

            OverlayConfigurationActions(
                isRunning = isRunning,
                onToggle = { onToggle(draft) },
                onSave = { onSave(draft) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlayConfigurationTopBar(
    title: String,
    onNavigateUp: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                )
            }
        },
    )
}

@Composable
private fun OverlayConfigurationActions(
    isRunning: Boolean,
    onToggle: () -> Unit,
    onSave: () -> Unit,
) {
    Spacer(modifier = Modifier.height(12.dp))
    FilledTonalButton(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = null,
        )
        Text(
            text = stringResource(if (isRunning) R.string.stop_overlay else R.string.start_overlay),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.save_configuration))
    }
}
