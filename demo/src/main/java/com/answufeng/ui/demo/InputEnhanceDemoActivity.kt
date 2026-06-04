package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityInputEnhanceDemoBinding
import com.answufeng.ui.viewBinding
import com.answufeng.ui.widget.AwDropDownMenu

class InputEnhanceDemoActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityInputEnhanceDemoBinding::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_enhance_demo)
        binding.topBar.setOnBackClickListener { finish() }

        // Stepper
        binding.stepper1.onValueChange = { value ->
            binding.stepperValue.text = "值：$value"
        }

        // DropDownMenu
        binding.dropDownMenu.items = listOf("北京", "上海", "广州", "深圳", "杭州", "成都")
        binding.dropDownMenu.onItemSelected = { index, text ->
            binding.topBar.subtitle = text
        }
    }
}


