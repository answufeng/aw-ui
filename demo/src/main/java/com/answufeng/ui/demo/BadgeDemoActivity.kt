package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwBadgeView
import com.answufeng.ui.widget.AwTitleBar

class BadgeDemoActivity : AppCompatActivity() {

    private lateinit var badgeView: AwBadgeView
    private var state = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        badgeView = findViewById(R.id.demo_badge)

        findViewById<Button>(R.id.btn_next_state).setOnClickListener {
            state = (state + 1) % 4
            when (state) {
                0 -> badgeView.showDot()
                1 -> badgeView.count = 8
                2 -> badgeView.count = 108
                else -> badgeView.setBadgeText("NEW")
            }
        }
        findViewById<Button>(R.id.btn_clear).setOnClickListener { badgeView.clear() }
    }
}
