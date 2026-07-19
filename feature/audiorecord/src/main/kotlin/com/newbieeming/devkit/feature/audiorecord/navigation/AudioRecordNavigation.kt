package com.newbieeming.devkit.feature.audiorecord.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val AUDIO_RECORD_ROUTE = "audio_record"

fun NavController.navigateToAudioRecord() = navigate(AUDIO_RECORD_ROUTE)

fun NavGraphBuilder.audioRecordScreen() {
    composable(route = AUDIO_RECORD_ROUTE) {
        // TODO: AudioRecordScreen()
    }
}
