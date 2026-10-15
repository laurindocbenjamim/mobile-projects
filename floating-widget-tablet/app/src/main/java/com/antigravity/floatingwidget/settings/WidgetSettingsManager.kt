package com.antigravity.floatingwidget.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.antigravity.floatingwidget.R
import com.antigravity.floatingwidget.databinding.CardWidgetSettingsBinding
import com.antigravity.floatingwidget.service.FloatingWidgetService
import com.antigravity.floatingwidget.theme.ThemeManager

/**
 * Gestor da configuração interativa do widget (Modo Inicial e Tamanho dinâmico).
 */
class WidgetSettingsManager(
    private val context: Context,
    private val binding: CardWidgetSettingsBinding
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("atue_widget_settings", Context.MODE_PRIVATE)
    private val themeManager = ThemeManager(context)

    fun setup() {
        val initialMode = prefs.getString("initial_mode", "avatar") ?: "avatar"
        updateSegmentedMode(initialMode == "avatar")

        binding.btnSegmentAvatar.setOnClickListener {
            prefs.edit().putString("initial_mode", "avatar").apply()
            updateSegmentedMode(true)
            notifyServiceModeChanged("avatar")
            Toast.makeText(context, "Modo: Avatar ativado", Toast.LENGTH_SHORT).show()
        }

        binding.btnSegmentCamera.setOnClickListener {
            prefs.edit().putString("initial_mode", "camera").apply()
            updateSegmentedMode(false)
            notifyServiceModeChanged("camera")
            Toast.makeText(context, "Modo: Câmara ativada", Toast.LENGTH_SHORT).show()
        }

        val initialSize = prefs.getInt("widget_size", 1)
        binding.seekWidgetSize.progress = initialSize
        updateSizeLabels(initialSize)
        updatePreviewScale(initialSize)

        binding.seekWidgetSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) applySizeSelection(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        binding.tvSizeSmall.setOnClickListener { applySizeSelection(0) }
        binding.tvSizeMedium.setOnClickListener { applySizeSelection(1) }
        binding.tvSizeLarge.setOnClickListener { applySizeSelection(2) }
    }

    private fun applySizeSelection(size: Int) {
        binding.seekWidgetSize.progress = size
        prefs.edit().putInt("widget_size", size).apply()
        updateSizeLabels(size)
        updatePreviewScale(size)
        notifyServiceSizeChanged(size)
        val name = when (size) {
            0 -> context.getString(R.string.size_small)
            2 -> context.getString(R.string.size_large)
            else -> context.getString(R.string.size_medium)
        }
        Toast.makeText(context, "Tamanho: $name", Toast.LENGTH_SHORT).show()
    }

    fun updateSegmentedMode(isAvatarSelected: Boolean) {
        val theme = themeManager.getActiveTheme()
        val darkOrWhite = if (theme.isLight) android.graphics.Color.WHITE else ContextCompat.getColor(context, R.color.text_dark_btn)
        val muted = theme.textSecondaryColor

        val activeBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 17 * context.resources.displayMetrics.density
            setColor(theme.accentColor)
        }

        if (isAvatarSelected) {
            binding.btnSegmentAvatar.background = activeBg
            binding.btnSegmentAvatar.setTextColor(darkOrWhite)
            binding.btnSegmentCamera.background = null
            binding.btnSegmentCamera.setTextColor(muted)
            binding.ivWidgetPreviewModeBadge.setImageResource(R.drawable.ic_person_user)
        } else {
            binding.btnSegmentCamera.background = activeBg
            binding.btnSegmentCamera.setTextColor(darkOrWhite)
            binding.btnSegmentAvatar.background = null
            binding.btnSegmentAvatar.setTextColor(muted)
            binding.ivWidgetPreviewModeBadge.setImageResource(R.drawable.ic_camera_switch)
        }
    }

    private fun updateSizeLabels(progress: Int) {
        val theme = themeManager.getActiveTheme()
        binding.tvSizeSmall.setTextColor(if (progress == 0) theme.textPrimaryColor else theme.textSecondaryColor)
        binding.tvSizeMedium.setTextColor(if (progress == 1) theme.textPrimaryColor else theme.textSecondaryColor)
        binding.tvSizeLarge.setTextColor(if (progress == 2) theme.textPrimaryColor else theme.textSecondaryColor)
    }

    private fun updatePreviewScale(progress: Int) {
        val scale = when (progress) {
            0 -> 0.82f
            2 -> 1.18f
            else -> 1.0f
        }
        binding.layoutWidgetSettingsPreviewContainer.animate().scaleX(scale).scaleY(scale).setDuration(200).start()
    }

    private fun notifyServiceModeChanged(mode: String) {
        context.startService(Intent(context, FloatingWidgetService::class.java).apply {
            action = FloatingWidgetService.ACTION_UPDATE_MODE
            putExtra(FloatingWidgetService.EXTRA_MODE, mode)
        })
    }

    private fun notifyServiceSizeChanged(size: Int) {
        context.startService(Intent(context, FloatingWidgetService::class.java).apply {
            action = FloatingWidgetService.ACTION_UPDATE_SIZE
            putExtra(FloatingWidgetService.EXTRA_SIZE, size)
        })
    }
}
