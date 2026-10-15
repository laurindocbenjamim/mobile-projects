package com.antigravity.floatingwidget.theme

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowInsetsControllerCompat
import com.antigravity.floatingwidget.R
import com.antigravity.floatingwidget.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

/**
 * Utilitário responsável por aplicar temas completos (fundo de ecrã, cartões e textos) na MainActivity.
 */
object ThemeApplier {

    fun apply(activity: Activity, binding: ActivityMainBinding, theme: ThemeManager.ThemeConfig) {
        val density = activity.resources.displayMetrics.density

        // 1. Janela de Sistema (Status Bar e Navigation Bar)
        activity.window.statusBarColor = theme.bgColor
        activity.window.navigationBarColor = theme.bgColor
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = theme.isLight
            isAppearanceLightNavigationBars = theme.isLight
        }

        // 2. Fundo global do ecrã
        binding.root.setBackgroundColor(theme.bgColor)

        // 3. Fundo e contorno dos 3 Cartões
        binding.cardPermissions.cardPermissionsContainer.background = createCardDrawable(theme, density)
        binding.cardAvatar.cardAvatarContainer.background = createCardDrawable(theme, density)
        binding.cardWidget.cardWidgetSettingsContainer.background = createCardDrawable(theme, density)

        // 4. Header Bar (Títulos e Botões Pill)
        binding.tvAppTitle.setTextColor(theme.textPrimaryColor)
        binding.tvAppSubtitle.setTextColor(theme.textSecondaryColor)

        val pillBg1 = createPillDrawable(theme, density)
        val pillBg2 = createPillDrawable(theme, density)
        binding.btnHeaderTheme.background = pillBg1
        binding.btnHeaderTheme.setTextColor(theme.textPrimaryColor)
        binding.btnHeaderTheme.iconTint = ColorStateList.valueOf(theme.accentColor)

        binding.btnHeaderSettings.background = pillBg2
        binding.btnHeaderSettings.setTextColor(theme.textPrimaryColor)
        binding.btnHeaderSettings.iconTint = ColorStateList.valueOf(theme.accentColor)

        // 5. Estilização dinâmica dos conteúdos dos cartões
        styleCardHierarchy(binding.cardPermissions.cardPermissionsContainer, theme)
        styleCardHierarchy(binding.cardAvatar.cardAvatarContainer, theme)
        styleCardHierarchy(binding.cardWidget.cardWidgetSettingsContainer, theme)

        // 6. Botões específicos dos cartões
        val btnManageBg1 = createPillDrawable(theme, density)
        val btnManageBg2 = createPillDrawable(theme, density)
        binding.cardPermissions.btnManageOverlay.background = btnManageBg1
        binding.cardPermissions.btnManageOverlay.setTextColor(theme.accentColor)
        binding.cardPermissions.btnManageOverlay.iconTint = ColorStateList.valueOf(theme.accentColor)

        binding.cardPermissions.btnManageCamera.background = btnManageBg2
        binding.cardPermissions.btnManageCamera.setTextColor(theme.accentColor)
        binding.cardPermissions.btnManageCamera.iconTint = ColorStateList.valueOf(theme.accentColor)

        val textDarkOrWhite = if (theme.isLight) Color.WHITE else Color.parseColor("#061226")
        binding.cardAvatar.btnChoosePhoto.backgroundTintList = ColorStateList.valueOf(theme.accentColor)
        binding.cardAvatar.btnChoosePhoto.setTextColor(textDarkOrWhite)
        binding.cardAvatar.btnChoosePhoto.iconTint = ColorStateList.valueOf(textDarkOrWhite)

        binding.cardAvatar.btnRestoreDefault.background = createPillDrawable(theme, density)
        binding.cardAvatar.btnRestoreDefault.setTextColor(theme.textPrimaryColor)
        binding.cardAvatar.btnRestoreDefault.iconTint = ColorStateList.valueOf(theme.textSecondaryColor)

        // 7. Slider e Badge de Preview do Widget
        binding.cardWidget.seekWidgetSize.progressTintList = ColorStateList.valueOf(theme.accentColor)
        binding.cardWidget.seekWidgetSize.thumbTintList = ColorStateList.valueOf(theme.accentColor)
        binding.cardWidget.seekWidgetSize.progressBackgroundTintList = ColorStateList.valueOf(
            if (theme.isLight) Color.parseColor("#CBD5E1") else theme.cardBorderColor
        )

        val segContainerBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f * density
            setColor(if (theme.isLight) Color.parseColor("#E2E8F0") else Color.parseColor("#09172E"))
            setStroke((1.2f * density).toInt(), if (theme.isLight) Color.parseColor("#CBD5E1") else theme.cardBorderColor)
        }
        binding.cardWidget.layoutSegmentedContainer.background = segContainerBg

        // 8. Botão de Início e Paragem do Widget
        binding.btnStartWidget.backgroundTintList = ColorStateList.valueOf(theme.accentColor)
        binding.btnStartWidget.setTextColor(textDarkOrWhite)
        binding.btnStartWidget.iconTint = ColorStateList.valueOf(textDarkOrWhite)
    }

    private fun styleCardHierarchy(view: View, theme: ThemeManager.ThemeConfig) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                styleCardHierarchy(view.getChildAt(i), theme)
            }
        } else if (view is TextView && view !is MaterialButton) {
            if (view.id != R.id.tvOverlayStatusText && view.id != R.id.tvCameraStatusText &&
                view.id != R.id.btnSegmentAvatar && view.id != R.id.btnSegmentCamera) {
                if (view.textSize >= 14 * view.resources.displayMetrics.scaledDensity) {
                    view.setTextColor(theme.textPrimaryColor)
                } else {
                    view.setTextColor(theme.textSecondaryColor)
                }
            }
        } else if (view is ImageView && view.id == View.NO_ID) {
            view.setColorFilter(theme.accentColor)
        } else if (view.layoutParams != null && (view.layoutParams.height == 1 || view.layoutParams.width == 1)) {
            view.setBackgroundColor(if (theme.isLight) Color.parseColor("#E2E8F0") else theme.cardBorderColor)
        }
    }

    private fun createCardDrawable(theme: ThemeManager.ThemeConfig, density: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f * density
            setColor(theme.cardColor)
            setStroke((1.5f * density).toInt(), theme.cardBorderColor)
        }
    }

    private fun createPillDrawable(theme: ThemeManager.ThemeConfig, density: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 19f * density
            setColor(if (theme.isLight) Color.parseColor("#F8FAFC") else theme.cardColor)
            setStroke((1.2f * density).toInt(), theme.cardBorderColor)
        }
    }
}
