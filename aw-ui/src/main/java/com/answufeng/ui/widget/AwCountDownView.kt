package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.answufeng.ui.R
import kotlin.math.roundToInt

class AwCountDownView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        companion object {
            private const val DEFAULT_DURATION_MS = 3000L
            private const val DEFAULT_MAX_MS = 3000L
            private const val DEFAULT_TEXT_SIZE_SP = 14f
            const val DEFAULT_COLOR = 0xFF999999.toInt()
        }

        private val updateHandler = Handler(Looper.getMainLooper())

        private val trackPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
            }

        private val progressPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }

        private val bgFillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }

        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
            }

        private val borderPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
            }

        private var countDownAnimationProgress: Int = 100
        private var countDownMaxMs: Long = DEFAULT_MAX_MS
        private var countDownFinished: Boolean = true
        private var isRunning: Boolean = false
        private var lastUpdateTimestamp: Long = 0L
        private var remainingTimeMs: Long = DEFAULT_MAX_MS

        private var defaultDurationMs: Long = DEFAULT_DURATION_MS
        private var defaultMaxMs: Long = DEFAULT_MAX_MS

        private var countDownListener: CountDownListener? = null

        enum class DisplayMode {
            CIRCLE,
            TEXT,
        }

        var displayMode: DisplayMode = DisplayMode.CIRCLE
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        enum class TimeDisplayMode {
            SECONDS,
            MM_SS,
        }

        var timeDisplayMode: TimeDisplayMode = TimeDisplayMode.SECONDS
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var timeTextFormatter: ((Long) -> String)? = null
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var finishText: String? = null
            set(value) {
                field = value
                invalidate()
            }

        var suffixText: String = ""
            set(value) {
                field = value
                invalidate()
            }

        var isClickableWhenFinished: Boolean = true

        var autoStartOnClick: Boolean = false

        var disabledTextColor: Int = 0xFFBBBBBB.toInt()
            set(value) {
                field = value
                invalidate()
            }

        var initialText: String = ""
            set(value) {
                field = value
                invalidate()
            }

        var runningTextFormat: String = "%d%s"
            set(value) {
                field = value
                invalidate()
            }

        var textBorderColor: Int = DEFAULT_COLOR
            set(value) {
                field = value
                invalidate()
            }

        var textBorderWidth: Float = 1f * resources.displayMetrics.density
            set(value) {
                field = value
                invalidate()
            }

        var textBgColor: Int = Color.TRANSPARENT
            set(value) {
                field = value
                invalidate()
            }

        var textCornerRadius: Float = 4f * resources.displayMetrics.density
            set(value) {
                field = value
                invalidate()
            }

        var textPaddingH: Int = (12f * resources.displayMetrics.density).roundToInt()
        var textPaddingV: Int = (4f * resources.displayMetrics.density).roundToInt()

        enum class State {
            IN_PROGRESS,
            FINISHED,
            SKIPPED,
        }

        interface CountDownListener {
            fun onFinish() {}

            fun onSkip() {}

            fun onProgress(
                progress: Int,
                remainingMs: Long,
            ) {}
        }

        var onStartClick: (() -> Boolean)? = null

        init {
            val dm = context.resources.displayMetrics
            val defaultStrokePx = (3f * dm.density).roundToInt()
            val defaultTextPx = (DEFAULT_TEXT_SIZE_SP * dm.scaledDensity).roundToInt()
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.AwCountDownView,
                defStyleAttr,
                0,
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
                    setProgressColor(
                        getColor(R.styleable.AwCountDownView_countDownStrokeColor, DEFAULT_COLOR),
                    )
                    if (hasValue(R.styleable.AwCountDownView_countDownProgressColor)) {
                        setProgressColor(
                            getColor(R.styleable.AwCountDownView_countDownProgressColor, DEFAULT_COLOR),
                        )
                    }
                    setTextColor(
                        getColor(R.styleable.AwCountDownView_countDownTextColor, DEFAULT_COLOR),
                    )
                    setTrackColor(
                        getColor(R.styleable.AwCountDownView_countDownTrackColor, 0x1A999999),
                    )
                    setStrokeWidth(
                        getDimensionPixelSize(R.styleable.AwCountDownView_countDownStrokeWidth, defaultStrokePx),
                    )
                    setTextSize(
                        getDimensionPixelSize(R.styleable.AwCountDownView_countDownTextSize, defaultTextPx),
                    )
                    finishText = getString(R.styleable.AwCountDownView_countDown_finishText)
                    suffixText = getString(R.styleable.AwCountDownView_countDown_suffixText) ?: ""
                    initialText = getString(R.styleable.AwCountDownView_countDown_initialText) ?: ""
                    runningTextFormat = getString(R.styleable.AwCountDownView_countDown_runningTextFormat) ?: "%d%s"
                    displayMode =
                        when (
                            getInt(R.styleable.AwCountDownView_countDown_displayMode, 0)
                        ) {
                            1 -> DisplayMode.TEXT
                            else -> DisplayMode.CIRCLE
                        }
                    timeDisplayMode =
                        when (
                            getInt(R.styleable.AwCountDownView_countDown_timeMode, 0)
                        ) {
                            1 -> TimeDisplayMode.MM_SS
                            else -> TimeDisplayMode.SECONDS
                        }
                    autoStartOnClick = getBoolean(R.styleable.AwCountDownView_countDown_autoStartOnClick, false)
                    if (hasValue(R.styleable.AwCountDownView_countDown_disabledTextColor)) {
                        disabledTextColor = getColor(R.styleable.AwCountDownView_countDown_disabledTextColor, disabledTextColor)
                    }
                    if (hasValue(R.styleable.AwCountDownView_countDown_textBorderColor)) {
                        textBorderColor = getColor(R.styleable.AwCountDownView_countDown_textBorderColor, textBorderColor)
                    }
                    if (hasValue(R.styleable.AwCountDownView_countDown_textBorderWidth)) {
                        textBorderWidth = getDimension(R.styleable.AwCountDownView_countDown_textBorderWidth, textBorderWidth)
                    }
                    if (hasValue(R.styleable.AwCountDownView_countDown_textBgColor)) {
                        textBgColor = getColor(R.styleable.AwCountDownView_countDown_textBgColor, textBgColor)
                    }
                    if (hasValue(R.styleable.AwCountDownView_countDown_textCornerRadius)) {
                        textCornerRadius = getDimension(R.styleable.AwCountDownView_countDown_textCornerRadius, textCornerRadius)
                    }
                    if (hasValue(R.styleable.AwCountDownView_countDown_textPaddingH)) {
                        textPaddingH = getDimensionPixelSize(R.styleable.AwCountDownView_countDown_textPaddingH, textPaddingH)
                    }
                    if (hasValue(R.styleable.AwCountDownView_countDown_textPaddingV)) {
                        textPaddingV = getDimensionPixelSize(R.styleable.AwCountDownView_countDown_textPaddingV, textPaddingV)
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

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)

            if (displayMode == DisplayMode.TEXT) {
                val sampleText = getDisplayText(60_000L)
                val textH = with(textPaint.fontMetrics) { descent - ascent }
                val textW = textPaint.measureText(sampleText)
                val desiredW = (textW + textPaddingH * 2 + textBorderWidth * 2).toInt()
                val desiredH = (textH + textPaddingV * 2 + textBorderWidth * 2).toInt()

                val w =
                    when (widthMode) {
                        MeasureSpec.EXACTLY -> widthSize
                        MeasureSpec.AT_MOST -> minOf(desiredW, widthSize)
                        else -> desiredW
                    }
                val h =
                    when (heightMode) {
                        MeasureSpec.EXACTLY -> heightSize
                        MeasureSpec.AT_MOST -> minOf(desiredH, heightSize)
                        else -> desiredH
                    }
                setMeasuredDimension(w, h)
            } else {
                val sample =
                    finishText ?: timeTextFormatter?.invoke(65_000L) ?: when (timeDisplayMode) {
                        TimeDisplayMode.MM_SS -> "00:00"
                        TimeDisplayMode.SECONDS -> "99"
                    }
                val textHeight = with(textPaint.fontMetrics) { descent - ascent }
                val textW = textPaint.measureText(sample)
                val sw = progressPaint.strokeWidth
                val minDp = 40f * resources.displayMetrics.density
                val desiredSize =
                    (maxOf(textW, textHeight) + sw * 3f)
                        .toInt()
                        .coerceAtLeast(minDp.toInt())

                val width =
                    when (widthMode) {
                        MeasureSpec.EXACTLY -> widthSize
                        MeasureSpec.AT_MOST -> minOf(desiredSize, widthSize)
                        else -> desiredSize
                    }
                val height =
                    when (heightMode) {
                        MeasureSpec.EXACTLY -> heightSize
                        MeasureSpec.AT_MOST -> minOf(desiredSize, heightSize)
                        else -> desiredSize
                    }
                setMeasuredDimension(width, height)
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (displayMode == DisplayMode.TEXT) {
                drawTextMode(canvas)
            } else {
                drawCircleMode(canvas)
            }
        }

        private fun drawTextMode(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()

            val isDisabled = isRunning || !countDownFinished

            bgFillPaint.color = textBgColor
            if (textBgColor != Color.TRANSPARENT) {
                canvas.drawRoundRect(0f, 0f, w, h, textCornerRadius, textCornerRadius, bgFillPaint)
            }

            borderPaint.color =
                if (isDisabled) {
                    (textBorderColor and 0x00FFFFFF) or 0x33000000
                } else {
                    textBorderColor
                }
            borderPaint.strokeWidth = textBorderWidth
            val half = textBorderWidth / 2f
            canvas.drawRoundRect(half, half, w - half, h - half, textCornerRadius, textCornerRadius, borderPaint)

            val text = getDisplayText(remainingTimeMs)
            if (text.isNotEmpty()) {
                val savedColor = textPaint.color
                if (isDisabled) {
                    textPaint.color = disabledTextColor
                }
                val textY = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, w / 2f, textY, textPaint)
                if (isDisabled) {
                    textPaint.color = savedColor
                }
            }
        }

        private fun drawCircleMode(canvas: Canvas) {
            val centerX = width / 2f
            val centerY = height / 2f
            val strokeWidth = progressPaint.strokeWidth
            val radius = minOf(width, height) / 2f - strokeWidth / 2f

            if (bgFillPaint.color != Color.TRANSPARENT) {
                canvas.drawCircle(centerX, centerY, radius, bgFillPaint)
            }

            canvas.drawCircle(centerX, centerY, radius, trackPaint)

            if (!countDownFinished) {
                val startAngle = -90f
                val sweepAngle = (countDownAnimationProgress / 100f) * 360f
                canvas.drawArc(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius,
                    startAngle,
                    sweepAngle,
                    false,
                    progressPaint,
                )
            }

            val text =
                if (countDownFinished && finishText != null) {
                    finishText!!
                } else if (isRunning || !countDownFinished) {
                    timeTextFormatter?.invoke(remainingTimeMs) ?: when (timeDisplayMode) {
                        TimeDisplayMode.SECONDS -> {
                            val sec = (remainingTimeMs / 1000 + 1).toInt()
                            "$sec$suffixText"
                        }
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

            if (text.isNotEmpty()) {
                val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, centerX, textY, textPaint)
            }
        }

        private fun getDisplayText(remainingMs: Long): String {
            if (countDownFinished) {
                return finishText ?: initialText
            }
            if (isRunning || !countDownFinished) {
                val sec = (remainingMs / 1000 + 1).toInt()
                val suffix = suffixText.ifEmpty { "s" }
                return String.format(runningTextFormat, sec, suffix)
            }
            return initialText
        }

        @JvmOverloads
        fun start(
            durationMs: Long = defaultDurationMs,
            maxMs: Long = defaultMaxMs,
        ) {
            countDownFinished = false
            countDownMaxMs = maxMs
            remainingTimeMs = durationMs
            countDownAnimationProgress = 100
            isRunning = true
            lastUpdateTimestamp = SystemClock.elapsedRealtime()

            if (displayMode == DisplayMode.TEXT) {
                isEnabled = false
            }

            updateHandler.removeCallbacksAndMessages(null)
            updateCountDown()
        }

        fun startSeconds(totalSeconds: Int) {
            val sec = totalSeconds.coerceAtLeast(1)
            val ms = sec * 1000L
            start(ms, ms)
        }

        private fun updateCountDown() {
            val currentTime = SystemClock.elapsedRealtime()
            val elapsed = currentTime - lastUpdateTimestamp
            remainingTimeMs -= elapsed
            lastUpdateTimestamp = currentTime

            if (remainingTimeMs <= 0) {
                remainingTimeMs = 0
                isRunning = false
                countDownFinished = true
                countDownAnimationProgress = 0

                if (displayMode == DisplayMode.TEXT) {
                    isEnabled = true
                }

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

        fun reset() {
            isRunning = false
            countDownAnimationProgress = 100
            countDownFinished = true
            remainingTimeMs = countDownMaxMs
            if (displayMode == DisplayMode.TEXT) {
                isEnabled = true
            }
            updateHandler.removeCallbacksAndMessages(null)
            invalidate()
        }

        fun setCountDownListener(listener: CountDownListener) {
            this.countDownListener = listener
        }

        fun skip(invokeListener: Boolean = true) {
            isRunning = false
            countDownAnimationProgress = 0
            countDownFinished = true
            remainingTimeMs = 0
            if (displayMode == DisplayMode.TEXT) {
                isEnabled = true
            }
            updateHandler.removeCallbacksAndMessages(null)
            if (invokeListener) {
                countDownListener?.onSkip()
            }
            invalidate()
        }

        fun setProgressColor(
            @ColorInt color: Int,
        ) {
            progressPaint.color = color
        }

        @Deprecated("Use setProgressColor instead", ReplaceWith("setProgressColor(color)"))
        fun setStrokeColor(
            @ColorInt color: Int,
        ) {
            setProgressColor(color)
        }

        fun setTrackColor(
            @ColorInt color: Int,
        ) {
            trackPaint.color = color
        }

        fun setBgFillColor(
            @ColorInt color: Int,
        ) {
            bgFillPaint.color = color
        }

        @Deprecated("Use setTrackColor instead", ReplaceWith("setTrackColor(color)"))
        fun setCountDownTrackColor(
            @ColorInt color: Int,
        ) {
            bgFillPaint.color = color
        }

        fun setTextColor(
            @ColorInt color: Int,
        ) {
            textPaint.color = color
        }

        fun setStrokeWidth(widthPx: Int) {
            trackPaint.strokeWidth = widthPx.toFloat()
            progressPaint.strokeWidth = widthPx.toFloat()
        }

        fun setTextSize(sizePx: Int) {
            textPaint.textSize = sizePx.toFloat()
        }

        override fun performClick(): Boolean {
            super.performClick()
            if (countDownFinished && isClickableWhenFinished) {
                if (autoStartOnClick) {
                    val shouldStart = onStartClick?.invoke() ?: true
                    if (shouldStart) {
                        start()
                    }
                } else {
                    skip()
                }
            }
            return true
        }
    }
