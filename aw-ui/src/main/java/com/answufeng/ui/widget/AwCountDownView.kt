@file:Suppress("unused")

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

/**
 * 倒计时视图组件，用于显示倒计时动画和文字。
 *
 * 支持圆形进度动画和文本显示，可自定义颜色、大小、描边宽度等属性。
 * 倒计时过程中可设置监听器来监听状态变化（进行中、完成、跳过）。
 *
 * ### XML 属性
 * - `countDownStrokeColor`: 倒计时圆环颜色（默认黑色）
 * - `countDownTextColor`: 倒计时文字颜色（默认黑色）
 * - `countDownBackgroundColor`: 倒计时圆环背景颜色（默认透明）
 * - `countDownStrokeWidth`: 倒计时圆环描边宽度（默认 4dp）
 * - `countDownTextSize`: 倒计时文字大小（默认 24sp）
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

    /** 倒计时监听器 */
    private var countDownListener: CountDownListener? = null

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
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.AwCountDownView, defStyleAttr, 0
        ).apply {
            try {
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
                setBackgroundColor(
                    getColor(
                        R.styleable.AwCountDownView_countDownBackgroundColor,
                        Color.TRANSPARENT
                    )
                )
                setStrokeWidth(
                    getDimensionPixelSize(
                        R.styleable.AwCountDownView_countDownStrokeWidth,
                        4.dp
                    )
                )
                setTextSize(
                    getDimensionPixelSize(
                        R.styleable.AwCountDownView_countDownTextSize,
                        DEFAULT_TEXT_SIZE_SP.spToPx
                    )
                )
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // 计算视图所需的最小尺寸，基于文字大小
        val textHeight = countDownTextPaint.fontMetrics.run { descent - ascent }
        val desiredSize = (textHeight + countDownStrokePaint.strokeWidth).toInt()

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

        // 绘制倒计时文字
        val text = if (isRunning) {
            (remainingTimeMs / 1000 + 1).toString()
        } else {
            ""
        }
        val textY = centerY - (countDownTextPaint.descent() + countDownTextPaint.ascent()) / 2f
        canvas.drawText(text, centerX, textY, countDownTextPaint)
    }

    /**
     * 开始倒计时
     * @param durationMs 倒计时总时长（毫秒）
     * @param maxMs 倒计时最大值（毫秒），用于计算进度百分比
     */
    fun start(durationMs: Long = DEFAULT_DURATION_MS, maxMs: Long = DEFAULT_MAX_MS) {
        countDownFinished = false
        countDownMaxMs = maxMs
        remainingTimeMs = durationMs
        countDownAnimationProgress = 100
        isRunning = true
        lastUpdateTimestamp = System.currentTimeMillis()

        updateHandler.removeCallbacksAndMessages(null)
        updateCountDown()
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

    /** 设置倒计时圆环背景颜色 */
    fun setBackgroundColor(@ColorInt color: Int) {
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
