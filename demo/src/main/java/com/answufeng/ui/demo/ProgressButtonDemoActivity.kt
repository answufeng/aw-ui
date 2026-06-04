package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwProgressButton

class ProgressButtonDemoActivity : AppCompatActivity() {

    private lateinit var progressButton: AwProgressButton
    private var currentProgress = 0f
    private var downloadThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_button_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        progressButton = findViewById(R.id.progress_button)
        progressButton.setOnClickListener {
            if (progressButton.isIndeterminate) {
                progressButton.isIndeterminate = false
                progressButton.text = "下载"
                currentProgress = 0f
                progressButton.progress = 0f
            } else {
                if (currentProgress >= 100f) {
                    currentProgress = 0f
                    progressButton.progress = 0f
                }
                simulateDownload()
            }
        }
        progressButton.setOnProgressCompleteListener {
            Toast.makeText(this, "下载完成！", Toast.LENGTH_SHORT).show()
            progressButton.text = "重新下载"
        }

        val indeterminateButton = findViewById<AwProgressButton>(R.id.indeterminate_button)
        indeterminateButton.setOnClickListener {
            if (indeterminateButton.isIndeterminate) {
                indeterminateButton.isIndeterminate = false
                indeterminateButton.text = "提交"
            } else {
                indeterminateButton.isIndeterminate = true
                indeterminateButton.text = "提交中..."
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_reset).setOnClickListener {
            downloadThread?.interrupt()
            downloadThread = null
            currentProgress = 0f
            progressButton.progress = 0f
            progressButton.text = "下载"
            progressButton.isIndeterminate = false
        }
    }

    private fun simulateDownload() {
        downloadThread?.interrupt()
        val thread = Thread {
            while (currentProgress < 100f) {
                currentProgress = (currentProgress + 2f).coerceAtMost(100f)
                runOnUiThread {
                    progressButton.progress = currentProgress
                    progressButton.text = "下载中 ${currentProgress.toInt()}%"
                }
                try {
                    Thread.sleep(50)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        downloadThread = thread
        thread.start()
    }
}
