package com.newbieeming.devkit.core.ui.overlay

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.newbieeming.devkit.core.designsystem.theme.DevKitTheme
import com.newbieeming.devkit.core.model.OverlayConfig
import kotlin.math.roundToInt

/**
 * Base generic service for creating Compose-based WindowManager overlays.
 * It handles the required Lifecycle/ViewModelStore/SavedStateRegistry boilerplate
 * for ComposeView and provides optional drag support.
 */
abstract class AbstractOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    protected lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    protected lateinit var overlayConfig: OverlayConfig
        private set

    // Lifecycle components for ComposeView in WindowManager
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    // --- Abstract/Configurable Methods ---
    
    /** 
     * Provide the notification to be shown for this foreground service. 
     */
    abstract fun createServiceNotification(): Notification

    /** 
     * Provide the notification ID. Must be > 0.
     */
    abstract val notificationId: Int

    /** 
     * Define the starting X coordinate for the overlay.
     */
    open val startX: Int get() = overlayConfig.startX

    /** 
     * Define the starting Y coordinate for the overlay.
     */
    open val startY: Int get() = overlayConfig.startY

    /** Default values used if Android recreates the service without extras. */
    open val defaultOverlayConfig: OverlayConfig = OverlayConfig(
        sizeDp = 72,
        startX = 100,
        startY = 100,
    )

    /** 
     * Provide the Gravity for the layout parameters.
     */
    open val layoutGravity: Int = Gravity.TOP or Gravity.START

    /** 
     * Specify whether the overlay should be draggable by the user.
     */
    open val isDraggable: Boolean = true

    /**
     * The actual Compose content to render in the overlay.
     * @param modifier Apply this modifier to the root of your content to enable drag support.
     */
    @Composable
    abstract fun OverlayContent(modifier: Modifier)

    // --- Service Lifecycle ---

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayConfig = defaultOverlayConfig
        
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent.isStopRequest()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val configChanged = updateConfig(intent)
        startForeground(notificationId, createServiceNotification())
        showOrRefreshOverlay(forceRefresh = configChanged || intent?.action == ACTION_UPDATE_CONFIG)
        return START_REDELIVER_INTENT
    }

    private fun Intent?.isStopRequest(): Boolean = this?.action == ACTION_STOP_SERVICE

    private fun updateConfig(intent: Intent?): Boolean {
        val updated = intent.overlayConfigOr(defaultOverlayConfig)
        return (updated != overlayConfig).also { overlayConfig = updated }
    }

    private fun showOrRefreshOverlay(forceRefresh: Boolean) {
        if (overlayView == null) {
            addOverlay()
        } else if (forceRefresh) {
            recreateOverlay()
        }
    }

    private fun recreateOverlay() {
        removeOverlay()
        addOverlay()
    }

    private fun addOverlay() {
        if (overlayView != null) return

        val layoutParams = createLayoutParams()
        val composeView = createComposeView(layoutParams)
        runCatching { windowManager.addView(composeView, layoutParams) }
            .onSuccess { overlayView = composeView }
            .onFailure { error -> Log.e(TAG, "Unable to add overlay", error) }
    }

    private fun createLayoutParams() = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = layoutGravity
            x = startX
            y = startY
        }

    @Suppress("DEPRECATION")
    private fun overlayWindowType(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }

    private fun createComposeView(layoutParams: WindowManager.LayoutParams): ComposeView {
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@AbstractOverlayService)
            setViewTreeViewModelStoreOwner(this@AbstractOverlayService)
            setViewTreeSavedStateRegistryOwner(this@AbstractOverlayService)
        }
        composeView.setContent {
            DevKitTheme {
                OverlayContent(modifier = dragModifier(composeView, layoutParams))
            }
        }
        return composeView
    }

    private fun dragModifier(
        overlay: View,
        layoutParams: WindowManager.LayoutParams,
    ): Modifier = if (isDraggable) {
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                layoutParams.apply {
                    x += dragAmount.x.roundToInt()
                    y += dragAmount.y.roundToInt()
                }
                windowManager.updateViewLayout(overlay, layoutParams)
            }
        }
    } else {
        Modifier
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        overlayView = null
        runCatching { windowManager.removeView(view) }
            .onFailure { error -> Log.w(TAG, "Unable to remove overlay", error) }
    }

    /**
     * Call this from subclasses if you need to force a recomposition of the UI
     * (e.g., when a system state changes that Compose isn't automatically tracking).
     * Usually, it's better to use mutableStateOf inside the service.
     */
    protected fun updateOverlayUI() {
        // Simple trick to force recomposition by sending an intent to itself
        // if state management isn't reactive enough.
        val intent = Intent(this, this::class.java)
        intent.action = ACTION_UPDATE_UI
        intent.putOverlayConfig(overlayConfig)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "AbstractOverlayService"
        const val ACTION_STOP_SERVICE = "STOP_OVERLAY_SERVICE"
        const val ACTION_UPDATE_UI = "UPDATE_OVERLAY_UI"
        const val ACTION_UPDATE_CONFIG = "UPDATE_OVERLAY_CONFIG"
    }
}
