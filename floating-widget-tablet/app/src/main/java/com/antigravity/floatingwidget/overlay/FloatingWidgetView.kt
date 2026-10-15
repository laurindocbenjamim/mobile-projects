package com.antigravity.floatingwidget.overlay

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import com.antigravity.floatingwidget.R
import com.antigravity.floatingwidget.avatar.AvatarManager
import com.antigravity.floatingwidget.camera.CameraManager
import com.antigravity.floatingwidget.theme.ThemeManager

/**
 * Gestor da UI e dos modos visuais (Avatar com contorno luminoso vs Câmara em tempo real) do Widget.
 */
class FloatingWidgetView(
    private val context: Context,
    private val cameraManager: CameraManager,
    var onModeChangedListener: ((DisplayMode) -> Unit)? = null
) {
    enum class DisplayMode { AVATAR, CAMERA }

    val rootView: View = LayoutInflater.from(context).inflate(R.layout.layout_floating_widget, null)
    val circularCard: CardView = rootView.findViewById(R.id.circularCard)
    private val luminousGlowRing: View = rootView.findViewById(R.id.luminousGlowRing)
    private val avatarImageView: ImageView = rootView.findViewById(R.id.avatarImageView)
    private val cameraPreviewView: PreviewView = rootView.findViewById(R.id.cameraPreviewView)
    private val cameraActiveIndicator: View = rootView.findViewById(R.id.cameraActiveIndicator)
    private val btnToggleMode: ImageButton = rootView.findViewById(R.id.btnToggleMode)

    private val avatarManager = AvatarManager(context)
    private val themeManager = ThemeManager(context)
    private var currentMode: DisplayMode = DisplayMode.AVATAR

    init {
        setupListeners()
        updateAvatar()
        applyTheme()
    }

    private fun setupListeners() {
        btnToggleMode.setOnClickListener {
            toggleMode()
            animateButtonClick()
        }
    }

    fun toggleMode() {
        if (currentMode == DisplayMode.AVATAR) switchToCamera() else switchToAvatar()
        onModeChangedListener?.invoke(currentMode)
    }

    fun setMode(mode: DisplayMode) {
        if (mode == currentMode) return
        if (mode == DisplayMode.CAMERA) switchToCamera() else switchToAvatar()
        onModeChangedListener?.invoke(currentMode)
    }

    private fun switchToCamera() {
        currentMode = DisplayMode.CAMERA
        avatarImageView.visibility = View.GONE
        cameraPreviewView.visibility = View.VISIBLE
        cameraActiveIndicator.visibility = View.VISIBLE
        pulseIndicator(cameraActiveIndicator)
        cameraManager.startCamera(cameraPreviewView)
    }

    private fun switchToAvatar() {
        currentMode = DisplayMode.AVATAR
        cameraManager.stopCamera()
        cameraPreviewView.visibility = View.GONE
        cameraActiveIndicator.visibility = View.GONE
        avatarImageView.visibility = View.VISIBLE
        avatarManager.loadAvatarInto(avatarImageView)
    }

    fun updateAvatar() {
        if (currentMode == DisplayMode.AVATAR) {
            avatarManager.loadAvatarInto(avatarImageView)
        }
    }

    fun applySize(sizeIndex: Int) {
        val density = context.resources.displayMetrics.density
        val (cardDp, glowDp, btnDp) = when (sizeIndex) {
            0 -> Triple(120, 134, 38)
            2 -> Triple(220, 238, 54)
            else -> Triple(170, 186, 46)
        }
        val cardPx = (cardDp * density).toInt()
        val glowPx = (glowDp * density).toInt()
        val btnPx = (btnDp * density).toInt()

        circularCard.layoutParams.width = cardPx
        circularCard.layoutParams.height = cardPx
        circularCard.radius = cardPx / 2f

        luminousGlowRing.layoutParams.width = glowPx
        luminousGlowRing.layoutParams.height = glowPx

        btnToggleMode.layoutParams.width = btnPx
        btnToggleMode.layoutParams.height = btnPx

        rootView.requestLayout()
    }

    fun applyTheme(themeId: String? = null) {
        val theme = if (themeId != null) themeManager.setTheme(themeId) else themeManager.getActiveTheme()
        themeManager.applyToFloatingWidget(luminousGlowRing, btnToggleMode, theme)
    }

    private fun animateButtonClick() {
        ObjectAnimator.ofFloat(btnToggleMode, "scaleX", 0.85f, 1.0f).setDuration(160).start()
        ObjectAnimator.ofFloat(btnToggleMode, "scaleY", 0.85f, 1.0f).setDuration(160).start()
    }

    private fun pulseIndicator(target: View) {
        target.scaleX = 0.5f
        target.scaleY = 0.5f
        target.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start()
    }

    fun release() {
        cameraManager.stopCamera()
    }

    fun getCurrentMode(): DisplayMode = currentMode
}
