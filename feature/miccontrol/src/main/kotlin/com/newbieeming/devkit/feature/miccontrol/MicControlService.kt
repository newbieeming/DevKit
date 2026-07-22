package com.newbieeming.devkit.feature.miccontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.AbstractOverlayService
import com.newbieeming.devkit.core.ui.overlay.putOverlayConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MicControlService : AbstractOverlayService() {

    private lateinit var audioManager:AudioManager
    private val isMicrophoneMute = MutableStateFlow(true)

    override val notificationId: Int = 1001

    override val defaultOverlayConfig: OverlayConfig = MIC_OVERLAY_DEFAULTS
    override val isDraggable: Boolean = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        isMicrophoneMute.update { audioManager.isMicrophoneMute }
        _isServiceRunning.value = true
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        super.onDestroy()
    }

    override fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.mic_notification_title))
            .setContentText(getString(R.string.mic_notification_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @Composable
    override fun OverlayContent(modifier: Modifier) {
        val isMuted = isMicrophoneMute.collectAsState()
        // We use the modifier passed by AbstractOverlayService which contains the drag logic
        OverlayContent(
            isMuted = isMuted.value,
            config = overlayConfig,
            modifier = modifier,
        ) {
            Log.d("OverlayContent","value: ${audioManager.isMicrophoneMute}")
            val newState = !isMicrophoneMute.value
            audioManager.isMicrophoneMute = newState
            isMicrophoneMute.value = newState
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.mic_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "mic_control_channel"
        const val ACTION_STOP_SERVICE = AbstractOverlayService.ACTION_STOP_SERVICE

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: kotlinx.coroutines.flow.StateFlow<Boolean> = _isServiceRunning

        fun start(context: Context, config: OverlayConfig) {
            val intent = Intent(context, MicControlService::class.java).putOverlayConfig(config)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, config: OverlayConfig) {
            val intent = Intent(context, MicControlService::class.java)
                .setAction(AbstractOverlayService.ACTION_UPDATE_CONFIG)
                .putOverlayConfig(config)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MicControlService::class.java)
            intent.action = ACTION_STOP_SERVICE
            context.startService(intent)
        }
    }
}
