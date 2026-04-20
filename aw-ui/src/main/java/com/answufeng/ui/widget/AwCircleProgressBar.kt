package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import com.answufeng.ui.R
import kotlin.math.min

/**
 * 圆形进度条，支持动画进度和可选的中心百分比文字。
 *
 * 绘制一个背景圆环和一个表示当前进度的前景弧线。
 * 进度变化默认使用 [ValueAnimator] 进行动画过渡。
 *
 * ### XML 使用
 * ```xml
 * <com.answufeng.ui.widget.AwCircleProgressBar
 *     android:layout_width="120dp"
 *     android:layout_height="120dp"
 *     app:circleProgress_progress="65"
 *     app:circleProgress_max="100"
 *     app:circleProgress_strokeWidth="8dp"
 *     app:circleProgress_progressColor="#4CAF50"
 *     app:circleProgress_bgColor="#E0E0E0"
 *     app:circleProgress_showText="true" />
 * ```
 *
 * ### 代码使用
 * ```kotlin
 * circleProgressBar.progress = 75f
 * circleProgressBar.progressWithAnimation = 75f
 * ```
 *
 * @property progress 当前进度值（0 到 [max]），默认 0。
 * @property max 最大进度值，默认 100。
 * @property strokeWidth 圆环描边宽度（像素）。
 * @property progressColor 进度弧线颜色。
 * @property bgColor 背景圆环颜色。
 * @property showText 是否在中心显示百分比文字。
 * @property textColor 中心文字颜色。
 * @property textSize 中心文字大小（像素）。
 *
 * | XML 属性 | 描述 | 默认值 |
 * |---|---|---|
 * | `circleProgress_progress` | 初始进度 | 0 |
 * | `circleProgress_max` | 最大值 | 100 |
 * | `circleProgress_strokeWidth` | 圆环描边宽度 | 8dp |
 * | `circleProgress_progressColor` | 弧线颜色 | #4CAF50 |
 * | `circleProgress_bgColor` | 背景圆环颜色 | #E0E0E0 |
 * | `circleProgress_showText` | 显示百分比文字 | true |
 */
class AwCircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()

    /**
     * 最大进度值。
     */
    var max: Float = 100f
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    /**
     * 当前进度值（0 到 [max]）。
     */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, max)
            invalidate()
        }

    /**
     * 圆环描边宽度（像素）。
     */
    var strokeWidth: Float
        get() = bgPaint.strokeWidth
        set(value) {
            bgPaint.strokeWidth = value
            progressPaint.strokeWidth = value
            invalidate()
        }

    /**
     * 进度弧线颜色。
     */
    var progressColor: Int
        get() = progressPaint.color
        set(value) {
            progressPaint.color = value
            invalidate()
        }

    /**
     * 背景圆环颜色。
     */
    var bgColor: Int
        get() = bgPaint.color
        set(value) {
            bgPaint.color = value
            invalidate()
        }

    /**
     * 是否在中心显示百分比文字。
     */
    var showText: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 中心文字颜色。
     */
    var textColor: Int
        get() = textPaint.color
        set(value) {
            textPaint.color = value
            invalidate()
        }

    /**
     * 中心文字大小（像素）。
     */
    var textSize: Float
        get() = textPaint.textSize
        set(value) {
            textPaint.textSize = value
            invalidate()
        }

    private var animator: ValueAnimator? = null

    init {
        val density = resources.displayMetrics.density
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwCircleProgressBar)
        progress = ta.getFloat(R.styleable.AwCircleProgressBar_circleProgress_progress, 0f)
        max = ta.getFloat(R.styleable.AwCircleProgressBar_circleProgress_max, 100f)
        val defaultStroke = 8f * density
        strokeWidth = ta.getDimension(R.styleable.AwCircleProgressBar_circleProgress_strokeWidth, defaultStroke)
        progressColor = ta.getColor(R.styleable.AwCircleProgressBar_circleProgress_progressColor, Color.parseColor("#4CAF50"))
        bgColor = ta.getColor(R.styleable.AwCircleProgressBar_circleProgress_bgColor, Color.parseColor("#E0E0E0"))
        showText = ta.getBoolean(R.styleable.AwCircleProgressBar_circleProgress_showText, true)
        ta.recycle()

        textPaint.color = Color.DKGRAY
        textPaint.textSize = 14f * density
    }

    /**
     * 以动画方式设置 [progress]。
     *
     * @param target 目标进度值。
     * @param duration 动画时长（毫秒），默认 800。
     */
    fun setProgressWithAnimation(target: Float, duration: Long = 800) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(progress, target.coerceIn(0f, max)).apply {
            this.duration = duration
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
            }
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (strokeWidth * 2 + 40 * resources.displayMetrics.density).toInt()
        val w = resolveSize(desired, widthMeasureSpec)
        val h = resolveSize(desired, heightMeasureSpec)
        val size = min(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val halfStroke = strokeWidth / 2f
        rectF.set(halfStroke, halfStroke, width - halfStroke, height - halfStroke)

        canvas.drawArc(rectF, 0f, 360f, false, bgPaint)

        val sweepAngle = if (max > 0f) (progress / max) * 360f else 0f
        if (sweepAngle > 0f) {
            canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
        }

        if (showText && max > 0f) {
            val percent = (progress / max * 100).toInt()
            val text = "$percent%"
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, width / 2f, y, textPaint)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putFloat("progress", progress)
            putFloat("max", max)
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
            progress = state.getFloat("progress", 0f)
            max = state.getFloat("max", 100f)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
