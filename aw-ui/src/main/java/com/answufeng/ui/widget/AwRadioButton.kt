package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
 * 自定义单选按钮（RadioButton）。
 *
 * 显示圆形选中/未选中指示器和标签文本。
 * 通常与 [AwRadioGroup] 配合使用以实现单选互斥。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwRadioButton
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:radio_text="选项 A"
 *     app:radio_checked="false" />
 * ```
 */
class AwRadioButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {

        var isChecked: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    invalidate()
                }
            }

        var label: String? = null
            set(v) {
                field = v
                invalidate()
            }

        var labelColor: Int = ContextCompat.getColor(context, R.color.aw_color_radio_text)
            set(v) {
                field = v
                invalidate()
            }

        var labelSize: Float = 14f.dpFloat
            set(v) {
                field = v
                requestLayout()
                invalidate()
            }

        var checkedColor: Int = ContextCompat.getColor(context, R.color.aw_color_radio_checked)
            set(v) {
                field = v
                invalidate()
            }

        var uncheckedColor: Int = ContextCompat.getColor(context, R.color.aw_color_radio_unchecked)
            set(v) {
                field = v
                invalidate()
            }

        // 内部回调，由 AwRadioGroup 设置
        internal var onCheckedChangeInternal: ((Boolean) -> Unit)? = null

        private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val radioSize: Float = 20f.dpFloat
        private val strokeWidth = 2f.dpFloat

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwRadioButton)
            isChecked = ta.getBoolean(R.styleable.AwRadioButton_radio_checked, false)
            label = ta.getString(R.styleable.AwRadioButton_radio_text)
            labelColor = ta.getColor(R.styleable.AwRadioButton_radio_textColor, labelColor)
            labelSize = ta.getDimension(R.styleable.AwRadioButton_radio_textSize, labelSize)
            checkedColor = ta.getColor(R.styleable.AwRadioButton_radio_checkedColor, checkedColor)
            uncheckedColor = ta.getColor(R.styleable.AwRadioButton_radio_uncheckedColor, uncheckedColor)
            ta.recycle()

            outerPaint.strokeWidth = strokeWidth
            labelPaint.textSize = labelSize
            isClickable = true
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val labelW = if (label != null) labelPaint.measureText(label) + 6f.dpFloat else 0f
            val totalW = radioSize + labelW + paddingLeft + paddingRight
            val totalH = radioSize.coerceAtLeast(labelSize) + paddingTop + paddingBottom
            val w = resolveSize(totalW.toInt(), widthMeasureSpec)
            val h = resolveSize(totalH.toInt(), heightMeasureSpec)
            setMeasuredDimension(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val cx = paddingLeft + radioSize / 2f
            val cy = height / 2f
            val radius = radioSize / 2f - strokeWidth / 2f

            // 外圈
            outerPaint.color = if (isChecked) checkedColor else uncheckedColor
            canvas.drawCircle(cx, cy, radius, outerPaint)

            // 内圆点
            if (isChecked) {
                innerPaint.color = checkedColor
                val dotRadius = radius * 0.55f
                canvas.drawCircle(cx, cy, dotRadius, innerPaint)
            }

            // 文本
            if (label != null) {
                labelPaint.color = labelColor
                labelPaint.textSize = labelSize
                val textX = paddingLeft + radioSize + 8f.dpFloat
                val textY = (height + labelSize / 3f) / 2f
                canvas.drawText(label!!, textX, textY, labelPaint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP && !isChecked) {
                isChecked = true
                onCheckedChangeInternal?.invoke(true)
                performClick()
                return true
            }
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putBoolean("radio_checked", isChecked)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                super.onRestoreInstanceState(state.getParcelable("superState"))
                isChecked = state.getBoolean("radio_checked", false)
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }
