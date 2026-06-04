package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 时间选择面板，基于 [AwPickerView] 实现时/分两列滚轮。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwTimePickerPanel
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:tp_is24Hour="true" />
 * ```
 *
 * 代码用法：
 * ```kotlin
 * timePicker.setOnTimeSelectedListener { hour, minute -> /* ... */ }
 * timePicker.setTime(14, 30)
 * ```
 */
class AwTimePickerPanel
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {

        var selectedHour: Int = 0
            set(v) {
                field = v.coerceIn(0, 23)
                if (::hourPicker.isInitialized) updateSelection()
            }
        var selectedMinute: Int = 0
            set(v) {
                field = v.coerceIn(0, 59)
                if (::minutePicker.isInitialized) updateSelection()
            }

        var textColor: Int = Color.parseColor("#999999")
        var selectedTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_picker_selected_text)
        var textSize: Float = 16f.dpFloat
        var showSeconds: Boolean = false
        var is24Hour: Boolean = true

        var onTimeSelectedListener: ((hour: Int, minute: Int) -> Unit)? = null

        private lateinit var hourPicker: AwPickerView
        private lateinit var minutePicker: AwPickerView
        private var suppressCallback: Boolean = false

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwTimePickerPanel)
            selectedHour = ta.getInt(R.styleable.AwTimePickerPanel_tp_hour, 0)
            selectedMinute = ta.getInt(R.styleable.AwTimePickerPanel_tp_minute, 0)
            textColor = ta.getColor(R.styleable.AwTimePickerPanel_tp_textColor, textColor)
            textSize = ta.getDimension(R.styleable.AwTimePickerPanel_tp_textSize, textSize)
            selectedTextColor = ta.getColor(R.styleable.AwTimePickerPanel_tp_selectedColor, selectedTextColor)
            showSeconds = ta.getBoolean(R.styleable.AwTimePickerPanel_tp_showSeconds, false)
            is24Hour = ta.getBoolean(R.styleable.AwTimePickerPanel_tp_is24Hour, true)
            ta.recycle()

            // 小时滚轮
            val hours = (0..23).map {
                val displayHour = when {
                    is24Hour -> it
                    it == 0 -> 12
                    it <= 12 -> it
                    else -> it - 12
                }
                val period = when {
                    is24Hour -> ""
                    it < 12 -> " AM"
                    else -> " PM"
                }
                String.format("%02d$period", displayHour)
            }.toList()
            hourPicker = createPicker(hours, selectedHour.coerceIn(0, 23))

            // 分隔符 ":"
            val colonView = TextView(context).apply {
                text = ":"
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(selectedTextColor)
            }

            // 分钟滚轮
            val minutes = (0..59).map { String.format("%02d", it) }.toList()
            minutePicker = createPicker(minutes, selectedMinute.coerceIn(0, 59))

            addView(hourPicker, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            addView(colonView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            addView(minutePicker, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        }

        private fun createPicker(
            data: List<String>,
            defaultIndex: Int,
        ): AwPickerView {
            return AwPickerView(context).apply {
                items = data
                this.textColor = this@AwTimePickerPanel.textColor
                this.selectedTextColor = this@AwTimePickerPanel.selectedTextColor
                this.textSize = this@AwTimePickerPanel.textSize
                selectedIndex = defaultIndex
                visibleItemCount = 5
                onSelectedListener = { _, _ ->
                    if (!suppressCallback) {
                        selectedHour = hourPicker.selectedIndex
                        selectedMinute = minutePicker.selectedIndex
                        onTimeSelectedListener?.invoke(selectedHour, selectedMinute)
                    }
                }
            }
        }

        private fun updateSelection() {
            suppressCallback = true
            hourPicker.selectedIndex = selectedHour.coerceIn(0, 23)
            minutePicker.selectedIndex = selectedMinute.coerceIn(0, 59)
            suppressCallback = false
        }

        fun setTime(hour: Int, minute: Int) {
            selectedHour = hour
            selectedMinute = minute
        }

        fun setOnTimeSelected(listener: (hour: Int, minute: Int) -> Unit) {
            onTimeSelectedListener = listener
        }
    }

