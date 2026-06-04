package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 数量加减选择器（Stepper）。
 *
 * 横向布局：左侧"－"按钮、中间当前值、右侧"＋"按钮。
 * 支持最小值、最大值、步长、长按连续增减。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwStepper
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:stepper_value="1"
 *     app:stepper_min="0"
 *     app:stepper_max="99"
 *     app:stepper_step="1" />
 * ```
 */
class AwStepper
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {

        var value: Int = 0
            set(v) {
                val clamped = v.coerceIn(min, max)
                if (field != clamped) {
                    field = clamped
                    invalidate()
                    onValueChange?.invoke(field)
                }
            }

        var min: Int = 0
            set(v) {
                field = v
                value = value.coerceIn(min, max)
                invalidate()
            }

        var max: Int = 99
            set(v) {
                field = v
                value = value.coerceIn(min, max)
                invalidate()
            }

        var step: Int = 1
            set(v) {
                field = v.coerceAtLeast(1)
            }

        var buttonWidth: Float = 32f.dpFloat
            set(v) {
                field = v
                requestLayout()
            }

        var textColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_text)
            set(v) {
                field = v
                invalidate()
            }

        var buttonTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_btn_text)
            set(v) {
                field = v
                invalidate()
            }

        var buttonBgColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_btn_bg)
            set(v) {
                field = v
                invalidate()
            }

        var valueBgColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_value_bg)
            set(v) {
                field = v
                invalidate()
            }

        var borderColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_border)
            set(v) {
                field = v
                invalidate()
            }

        var borderWidth: Float = 1f.dpFloat
            set(v) {
                field = v
                invalidate()
            }

        var cornerRadius: Float = 4f.dpFloat
            set(v) {
                field = v
                invalidate()
            }

        var textSize: Float = 14f.dpFloat
            set(v) {
                field = v
                invalidate()
            }

        var editable: Boolean = false
        var longPressSpeed: Int = 200
        var showDividers: Boolean = true

        var onValueChange: ((Int) -> Unit)? = null

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rectF = RectF()
        private val textRect = Rect()

        private var touchTarget: TouchTarget = TouchTarget.NONE
        private var longPressRunnable: Runnable? = null
        private var isLongPressing: Boolean = false

        private enum class TouchTarget { NONE, DECREASE, INCREASE }

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwStepper)
            value = ta.getInt(R.styleable.AwStepper_stepper_value, 0)
            min = ta.getInt(R.styleable.AwStepper_stepper_min, 0)
            max = ta.getInt(R.styleable.AwStepper_stepper_max, 99)
            step = ta.getInt(R.styleable.AwStepper_stepper_step, 1)
            buttonWidth = ta.getDimension(R.styleable.AwStepper_stepper_buttonWidth, buttonWidth)
            textColor = ta.getColor(R.styleable.AwStepper_stepper_textColor, textColor)
            buttonTextColor = ta.getColor(R.styleable.AwStepper_stepper_buttonTextColor, buttonTextColor)
            buttonBgColor = ta.getColor(R.styleable.AwStepper_stepper_buttonBgColor, buttonBgColor)
            valueBgColor = ta.getColor(R.styleable.AwStepper_stepper_valueBgColor, valueBgColor)
            borderColor = ta.getColor(R.styleable.AwStepper_stepper_borderColor, borderColor)
            borderWidth = ta.getDimension(R.styleable.AwStepper_stepper_borderWidth, borderWidth)
            cornerRadius = ta.getDimension(R.styleable.AwStepper_stepper_cornerRadius, cornerRadius)
            textSize = ta.getDimension(R.styleable.AwStepper_stepper_textSize, textSize)
            editable = ta.getBoolean(R.styleable.AwStepper_stepper_editable, false)
            longPressSpeed = ta.getInt(R.styleable.AwStepper_stepper_longPressSpeed, 200)
            showDividers = ta.getBoolean(R.styleable.AwStepper_stepper_showDividers, true)
            ta.recycle()

            borderPaint.strokeWidth = borderWidth
            dividerPaint.color = borderColor
            textPaint.textAlign = Paint.Align.CENTER
            isClickable = true
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val height = resolveSize((36f.dpFloat).toInt(), heightMeasureSpec)
            val width = resolveSize(((buttonWidth * 2 + 60f.dpFloat)).toInt(), widthMeasureSpec)
            setMeasuredDimension(width, height)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val h = height.toFloat()
            val w = width.toFloat()
            val halfH = h / 2f
            textPaint.textSize = textSize

            val btnW = buttonWidth.coerceAtMost(w / 3f)
            val valueW = w - btnW * 2

            // 整体外框背景
            bgPaint.color = valueBgColor
            borderPaint.strokeWidth = borderWidth
            rectF.set(0f, 0f, w, h)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

            // 减号按钮背景
            bgPaint.color = buttonBgColor
            canvas.save()
            canvas.clipRect(0f, 0f, btnW, h)
            rectF.set(0f, 0f, btnW, h)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)
            canvas.restore()

            // 加号按钮背景
            bgPaint.color = buttonBgColor
            canvas.save()
            canvas.clipRect(w - btnW, 0f, w, h)
            rectF.set(w - btnW, 0f, w, h)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)
            canvas.restore()

            // 分割线
            if (showDividers) {
                dividerPaint.strokeWidth = 0.5f.dpFloat
                val dividerAlpha = ((Color.alpha(borderColor) * 0.5f).toInt()).coerceIn(0, 255)
                dividerPaint.alpha = dividerAlpha
                canvas.drawLine(btnW, 0f, btnW, h, dividerPaint)
                canvas.drawLine(w - btnW, 0f, w - btnW, h, dividerPaint)
            }

            // "-" 文本
            textPaint.color = if (value > min) buttonTextColor else borderColor
            textPaint.textSize = textSize * 1.2f
            textPaint.getTextBounds("-", 0, 1, textRect)
            canvas.drawText("-", btnW / 2f, halfH + textRect.height() / 2f, textPaint)

            // "+" 文本
            textPaint.color = if (value < max) buttonTextColor else borderColor
            canvas.drawText("+", w - btnW / 2f, halfH + textRect.height() / 2f, textPaint)

            // 值文本
            textPaint.color = textColor
            textPaint.textSize = textSize
            val valueText = value.toString()
            textPaint.getTextBounds(valueText, 0, valueText.length, textRect)
            canvas.drawText(valueText, btnW + valueW / 2f, halfH + textRect.height() / 2f, textPaint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val btnW = buttonWidth.coerceAtMost(width / 3f)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchTarget = when {
                        x <= btnW -> TouchTarget.DECREASE
                        x >= width - btnW -> TouchTarget.INCREASE
                        else -> TouchTarget.NONE
                    }
                    if (touchTarget != TouchTarget.NONE) {
                        performAction(touchTarget)
                        startLongPress(touchTarget)
                        return true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isLongPressing) return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cancelLongPress()
                    isLongPressing = false
                    touchTarget = TouchTarget.NONE
                    performClick()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean {
            return super.performClick()
        }

        private fun performAction(target: TouchTarget) {
            when (target) {
                TouchTarget.DECREASE -> value = value - step
                TouchTarget.INCREASE -> value = value + step
                TouchTarget.NONE -> {}
            }
        }

        private fun startLongPress(target: TouchTarget) {
            cancelLongPress()
            isLongPressing = false
            longPressRunnable = Runnable {
                isLongPressing = true
                performAction(target)
                postDelayed(longPressRunnable, longPressSpeed.toLong())
            }
            postDelayed(longPressRunnable, 400L)
        }

        override fun cancelLongPress() {
            super.cancelLongPress()
            longPressRunnable?.let { removeCallbacks(it) }
            longPressRunnable = null
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putInt("stepper_value", value)
                putInt("stepper_min", min)
                putInt("stepper_max", max)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                super.onRestoreInstanceState(state.getParcelable("superState"))
                min = state.getInt("stepper_min", 0)
                max = state.getInt("stepper_max", 99)
                value = state.getInt("stepper_value", 0)
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }

