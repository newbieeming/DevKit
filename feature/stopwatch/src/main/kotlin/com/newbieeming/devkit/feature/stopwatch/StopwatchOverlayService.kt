package com.newbieeming.devkit.feature.stopwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import com.newbieeming.devkit.core.model.OverlayConfig
import com.newbieeming.devkit.core.ui.overlay.AbstractOverlayService
import com.newbieeming.devkit.core.ui.overlay.putOverlayConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopwatchOverlayService : AbstractOverlayService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var accumulatedMillis = 0L
    private var startedAtElapsedRealtime = 0L

    override val notificationId: Int = 1004
    override val defaultOverlayConfig: OverlayConfig = STOPWATCH_OVERLAY_DEFAULTS

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        _isServiceRunning.value = true
        updateElapsedTime()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> startOrResumeTimer()
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESET_TIMER -> resetTimer()
        }
        if (intent?.getBooleanExtra(EXTRA_CONTROL_ONLY, false) == true) {
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        accumulatedMillis = 0L
        _timerState.value = StopwatchState()
        _isServiceRunning.value = false
        super.onDestroy()
    }

    override fun createServiceNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.stopwatch_notification_title))
            .setContentText(getString(R.string.stopwatch_notification_text))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    @Composable
    override fun OverlayContent(modifier: Modifier) {
        val state = timerState.collectAsState()
        StopwatchOverlayContent(
            state = state.value,
            config = overlayConfig,
            onStartOrResume = { sendTimerAction(this, ACTION_START_TIMER) },
            onPause = { sendTimerAction(this, ACTION_PAUSE_TIMER) },
            onReset = { sendTimerAction(this, ACTION_RESET_TIMER) },
            modifier = modifier,
        )
    }

    private fun startOrResumeTimer() {
        if (_timerState.value.status == StopwatchStatus.RUNNING) return
        startedAtElapsedRealtime = SystemClock.elapsedRealtime()
        _timerState.value = StopwatchState(
            elapsedMillis = accumulatedMillis,
            status = StopwatchStatus.RUNNING,
        )
    }

    private fun pauseTimer() {
        if (_timerState.value.status != StopwatchStatus.RUNNING) return
        accumulatedMillis = currentElapsedMillis()
        _timerState.value = StopwatchState(
            elapsedMillis = accumulatedMillis,
            status = StopwatchStatus.PAUSED,
        )
    }

    private fun resetTimer() {
        accumulatedMillis = 0L
        startedAtElapsedRealtime = 0L
        _timerState.value = StopwatchState()
    }

    private fun updateElapsedTime() {
        serviceScope.launch {
            while (isActive) {
                if (_timerState.value.status == StopwatchStatus.RUNNING) {
                    _timerState.value = StopwatchState(
                        elapsedMillis = currentElapsedMillis(),
                        status = StopwatchStatus.RUNNING,
                    )
                }
                delay(TIMER_UPDATE_INTERVAL_MILLIS)
            }
        }
    }

    private fun currentElapsedMillis(): Long =
        accumulatedMillis + (SystemClock.elapsedRealtime() - startedAtElapsedRealtime)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.stopwatch_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "stopwatch_overlay_channel"
        private const val ACTION_START_TIMER = "devkit.stopwatch.START_TIMER"
        private const val ACTION_PAUSE_TIMER = "devkit.stopwatch.PAUSE_TIMER"
        private const val ACTION_RESET_TIMER = "devkit.stopwatch.RESET_TIMER"
        private const val EXTRA_CONTROL_ONLY = "devkit.stopwatch.CONTROL_ONLY"
        private const val TIMER_UPDATE_INTERVAL_MILLIS = 20L

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _timerState = MutableStateFlow(StopwatchState())
        val timerState: StateFlow<StopwatchState> = _timerState.asStateFlow()

        fun start(context: Context, config: OverlayConfig) {
            val intent = Intent(context, StopwatchOverlayService::class.java)
                .setAction(ACTION_START_TIMER)
                .putOverlayConfig(config)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, config: OverlayConfig) {
            val intent = Intent(context, StopwatchOverlayService::class.java)
                .setAction(AbstractOverlayService.ACTION_UPDATE_CONFIG)
                .putOverlayConfig(config)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StopwatchOverlayService::class.java)
                .setAction(AbstractOverlayService.ACTION_STOP_SERVICE)
            context.startService(intent)
        }

        fun startOrResume(context: Context) = sendTimerAction(context, ACTION_START_TIMER)

        fun pause(context: Context) = sendTimerAction(context, ACTION_PAUSE_TIMER)

        fun reset(context: Context) = sendTimerAction(context, ACTION_RESET_TIMER)

        private fun sendTimerAction(context: Context, action: String) {
            context.startService(
                Intent(context, StopwatchOverlayService::class.java)
                    .setAction(action)
                    .putExtra(EXTRA_CONTROL_ONLY, true),
            )
        }
    }
}

internal val STOPWATCH_OVERLAY_DEFAULTS = OverlayConfig(
    sizeDp = 200,
    startX = 100,
    startY = 620,
)
