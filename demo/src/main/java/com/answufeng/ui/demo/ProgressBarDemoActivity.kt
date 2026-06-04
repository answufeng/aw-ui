package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityProgressBarDemoBinding
import com.answufeng.ui.viewBinding

class ProgressBarDemoActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityProgressBarDemoBinding::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar_demo)
        binding.topBar.setOnBackClickListener { finish() }

        binding.btnDecrease.setOnClickListener {
            binding.progressBar.progress = (binding.progressBar.progress - 10).coerceAtLeast(0)
        }
        binding.btnIncrease.setOnClickListener {
            binding.progressBar.progress = (binding.progressBar.progress + 10).coerceAtMost(100)
        }
    }
}


