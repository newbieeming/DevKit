package com.newbieeming.devkit.feature.miccontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import com.newbieeming.devkit.core.ui.overlay.AbstractOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.update

class MicControlService : AbstractOverlayService() {

    private lateinit var audioManager:AudioManager
    private val isMicrophoneMute = MutableStateFlow(true)

    override val notificationId: Int = 1001

    // We can override default position if needed
    override val startX: Int = 100
    override val startY: Int = 200
    override val layoutGravity: Int = Gravity.TOP or Gravity.START
    override val isDraggable: Boolean = true

    override fun onCreate() {
        createNotificationChannel()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        isMicrophoneMute.update { audioManager.isMicrophoneMute }
        _isServiceRunning.value = true
        super.onCreate()
    }

    override fun onDestroy() {
        _isServiceRunning.value = false
        super.onDestroy()
    }

    override fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mic Control")
            .setContentText("Floating microphone control is active")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // placeholder icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @Composable
    override fun OverlayContent(modifier: Modifier) {
        val isMuted = isMicrophoneMute.collectAsState()
        // We use the modifier passed by AbstractOverlayService which contains the drag logic
        OverlayContent(
            isMuted = isMuted.value,
            modifier = modifier
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
                "Mic Control Service",
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

        fun start(context: Context) {
            val intent = Intent(context, MicControlService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MicControlService::class.java)
            intent.action = ACTION_STOP_SERVICE
            context.startService(intent)
        }
    }
}
