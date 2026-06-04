package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwStickyHeaderLayout

class StickyHeaderLayoutDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticky_header_layout_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val stickyLayout = findViewById<AwStickyHeaderLayout>(R.id.sticky_header_layout)
        val tvStatus = findViewById<TextView>(R.id.tv_sticky_status)

        stickyLayout.onHeaderStickListener = { stuck ->
            tvStatus.text = if (stuck) "头部已粘住" else "头部未粘住"
            tvStatus.setTextColor(if (stuck) Color.parseColor("#F44336") else Color.parseColor("#4CAF50"))
        }
    }
}
