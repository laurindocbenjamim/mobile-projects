package com.antigravity.floatingwidget.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utilitário responsável pela validação e requisição de permissões em tempo de execução.
 * Trata permissões de sobreposição de sistema (Overlay) e de câmara física ou virtual.
 */
object PermissionHelper {

    /**
     * Valida se a aplicação tem autorização para desenhar janelas flutuantes sobre outras apps.
     *
     * @param context Contexto da aplicação ou Activity.
     * @return Verdadeiro se a permissão estiver ativa; falso caso contrário.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Valida se a aplicação possui autorização para aceder ao sensor de câmara.
     *
     * @param context Contexto da aplicação ou Activity.
     * @return Verdadeiro se o utilizador concedeu a permissão CAMERA.
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica se todos os pré-requisitos de permissão para o widget flutuante estão satisfeitos.
     *
     * @param context Contexto de verificação.
     * @return Verdadeiro se tanto o Overlay quanto a Câmara estiverem concedidos.
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasOverlayPermission(context) && hasCameraPermission(context)
    }

    /**
     * Cria a Intent com URI explícita do pacote para as definições de permissão de Overlay.
     *
     * @param context Contexto utilizado para extrair o packageName.
     * @return Intent configurada para Settings.ACTION_MANAGE_OVERLAY_PERMISSION.
     */
    fun createOverlaySettingsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
