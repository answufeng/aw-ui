package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityChoiceDemoBinding
import com.answufeng.ui.viewBinding

class ChoiceDemoActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityChoiceDemoBinding::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_demo)
        binding.topBar.setOnBackClickListener { finish() }

        // Vertical RadioGroup
        binding.radioGroupVertical.onCheckedChange = { index, label ->
            binding.radioGroupResult.text = "已选：${label ?: "无"}"
        }
    }
}


