package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwCalendarView
import java.util.Calendar

class CalendarDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val calendarView = findViewById<AwCalendarView>(R.id.calendar_view)
        val tvSelectedDate = findViewById<TextView>(R.id.tv_selected_date)
        val tvMonth = findViewById<TextView>(R.id.tv_month)

        updateMonthLabel(calendarView, tvMonth)

        calendarView.setOnDateSelectedListener { year, month, day ->
            tvSelectedDate.text = "选中日期：${year}年${month + 1}月${day}日"
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_prev_month).setOnClickListener {
            calendarView.previousMonth()
            updateMonthLabel(calendarView, tvMonth)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_next_month).setOnClickListener {
            calendarView.nextMonth()
            updateMonthLabel(calendarView, tvMonth)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_go_today).setOnClickListener {
            calendarView.goToToday()
            updateMonthLabel(calendarView, tvMonth)
        }
    }

    private fun updateMonthLabel(calendarView: AwCalendarView, tvMonth: TextView) {
        val cal = calendarView.monthYear
        tvMonth.text = "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月"
    }
}
