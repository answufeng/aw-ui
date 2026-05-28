package com.answufeng.ui.widget.skeleton

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.annotation.ColorInt

/**
 * 共享 shimmer 动画引擎：ValueAnimator + LinearGradient，供 [AwSkeletonView] 与 [AwSkeletonMaskView] 复用。
 */
internal class AwSkeletonShimmer(
    @ColorInt var baseColor: Int,
    @ColorInt var highlightColor: Int,
    var durationMs: Long = 1500L,
) {
    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true }
    private val matrix = Matrix()
    private var animator: ValueAnimator? = null
    private var shimmerOffset = -1f
    private var cachedShader: LinearGradient? = null
    private var gradientWidth = 0f

    var isRunning: Boolean = false
        private set

    var onInvalidate: (() -> Unit)? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        animator =
            ValueAnimator.ofFloat(-1f, 1f).apply {
                duration = durationMs
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                addUpdateListener {
                    shimmerOffset = it.animatedValue as Float
                    onInvalidate?.invoke()
                }
                start()
            }
    }

    fun stop() {
        animator?.cancel()
        animator = null
        isRunning = false
        shimmerOffset = -1f
        onInvalidate?.invoke()
    }

    fun invalidateShader(width: Int) {
        gradientWidth = width.toFloat()
        cachedShader = null
    }

    fun drawShimmerOnPath(
        canvas: Canvas,
        path: Path,
        width: Int,
    ) {
        if (!isRunning || shimmerOffset <= -1f || width <= 0) return
        val gw = if (gradientWidth > 0f) gradientWidth else width.toFloat()
        val translateX = shimmerOffset * gw * 2
        val shader =
            cachedShader ?: LinearGradient(
                -gw,
                0f,
                0f,
                0f,
                intArrayOf(baseColor, highlightColor, baseColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP,
            ).also { cachedShader = it }
        shader.setLocalMatrix(matrix.apply { setTranslate(translateX, 0f) })
        shimmerPaint.shader = shader
        canvas.drawPath(path, shimmerPaint)
    }

    fun updateColors(
        @ColorInt base: Int,
        @ColorInt highlight: Int,
    ) {
        baseColor = base
        highlightColor = highlight
        cachedShader = null
    }
}
