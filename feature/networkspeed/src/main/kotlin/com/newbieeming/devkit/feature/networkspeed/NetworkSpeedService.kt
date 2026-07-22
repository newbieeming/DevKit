package com.newbieeming.devkit.feature.networkspeed

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import com.newbieeming.devkit.core.model.NetworkSpeedSnapshot
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.AbstractOverlayService
import com.newbieeming.devkit.core.ui.overlay.putOverlayConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetworkSpeedService : AbstractOverlayService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val speed = MutableStateFlow(NetworkSpeedSnapshot(0, 0, 0))
    private val options = MutableStateFlow(NetworkOverlayOptions())

    override val notificationId: Int = 1002
    override val defaultOverlayConfig: OverlayConfig = NETWORK_OVERLAY_DEFAULTS

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        _isServiceRunning.value = true
        observeNetworkSpeed()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        _isServiceRunning.value = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra(EXTRA_DISPLAY_MODE) == true) {
            options.value = NetworkOverlayOptions.fromIntentValue(
                displayMode = intent.getStringExtra(EXTRA_DISPLAY_MODE),
                indicatorStyle = intent.getStringExtra(EXTRA_INDICATOR_STYLE),
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun createServiceNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.network_notification_title))
            .setContentText(getString(R.string.network_notification_text))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    @Composable
    override fun OverlayContent(modifier: Modifier) {
        val snapshot = speed.collectAsState()
        val overlayOptions = options.collectAsState()
        NetworkSpeedOverlayContent(
            snapshot = snapshot.value,
            config = overlayConfig,
            options = overlayOptions.value,
            modifier = modifier,
        )
    }

    private fun observeNetworkSpeed() {
        serviceScope.launch {
            var previousRx = supportedBytes(TrafficStats.getTotalRxBytes())
            var previousTx = supportedBytes(TrafficStats.getTotalTxBytes())
            var previousTime = SystemClock.elapsedRealtime()
            while (isActive) {
                delay(UPDATE_INTERVAL_MS)
                val now = SystemClock.elapsedRealtime()
                val rx = supportedBytes(TrafficStats.getTotalRxBytes())
                val tx = supportedBytes(TrafficStats.getTotalTxBytes())
                val elapsedMs = (now - previousTime).coerceAtLeast(1L)
                speed.value = NetworkSpeedSnapshot(
                    rxBytesPerSec = ((rx - previousRx).coerceAtLeast(0L) * 1_000L) / elapsedMs,
                    txBytesPerSec = ((tx - previousTx).coerceAtLeast(0L) * 1_000L) / elapsedMs,
                    timestampMs = System.currentTimeMillis(),
                )
                previousRx = rx
                previousTx = tx
                previousTime = now
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.network_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "network_speed_channel"
        private val UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1)
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

        fun start(context: Context, config: OverlayConfig, options: NetworkOverlayOptions) {
            val intent = Intent(context, NetworkSpeedService::class.java)
                .putOverlayConfig(config)
                .putNetworkOptions(options)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, config: OverlayConfig, options: NetworkOverlayOptions) {
            val intent = Intent(context, NetworkSpeedService::class.java)
                .setAction(AbstractOverlayService.ACTION_UPDATE_CONFIG)
                .putOverlayConfig(config)
                .putNetworkOptions(options)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, NetworkSpeedService::class.java)
                .setAction(AbstractOverlayService.ACTION_STOP_SERVICE)
            context.startService(intent)
        }

        private fun supportedBytes(value: Long): Long =
            if (value == TrafficStats.UNSUPPORTED.toLong()) 0L else value.coerceAtLeast(0L)

        private const val EXTRA_DISPLAY_MODE = "devkit.network.DISPLAY_MODE"
        private const val EXTRA_INDICATOR_STYLE = "devkit.network.INDICATOR_STYLE"

        private fun Intent.putNetworkOptions(options: NetworkOverlayOptions): Intent = apply {
            putExtra(EXTRA_DISPLAY_MODE, options.displayMode.name)
            putExtra(EXTRA_INDICATOR_STYLE, options.indicatorStyle.name)
        }
    }
}
