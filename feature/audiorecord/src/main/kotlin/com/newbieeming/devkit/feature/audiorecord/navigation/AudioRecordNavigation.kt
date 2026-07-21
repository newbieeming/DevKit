package com.newbieeming.devkit.feature.audiorecord.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.newbieeming.devkit.feature.audiorecord.ui.screens.AudioRecordScreen

const val AUDIO_RECORD_ROUTE = "audio_record"

fun NavGraphBuilder.audioRecordScreen(onNavigateUp: () -> Unit) {
    composable(route = AUDIO_RECORD_ROUTE) {
        AudioRecordScreen(onNavigateUp = onNavigateUp)
    }
}
