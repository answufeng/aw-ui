package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.answufeng.ui.R
import kotlin.math.roundToInt

/**
 * 倒计时视图组件，用于显示倒计时动画和文字。
 *
 * 支持圆形进度动画和文本显示，可自定义颜色、大小、描边宽度等属性。
 * 倒计时过程中可设置监听器来监听状态变化（进行中、完成、跳过）。
 *
 * ### XML 属性
 * - `countDownStrokeColor`: 倒计时圆环颜色（默认黑色）
 * - `countDownTextColor`: 倒计时文字颜色（默认黑色）
 * - `countDownTrackColor`: 圆环轨道填充颜色（默认透明，非整 View 背景）
 * - `countDownStrokeWidth`: 倒计时圆环描边宽度（默认 4dp）
 * - `countDownTextSize`: 倒计时文字大小（默认 24sp）
 * - `countdown_seconds`: 默认倒计时时长（秒），>0 时作为 [start] / [startSeconds] 未传参时的默认总时长
 *
 * ### 用法
 * ```kotlin
 * countDownView.start(durationMs = 3000, maxMs = 3000)
 * countDownView.setCountDownListener(object : AwCountDownView.CountDownListener {
 *     override fun onFinish() { /* 倒计时完成 */ }
 *     override fun onSkip() { /* 用户跳过 */ }
 *     override fun onProgress(progress: Int, remaining: Long) { /* 进度更新 */ }
 * })
 * ```
 */
class AwCountDownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_DURATION_MS = 3000L
        private const val DEFAULT_MAX_MS = 3000L
        private const val DEFAULT_TEXT_SIZE_SP = 24f
    }

    /** 倒计时过程中更新 UI 的 Handler */
    private val updateHandler = Handler(Looper.getMainLooper())

    /** 倒计时圆环画笔 */
    private val countDownStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /** 倒计时圆环背景填充画笔 */
    private val countDownBgFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /** 倒计时文字画笔 */
    private val countDownTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    /** 倒计时动画进度（0~100） */
    private var countDownAnimationProgress: Int = 100

    /** 倒计时总时长（毫秒） */
    private var countDownMaxMs: Long = DEFAULT_MAX_MS

    /** 倒计时是否已完成 */
    private var countDownFinished: Boolean = true

    /** 倒计时是否正在进行 */
    private var isRunning: Boolean = false

    /** 上次更新时间戳 */
    private var lastUpdateTimestamp: Long = 0L

    /** 倒计时剩余时间 */
    private var remainingTimeMs: Long = DEFAULT_MAX_MS

    /** [start] 未显式传入时使用的默认时长与进度分母（可由 XML `countdown_seconds` 覆盖） */
    private var defaultDurationMs: Long = DEFAULT_DURATION_MS
    private var defaultMaxMs: Long = DEFAULT_MAX_MS

    /** 倒计时监听器 */
    private var countDownListener: CountDownListener? = null

    /** 时间展示方式；也可设置 [timeTextFormatter] 完全自定义文案 */
    enum class TimeDisplayMode {
        /** 只显示剩余秒数（进位，与原先一致） */
        SECONDS,
        /** 分:秒 如 01:40 */
        MM_SS
    }

    var timeDisplayMode: TimeDisplayMode = TimeDisplayMode.SECONDS
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /**
     * 若非 null，优先于 [timeDisplayMode] 生成中间文字，参数为剩余毫秒
     */
    var timeTextFormatter: ((Long) -> String)? = null
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /** 倒计时状态枚举 */
    enum class State {
        IN_PROGRESS, FINISHED, SKIPPED
    }

    /** 倒计时回调接口 */
    interface CountDownListener {
        /** 倒计时完成 */
        fun onFinish() {}

        /** 用户跳过倒计时 */
        fun onSkip() {}

        /**
         * 倒计时进行中
         * @param progress 进度百分比（0-100）
         * @param remainingMs 剩余毫秒数
         */
        fun onProgress(progress: Int, remainingMs: Long) {}
    }

    init {
        val dm = context.resources.displayMetrics
        val defaultStrokePx = (4f * dm.density).roundToInt()
        val defaultTextPx = (DEFAULT_TEXT_SIZE_SP * dm.scaledDensity).roundToInt()
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.AwCountDownView, defStyleAttr, 0
        ).apply {
            try {
                val sec = getInt(R.styleable.AwCountDownView_countdown_seconds, 0)
                if (sec > 0) {
                    val ms = sec * 1000L
                    defaultDurationMs = ms
                    defaultMaxMs = ms
                    countDownMaxMs = ms
                    remainingTimeMs = ms
                }
                setStrokeColor(
                    getColor(
                        R.styleable.AwCountDownView_countDownStrokeColor,
                        Color.BLACK
                    )
                )
                setTextColor(
                    getColor(
                        R.styleable.AwCountDownView_countDownTextColor,
                        Color.BLACK
                    )
                )
                setCountDownTrackColor(
                    getColor(
                        R.styleable.AwCountDownView_countDownTrackColor,
                        Color.TRANSPARENT
                    )
                )
                setStrokeWidth(
                    getDimensionPixelSize(
                        R.styleable.AwCountDownView_countDownStrokeWidth,
                        defaultStrokePx
                    )
                )
                setTextSize(
                    getDimensionPixelSize(
                        R.styleable.AwCountDownView_countDownTextSize,
                        defaultTextPx
                    )
                )
                timeDisplayMode = when (
                    getInt(R.styleable.AwCountDownView_countDown_timeMode, 0)
                ) {
                    1 -> TimeDisplayMode.MM_SS
                    else -> TimeDisplayMode.SECONDS
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onDetachedFromWindow() {
        updateHandler.removeCallbacksAndMessages(null)
        isRunning = false
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val sample = timeTextFormatter?.invoke(65_000L) ?: when (timeDisplayMode) {
            TimeDisplayMode.MM_SS -> "00:00"
            TimeDisplayMode.SECONDS -> "99"
        }
        val textHeight = with(countDownTextPaint.fontMetrics) { descent - ascent }
        val textW = countDownTextPaint.measureText(sample)
        val sw = countDownStrokePaint.strokeWidth
        val minDp = 48f * resources.displayMetrics.density
        val desiredSize = (maxOf(textW, textHeight) + sw * 3f)
            .toInt()
            .coerceAtLeast(minDp.toInt())

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredSize, widthSize)
            else -> desiredSize
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredSize, heightSize)
            else -> desiredSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        val strokeWidth = countDownStrokePaint.strokeWidth
        val radius = minOf(width, height) / 2f - strokeWidth / 2f

        // 绘制圆环背景
        canvas.drawCircle(centerX, centerY, radius, countDownBgFillPaint)
        canvas.drawCircle(centerX, centerY, radius, countDownStrokePaint)

        // 绘制倒计时进度圆弧
        if (!countDownFinished) {
            val startAngle = -90f
            val sweepAngle = (countDownAnimationProgress / 100f) * 360f
            canvas.drawArc(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                startAngle, sweepAngle, false, countDownStrokePaint
            )
        }

        val text = if (isRunning) {
            timeTextFormatter?.invoke(remainingTimeMs) ?: when (timeDisplayMode) {
                TimeDisplayMode.SECONDS -> (remainingTimeMs / 1000 + 1).toString()
                TimeDisplayMode.MM_SS -> {
                    val totalSec = (remainingTimeMs + 999) / 1000
                    val m = totalSec / 60
                    val s = totalSec % 60
                    String.format("%02d:%02d", m, s)
                }
            }
        } else {
            ""
        }
        val textY = centerY - (countDownTextPaint.descent() + countDownTextPaint.ascent()) / 2f
        canvas.drawText(text, centerX, textY, countDownTextPaint)
    }

    /**
     * 开始倒计时
     * @param durationMs 倒计时总时长（**毫秒**）
     * @param maxMs 进度条分母（**毫秒**），用于计算进度百分比
     * @see startSeconds
     */
    @JvmOverloads
    fun start(durationMs: Long = defaultDurationMs, maxMs: Long = defaultMaxMs) {
        countDownFinished = false
        countDownMaxMs = maxMs
        remainingTimeMs = durationMs
        countDownAnimationProgress = 100
        isRunning = true
        lastUpdateTimestamp = System.currentTimeMillis()

        updateHandler.removeCallbacksAndMessages(null)
        updateCountDown()
    }

    /**
     * 以**秒**为单位开始倒计时（与 [start] 的毫秒参数区分，避免误传）。
     * @param totalSeconds 总秒数，至少为 1
     */
    fun startSeconds(totalSeconds: Int) {
        val sec = totalSeconds.coerceAtLeast(1)
        val ms = sec * 1000L
        start(ms, ms)
    }

    /** 更新倒计时进度 */
    private fun updateCountDown() {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastUpdateTimestamp
        remainingTimeMs -= elapsed
        lastUpdateTimestamp = currentTime

        if (remainingTimeMs <= 0) {
            remainingTimeMs = 0
            isRunning = false
            countDownFinished = true
            countDownAnimationProgress = 0

            updateHandler.removeCallbacksAndMessages(null)

            countDownListener?.onFinish()
            countDownListener?.onProgress(countDownAnimationProgress, remainingTimeMs)
        } else {
            countDownAnimationProgress = ((remainingTimeMs.toFloat() / countDownMaxMs) * 100).toInt()

            updateHandler.postDelayed({ updateCountDown() }, 16L)
            countDownListener?.onProgress(countDownAnimationProgress, remainingTimeMs)
        }

        invalidate()
    }

    /** 重置倒计时视图 */
    fun reset() {
        isRunning = false
        countDownAnimationProgress = 100
        countDownFinished = true
        remainingTimeMs = countDownMaxMs
        updateHandler.removeCallbacksAndMessages(null)
        invalidate()
    }

    /** 设置倒计时监听器 */
    fun setCountDownListener(listener: CountDownListener) {
        this.countDownListener = listener
    }

    /**
     * 跳过倒计时
     * @param invokeListener 是否触发 onSkip 回调（默认 true）
     */
    fun skip(invokeListener: Boolean = true) {
        isRunning = false
        countDownAnimationProgress = 0
        countDownFinished = true
        remainingTimeMs = 0
        updateHandler.removeCallbacksAndMessages(null)
        if (invokeListener) {
            countDownListener?.onSkip()
        }
        invalidate()
    }

    /** 设置倒计时圆环颜色 */
    fun setStrokeColor(@ColorInt color: Int) {
        countDownStrokePaint.color = color
    }

    /** 设置倒计时文字颜色 */
    fun setTextColor(@ColorInt color: Int) {
        countDownTextPaint.color = color
    }

    /** 设置圆环内轨道（底层填充）颜色，与 [View.setBackgroundColor] 无关 */
    fun setCountDownTrackColor(@ColorInt color: Int) {
        countDownBgFillPaint.color = color
    }

    /** 设置倒计时圆环描边宽度 */
    fun setStrokeWidth(widthPx: Int) {
        countDownStrokePaint.strokeWidth = widthPx.toFloat()
    }

    /** 设置倒计时文字大小 */
    fun setTextSize(sizePx: Int) {
        countDownTextPaint.textSize = sizePx.toFloat()
    }
}
