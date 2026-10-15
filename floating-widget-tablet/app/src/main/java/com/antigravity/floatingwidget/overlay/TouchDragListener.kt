package com.antigravity.floatingwidget.overlay

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

/**
 * Listener de toque para arrasto fluído e deteção de duplo toque no widget flutuante.
 * Elimina trepidações calculando deltas relativos de coordenadas raw do ecrã.
 */
class TouchDragListener(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val containerView: View? = null,
    private val onDoubleTap: () -> Unit,
    private val onSingleTap: (() -> Unit)? = null
) : View.OnTouchListener {

    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var lastTapTime: Long = 0L

    companion object {
        private const val DOUBLE_TAP_TIMEOUT_MS = 400L
        private const val TOUCH_SLOP_PIXELS = 25f
    }

    /**
     * Trata os eventos de toque: ACTION_DOWN, ACTION_MOVE e ACTION_UP.
     * Atualiza os LayoutParams do WindowManager sem atraso de frame.
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                android.util.Log.d("TouchDragListener", "ACTION_DOWN at ($initialTouchX, $initialTouchY)")
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // Cálculo de delta relativo para evitar trepidações (jittering)
                val deltaX = (event.rawX - initialTouchX).toInt()
                val deltaY = (event.rawY - initialTouchY).toInt()

                layoutParams.x = initialX + deltaX
                layoutParams.y = initialY + deltaY

                windowManager.updateViewLayout(containerView ?: v, layoutParams)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val totalDistanceX = abs(event.rawX - initialTouchX)
                val totalDistanceY = abs(event.rawY - initialTouchY)
                android.util.Log.d("TouchDragListener", "ACTION_UP dist: ($totalDistanceX, $totalDistanceY), slop: $TOUCH_SLOP_PIXELS")

                // Verifica se o movimento foi estático (toque simples ou duplo)
                if (totalDistanceX < TOUCH_SLOP_PIXELS && totalDistanceY < TOUCH_SLOP_PIXELS) {
                    val currentTime = System.currentTimeMillis()
                    val timeDiff = currentTime - lastTapTime
                    android.util.Log.d("TouchDragListener", "Tap detected! timeDiff: $timeDiff")
                    if (timeDiff < DOUBLE_TAP_TIMEOUT_MS) {
                        android.util.Log.d("TouchDragListener", "Triggering onDoubleTap")
                        onDoubleTap.invoke()
                        lastTapTime = 0L
                    } else {
                        lastTapTime = currentTime
                        android.util.Log.d("TouchDragListener", "Triggering onSingleTap")
                        onSingleTap?.invoke()
                    }
                }
                return true
            }
        }
        return false
    }

    /**
     * Utilitário puro para cálculo de nova posição cartesiana no ecrã.
     */
    fun calculateNewPosition(initPos: Int, currentRaw: Float, startRaw: Float): Int {
        return initPos + (currentRaw - startRaw).toInt()
    }
}
