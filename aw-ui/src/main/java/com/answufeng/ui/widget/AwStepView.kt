package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 水平步骤指示器，用于注册、下单等多步流程。
 */
class AwStepView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        var stepCount: Int = 3
            set(value) {
                field = value.coerceAtLeast(1)
                requestLayout()
                invalidate()
            }

        var currentStep: Int = 0
            set(value) {
                field = value.coerceIn(0, stepCount - 1)
                invalidate()
            }

        var activeColor: Int = ContextCompat.getColor(context, R.color.aw_color_primary)
            set(value) {
                field = value
                activePaint.color = value
                linePaint.color = value
                invalidate()
            }

        var inactiveColor: Int = 0xFFBDBDBD.toInt()
            set(value) {
                field = value
                inactivePaint.color = value
                invalidate()
            }

        var labelTexts: List<String> = emptyList()
            set(value) {
                field = value
                invalidate()
            }

        private val activePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
        private val inactivePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
        private val linePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = 2f.dpFloat
                style = Paint.Style.STROKE
            }
        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 12f.dpFloat
                textAlign = Paint.Align.CENTER
            }

        private val circleRadius = 10f.dpFloat
        private val labelMarginTop = 6f.dpFloat

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwStepView)
            stepCount = ta.getInt(R.styleable.AwStepView_step_count, 3).coerceAtLeast(1)
            currentStep = ta.getInt(R.styleable.AwStepView_step_current, 0)
            activeColor = ta.getColor(R.styleable.AwStepView_step_activeColor, activeColor)
            inactiveColor = ta.getColor(R.styleable.AwStepView_step_inactiveColor, inactiveColor)
            ta.recycle()
            activePaint.color = activeColor
            inactivePaint.color = inactiveColor
            linePaint.color = activeColor
            textPaint.color = 0xFF666666.toInt()
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val textHeight = if (labelTexts.isNotEmpty()) textPaint.textSize + labelMarginTop else 0f
            val desiredHeight = (circleRadius * 2 + textHeight + paddingTop + paddingBottom).toInt()
            val width = MeasureSpec.getSize(widthMeasureSpec)
            setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (stepCount <= 0) return

            val contentWidth = width - paddingLeft - paddingRight
            val stepWidth = contentWidth / stepCount.toFloat()
            val centerY = paddingTop + circleRadius

            for (i in 0 until stepCount) {
                val cx = paddingLeft + stepWidth * i + stepWidth / 2f
                val paint = if (i <= currentStep) activePaint else inactivePaint
                canvas.drawCircle(cx, centerY, circleRadius, paint)

                if (i < stepCount - 1) {
                    val nextCx = paddingLeft + stepWidth * (i + 1) + stepWidth / 2f
                    linePaint.color = if (i < currentStep) activeColor else inactiveColor
                    canvas.drawLine(cx + circleRadius, centerY, nextCx - circleRadius, centerY, linePaint)
                }

                labelTexts.getOrNull(i)?.let { label ->
                    canvas.drawText(label, cx, centerY + circleRadius + labelMarginTop + textPaint.textSize * 0.35f, textPaint)
                }
            }
        }
    }
