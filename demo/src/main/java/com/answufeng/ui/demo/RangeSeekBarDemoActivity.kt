package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwRangeSeekBar

class RangeSeekBarDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_range_seek_bar_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val tvRange = findViewById<TextView>(R.id.tv_range)
        val rangeSeekBar = findViewById<AwRangeSeekBar>(R.id.range_seek_bar)
        rangeSeekBar.onRangeChangeListener = { left, right ->
            tvRange.text = "价格范围：${left.toInt()} - ${right.toInt()}"
        }
        // 初始化显示
        tvRange.text = "价格范围：${rangeSeekBar.leftValue.toInt()} - ${rangeSeekBar.rightValue.toInt()}"

        val tvStepRange = findViewById<TextView>(R.id.tv_step_range)
        val stepSeekBar = findViewById<AwRangeSeekBar>(R.id.range_seek_bar_step)
        stepSeekBar.setRange(0f, 1000f, 50f)
        stepSeekBar.onRangeChangeListener = { left, right ->
            tvStepRange.text = "步长选择：${left.toInt()} - ${right.toInt()}"
        }
        tvStepRange.text = "步长选择：${stepSeekBar.leftValue.toInt()} - ${stepSeekBar.rightValue.toInt()}"
    }
}
