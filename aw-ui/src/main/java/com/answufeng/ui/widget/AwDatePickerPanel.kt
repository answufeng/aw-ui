package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat
import java.util.Calendar

/**
 * 日期选择面板，基于 [AwPickerView] 实现年/月/日三列滚轮。
 *
 * 可通过属性或代码设置初始日期、文字颜色、选中颜色等。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwDatePickerPanel
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:dp_selectedColor="@color/aw_color_primary" />
 * ```
 *
 * 代码用法：
 * ```kotlin
 * datePicker.setOnDateSelectedListener { year, month, day -> /* ... */ }
 * datePicker.setDate(2026, 6, 3)
 * ```
 */
class AwDatePickerPanel
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {

        var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
            set(v) { field = v; if (::yearPicker.isInitialized) updateDateSelection() }
        var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
            set(v) { field = v.coerceIn(1, 12); if (::monthPicker.isInitialized) updateDateSelection() }
        var selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            set(v) { field = v.coerceIn(1, getDaysInMonth(selectedYear, selectedMonth)); if (::dayPicker.isInitialized) updateDateSelection() }

        var textColor: Int = Color.parseColor("#999999")
        var selectedTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_picker_selected_text)
        var headerColor: Int = ContextCompat.getColor(context, R.color.aw_color_date_header)
        var headerTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_date_header_text)
        var textSize: Float = 16f.dpFloat

        var onDateSelectedListener: ((year: Int, month: Int, day: Int) -> Unit)? = null

        private var startYear: Int = Calendar.getInstance().get(Calendar.YEAR) - 20
        private var endYear: Int = Calendar.getInstance().get(Calendar.YEAR) + 20

        private lateinit var yearPicker: AwPickerView
        private lateinit var monthPicker: AwPickerView
        private lateinit var dayPicker: AwPickerView
        private var suppressCallback: Boolean = false

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwDatePickerPanel)
            selectedYear = ta.getInt(R.styleable.AwDatePickerPanel_dp_year, selectedYear)
            selectedMonth = ta.getInt(R.styleable.AwDatePickerPanel_dp_month, selectedMonth)
            selectedDay = ta.getInt(R.styleable.AwDatePickerPanel_dp_day, selectedDay)
            textColor = ta.getColor(R.styleable.AwDatePickerPanel_dp_textColor, textColor)
            textSize = ta.getDimension(R.styleable.AwDatePickerPanel_dp_textSize, textSize)
            selectedTextColor = ta.getColor(R.styleable.AwDatePickerPanel_dp_selectedColor, selectedTextColor)
            headerColor = ta.getColor(R.styleable.AwDatePickerPanel_dp_headerColor, headerColor)
            headerTextColor = ta.getColor(R.styleable.AwDatePickerPanel_dp_headerTextColor, headerTextColor)
            startYear = ta.getInt(R.styleable.AwDatePickerPanel_dp_startYear, startYear)
            endYear = ta.getInt(R.styleable.AwDatePickerPanel_dp_endYear, endYear)
            ta.recycle()

            if (startYear > endYear) {
                val tmp = startYear
                startYear = endYear
                endYear = tmp
            }

            // 年份滚轮
            val years = (startYear..endYear).map { "${it}年" }.toList()
            yearPicker = createPicker(years, (selectedYear - startYear).coerceIn(0, years.size - 1))

            // 月份滚轮
            val months = (1..12).map { "${it}月" }.toList()
            monthPicker = createPicker(months, (selectedMonth - 1).coerceIn(0, 11))

            // 日滚轮
            val days = (1..getDaysInMonth(selectedYear, selectedMonth)).map { "${it}日" }.toList()
            dayPicker = createPicker(days, (selectedDay - 1).coerceIn(0, days.size - 1))

            addView(yearPicker, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            addView(monthPicker, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
            addView(dayPicker, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        }

        private fun createPicker(
            data: List<String>,
            defaultIndex: Int,
        ): AwPickerView {
            return AwPickerView(context).apply {
                items = data
                this.textColor = this@AwDatePickerPanel.textColor
                this.selectedTextColor = this@AwDatePickerPanel.selectedTextColor
                this.textSize = this@AwDatePickerPanel.textSize
                selectedIndex = defaultIndex
                visibleItemCount = 5
                onSelectedListener = { _, _ ->
                    if (!suppressCallback) {
                        updateFromPickers()
                    }
                }
            }
        }

        private fun updateFromPickers() {
            val yearIndex = yearPicker.selectedIndex
            val monthIndex = monthPicker.selectedIndex
            val dayIndex = dayPicker.selectedIndex

            val newYear = startYear + yearIndex
            val newMonth = monthIndex + 1
            val maxDay = getDaysInMonth(newYear, newMonth)
            val newDay = (dayIndex + 1).coerceAtMost(maxDay)

            selectedYear = newYear
            selectedMonth = newMonth
            selectedDay = newDay

            // 更新天数
            val days = (1..getDaysInMonth(newYear, newMonth)).map { "${it}日" }.toList()
            suppressCallback = true
            dayPicker.items = days
            dayPicker.selectedIndex = (newDay - 1).coerceIn(0, days.size - 1)
            suppressCallback = false

            onDateSelectedListener?.invoke(newYear, newMonth, newDay)
        }

        private fun updateDateSelection() {
            suppressCallback = true

            val yearIndex = (selectedYear - startYear).coerceIn(0, endYear - startYear)
            yearPicker.selectedIndex = yearIndex

            val monthIndex = (selectedMonth - 1).coerceIn(0, 11)
            monthPicker.selectedIndex = monthIndex

            val days = (1..getDaysInMonth(selectedYear, selectedMonth))
            dayPicker.items = days.map { "${it}日" }.toList()
            dayPicker.selectedIndex = (selectedDay - 1).coerceIn(0, days.last - 1)

            suppressCallback = false
        }

        private fun getDaysInMonth(year: Int, month: Int): Int {
            return when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 30
            }
        }

        private fun isLeapYear(year: Int): Boolean =
            (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

        fun setDate(year: Int, month: Int, day: Int) {
            selectedYear = year
            selectedMonth = month
            selectedDay = day
        }

        fun setOnDateSelected(listener: (year: Int, month: Int, day: Int) -> Unit) {
            onDateSelectedListener = listener
        }
    }



