package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.answufeng.ui.R
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 双滑块范围选择条。
 *
 * 用户可以拖动左右两个滑块来选择一个最小/最大范围值。
 * 支持通过 XML 属性配置最小值、最大值、步长、轨道颜色、进度颜色、滑块颜色等。
 * 当 showLabels 为 true 时，会在滑块上方显示当前值标签。
 */
class AwRangeSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_MIN = 0f
        private const val DEFAULT_MAX = 100f
        private const val DEFAULT_STEP = 1f
        private const val DEFAULT_THUMB_RADIUS_DP = 12f
        private const val DEFAULT_TRACK_HEIGHT_DP = 4f
        private const val DEFAULT_SHOW_LABELS = true
    }

    /** 当前左滑块值 */
    var leftValue: Float = DEFAULT_MIN
        private set

    /** 当前右滑块值 */
    var rightValue: Float = DEFAULT_MAX
        private set

    /** 范围变化监听器 */
    var onRangeChangeListener: ((left: Float, right: Float) -> Unit)? = null

    private var min = DEFAULT_MIN
    private var max = DEFAULT_MAX
    private var step = DEFAULT_STEP
    private var showLabels = DEFAULT_SHOW_LABELS

    private var thumbRadius: Float = 0f
    private var trackHeight: Float = 0f
    private var trackColor: Int = 0xFFBBBBBB.toInt()
    private var progressColor: Int = 0xFF4488FF.toInt()
    private var thumbColor: Int = 0xFF4488FF.toInt()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var draggingThumb: Int = 0 // 0=none, 1=left, 2=right
    private var touchDownX = 0f
    private var touchDownY = 0f

    private val density by lazy { context.resources.displayMetrics.density }

    init {
        attrs?.let { parseAttributes(it, defStyleAttr) }
        initPaints()
    }

    private fun parseAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.AwRangeSeekBar, defStyleAttr, 0
        )

        min = a.getFloat(R.styleable.AwRangeSeekBar_rsb_min, DEFAULT_MIN)
        max = a.getFloat(R.styleable.AwRangeSeekBar_rsb_max, DEFAULT_MAX)
        step = a.getFloat(R.styleable.AwRangeSeekBar_rsb_step, DEFAULT_STEP)
        leftValue = a.getFloat(R.styleable.AwRangeSeekBar_rsb_leftValue, min)
        rightValue = a.getFloat(R.styleable.AwRangeSeekBar_rsb_rightValue, max)
        trackColor = a.getColor(R.styleable.AwRangeSeekBar_rsb_trackColor, trackColor)
        progressColor = a.getColor(R.styleable.AwRangeSeekBar_rsb_progressColor, progressColor)
        thumbColor = a.getColor(R.styleable.AwRangeSeekBar_rsb_thumbColor, thumbColor)
        thumbRadius = a.getDimension(
            R.styleable.AwRangeSeekBar_rsb_thumbRadius,
            DEFAULT_THUMB_RADIUS_DP * density
        )
        trackHeight = a.getDimension(
            R.styleable.AwRangeSeekBar_rsb_trackHeight,
            DEFAULT_TRACK_HEIGHT_DP * density
        )
        showLabels = a.getBoolean(R.styleable.AwRangeSeekBar_rsb_showLabels, DEFAULT_SHOW_LABELS)

        a.recycle()

        // 确保初始值在合法范围内
        leftValue = snapToStep(leftValue.coerceIn(min, max))
        rightValue = snapToStep(rightValue.coerceIn(min, max))
        if (leftValue > rightValue) {
            leftValue = rightValue
        }
    }

    private fun initPaints() {
        trackPaint.color = trackColor
        trackPaint.style = Paint.Style.FILL

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.FILL

        thumbPaint.color = thumbColor
        thumbPaint.style = Paint.Style.FILL

        labelPaint.color = 0xFFFFFFFF.toInt()
        labelPaint.textSize = thumbRadius * 0.8f
        labelPaint.textAlign = Paint.Align.CENTER

        labelBackgroundPaint.color = progressColor
        labelBackgroundPaint.style = Paint.Style.FILL
    }

    /**
     * 设置范围参数。
     *
     * @param min 最小值
     * @param max 最大值
     * @param step 步长
     */
    fun setRange(min: Float, max: Float, step: Float) {
        this.min = min
        this.max = max
        this.step = step.coerceAtLeast(0f)
        leftValue = snapToStep(leftValue.coerceIn(min, max))
        rightValue = snapToStep(rightValue.coerceIn(min, max))
        if (leftValue > rightValue) {
            leftValue = rightValue
        }
        invalidate()
    }

    private fun snapToStep(value: Float): Float {
        if (step <= 0f) return value
        val steps = ((value - min) / step).roundToInt()
        return min + steps * step
    }

    private fun getTrackLeft(): Float = paddingLeft.toFloat() + thumbRadius

    private fun getTrackRight(): Float = width.toFloat() - paddingRight.toFloat() - thumbRadius

    private fun getTrackWidth(): Float = getTrackRight() - getTrackLeft()

    private fun valueToX(value: Float): Float {
        val range = max - min
        if (range <= 0f) return getTrackLeft()
        val fraction = (value - min) / range
        return getTrackLeft() + fraction * getTrackWidth()
    }

    private fun xToValue(x: Float): Float {
        val trackWidth = getTrackWidth()
        if (trackWidth <= 0f) return min
        val fraction = ((x - getTrackLeft()) / trackWidth).coerceIn(0f, 1f)
        return min + fraction * (max - min)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val trackLeft = getTrackLeft()
        val trackRight = getTrackRight()
        val trackCenterY = paddingTop + (height - paddingTop - paddingBottom) / 2f

        // 绘制背景轨道
        val trackRect = RectF(
            trackLeft,
            trackCenterY - trackHeight / 2f,
            trackRight,
            trackCenterY + trackHeight / 2f
        )
        canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, trackPaint)

        // 绘制进度轨道（两滑块之间）
        val leftX = valueToX(leftValue)
        val rightX = valueToX(rightValue)
        val progressRect = RectF(
            leftX,
            trackCenterY - trackHeight / 2f,
            rightX,
            trackCenterY + trackHeight / 2f
        )
        canvas.drawRoundRect(progressRect, trackHeight / 2f, trackHeight / 2f, progressPaint)

        // 绘制左滑块
        canvas.drawCircle(leftX, trackCenterY, thumbRadius, thumbPaint)

        // 绘制右滑块
        canvas.drawCircle(rightX, trackCenterY, thumbRadius, thumbPaint)

        // 绘制标签
        if (showLabels) {
            drawLabel(canvas, leftX, trackCenterY, formatValue(leftValue))
            drawLabel(canvas, rightX, trackCenterY, formatValue(rightValue))
        }
    }

    private fun drawLabel(canvas: Canvas, x: Float, trackCenterY: Float, text: String) {
        val labelOffset = thumbRadius + thumbRadius * 0.6f
        val labelY = trackCenterY - labelOffset

        val textWidth = labelPaint.measureText(text)
        val labelPaddingH = thumbRadius * 0.5f
        val labelPaddingV = thumbRadius * 0.3f
        val labelHeight = labelPaint.textSize + labelPaddingV * 2f

        val bgRect = RectF(
            x - textWidth / 2f - labelPaddingH,
            labelY - labelHeight / 2f - labelPaddingV,
            x + textWidth / 2f + labelPaddingH,
            labelY + labelHeight / 2f + labelPaddingV
        )
        val cornerRadius = labelHeight / 2f
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, labelBackgroundPaint)

        canvas.drawText(text, x, labelY + labelPaint.textSize / 3f, labelPaint)
    }

    private fun formatValue(value: Float): String {
        return if (step >= 1f && step == step.toInt().toFloat()) {
            value.toInt().toString()
        } else {
            val decimalPlaces = if (step > 0f) {
                val s = step.toString()
                val dotIndex = s.indexOf('.')
                if (dotIndex >= 0) s.length - dotIndex - 1 else 0
            } else 1
            String.format("%.${decimalPlaces}f", value)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = event.x
                touchDownY = event.y
                draggingThumb = getClosestThumb(event.x, event.y)
                if (draggingThumb != 0) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                return draggingThumb != 0
            }
            MotionEvent.ACTION_MOVE -> {
                if (draggingThumb != 0) {
                    val rawValue = xToValue(event.x)
                    val snappedValue = snapToStep(rawValue.coerceIn(min, max))
                    if (draggingThumb == 1) {
                        val newLeft = snappedValue.coerceAtMost(rightValue)
                        if (newLeft != leftValue) {
                            leftValue = newLeft
                            onRangeChangeListener?.invoke(leftValue, rightValue)
                            invalidate()
                        }
                    } else {
                        val newRight = snappedValue.coerceAtLeast(leftValue)
                        if (newRight != rightValue) {
                            rightValue = newRight
                            onRangeChangeListener?.invoke(leftValue, rightValue)
                            invalidate()
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (draggingThumb != 0) {
                    onRangeChangeListener?.invoke(leftValue, rightValue)
                }
                draggingThumb = 0
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getClosestThumb(x: Float, y: Float): Int {
        val trackCenterY = height / 2f
        val leftX = valueToX(leftValue)
        val rightX = valueToX(rightValue)

        val distToLeft = sqrt((x - leftX) * (x - leftX) + (y - trackCenterY) * (y - trackCenterY))
        val distToRight = sqrt((x - rightX) * (x - rightX) + (y - trackCenterY) * (y - trackCenterY))

        val touchRadius = thumbRadius * 2f
        if (distToLeft > touchRadius && distToRight > touchRadius) {
            // 如果两个滑块都不在触摸范围内，选择水平方向更近的
            return if (abs(x - leftX) <= abs(x - rightX)) 1 else 2
        }

        return if (distToLeft <= distToRight) 1 else 2
    }

    private fun sqrt(v: Float): Float = kotlin.math.sqrt(v)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (thumbRadius * 2 + if (showLabels) thumbRadius * 2f else 0f).toInt() +
                paddingTop + paddingBottom

        val width = resolveSize(0, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}
