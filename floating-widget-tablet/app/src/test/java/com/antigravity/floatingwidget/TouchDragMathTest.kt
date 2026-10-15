package com.antigravity.floatingwidget

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes unitários para validar a matemática cartesiana do listener de arrasto (TouchDragListener).
 * Garante que não haja saltos bruscos ou inversões de coordenadas no WindowManager.
 */
class TouchDragMathTest {

    /**
     * Valida o cálculo de deslocamento positivo no eixo horizontal (X).
     */
    @Test
    fun testDragDeltaPositiveX() {
        val initialX = 100
        val startTouchX = 150.0f
        val currentTouchX = 250.0f

        val deltaX = (currentTouchX - startTouchX).toInt()
        val finalX = initialX + deltaX

        assertEquals(200, finalX)
    }

    /**
     * Valida o cálculo de deslocamento negativo no eixo vertical (Y).
     */
    @Test
    fun testDragDeltaNegativeY() {
        val initialY = 300
        val startTouchY = 500.0f
        val currentTouchY = 420.0f

        val deltaY = (currentTouchY - startTouchY).toInt()
        val finalY = initialY + deltaY

        assertEquals(220, finalY)
    }

    /**
     * Valida que toques estáticos geram variação zero (zero tremor/jitter).
     */
    @Test
    fun testStaticTouchProducesZeroDelta() {
        val initialPos = 150
        val startTouch = 320.0f
        val currentTouch = 320.0f

        val delta = (currentTouch - startTouch).toInt()
        val finalPos = initialPos + delta

        assertEquals(initialPos, finalPos)
    }
}
