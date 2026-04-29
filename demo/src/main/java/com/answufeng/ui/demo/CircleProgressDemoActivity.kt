package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwCircleProgressBar
import com.answufeng.ui.widget.AwTitleBar

class CircleProgressDemoActivity : AppCompatActivity() {

    private lateinit var progressBar: AwCircleProgressBar
    private lateinit var tvProgressValue: TextView
    private var currentProgress = 32f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle_progress_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        progressBar = findViewById(R.id.demo_progress)
        tvProgressValue = findViewById(R.id.tv_progress_value)
        progressBar.setProgressAndMax(currentProgress, 100f)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_animate).setOnClickListener {
            currentProgress = 100f
            progressBar.setProgressWithAnimation(currentProgress, 1500L)
            tvProgressValue.text = "当前进度：100%"
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_increase).setOnClickListener {
            currentProgress = (currentProgress + 20f).coerceAtMost(100f)
            progressBar.setProgressWithAnimation(currentProgress)
            tvProgressValue.text = "当前进度：${currentProgress.toInt()}%"
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_reset).setOnClickListener {
            currentProgress = 0f
            progressBar.setProgressWithAnimation(currentProgress)
            tvProgressValue.text = "当前进度：0%"
        }
    }
}
