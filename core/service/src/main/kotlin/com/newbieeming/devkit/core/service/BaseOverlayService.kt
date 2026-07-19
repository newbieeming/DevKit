package com.newbieeming.devkit.core.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.WindowManager

/**
 * 悬浮窗 Service 基类
 *
 * 封装 [WindowManager] 的 addView / updateViewLayout / removeView 生命周期，
 * 子类只需实现 [createOverlayView] 和 [onOverlayReady]，无需重复处理窗口参数。
 *
 * 需要权限：SYSTEM_ALERT_WINDOW（在 feature 模块的 Manifest 中声明）
 */
abstract class BaseOverlayService : Service() {

    protected lateinit var windowManager: WindowManager
        private set

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** 创建默认悬浮窗 LayoutParams（子类可覆盖调整大小/位置/类型） */
    protected open fun defaultLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT,
    )
}
