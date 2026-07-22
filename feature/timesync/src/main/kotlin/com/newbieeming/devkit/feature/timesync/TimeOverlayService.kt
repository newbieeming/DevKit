package com.newbieeming.devkit.feature.timesync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.AbstractOverlayService
import com.newbieeming.devkit.core.ui.overlay.putOverlayConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class TimeOverlayService : AbstractOverlayService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val currentTime = MutableStateFlow("")
    private val options = MutableStateFlow(TimeOverlayOptions())
    private val formatters = mutableMapOf<String, SimpleDateFormat>()

    override val notificationId: Int = 1003
    override val defaultOverlayConfig: OverlayConfig = TIME_OVERLAY_DEFAULTS

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        _isServiceRunning.value = true
        observeCurrentTime()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        _isServiceRunning.value = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra(EXTRA_TIME_FORMAT) == true) {
            options.value = TimeOverlayOptions.fromIntentValue(intent.getStringExtra(EXTRA_TIME_FORMAT))
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun createServiceNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.time_notification_title))
            .setContentText(getString(R.string.time_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    @Composable
    override fun OverlayContent(modifier: Modifier) {
        val time = currentTime.collectAsState()
        TimeOverlayContent(
            time = time.value,
            config = overlayConfig,
            modifier = modifier,
        )
    }

    private fun observeCurrentTime() {
        serviceScope.launch {
            while (isActive) {
                currentTime.value = formatCurrentTime()
                delay(1_000L.milliseconds)
            }
        }
    }

    private fun formatCurrentTime(): String {
        val pattern = options.value.format.resolvePattern(
            systemUses24Hour = DateFormat.is24HourFormat(this),
        )
        val formatter = formatters.getOrPut(pattern) { SimpleDateFormat(pattern, Locale.getDefault()) }
        return formatter.format(Date())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.time_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "time_overlay_channel"
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

        fun start(context: Context, config: OverlayConfig, options: TimeOverlayOptions) {
            val intent = Intent(context, TimeOverlayService::class.java)
                .putOverlayConfig(config)
                .putTimeOptions(options)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, config: OverlayConfig, options: TimeOverlayOptions) {
            val intent = Intent(context, TimeOverlayService::class.java)
                .setAction(AbstractOverlayService.ACTION_UPDATE_CONFIG)
                .putOverlayConfig(config)
                .putTimeOptions(options)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimeOverlayService::class.java)
                .setAction(AbstractOverlayService.ACTION_STOP_SERVICE)
            context.startService(intent)
        }

        private const val EXTRA_TIME_FORMAT = "devkit.time.TIME_FORMAT"

        private fun Intent.putTimeOptions(options: TimeOverlayOptions): Intent = apply {
            putExtra(EXTRA_TIME_FORMAT, options.format.name)
        }
    }
}
