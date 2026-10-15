package com.antigravity.floatingwidget

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.antigravity.floatingwidget.avatar.AvatarManager
import com.antigravity.floatingwidget.databinding.ActivityMainBinding
import com.antigravity.floatingwidget.permission.PermissionHelper
import com.antigravity.floatingwidget.service.FloatingWidgetService
import com.antigravity.floatingwidget.settings.WidgetSettingsManager
import com.antigravity.floatingwidget.theme.ThemeManager

/**
 * Activity principal do dashboard BeYou para tablet com estética néon.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var avatarManager: AvatarManager
    private lateinit var themeManager: ThemeManager
    private lateinit var widgetSettingsManager: WidgetSettingsManager

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) checkPermissionsAndStartWidget()
        updatePermissionsUi()
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && avatarManager.saveCustomAvatar(uri)) {
            updateAvatarPreviews()
            notifyServiceAvatarChanged()
            Toast.makeText(this, getString(R.string.msg_avatar_updated), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        avatarManager = AvatarManager(this)
        themeManager = ThemeManager(this)
        widgetSettingsManager = WidgetSettingsManager(this, binding.cardWidget)

        setupPermissionsCard()
        setupAvatarCard()
        widgetSettingsManager.setup()
        setupBottomActions()
        setupThemeSelector()
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionsUi()
        updateAvatarPreviews()
    }

    private fun setupPermissionsCard() {
        binding.cardPermissions.btnManageOverlay.setOnClickListener {
            startActivity(PermissionHelper.createOverlaySettingsIntent(this))
        }
        binding.cardPermissions.btnManageCamera.setOnClickListener {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun setupAvatarCard() {
        binding.cardAvatar.btnChoosePhoto.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.cardAvatar.btnRestoreDefault.setOnClickListener {
            avatarManager.resetToDefault()
            updateAvatarPreviews()
            notifyServiceAvatarChanged()
            Toast.makeText(this, getString(R.string.msg_avatar_reset), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomActions() {
        binding.btnStartWidget.setOnClickListener { checkPermissionsAndStartWidget() }
        binding.btnStopWidget.setOnClickListener { stopFloatingService() }
        binding.btnHeaderSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
        }
    }

    private fun setupThemeSelector() {
        binding.btnHeaderTheme.setOnClickListener {
            val selected = themeManager.cycleNextTheme()
            applyCurrentTheme()
            notifyServiceThemeChanged(selected.id)
            Toast.makeText(this, "Tema: ${selected.displayName}", Toast.LENGTH_SHORT).show()
        }
        binding.btnHeaderTheme.setOnLongClickListener { view ->
            val popup = PopupMenu(this, view)
            themeManager.getAvailableThemes().forEachIndexed { index, theme ->
                popup.menu.add(0, index, index, theme.displayName)
            }
            popup.setOnMenuItemClickListener { item ->
                val selected = themeManager.getAvailableThemes()[item.itemId]
                themeManager.setTheme(selected.id)
                applyCurrentTheme()
                notifyServiceThemeChanged(selected.id)
                Toast.makeText(this, "Tema: ${selected.displayName}", Toast.LENGTH_SHORT).show()
                true
            }
            popup.show()
            true
        }
    }

    private fun applyCurrentTheme() {
        val theme = themeManager.getActiveTheme()
        com.antigravity.floatingwidget.theme.ThemeApplier.apply(this, binding, theme)
        val initialMode = getSharedPreferences("atue_widget_settings", Context.MODE_PRIVATE)
            .getString("initial_mode", "avatar") ?: "avatar"
        widgetSettingsManager.updateSegmentedMode(initialMode == "avatar")
    }

    private fun updateAvatarPreviews() {
        avatarManager.loadAvatarInto(binding.cardAvatar.ivAvatarThumbnail)
        avatarManager.loadAvatarInto(binding.cardWidget.ivWidgetSettingsPreview)
    }

    private fun updatePermissionsUi() {
        bindPermissionRow(binding.cardPermissions.tvOverlayStatusText, binding.cardPermissions.dotOverlayStatus, PermissionHelper.hasOverlayPermission(this))
        bindPermissionRow(binding.cardPermissions.tvCameraStatusText, binding.cardPermissions.dotCameraStatus, PermissionHelper.hasCameraPermission(this))
    }

    private fun bindPermissionRow(tv: TextView, dot: View, isGranted: Boolean) {
        tv.text = getString(if (isGranted) R.string.status_granted_label else R.string.status_pending_label)
        tv.setTextColor(ContextCompat.getColor(this, if (isGranted) R.color.dot_granted else R.color.dot_pending))
        dot.setBackgroundResource(if (isGranted) R.drawable.bg_status_dot_green else R.drawable.bg_status_dot_red)
    }

    private fun checkPermissionsAndStartWidget() {
        if (!PermissionHelper.hasOverlayPermission(this)) {
            Toast.makeText(this, "Ative a permissão de sobreposição.", Toast.LENGTH_LONG).show()
            startActivity(PermissionHelper.createOverlaySettingsIntent(this))
            return
        }
        if (!PermissionHelper.hasCameraPermission(this)) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }
        startFloatingService()
    }

    private fun startFloatingService() {
        val serviceIntent = Intent(this, FloatingWidgetService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopFloatingService() {
        startService(Intent(this, FloatingWidgetService::class.java).apply {
            action = FloatingWidgetService.ACTION_STOP_SERVICE
        })
        Toast.makeText(this, "Widget flutuante encerrado.", Toast.LENGTH_SHORT).show()
    }

    private fun notifyServiceAvatarChanged() {
        startService(Intent(this, FloatingWidgetService::class.java).apply {
            action = FloatingWidgetService.ACTION_UPDATE_AVATAR
        })
    }

    private fun notifyServiceThemeChanged(themeId: String) {
        startService(Intent(this, FloatingWidgetService::class.java).apply {
            action = FloatingWidgetService.ACTION_UPDATE_THEME
            putExtra(FloatingWidgetService.EXTRA_THEME, themeId)
        })
    }
}
