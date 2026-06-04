package com.answufeng.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import com.answufeng.ui.R
import java.util.Calendar

/**
 * 日历网格视图，用于展示一个月的日期网格并支持日期选择。
 *
 * 支持功能：
 * - 显示月份日期网格，包含星期头部行和最多6行日期单元格
 * - 单日期选择模式，选中日期以实心圆高亮
 * - 今日以空心圆标记
 * - 上下月日期以较浅颜色显示
 * - 左右滑动切换月份
 * - 通过 XML 属性自定义颜色、字号、首日星期等
 *
 * XML 属性：
 * - cv_firstDayOfWeek: 首日星期（SUN=1, MON=2），默认 MON
 * - cv_showWeekHeaders: 是否显示星期头部行，默认 true
 * - cv_todayColor: 今日标记颜色
 * - cv_selectedColor: 选中日期颜色
 * - cv_dayTextColor: 日期文字颜色
 * - cv_weekHeaderTextColor: 星期头部文字颜色
 * - cv_dayTextSize: 日期文字大小
 * - cv_weekHeaderTextSize: 星期头部文字大小
 */
class AwCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private const val ROWS = 7 // 1 header + 6 day rows
        private const val DAY_CELLS = 6
        private const val WEEK_HEADERS = 1
    }

    /** 当前选中的日期，null 表示未选中 */
    var selectedDate: Calendar? = null
        private set

    /** 当前显示的月份/年份，控制日历显示哪个月 */
    var monthYear: Calendar = Calendar.getInstance()
        private set

    /** 一周的第一天，Calendar.SUNDAY(1) 或 Calendar.MONDAY(2) */
    var firstDayOfWeek: Int = Calendar.MONDAY
        set(value) {
            field = value
            monthYear.firstDayOfWeek = value
            calculateCells()
            invalidate()
        }

    /** 是否显示星期头部行 */
    var showWeekHeaders: Boolean = true
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /** 今日标记颜色 */
    var todayColor: Int = Color.parseColor("#2196F3")
        set(value) {
            field = value
            invalidate()
        }

    /** 选中日期颜色 */
    var selectedColor: Int = Color.parseColor("#1565C0")
        set(value) {
            field = value
            invalidate()
        }

    /** 日期文字颜色 */
    var dayTextColor: Int = Color.parseColor("#212121")
        set(value) {
            field = value
            invalidate()
        }

    /** 星期头部文字颜色 */
    var weekHeaderTextColor: Int = Color.parseColor("#757575")
        set(value) {
            field = value
            invalidate()
        }

    /** 日期文字大小（px） */
    var dayTextSize: Float = 0f
        set(value) {
            field = value
            dayPaint.textSize = value
            invalidate()
        }

    /** 星期头部文字大小（px） */
    var weekHeaderTextSize: Float = 0f
        set(value) {
            field = value
            weekHeaderPaint.textSize = value
            invalidate()
        }

    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val weekHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val todayCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textBounds = Rect()

    /** 日历单元格数据，共 42 个（6行 × 7列） */
    private val cells = arrayOfNulls<CellData>(42)

    /** 月份外日期的文字颜色 */
    private val otherMonthTextColor: Int = Color.parseColor("#BDBDBD")

    private var cellWidth: Int = 0
    private var cellHeight: Int = 0
    private var headerHeight: Int = 0

    private var onDateSelectedListener: ((year: Int, month: Int, day: Int) -> Unit)? = null

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val startX = e1?.x ?: 0f
            val dx = e2.x - startX
            if (Math.abs(dx) > 100 && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (dx < 0) {
                    nextMonth()
                } else {
                    previousMonth()
                }
                return true
            }
            return false
        }
    })

    private data class CellData(
        val day: Int,
        val month: Int,
        val year: Int,
        val isCurrentMonth: Boolean,
        val isToday: Boolean
    )

    init {
        setWillNotDraw(false)

        val defaultDayTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics
        )
        val defaultWeekHeaderTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics
        )

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AwCalendarView, defStyleAttr, 0)
            try {
                firstDayOfWeek = a.getInt(R.styleable.AwCalendarView_cv_firstDayOfWeek, Calendar.MONDAY)
                showWeekHeaders = a.getBoolean(R.styleable.AwCalendarView_cv_showWeekHeaders, true)
                todayColor = a.getColor(R.styleable.AwCalendarView_cv_todayColor, todayColor)
                selectedColor = a.getColor(R.styleable.AwCalendarView_cv_selectedColor, selectedColor)
                dayTextColor = a.getColor(R.styleable.AwCalendarView_cv_dayTextColor, dayTextColor)
                weekHeaderTextColor = a.getColor(R.styleable.AwCalendarView_cv_weekHeaderTextColor, weekHeaderTextColor)
                dayTextSize = a.getDimension(R.styleable.AwCalendarView_cv_dayTextSize, defaultDayTextSize)
                weekHeaderTextSize = a.getDimension(R.styleable.AwCalendarView_cv_weekHeaderTextSize, defaultWeekHeaderTextSize)
            } finally {
                a.recycle()
            }
        }

        if (dayTextSize == 0f) dayTextSize = defaultDayTextSize
        if (weekHeaderTextSize == 0f) weekHeaderTextSize = defaultWeekHeaderTextSize

        dayPaint.textAlign = Paint.Align.CENTER
        dayPaint.textSize = dayTextSize
        dayPaint.typeface = Typeface.DEFAULT

        weekHeaderPaint.textAlign = Paint.Align.CENTER
        weekHeaderPaint.textSize = weekHeaderTextSize
        weekHeaderPaint.typeface = Typeface.DEFAULT_BOLD

        circlePaint.style = Paint.Style.FILL

        todayCirclePaint.style = Paint.Style.STROKE
        todayCirclePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        )

        monthYear.firstDayOfWeek = firstDayOfWeek
        calculateCells()
    }

    /** 设置日期选择监听器 */
    fun setOnDateSelectedListener(listener: (year: Int, month: Int, day: Int) -> Unit) {
        onDateSelectedListener = listener
    }

    /** 设置显示的月份 */
    fun setMonth(year: Int, month: Int) {
        monthYear.set(year, month, 1)
        calculateCells()
        invalidate()
    }

    /** 跳转到今日所在月份 */
    fun goToToday() {
        val today = Calendar.getInstance()
        monthYear.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), 1)
        monthYear.firstDayOfWeek = firstDayOfWeek
        selectedDate = today
        calculateCells()
        invalidate()
    }

    /** 切换到下一个月 */
    fun nextMonth() {
        monthYear.add(Calendar.MONTH, 1)
        calculateCells()
        invalidate()
    }

    /** 切换到上一个月 */
    fun previousMonth() {
        monthYear.add(Calendar.MONTH, -1)
        calculateCells()
        invalidate()
    }

    private fun calculateCells() {
        val displayMonth = monthYear.get(Calendar.MONTH)
        val displayYear = monthYear.get(Calendar.YEAR)

        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = firstDayOfWeek
        cal.set(displayYear, displayMonth, 1)

        val today = Calendar.getInstance()

        // 计算当月1号是星期几（相对于 firstDayOfWeek 的偏移）
        var firstDayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek
        if (firstDayOfWeekInMonth < 0) {
            firstDayOfWeekInMonth += 7
        }

        // 前移到日历网格的起始日期
        cal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeekInMonth)

        for (i in 0 until 42) {
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val isCurrentMonth = month == displayMonth && year == displayYear
            val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            cells[i] = CellData(day, month, year, isCurrentMonth, isToday)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val desiredWidth = suggestedMinimumWidth.coerceAtLeast(0)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            else -> desiredWidth
        }

        cellWidth = width / 7

        // 计算行高
        dayPaint.getTextBounds("30", 0, 2, textBounds)
        val dayTextHeight = textBounds.height()
        weekHeaderPaint.getTextBounds("周三", 0, 2, textBounds)
        val weekHeaderTextHeight = textBounds.height()

        cellHeight = (maxOf(dayTextHeight, weekHeaderTextHeight) * 2.5f).toInt().coerceAtLeast(1)
        headerHeight = if (showWeekHeaders) cellHeight else 0

        val totalRows = if (showWeekHeaders) DAY_CELLS + WEEK_HEADERS else DAY_CELLS
        val desiredHeight = totalRows * cellHeight

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 无子 View，不需要布局
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            handleTap(event.x, event.y)
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return false
    }

    private fun handleTap(x: Float, y: Float) {
        if (x < 0 || x > width) return

        val row = ((y - headerHeight) / cellHeight).toInt()
        if (row < 0 || row >= DAY_CELLS) return

        val col = (x / cellWidth).toInt()
        if (col < 0 || col >= 7) return

        val index = row * 7 + col
        val cell = cells[index] ?: return

        if (!cell.isCurrentMonth) {
            // 点击了非当月日期，切换到对应月份
            monthYear.set(cell.year, cell.month, 1)
            monthYear.firstDayOfWeek = firstDayOfWeek
        }

        val selected = Calendar.getInstance()
        selected.set(cell.year, cell.month, cell.day)
        selectedDate = selected

        onDateSelectedListener?.invoke(cell.year, cell.month, cell.day)
        calculateCells()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (showWeekHeaders) {
            drawWeekHeaders(canvas)
        }

        drawDayCells(canvas)
    }

    private fun drawWeekHeaders(canvas: Canvas) {
        weekHeaderPaint.color = weekHeaderTextColor

        val labels = getWeekLabels()
        for (i in 0 until 7) {
            val cx = i * cellWidth + cellWidth / 2f
            val cy = headerHeight / 2f + getTextCenterOffset(weekHeaderPaint)
            canvas.drawText(labels[i], cx, cy, weekHeaderPaint)
        }
    }

    private fun getWeekLabels(): Array<String> {
        val shortWeekdays = arrayOf(
            context.getString(R.string.aw_cal_sun),
            context.getString(R.string.aw_cal_mon),
            context.getString(R.string.aw_cal_tue),
            context.getString(R.string.aw_cal_wed),
            context.getString(R.string.aw_cal_thu),
            context.getString(R.string.aw_cal_fri),
            context.getString(R.string.aw_cal_sat),
        )
        // 根据 firstDayOfWeek 排列
        val offset = firstDayOfWeek - Calendar.SUNDAY
        val result = arrayOfNulls<String>(7)
        for (i in 0 until 7) {
            result[i] = shortWeekdays[(i + offset) % 7]
        }
        return result.map { it ?: "" }.toTypedArray()
    }

    private fun drawDayCells(canvas: Canvas) {
        val selected = selectedDate
        val today = Calendar.getInstance()

        for (row in 0 until DAY_CELLS) {
            for (col in 0 until 7) {
                val index = row * 7 + col
                val cell = cells[index] ?: continue

                val cx = col * cellWidth + cellWidth / 2f
                val cy = headerHeight + row * cellHeight + cellHeight / 2f + getTextCenterOffset(dayPaint)

                val isSelected = selected != null
                        && cell.year == selected.get(Calendar.YEAR)
                        && cell.month == selected.get(Calendar.MONTH)
                        && cell.day == selected.get(Calendar.DAY_OF_MONTH)

                val isToday = cell.isToday

                val radius = cellWidth.coerceAtMost(cellHeight) / 2f * 0.38f

                // 绘制选中日期的实心圆
                if (isSelected) {
                    circlePaint.color = selectedColor
                    canvas.drawCircle(cx, cy - getTextCenterOffset(dayPaint), radius, circlePaint)
                }

                // 绘制今日的空心圆（仅当非选中时）
                if (isToday && !isSelected) {
                    todayCirclePaint.color = todayColor
                    canvas.drawCircle(cx, cy - getTextCenterOffset(dayPaint), radius, todayCirclePaint)
                }

                // 绘制日期文字
                dayPaint.color = when {
                    isSelected -> Color.WHITE
                    isToday -> todayColor
                    cell.isCurrentMonth -> dayTextColor
                    else -> otherMonthTextColor
                }

                canvas.drawText(cell.day.toString(), cx, cy, dayPaint)
            }
        }
    }

    /** 计算文字垂直居中偏移量 */
    private fun getTextCenterOffset(paint: Paint): Float {
        val fm = paint.fontMetrics
        return (fm.descent - fm.ascent) / 2f - fm.descent
    }
}
