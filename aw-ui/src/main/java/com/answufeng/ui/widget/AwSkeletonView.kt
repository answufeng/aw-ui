package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import com.answufeng.ui.R

/**
 * Skeleton loading view with a shimmer/gradient animation sweeping left to right.
 *
 * Displays a rounded rectangle with a sweeping highlight gradient to indicate
 * loading state. The shimmer animation runs continuously until [stopShimmer] is called.
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwSkeletonView
 *     android:layout_width="match_parent"
 *     android:layout_height="20dp"
 *     app:skeleton_baseColor="#E0E0E0"
 *     app:skeleton_highlightColor="#F5F5F5"
 *     app:skeleton_cornerRadius="4dp"
 *     app:skeleton_duration="1000" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * skeletonView.startShimmer()
 * skeletonView.stopShimmer()
 * ```
 *
 * @property baseColor Base color of the skeleton. Default #E0E0E0.
 * @property highlightColor Highlight shimmer color. Default #F5F5F5.
 * @property cornerRadius Corner radius of the skeleton rectangle in pixels.
 * @property animationDuration Shimmer animation cycle duration in milliseconds. Default 1000.
 *
 * | XML Attribute | Description | Default |
 * |---|---|---|
 * | `skeleton_baseColor` | Base fill color | #E0E0E0 |
 * | `skeleton_highlightColor` | Shimmer highlight color | #F5F5F5 |
 * | `skeleton_cornerRadius` | Corner radius | 4dp |
 * | `skeleton_duration` | Animation duration (ms) | 1000 |
 */
class AwSkeletonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rectF = RectF()

    /**
     * Base color of the skeleton.
     */
    var baseColor: Int = Color.parseColor("#E0E0E0")
        set(value) {
            field = value
            basePaint.color = value
            invalidate()
        }

    /**
     * Highlight shimmer color.
     */
    var highlightColor: Int = Color.parseColor("#F5F5F5")
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Corner radius of the skeleton rectangle in pixels.
     */
    var cornerRadius: Float = 4f * resources.displayMetrics.density
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Shimmer animation cycle duration in milliseconds.
     */
    var animationDuration: Long = 1000L
        set(value) {
            field = value
            if (isShimmering) {
                stopShimmer()
                startShimmer()
            }
        }

    /**
     * Whether the shimmer animation is currently running.
     */
    var isShimmering: Boolean = false
        private set

    private var shimmerOffset: Float = 0f
    private var animator: ValueAnimator? = null

    init {
        val density = resources.displayMetrics.density
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSkeletonView)
        baseColor = ta.getColor(R.styleable.AwSkeletonView_skeleton_baseColor, Color.parseColor("#E0E0E0"))
        highlightColor = ta.getColor(R.styleable.AwSkeletonView_skeleton_highlightColor, Color.parseColor("#F5F5F5"))
        cornerRadius = ta.getDimension(R.styleable.AwSkeletonView_skeleton_cornerRadius, 4f * density)
        animationDuration = ta.getInteger(R.styleable.AwSkeletonView_skeleton_duration, 1000).toLong()
        ta.recycle()

        basePaint.color = baseColor
        shimmerPaint.isDither = true
    }

    /**
     * Starts the shimmer animation. If already running, this is a no-op.
     */
    fun startShimmer() {
        if (isShimmering) return
        isShimmering = true
        animator = ValueAnimator.ofFloat(-1f, 1f).apply {
            duration = animationDuration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation ->
                shimmerOffset = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * Stops the shimmer animation and resets the highlight offset.
     */
    fun stopShimmer() {
        animator?.cancel()
        animator = null
        isShimmering = false
        shimmerOffset = -1f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        path.reset()
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)

        canvas.drawPath(path, basePaint)

        if (isShimmering && shimmerOffset > -1f) {
            val gradientWidth = width.toFloat()
            val translateX = shimmerOffset * gradientWidth * 2
            val shader = LinearGradient(
                translateX - gradientWidth,
                0f,
                translateX,
                0f,
                intArrayOf(baseColor, highlightColor, baseColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            shimmerPaint.shader = shader
            canvas.drawPath(path, shimmerPaint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startShimmer()
    }

    override fun onDetachedFromWindow() {
        stopShimmer()
        super.onDetachedFromWindow()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.VISIBLE) {
            startShimmer()
        } else {
            stopShimmer()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putBoolean("isShimmering", isShimmering)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState: Parcelable? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                state.getParcelable("superState", Parcelable::class.java)
            } else {
                @Suppress("DEPRECATION")
                state.getParcelable("superState")
            }
            super.onRestoreInstanceState(superState)
            val wasShimmering = state.getBoolean("isShimmering", false)
            if (wasShimmering) startShimmer()
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
