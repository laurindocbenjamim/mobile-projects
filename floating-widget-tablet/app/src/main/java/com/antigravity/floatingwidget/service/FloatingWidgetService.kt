package com.antigravity.floatingwidget.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.antigravity.floatingwidget.MainActivity
import com.antigravity.floatingwidget.R
import com.antigravity.floatingwidget.camera.CameraManager
import com.antigravity.floatingwidget.overlay.FloatingWidgetView
import com.antigravity.floatingwidget.overlay.StatusPillOverlay
import com.antigravity.floatingwidget.overlay.TouchDragListener

/**
 * Serviço em primeiro plano responsável pela janela flutuante no WindowManager.
 */
class FloatingWidgetService : LifecycleService() {

    private lateinit var windowManager: WindowManager
    private lateinit var cameraManager: CameraManager
    private lateinit var widgetView: FloatingWidgetView
    private lateinit var statusPillOverlay: StatusPillOverlay
    private lateinit var layoutParams: WindowManager.LayoutParams

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "floating_widget_channel"
        const val ACTION_STOP_SERVICE = "com.antigravity.floatingwidget.STOP_SERVICE"
        const val ACTION_UPDATE_AVATAR = "com.antigravity.floatingwidget.UPDATE_AVATAR"
        const val ACTION_UPDATE_MODE = "com.antigravity.floatingwidget.UPDATE_MODE"
        const val ACTION_UPDATE_SIZE = "com.antigravity.floatingwidget.UPDATE_SIZE"
        const val ACTION_UPDATE_THEME = "com.antigravity.floatingwidget.UPDATE_THEME"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_SIZE = "extra_size"
        const val EXTRA_THEME = "extra_theme"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        cameraManager = CameraManager(this, this)
        cameraManager.initProvider()
        startAsForeground()
        setupFloatingWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_AVATAR -> {
                if (::widgetView.isInitialized) widgetView.updateAvatar()
                return START_STICKY
            }
            ACTION_UPDATE_MODE -> {
                val mode = intent.getStringExtra(EXTRA_MODE)
                if (::widgetView.isInitialized && mode != null) {
                    val target = if (mode == "camera") FloatingWidgetView.DisplayMode.CAMERA else FloatingWidgetView.DisplayMode.AVATAR
                    widgetView.setMode(target)
                }
                return START_STICKY
            }
            ACTION_UPDATE_SIZE -> {
                val size = intent.getIntExtra(EXTRA_SIZE, 1)
                if (::widgetView.isInitialized) widgetView.applySize(size)
                return START_STICKY
            }
            ACTION_UPDATE_THEME -> {
                val themeId = intent.getStringExtra(EXTRA_THEME)
                if (::widgetView.isInitialized) widgetView.applyTheme(themeId)
                return START_STICKY
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setupFloatingWindow() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 120
            y = 160
        }

        widgetView = FloatingWidgetView(this, cameraManager)
        statusPillOverlay = StatusPillOverlay(this, windowManager) { widgetView.toggleMode() }

        widgetView.onModeChangedListener = { mode ->
            statusPillOverlay.updateMode(mode)
        }

        val prefs = getSharedPreferences("atue_widget_settings", Context.MODE_PRIVATE)
        val initialMode = prefs.getString("initial_mode", "avatar")
        val size = prefs.getInt("widget_size", 1)
        widgetView.applySize(size)
        if (initialMode == "camera") {
            widgetView.setMode(FloatingWidgetView.DisplayMode.CAMERA)
        }

        val touchListener = TouchDragListener(
            windowManager = windowManager,
            layoutParams = layoutParams,
            containerView = widgetView.rootView,
            onDoubleTap = { widgetView.toggleMode() },
            onSingleTap = null
        )
        widgetView.circularCard.setOnTouchListener(touchListener)

        windowManager.addView(widgetView.rootView, layoutParams)
        statusPillOverlay.show()
    }

    private fun startAsForeground() {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_status_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::statusPillOverlay.isInitialized) statusPillOverlay.remove()
        if (::widgetView.isInitialized) {
            widgetView.release()
            try {
                windowManager.removeView(widgetView.rootView)
            } catch (e: Exception) {
                // View já removida
            }
        }
    }
}
