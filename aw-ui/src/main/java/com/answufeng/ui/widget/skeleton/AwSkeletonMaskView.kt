package com.answufeng.ui.widget.skeleton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.answufeng.ui.statelayout.StateTransition

/**
 * 骨架屏遮罩绘制层，覆盖在 content 之上绘制各 mask 区域与 shimmer。
 */
internal class AwSkeletonMaskView(
    context: Context,
) : View(context) {
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val shimmer = AwSkeletonShimmer(0, 0)
    private var targets: List<AwSkeletonMaskTarget> = emptyList()
    private var config: AwSkeletonConfig = AwSkeletonConfig.default(context)

    init {
        shimmer.onInvalidate = { invalidate() }
        isClickable = true
        isFocusable = true
    }

    fun applyConfig(config: AwSkeletonConfig) {
        this.config = config
        basePaint.color = config.maskColor
        shimmer.updateColors(config.maskColor, config.shimmerColor)
        shimmer.durationMs = config.shimmerDurationMs
        invalidateShader()
        invalidate()
    }

    fun setTargets(targets: List<AwSkeletonMaskTarget>) {
        this.targets = targets
        invalidateShader()
        invalidate()
    }

    fun startShimmer() {
        if (config.showShimmer) shimmer.start()
    }

    fun stopShimmer() {
        shimmer.stop()
    }

    private fun invalidateShader() {
        shimmer.invalidateShader(width.coerceAtLeast(1))
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidateShader()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (target in targets) {
            path.reset()
            path.addRoundRect(
                target.bounds,
                target.cornerRadiusPx,
                target.cornerRadiusPx,
                Path.Direction.CW,
            )
            canvas.drawPath(path, basePaint)
            if (config.showShimmer) {
                shimmer.drawShimmerOnPath(canvas, path, width)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (config.showShimmer) shimmer.start()
    }

    override fun onDetachedFromWindow() {
        shimmer.stop()
        super.onDetachedFromWindow()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            shimmer.stop()
        } else if (visibility == VISIBLE && config.showShimmer) {
            shimmer.start()
        }
    }
}

internal fun fadeInContent(
    view: View,
    durationMs: Long,
) {
    StateTransition.FADE.transition(view, durationMs)
}
