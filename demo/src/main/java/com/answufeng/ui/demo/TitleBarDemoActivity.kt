package com.answufeng.ui.demo


import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwTitleBar

class TitleBarDemoActivity : AppCompatActivity() {

    private lateinit var titleBar: AwTitleBar
    private var compact = false
    private var subtitleVisible = true
    private var actionText = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_titlebar_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        titleBar = findViewById(R.id.demo_titlebar)
        titleBar.title = "订单中心"
        titleBar.subtitle = "支持副标题、图标、文字动作和沉浸式"
        titleBar.setRightText("编辑") { show("点击了右侧文字") }
        titleBar.setRightIcon(R.drawable.ic_message) { show("点击了右侧图标") }
        titleBar.setOnBackClickListener { finish() }

        findViewById<Button>(R.id.btn_toggle_subtitle).setOnClickListener {
            subtitleVisible = !subtitleVisible
            titleBar.subtitle = if (subtitleVisible) "支持副标题、图标、文字动作和沉浸式" else ""
        }
        findViewById<Button>(R.id.btn_toggle_action).setOnClickListener {
            actionText = !actionText
            if (actionText) {
                titleBar.setRightText("编辑") { show("点击了右侧文字") }
                titleBar.setRightIcon(0)
            } else {
                titleBar.setRightText("")
                titleBar.setRightIcon(R.drawable.ic_message) { show("点击了右侧图标") }
            }
        }
        findViewById<Button>(R.id.btn_toggle_height).setOnClickListener {
            compact = !compact
            titleBar.barHeightPx = if (compact) (48 * resources.displayMetrics.density).toInt() else (64 * resources.displayMetrics.density).toInt()
        }
    }

    private fun show(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
