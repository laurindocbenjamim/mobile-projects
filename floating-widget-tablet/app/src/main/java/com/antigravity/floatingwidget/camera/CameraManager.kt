package com.antigravity.floatingwidget.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

/**
 * Gestor encapsulado do ciclo de vida da câmara utilizando Jetpack CameraX.
 * Previne memory leaks e bloqueios de câmara vinculando e desvinculando instâncias sob demanda.
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var isStreaming: Boolean = false

    companion object {
        private const val TAG = "CameraManager"
    }

    /**
     * Inicializa o ProcessCameraProvider de forma assíncrona.
     *
     * @param onReady Callback disparado quando o provedor da câmara estiver pronto.
     */
    fun initProvider(onReady: (() -> Unit)? = null) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "ProcessCameraProvider inicializado com sucesso.")
                onReady?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao obter ProcessCameraProvider", e)
            }
        }, mainExecutor)
    }

    /**
     * Vincula o Preview da câmara à PreviewView do widget flutuante.
     * Tenta primeiro a câmara frontal (ideal para widgets de avatar), recuando para traseira/externa.
     *
     * @param previewView Componente visual onde os frames serão renderizados.
     */
    fun startCamera(previewView: PreviewView) {
        val provider = cameraProvider ?: run {
            initProvider { startCamera(previewView) }
            return
        }

        Log.e(TAG, "Tentando iniciar câmara...")
        try {
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = selectBestCamera(provider)

            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            isStreaming = true
            Log.e(TAG, "Câmara vinculada com sucesso ao LifecycleOwner.")
        } catch (exc: Exception) {
            Log.e(TAG, "Erro ao vincular preview da câmara", exc)
        }
    }

    /**
     * Liberta os recursos da câmara desvinculando do ciclo de vida.
     * Essencial para transição ao Avatar estático ou destruição do serviço.
     */
    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            isStreaming = false
            Log.d(TAG, "Câmara desvinculada e recursos libertados.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar câmara", e)
        }
    }

    /**
     * Retorna o estado atual de transmissão de vídeo.
     */
    fun isCameraStreaming(): Boolean = isStreaming

    /**
     * Seleciona a câmara mais adequada disponível no dispositivo ou tablet.
     */
    private fun selectBestCamera(provider: ProcessCameraProvider): CameraSelector {
        return when {
            provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            else -> {
                CameraSelector.Builder().build()
            }
        }
    }
}
