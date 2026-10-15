package com.antigravity.floatingwidget.theme

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.antigravity.floatingwidget.R

/**
 * Gestor de temas estéticos da aplicação (Néon Ciano, Néon Violeta, Néon Matrix e Claro).
 */
class ThemeManager(private val context: Context) {

    data class ThemeConfig(
        val id: String,
        val displayName: String,
        val accentColor: Int,
        val glowColor: Int,
        val bgColor: Int,
        val cardColor: Int,
        val cardBorderColor: Int,
        val textPrimaryColor: Int,
        val textSecondaryColor: Int,
        val isLight: Boolean = false
    )

    private val prefs: SharedPreferences =
        context.getSharedPreferences("beyou_theme_prefs", Context.MODE_PRIVATE)

    companion object {
        const val THEME_CYAN = "cyan"
        const val THEME_PURPLE = "purple"
        const val THEME_EMERALD = "emerald"
        const val THEME_LIGHT = "light"
    }

    fun getAvailableThemes(): List<ThemeConfig> = listOf(
        ThemeConfig(
            THEME_CYAN,
            "🔵 " + context.getString(R.string.theme_cyan),
            Color.parseColor("#00D2FF"),
            Color.parseColor("#4D00D2FF"),
            Color.parseColor("#080E1E"),
            Color.parseColor("#0A162C"),
            Color.parseColor("#18345E"),
            Color.parseColor("#F8FAFC"),
            Color.parseColor("#94A3B8"),
            false
        ),
        ThemeConfig(
            THEME_PURPLE,
            "🟣 " + context.getString(R.string.theme_purple),
            Color.parseColor("#C084FC"),
            Color.parseColor("#4DC084FC"),
            Color.parseColor("#11081F"),
            Color.parseColor("#1C0F33"),
            Color.parseColor("#3B1C6E"),
            Color.parseColor("#FAF5FF"),
            Color.parseColor("#C084FC"),
            false
        ),
        ThemeConfig(
            THEME_EMERALD,
            "🟢 " + context.getString(R.string.theme_emerald),
            Color.parseColor("#00E676"),
            Color.parseColor("#4D00E676"),
            Color.parseColor("#05140C"),
            Color.parseColor("#0A2417"),
            Color.parseColor("#144A30"),
            Color.parseColor("#F0FDF4"),
            Color.parseColor("#86EFAC"),
            false
        ),
        ThemeConfig(
            THEME_LIGHT,
            "☀️ " + context.getString(R.string.theme_light),
            Color.parseColor("#0284C7"),
            Color.parseColor("#330284C7"),
            Color.parseColor("#F1F5F9"),
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#CBD5E1"),
            Color.parseColor("#0F172A"),
            Color.parseColor("#64748B"),
            true
        )
    )

    fun getActiveTheme(): ThemeConfig {
        val id = prefs.getString("active_theme_id", THEME_CYAN) ?: THEME_CYAN
        return getAvailableThemes().firstOrNull { it.id == id } ?: getAvailableThemes().first()
    }

    fun setTheme(themeId: String): ThemeConfig {
        prefs.edit().putString("active_theme_id", themeId).apply()
        return getActiveTheme()
    }

    fun cycleNextTheme(): ThemeConfig {
        val themes = getAvailableThemes()
        val currentIndex = themes.indexOfFirst { it.id == getActiveTheme().id }
        val nextTheme = themes[(currentIndex + 1) % themes.size]
        return setTheme(nextTheme.id)
    }

    fun applyToFloatingWidget(glowRing: View, toggleBtn: View, theme: ThemeConfig) {
        val gd = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
            setStroke((5 * context.resources.displayMetrics.density).toInt(), theme.accentColor)
        }
        glowRing.background = gd

        val btnBg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(theme.accentColor, theme.glowColor)
            orientation = GradientDrawable.Orientation.TL_BR
        }
        toggleBtn.background = btnBg
    }
}
