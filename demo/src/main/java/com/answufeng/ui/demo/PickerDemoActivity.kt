package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityPickerDemoBinding
import com.answufeng.ui.viewBinding

class PickerDemoActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityPickerDemoBinding::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker_demo)
        binding.topBar.setOnBackClickListener { finish() }

        // PickerView
        val cities = listOf("北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "南京", "重庆", "西安")
        binding.pickerView.items = cities
        binding.pickerView.selectedIndex = 0
        binding.pickerView.onSelectedListener = { index, text ->
            binding.pickerResult.text = "已选：$text"
        }
        binding.pickerResult.text = "已选：${cities[0]}"

        // DatePicker
        val cal = java.util.Calendar.getInstance()
        binding.datePicker.setDate(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
        binding.datePicker.onDateSelectedListener = { year, month, day ->
            binding.dateResult.text = "日期：${year}年${month}月${day}日"
        }
        binding.dateResult.text = "日期：${cal.get(java.util.Calendar.YEAR)}年${cal.get(java.util.Calendar.MONTH) + 1}月${cal.get(java.util.Calendar.DAY_OF_MONTH)}日"

        // TimePicker
        binding.timePicker.setTime(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
        binding.timePicker.onTimeSelectedListener = { hour, minute ->
            binding.timeResult.text = "时间：${String.format("%02d", hour)}:${String.format("%02d", minute)}"
        }
        binding.timeResult.text = "时间：${String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY))}:${String.format("%02d", cal.get(java.util.Calendar.MINUTE))}"
    }
}


