package com.answufeng.ui.widget



import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import com.google.android.material.color.MaterialColors

class AwLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class Style {
        CIRCULAR, DOTS, HORIZONTAL, FLOWER, BARS
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

    var loaderSizePx: Int = 48f.dp().toInt()
        set(value) {
            val safe = value.coerceAtLeast(24f.dp().toInt())
            if (field == safe) return
            field = safe
            rebuild()
        }

    var isAnimating: Boolean = true
        private set

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwLoadingView)
        style = Style.values().getOrElse(ta.getInt(R.styleable.AwLoadingView_loading_style, 0)) { Style.CIRCULAR }
        if (ta.hasValue(R.styleable.AwLoadingView_loading_tint)) {
            tintColor = ta.getColor(R.styleable.AwLoadingView_loading_tint, Color.GRAY)
        }
        loaderSizePx = ta.getDimensionPixelSize(R.styleable.AwLoadingView_loading_size, 48f.dp().toInt())
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
        val superState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

        val primary = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, Color.GRAY)
        val resolvedTint = tintColor ?: primary

        val view = when (style) {
            Style.CIRCULAR -> ProgressBar(context).apply {
                isIndeterminate = true
                indeterminateDrawable?.setTint(resolvedTint)
            }
            Style.HORIZONTAL -> ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                isIndeterminate = true
                indeterminateDrawable?.setTint(resolvedTint)
            }
            Style.DOTS -> DotsLoaderView(context, resolvedTint).also { animatedChild = it }
            Style.FLOWER -> FlowerLoaderView(context, resolvedTint).also { animatedChild = it }
            Style.BARS -> BarsLoaderView(context, resolvedTint).also { animatedChild = it }
        }

        val layoutParams = LayoutParams(
            if (style == Style.HORIZONTAL) LayoutParams.MATCH_PARENT else loaderSizePx,
            loaderSizePx
        ).apply { gravity = Gravity.CENTER }
        addView(view, layoutParams)
        childView = view

        if (isAttachedToWindow && isAnimating) {
            animatedChild?.start()
        }
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density

    companion object {
        private const val KEY_SUPER_STATE = "superState"
        private const val KEY_STYLE = "style"
        private const val KEY_RUNNING = "running"
    }
}

private interface AnimatedLoader {
    fun start()
    fun stop()
}

private class DotsLoaderView(context: Context, dotColor: Int) : View(context), AnimatedLoader {
    private val radius = 4f.dp()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dotColor }
    private var phase = 0
    private val frame = object : Runnable {
        override fun run() {
            phase = (phase + 1) % 6
            invalidate()
            if (isAttachedToWindow) postDelayed(this, 90L)
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
        val step = 14f.dp()
        repeat(3) { index ->
            val t = (phase + 6 - (index * 2)) % 6
            val alpha = 0.35f + 0.65f * (t / 5f)
            paint.alpha = (255 * alpha.coerceIn(0.35f, 1f)).toInt()
            val cx = width / 2f + (index - 1) * step
            canvas.drawCircle(cx, cy, radius, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (40f.dp()).toInt()
        setMeasuredDimension(size, size)
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density
}

private class FlowerLoaderView(context: Context, lineColor: Int) : View(context), AnimatedLoader {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 3f.dp()
        strokeCap = Paint.Cap.ROUND
    }
    private var rotationDeg = 0f
    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1500L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotationDeg = it.animatedValue as Float
            invalidate()
        }
    }

    override fun start() {
        if (!animator.isStarted) animator.start() else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && animator.isPaused) animator.resume()
    }

    override fun stop() {
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val inner = 6f.dp()
        val outer = width / 2f - paint.strokeWidth * 2
        val count = 10
        canvas.save()
        canvas.rotate(rotationDeg, cx, cy)
        repeat(count) { index ->
            paint.alpha = (60 + 195 * index / count).coerceIn(60, 255)
            val angle = Math.PI * 2 * index / count
            val x1 = cx + (kotlin.math.cos(angle) * inner).toFloat()
            val y1 = cy + (kotlin.math.sin(angle) * inner).toFloat()
            val x2 = cx + (kotlin.math.cos(angle) * outer).toFloat()
            val y2 = cy + (kotlin.math.sin(angle) * outer).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 40f.dp().toInt()
        setMeasuredDimension(size, size)
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density
}

private class BarsLoaderView(context: Context, barColor: Int) : View(context), AnimatedLoader {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = barColor }
    private var phase = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 500L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    override fun start() {
        if (!animator.isStarted) animator.start() else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && animator.isPaused) animator.resume()
    }

    override fun stop() {
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val count = 3
        val gap = 4f.dp()
        val barWidth = 5f.dp()
        val maxHeight = height * 0.85f
        val baseLine = height * 0.9f
        var left = (width - (count * barWidth + (count - 1) * gap)) / 2f
        repeat(count) { index ->
            val wave = kotlin.math.sin(phase * kotlin.math.PI * 2 * 1.5 + index * 0.9).toFloat()
            val scale = 0.35f + 0.65f * (wave * 0.5f + 0.5f).coerceIn(0f, 1f)
            val barHeight = maxHeight * scale
            canvas.drawRoundRect(left, baseLine - barHeight, left + barWidth, baseLine, barWidth / 2f, barWidth / 2f, paint)
            left += barWidth + gap
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 40f.dp().toInt()
        setMeasuredDimension(size, size)
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density
}
