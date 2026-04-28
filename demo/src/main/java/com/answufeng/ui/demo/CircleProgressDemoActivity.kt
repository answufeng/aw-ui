package com.answufeng.ui.demo


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwCircleProgressBar

class CircleProgressDemoActivity : AppCompatActivity() {

    private lateinit var progressBar: AwCircleProgressBar
    private var currentProgress = 32f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle_progress_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        progressBar = findViewById(R.id.demo_progress)
        progressBar.setProgressAndMax(currentProgress, 100f)

        findViewById<Button>(R.id.btn_increase).setOnClickListener {
            currentProgress = (currentProgress + 18f).coerceAtMost(100f)
            progressBar.setProgressWithAnimation(currentProgress)
        }
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            currentProgress = 0f
            progressBar.setProgressWithAnimation(currentProgress)
        }
    }
}
