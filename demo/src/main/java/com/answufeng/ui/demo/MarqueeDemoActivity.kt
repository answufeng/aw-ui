package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwMarqueeTextView
import com.answufeng.ui.widget.AwTitleBar

class MarqueeDemoActivity : AppCompatActivity() {

    private var updateCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marquee_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val marqueeDynamic = findViewById<AwMarqueeTextView>(R.id.marquee_dynamic)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_update_text).setOnClickListener {
            updateCount++
            marqueeDynamic.setText("第 $updateCount 次更新的滚动文本：这是一条动态更新的公告信息。")
        }
    }
}
