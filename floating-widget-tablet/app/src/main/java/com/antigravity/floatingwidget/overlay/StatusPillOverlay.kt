package com.antigravity.floatingwidget.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.antigravity.floatingwidget.R

/**
 * Gestor da pílula indicadora flutuante no canto superior direito do ecrã do tablet.
 */
class StatusPillOverlay(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onPillClicked: () -> Unit
) {
    val rootView: View = LayoutInflater.from(context).inflate(
        R.layout.layout_status_pill,
        null
    )

    private val ivIcon: ImageView = rootView.findViewById(R.id.ivStatusPillIcon)
    private val tvText: TextView = rootView.findViewById(R.id.tvStatusPillText)
    private var isAttached = false

    init {
        rootView.setOnClickListener {
            onPillClicked()
        }
    }

    /**
     * Adiciona a pílula de estado ao WindowManager como sobreposição flutuante.
     */
    fun show() {
        if (isAttached) return

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 80
            y = 40
        }

        try {
            windowManager.addView(rootView, params)
            isAttached = true
        } catch (e: Exception) {
            android.util.Log.e("StatusPillOverlay", "Erro ao adicionar pill overlay", e)
        }
    }

    /**
     * Atualiza o estado visual da pílula consoante o modo ativo (Avatar ou Câmara).
     */
    fun updateMode(mode: FloatingWidgetView.DisplayMode) {
        if (mode == FloatingWidgetView.DisplayMode.CAMERA) {
            tvText.text = context.getString(R.string.mode_camera_label)
            tvText.setTextColor(Color.parseColor("#00E676"))
            ivIcon.setColorFilter(Color.parseColor("#00E676"))
        } else {
            tvText.text = context.getString(R.string.mode_avatar_label)
            tvText.setTextColor(Color.parseColor("#F8FAFC"))
            ivIcon.setColorFilter(Color.parseColor("#00D2FF"))
        }
    }

    /**
     * Remove a pílula do WindowManager com segurança.
     */
    fun remove() {
        if (!isAttached) return
        try {
            windowManager.removeView(rootView)
            isAttached = false
        } catch (e: Exception) {
            android.util.Log.e("StatusPillOverlay", "Erro ao remover pill overlay", e)
        }
    }
}
