package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwLoadingView
import com.answufeng.ui.widget.AwTitleBar

class LoadingDemoActivity : AppCompatActivity() {

    private lateinit var loadingView: AwLoadingView
    private lateinit var tvCurrentStyle: TextView
    private var styleIndex = 0
    private val styles = AwLoadingView.Style.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        loadingView = findViewById(R.id.demo_loading)
        tvCurrentStyle = findViewById(R.id.tv_current_style)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_switch_style).setOnClickListener {
            styleIndex = (styleIndex + 1) % styles.size
            loadingView.style = styles[styleIndex]
            tvCurrentStyle.text = "当前样式：${styles[styleIndex].name}"
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_toggle_animation).setOnClickListener {
            if (loadingView.isAnimating) loadingView.stop() else loadingView.start()
        }
    }
}
