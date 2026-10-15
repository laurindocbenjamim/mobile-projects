package com.antigravity.floatingwidget.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.antigravity.floatingwidget.R
import java.io.File
import java.io.FileOutputStream

/**
 * Gestor do armazenamento e carregamento de avatares personalizados do utilizador.
 */
class AvatarManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "beyou_avatar_prefs"
        private const val KEY_USE_CUSTOM = "use_custom_avatar"
        private const val AVATAR_FILE_NAME = "custom_avatar.png"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val avatarFile: File get() = File(context.filesDir, AVATAR_FILE_NAME)

    /**
     * Guarda o ficheiro de imagem selecionado pelo utilizador no armazenamento interno da app.
     */
    fun saveCustomAvatar(sourceUri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return false
                FileOutputStream(avatarFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
            }
            prefs.edit().putBoolean(KEY_USE_CUSTOM, true).apply()
            true
        } catch (e: Exception) {
            android.util.Log.e("AvatarManager", "Erro ao guardar imagem personalizada", e)
            false
        }
    }

    /**
     * Restaura a imagem predefinida do conceito Atue/BeYou.
     */
    fun resetToDefault() {
        prefs.edit().putBoolean(KEY_USE_CUSTOM, false).apply()
        if (avatarFile.exists()) {
            avatarFile.delete()
        }
    }

    /**
     * Verifica se o utilizador tem atualmente um avatar personalizado ativo.
     */
    fun isCustomAvatarActive(): Boolean {
        return prefs.getBoolean(KEY_USE_CUSTOM, false) && avatarFile.exists()
    }

    /**
     * Carrega a imagem ativa (personalizada ou predefinida) na ImageView indicada.
     */
    fun loadAvatarInto(imageView: ImageView) {
        if (isCustomAvatarActive()) {
            try {
                val bitmap: Bitmap? = BitmapFactory.decodeFile(avatarFile.absolutePath)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    return
                }
            } catch (e: Exception) {
                android.util.Log.e("AvatarManager", "Falha ao descodificar imagem personalizada", e)
            }
        }
        imageView.setImageResource(R.drawable.avatar_profile)
    }
}
