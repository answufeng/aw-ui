package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat
import androidx.core.content.ContextCompat
import kotlin.math.min

class AwCircleProgressBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val rectF = RectF()
        private val backgroundPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
        private val progressPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
            }

        private var animator: ValueAnimator? = null

        var max: Float = 100f
            set(value) {
                field = value.coerceAtLeast(1f)
                progress = progress.coerceIn(0f, field)
                invalidate()
            }

        var progress: Float = 0f
            set(value) {
                field = value.coerceIn(0f, max)
                invalidate()
            }

        var strokeWidthPx: Float = 8f.dpFloat
            set(value) {
                field = value.coerceAtLeast(1f.dpFloat)
                backgroundPaint.strokeWidth = field
                progressPaint.strokeWidth = field
                requestLayout()
                invalidate()
            }

        var progressColor: Int = 0xFF4CAF50.toInt()
            set(value) {
                field = value
                progressPaint.color = value
                invalidate()
            }

        var trackColor: Int = 0xFFE0E0E0.toInt()
            set(value) {
                field = value
                backgroundPaint.color = value
                invalidate()
            }

        var showText: Boolean = true
            set(value) {
                field = value
                invalidate()
            }

        var textColor: Int = Color.DKGRAY
            set(value) {
                field = value
                textPaint.color = value
                invalidate()
            }

        var textSizePx: Float = 14f.sp()
            set(value) {
                field = value.coerceAtLeast(8f.sp())
                textPaint.textSize = field
                invalidate()
            }

        var startAngle: Float = -90f
            set(value) {
                field = value
                invalidate()
            }

        var progressSuffix: String = "%"
            set(value) {
                field = value
                invalidate()
            }

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwCircleProgressBar)
            max = ta.getFloat(R.styleable.AwCircleProgressBar_circleProgress_max, 100f)
            progress = ta.getFloat(R.styleable.AwCircleProgressBar_circleProgress_progress, 0f)
            strokeWidthPx = ta.getDimension(R.styleable.AwCircleProgressBar_circleProgress_strokeWidth, 8f.dpFloat)
            progressColor = ta.getColor(R.styleable.AwCircleProgressBar_circleProgress_progressColor, ContextCompat.getColor(context, R.color.aw_color_progress))
            trackColor = ta.getColor(R.styleable.AwCircleProgressBar_circleProgress_bgColor, ContextCompat.getColor(context, R.color.aw_color_progress_track))
            showText = ta.getBoolean(R.styleable.AwCircleProgressBar_circleProgress_showText, true)
            textColor = ta.getColor(R.styleable.AwCircleProgressBar_circleProgress_textColor, Color.DKGRAY)
            textSizePx = ta.getDimension(R.styleable.AwCircleProgressBar_circleProgress_textSize, 14f.sp())
            startAngle = ta.getFloat(R.styleable.AwCircleProgressBar_circleProgress_startAngle, -90f)
            progressSuffix = ta.getString(R.styleable.AwCircleProgressBar_circleProgress_suffix) ?: "%"
            ta.recycle()
        }

        fun setProgressWithAnimation(
            target: Float,
            duration: Long = 800L,
        ) {
            animator?.cancel()
            animator =
                ValueAnimator.ofFloat(progress, target.coerceIn(0f, max)).apply {
                    this.duration = duration
                    addUpdateListener { progress = it.animatedValue as Float }
                    start()
                }
        }

        fun setProgressAndMax(
            progress: Float,
            max: Float,
        ) {
            this.max = max
            this.progress = progress
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val desired = (strokeWidthPx * 2 + 96f.dpFloat).toInt()
            val resolvedWidth = resolveSize(desired, widthMeasureSpec)
            val resolvedHeight = resolveSize(desired, heightMeasureSpec)
            val size = min(resolvedWidth, resolvedHeight)
            setMeasuredDimension(size, size)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val halfStroke = strokeWidthPx / 2f
            rectF.set(halfStroke, halfStroke, width - halfStroke, height - halfStroke)

            canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)

            val sweepAngle = (progress / max) * 360f
            if (sweepAngle > 0f) {
                canvas.drawArc(rectF, startAngle, sweepAngle, false, progressPaint)
            }

            if (showText) {
                val text = "${((progress / max) * 100).toInt()}$progressSuffix"
                val baseline = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, width / 2f, baseline, textPaint)
            }
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
                putFloat(KEY_PROGRESS, progress)
                putFloat(KEY_MAX, max)
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
            max = state.getFloat(KEY_MAX, 100f)
            progress = state.getFloat(KEY_PROGRESS, 0f)
        }

        override fun onDetachedFromWindow() {
            animator?.cancel()
            super.onDetachedFromWindow()
        }

        private fun Float.sp(): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, resources.displayMetrics)

        companion object {
            private const val KEY_SUPER_STATE = "superState"
            private const val KEY_PROGRESS = "progress"
            private const val KEY_MAX = "max"
        }
    }
