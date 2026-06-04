package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 自定义复选框（CheckBox）。
 *
 * 支持纯 XML 属性配置选中状态、颜色、大小和文本。
 * 与 [AwRadioButton] 和 [AwRadioGroup] 配合使用，也可独立使用。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwCheckBox
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:checkBox_text="同意协议"
 *     app:checkBox_checked="false" />
 * ```
 */
class AwCheckBox
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
                    onCheckedChange?.invoke(field)
                }
            }

        var label: String? = null
            set(v) {
                field = v
                invalidate()
            }

        var labelColor: Int = ContextCompat.getColor(context, R.color.aw_color_checkbox_text)
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

        var checkedColor: Int = ContextCompat.getColor(context, R.color.aw_color_checkbox_checked)
            set(v) {
                field = v
                invalidate()
            }

        var uncheckedColor: Int = ContextCompat.getColor(context, R.color.aw_color_checkbox_unchecked)
            set(v) {
                field = v
                invalidate()
            }

        var checkIconSize: Float = 20f.dpFloat
            set(v) {
                field = v
                requestLayout()
                invalidate()
            }

        var customDrawable: Drawable? = null
        var customDrawableChecked: Drawable? = null

        var onCheckedChange: ((Boolean) -> Unit)? = null

        private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val rectF = RectF()

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwCheckBox)
            isChecked = ta.getBoolean(R.styleable.AwCheckBox_checkBox_checked, false)
            label = ta.getString(R.styleable.AwCheckBox_checkBox_text)
            labelColor = ta.getColor(R.styleable.AwCheckBox_checkBox_textColor, labelColor)
            labelSize = ta.getDimension(R.styleable.AwCheckBox_checkBox_textSize, labelSize)
            checkedColor = ta.getColor(R.styleable.AwCheckBox_checkBox_checkedColor, checkedColor)
            uncheckedColor = ta.getColor(R.styleable.AwCheckBox_checkBox_uncheckedColor, uncheckedColor)
            checkIconSize = ta.getDimension(R.styleable.AwCheckBox_checkBox_checkIconSize, checkIconSize)
            val drawableRes = ta.getResourceId(R.styleable.AwCheckBox_checkBox_drawable, 0)
            if (drawableRes != 0) customDrawable = ContextCompat.getDrawable(context, drawableRes)
            val drawableCheckedRes = ta.getResourceId(R.styleable.AwCheckBox_checkBox_drawableChecked, 0)
            if (drawableCheckedRes != 0) customDrawableChecked = ContextCompat.getDrawable(context, drawableCheckedRes)
            ta.recycle()

            boxPaint.strokeWidth = 2f.dpFloat
            checkPaint.strokeWidth = 2.5f.dpFloat
            checkPaint.strokeCap = Paint.Cap.ROUND
            checkPaint.strokeJoin = Paint.Join.ROUND
            labelPaint.textSize = labelSize
            isClickable = true
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val labelW = if (label != null) labelPaint.measureText(label) + 6f.dpFloat else 0f
            val totalW = checkIconSize + labelW + paddingLeft + paddingRight
            val totalH = checkIconSize.coerceAtLeast(labelSize) + paddingTop + paddingBottom
            val w = resolveSize(totalW.toInt(), widthMeasureSpec)
            val h = resolveSize(totalH.toInt(), heightMeasureSpec)
            setMeasuredDimension(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (customDrawable != null && customDrawableChecked != null) {
                val icon = if (isChecked) customDrawableChecked else customDrawable
                val tinted = icon!!.mutate()
                val tint = if (isChecked) checkedColor else uncheckedColor
                DrawableCompat.setTint(tinted, tint)
                val iconSize = checkIconSize.toInt()
                val iconTop = ((height - iconSize) / 2f).toInt()
                tinted.setBounds(paddingLeft, iconTop, paddingLeft + iconSize, iconTop + iconSize)
                tinted.draw(canvas)
            } else {
                // 绘制方块
                val boxLeft = paddingLeft + 2f.dpFloat
                val boxTop = (height - checkIconSize) / 2f

                if (isChecked) {
                    fillPaint.color = checkedColor
                    rectF.set(boxLeft, boxTop, boxLeft + checkIconSize, boxTop + checkIconSize)
                    canvas.drawRoundRect(rectF, 2f.dpFloat, 2f.dpFloat, fillPaint)

                    // 勾
                    checkPaint.color = Color.WHITE
                    val cx = boxLeft + checkIconSize / 2f
                    val cy = boxTop + checkIconSize / 2f
                    val s = checkIconSize * 0.25f
                    canvas.drawLine(cx - s, cy, cx - s * 0.2f, cy + s, checkPaint)
                    canvas.drawLine(cx - s * 0.2f, cy + s, cx + s * 0.8f, cy - s * 0.4f, checkPaint)
                } else {
                    boxPaint.color = uncheckedColor
                    rectF.set(boxLeft, boxTop, boxLeft + checkIconSize, boxTop + checkIconSize)
                    canvas.drawRoundRect(rectF, 2f.dpFloat, 2f.dpFloat, boxPaint)
                }
            }

            // 标签文本
            if (label != null) {
                labelPaint.color = labelColor
                labelPaint.textSize = labelSize
                val textX = paddingLeft + checkIconSize + 8f.dpFloat
                val textY = (height + labelSize / 3f) / 2f
                canvas.drawText(label!!, textX, textY, labelPaint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP) {
                isChecked = !isChecked
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
                putBoolean("checkBox_checked", isChecked)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                super.onRestoreInstanceState(state.getParcelable("superState"))
                isChecked = state.getBoolean("checkBox_checked", false)
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }

