package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.answufeng.ui.R
import com.answufeng.ui.dp
import com.answufeng.ui.dpFloat

class AwLoadingView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        enum class Style {
            CIRCULAR,
            DOTS,
            HORIZONTAL,
            FLOWER,
            BARS,
        }

        private var childView: View? = null
        private var animatedChild: AnimatedLoader? = null

        var style: Style = Style.CIRCULAR
            set(value) {
                if (field == value) return
                field = value
                rebuild()
            }

        var tintColor: Int? = null
            set(value) {
                field = value
                rebuild()
            }

        var loaderSizePx: Int = 48
            set(value) {
                val safe = value.coerceAtLeast(24)
                if (field == safe) return
                field = safe
                rebuild()
            }

        var isAnimating: Boolean = true
            private set

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwLoadingView)
            style = Style.entries.getOrElse(ta.getInt(R.styleable.AwLoadingView_loading_style, 0)) { Style.CIRCULAR }
            if (ta.hasValue(R.styleable.AwLoadingView_loading_tint)) {
                tintColor = ta.getColor(R.styleable.AwLoadingView_loading_tint, DEFAULT_COLOR)
            }
            loaderSizePx = ta.getDimensionPixelSize(R.styleable.AwLoadingView_loading_size, 48.dp)
            ta.recycle()
            rebuild()
        }

        fun setColorTint(color: Int) {
            tintColor = color
        }

        fun start() {
            isAnimating = true
            animatedChild?.start()
        }

        fun stop() {
            isAnimating = false
            animatedChild?.stop()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (isAnimating) animatedChild?.start()
        }

        override fun onDetachedFromWindow() {
            animatedChild?.stop()
            super.onDetachedFromWindow()
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
                putString(KEY_STYLE, style.name)
                putBoolean(KEY_RUNNING, isAnimating)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state !is Bundle) {
                super.onRestoreInstanceState(state)
                return
            }
            val superState =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    state.getParcelable(KEY_SUPER_STATE, Parcelable::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    state.getParcelable(KEY_SUPER_STATE)
                }
            super.onRestoreInstanceState(superState)
            style = state.getString(KEY_STYLE)?.let { Style.valueOf(it) } ?: Style.CIRCULAR
            if (state.getBoolean(KEY_RUNNING, true)) start() else stop()
        }

        private fun rebuild() {
            childView?.let { removeView(it) }
            animatedChild?.stop()
            animatedChild = null

            val resolvedTint = tintColor ?: DEFAULT_COLOR

            val view =
                when (style) {
                    Style.CIRCULAR ->
                        ProgressBar(context).apply {
                            isIndeterminate = true
                            indeterminateDrawable?.setTint(resolvedTint)
                        }
                    Style.HORIZONTAL -> HorizontalLoaderView(context, resolvedTint).also { animatedChild = it }
                    Style.DOTS -> DotsLoaderView(context, resolvedTint).also { animatedChild = it }
                    Style.FLOWER -> FlowerLoaderView(context, resolvedTint).also { animatedChild = it }
                    Style.BARS -> BarsLoaderView(context, resolvedTint).also { animatedChild = it }
                }

            val layoutParams = LayoutParams(loaderSizePx, loaderSizePx).apply { gravity = Gravity.CENTER }
            addView(view, layoutParams)
            childView = view

            if (isAttachedToWindow && isAnimating) {
                animatedChild?.start()
            }
        }

        companion object {
            private const val KEY_SUPER_STATE = "superState"
            private const val KEY_STYLE = "style"
            private const val KEY_RUNNING = "running"
            const val DEFAULT_COLOR = 0xFF999999.toInt()
        }
    }

private interface AnimatedLoader {
    fun start()

    fun stop()
}

private class DotsLoaderView(context: Context, dotColor: Int) : View(context), AnimatedLoader {
    private val radius = 4f.dpFloat
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dotColor }
    private var phase = 0
    private val frame =
        object : Runnable {
            override fun run() {
                phase = (phase + 1) % 6
                invalidate()
                if (isAttachedToWindow) postDelayed(this, 120L)
            }
        }

    override fun start() {
        removeCallbacks(frame)
        post(frame)
    }

    override fun stop() {
        removeCallbacks(frame)
    }

    override fun onDraw(canvas: Canvas) {
        val cy = height / 2f
        val step = 14f.dpFloat
        repeat(3) { index ->
            val t = (phase + 6 - (index * 2)) % 6
            val alpha = 0.3f + 0.7f * (t / 5f)
            paint.alpha = (255 * alpha.coerceIn(0.3f, 1f)).toInt()
            val cx = width / 2f + (index - 1) * step
            canvas.drawCircle(cx, cy, radius, paint)
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val size = 40.dp
        setMeasuredDimension(size, size)
    }
}

private class HorizontalLoaderView(context: Context, barColor: Int) : View(context), AnimatedLoader {
    private val trackPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = barColor
            alpha = 40
        }
    private val indicatorPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = barColor
        }
    private var phase = 0f
    private val trackHeight = 3f.dpFloat
    private val indicatorWidthRatio = 0.35f
    private val animator =
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
        }

    override fun start() {
        if (!animator.isStarted) {
            animator.start()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && animator.isPaused) {
            animator.resume()
        }
    }

    override fun stop() {
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cy = h / 2f
        val halfTrack = trackHeight / 2f

        canvas.drawRoundRect(0f, cy - halfTrack, w, cy + halfTrack, halfTrack, halfTrack, trackPaint)

        val indW = w * indicatorWidthRatio
        val travel = w - indW
        val t = phase * 2f
        val progress = if (t <= 1f) t else 2f - t
        val indLeft = progress * travel
        val indRight = indLeft + indW

        canvas.drawRoundRect(indLeft, cy - halfTrack, indRight, cy + halfTrack, halfTrack, halfTrack, indicatorPaint)
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val size = 40.dp
        setMeasuredDimension(size, size)
    }
}

private class FlowerLoaderView(context: Context, lineColor: Int) : View(context), AnimatedLoader {
    private val petalCount = 8
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColor
            strokeCap = Paint.Cap.ROUND
        }
    private var phase = 0f
    private val strokeWidth = 2.5f.dpFloat
    private val animator =
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 750L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
        }

    override fun start() {
        if (!animator.isStarted) {
            animator.start()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && animator.isPaused) {
            animator.resume()
        }
    }

    override fun stop() {
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) / 2f - strokeWidth
        val innerR = radius * 0.56f
        val petalLen = radius * 0.34f

        for (i in 0 until petalCount) {
            val offset = (i.toFloat() / petalCount - phase + 1f) % 1f
            val alpha =
                when {
                    offset < 0.5f -> 0.08f + 0.92f * (offset * 2f).coerceIn(0f, 1f)
                    else -> 0.08f + 0.92f * ((1f - offset) * 2f).coerceIn(0f, 1f)
                }
            paint.alpha = (255 * alpha).toInt()
            paint.strokeWidth = strokeWidth

            val angle = Math.PI * 2 * i / petalCount - Math.PI / 2
            val x1 = cx + (kotlin.math.cos(angle) * innerR).toFloat()
            val y1 = cy + (kotlin.math.sin(angle) * innerR).toFloat()
            val x2 = cx + (kotlin.math.cos(angle) * (innerR + petalLen)).toFloat()
            val y2 = cy + (kotlin.math.sin(angle) * (innerR + petalLen)).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val size = 40.dp
        setMeasuredDimension(size, size)
    }
}

private class BarsLoaderView(context: Context, barColor: Int) : View(context), AnimatedLoader {
    private val barCount = 5
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = barColor }
    private var phase = 0f
    private val animator =
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
        }

    override fun start() {
        if (!animator.isStarted) {
            animator.start()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && animator.isPaused) {
            animator.resume()
        }
    }

    override fun stop() {
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val gap = 3f.dpFloat
        val barWidth = 4f.dpFloat
        val maxHeight = height * 0.85f
        val baseLine = height * 0.9f
        var left = (width - (barCount * barWidth + (barCount - 1) * gap)) / 2f
        repeat(barCount) { index ->
            val wave = kotlin.math.sin(phase * kotlin.math.PI * 2 + index * 0.7).toFloat()
            val scale = 0.3f + 0.7f * (wave * 0.5f + 0.5f).coerceIn(0f, 1f)
            val barHeight = maxHeight * scale
            canvas.drawRoundRect(
                left,
                baseLine - barHeight,
                left + barWidth,
                baseLine,
                barWidth / 2f,
                barWidth / 2f,
                paint,
            )
            left += barWidth + gap
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val size = 40.dp
        setMeasuredDimension(size, size)
    }
}
