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
 * 骨架屏加载视图，带有从左到右扫过的闪光/渐变动画。
 *
 * 显示一个圆角矩形，带有扫过式高亮渐变以表示加载状态。
 * 闪光动画持续运行，直到调用 [stopShimmer]。
 *
 * ### XML 用法
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
 * ### 代码用法
 * ```kotlin
 * skeletonView.startShimmer()
 * skeletonView.stopShimmer()
 * ```
 *
 * @property baseColor 骨架屏基础颜色，默认 #E0E0E0
 * @property highlightColor 闪光高亮颜色，默认 #F5F5F5
 * @property cornerRadius 骨架屏矩形的圆角半径（像素）
 * @property animationDuration 闪光动画周期时长（毫秒），默认 1000
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `skeleton_baseColor` | 基础填充颜色 | #E0E0E0 |
 * | `skeleton_highlightColor` | 闪光高亮颜色 | #F5F5F5 |
 * | `skeleton_cornerRadius` | 圆角半径 | 4dp |
 * | `skeleton_duration` | 动画时长（毫秒） | 1000 |
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
    private val cachedMatrix = android.graphics.Matrix()

    /** 骨架屏基础颜色 */
    var baseColor: Int = Color.parseColor("#E0E0E0")
        set(value) {
            field = value
            basePaint.color = value
            invalidate()
        }

    /** 闪光高亮颜色 */
    var highlightColor: Int = Color.parseColor("#F5F5F5")
        set(value) {
            field = value
            invalidate()
        }

    /** 骨架屏矩形的圆角半径（像素） */
    var cornerRadius: Float = 4f * resources.displayMetrics.density
        set(value) {
            field = value
            invalidate()
        }

    /** 闪光动画周期时长（毫秒） */
    var animationDuration: Long = 1000L
        set(value) {
            field = value
            if (isShimmering) {
                stopShimmer()
                startShimmer()
            }
        }

    /** 闪光动画当前是否正在运行 */
    val isShimmering: Boolean = false
        private set

    var autoStart: Boolean = true

    private var shimmerOffset: Float = 0f
    private var animator: ValueAnimator? = null
    private var cachedShader: LinearGradient? = null

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

    /** 启动闪光动画。如果已在运行，则不执行任何操作 */
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

    /** 停止闪光动画并重置高亮偏移量 */
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
            val shader = cachedShader ?: LinearGradient(
                -gradientWidth,
                0f,
                0f,
                0f,
                intArrayOf(baseColor, highlightColor, baseColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            ).also { cachedShader = it }
            shader.setLocalMatrix(cachedMatrix.apply {
                setTranslate(translateX, 0f)
            })
            shimmerPaint.shader = shader
            canvas.drawPath(path, shimmerPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cachedShader = null
        if (isShimmering) {
            val gradientWidth = w.toFloat()
            cachedShader = LinearGradient(
                -gradientWidth, 0f, 0f, 0f,
                intArrayOf(baseColor, highlightColor, baseColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoStart) startShimmer()
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
