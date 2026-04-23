package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.answufeng.ui.R
import com.google.android.material.color.MaterialColors

/**
 * 常用 loading 展示：转圈、三点、横向、**菊花（放射线）**、**竖条起伏**等。用于列表占位、空态、按钮内等。
 */
class AwLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class Style {
        CIRCULAR,
        DOTS,
        HORIZONTAL,
        /** 菊花形放射条旋转。 */
        FLOWER,
        /** 多竖条起伏（Material/音频条风格）。 */
        BARS
    }

    var style: Style = Style.CIRCULAR
        set(value) {
            if (field == value) return
            field = value
            rebuild()
        }

    private var child: View? = null

    private var tintColor: Int? = null

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwLoadingView)
        val s = ta.getInt(R.styleable.AwLoadingView_loading_style, 0)
        style = Style.values().getOrElse(s) { Style.CIRCULAR }
        if (ta.hasValue(R.styleable.AwLoadingView_loading_tint)) {
            tintColor = ta.getColor(R.styleable.AwLoadingView_loading_tint, Color.GRAY)
        }
        ta.recycle()
        rebuild()
    }

    fun setColorTint(color: Int) {
        tintColor = color
        rebuild()
    }

    private fun rebuild() {
        child?.let { removeView(it) }
        val primary = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimary,
            Color.GRAY
        )
        val c = tintColor ?: primary
        val v = when (style) {
            Style.CIRCULAR -> ProgressBar(context).apply {
                isIndeterminate = true
                indeterminateDrawable?.setTint(c)
            }
            Style.HORIZONTAL -> ProgressBar(
                context,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                isIndeterminate = true
                indeterminateDrawable?.setTint(c)
            }
            Style.DOTS -> AwDotsLoaderView(context, c)
            Style.FLOWER -> AwFlowerLoaderView(context, c)
            Style.BARS -> AwBarsLoaderView(context, c)
        }
        val box = (48f * resources.displayMetrics.density).toInt()
        val lp = LayoutParams(
            if (style == Style.HORIZONTAL) LayoutParams.MATCH_PARENT else box,
            box
        ).apply { gravity = Gravity.CENTER }
        addView(v, lp)
        child = v
    }
}

/** 三圆点轻量脉动，用于 [AwLoadingView.Style.DOTS] */
private class AwDotsLoaderView(
    context: Context,
    dotColor: Int
) : View(context) {
    private val r = 4f * resources.displayMetrics.density
    private var phase = 0
    private val runner = object : Runnable {
        override fun run() {
            phase = (phase + 1) % 6
            invalidate()
            if (isAttachedToWindow) postDelayed(this, 90L)
        }
    }
    private val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = dotColor
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(runner)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(runner)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val cy = height / 2f
        val step = 14f * resources.displayMetrics.density
        for (i in 0..2) {
            val t = (phase + 6 - (i * 2)) % 6
            val a = 0.35f + 0.65f * (t / 5f)
            p.alpha = (255 * a.coerceIn(0.35f, 1f)).toInt()
            val cx = width / 2f + (i - 1) * step
            canvas.drawCircle(cx, cy, r, p)
        }
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val s = (r * 2 + 2 * 16f * resources.displayMetrics.density).toInt()
        setMeasuredDimension(s, s)
    }
}

/** 菊花：放射线随整图旋转。 */
private class AwFlowerLoaderView(context: Context, private val lineColor: Int) : View(context) {
    private val n = 10
    private var rotationDeg = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lineColor; strokeWidth = 3f * resources.displayMetrics.density; strokeCap = Paint.Cap.ROUND }
    private val anim: ValueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 800L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotationDeg = animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        anim.start()
    }

    override fun onDetachedFromWindow() {
        anim.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r1 = 6f * resources.displayMetrics.density
        val r2 = width / 2f - paint.strokeWidth * 2
        canvas.save()
        canvas.rotate(rotationDeg, cx, cy)
        for (i in 0 until n) {
            paint.alpha = (60 + 195 * i / n).coerceIn(60, 255)
            val ang = Math.PI * 2 * i / n
            val x1 = cx + (Math.cos(ang) * r1).toFloat()
            val y1 = cy + (Math.sin(ang) * r1).toFloat()
            val x2 = cx + (Math.cos(ang) * r2).toFloat()
            val y2 = cy + (Math.sin(ang) * r2).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
        canvas.restore()
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val s = (40f * resources.displayMetrics.density).toInt()
        setMeasuredDimension(s, s)
    }
}

/** 竖条节奏动画。 */
private class AwBarsLoaderView(context: Context, private val barColor: Int) : View(context) {
    private val n = 3
    private var phase = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = barColor }
    private val anim: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 500L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        anim.start()
    }

    override fun onDetachedFromWindow() {
        anim.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val gap = 4f * resources.displayMetrics.density
        val wBar = 5f * resources.displayMetrics.density
        val hMax = height * 0.85f
        val yBase = height * 0.9f
        var left = (width - (n * wBar + (n - 1) * gap)) / 2f
        for (i in 0 until n) {
            val wave = kotlin.math.sin((phase * kotlin.math.PI * 2 * 1.5 + i * 0.9)).toFloat()
            val s = 0.35f + 0.65f * (wave * 0.5f + 0.5f).coerceIn(0f, 1f)
            val hB = hMax * s
            canvas.drawRoundRect(left, yBase - hB, left + wBar, yBase, wBar / 2f, wBar / 2f, paint)
            left += wBar + gap
        }
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val s = (40f * resources.displayMetrics.density).toInt()
        setMeasuredDimension(s, s)
    }
}
