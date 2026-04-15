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
 * Circular progress bar with animated progress and optional center percentage text.
 *
 * Draws a background ring and a foreground arc representing the current progress.
 * Progress changes are animated by default using [ValueAnimator].
 *
 * ### XML usage
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
 * ### Programmatic usage
 * ```kotlin
 * circleProgressBar.progress = 75f
 * circleProgressBar.progressWithAnimation = 75f
 * ```
 *
 * @property progress Current progress value (0 to [max]). Default 0.
 * @property max Maximum progress value. Default 100.
 * @property strokeWidth Stroke width of the ring in pixels.
 * @property progressColor Color of the progress arc.
 * @property bgColor Color of the background ring.
 * @property showText Whether to display percentage text in the center.
 * @property textColor Color of the center text.
 * @property textSize Size of the center text in pixels.
 *
 * | XML Attribute | Description | Default |
 * |---|---|---|
 * | `circleProgress_progress` | Initial progress | 0 |
 * | `circleProgress_max` | Maximum value | 100 |
 * | `circleProgress_strokeWidth` | Ring stroke width | 8dp |
 * | `circleProgress_progressColor` | Arc color | #4CAF50 |
 * | `circleProgress_bgColor` | Background ring color | #E0E0E0 |
 * | `circleProgress_showText` | Show percentage text | true |
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
     * Maximum progress value.
     */
    var max: Float = 100f
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    /**
     * Current progress value (0 to [max]).
     */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, max)
            invalidate()
        }

    /**
     * Stroke width of the ring in pixels.
     */
    var strokeWidth: Float
        get() = bgPaint.strokeWidth
        set(value) {
            bgPaint.strokeWidth = value
            progressPaint.strokeWidth = value
            invalidate()
        }

    /**
     * Color of the progress arc.
     */
    var progressColor: Int
        get() = progressPaint.color
        set(value) {
            progressPaint.color = value
            invalidate()
        }

    /**
     * Color of the background ring.
     */
    var bgColor: Int
        get() = bgPaint.color
        set(value) {
            bgPaint.color = value
            invalidate()
        }

    /**
     * Whether to display percentage text in the center.
     */
    var showText: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Color of the center text.
     */
    var textColor: Int
        get() = textPaint.color
        set(value) {
            textPaint.color = value
            invalidate()
        }

    /**
     * Size of the center text in pixels.
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
     * Sets [progress] with an animation from the current value.
     *
     * @param target The target progress value.
     * @param duration Animation duration in milliseconds. Default 800.
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
