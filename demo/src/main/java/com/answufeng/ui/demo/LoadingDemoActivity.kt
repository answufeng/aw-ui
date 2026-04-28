package com.answufeng.ui.demo


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwLoadingView

class LoadingDemoActivity : AppCompatActivity() {

    private lateinit var loadingView: AwLoadingView
    private var styleIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        loadingView = findViewById(R.id.demo_loading)

        findViewById<Button>(R.id.btn_switch_style).setOnClickListener {
            styleIndex = (styleIndex + 1) % AwLoadingView.Style.values().size
            loadingView.style = AwLoadingView.Style.values()[styleIndex]
        }
        findViewById<Button>(R.id.btn_toggle_animation).setOnClickListener {
            if (loadingView.isAnimating) loadingView.stop() else loadingView.start()
        }
    }
}
