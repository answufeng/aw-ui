package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwBadgeView
import com.answufeng.ui.widget.AwTitleBar

class BadgeDemoActivity : AppCompatActivity() {

    private lateinit var badgeView: AwBadgeView
    private lateinit var badgeDot: AwBadgeView
    private lateinit var badgeText: AwBadgeView
    private var state = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        badgeView = findViewById(R.id.demo_badge)
        badgeDot = findViewById(R.id.demo_badge_dot)
        badgeText = findViewById(R.id.demo_badge_text)

        badgeView.count = 5
        badgeDot.showDot()
        badgeText.setBadgeText("NEW")

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_next_state).setOnClickListener {
            state = (state + 1) % 4
            when (state) {
                0 -> {
                    badgeView.showDot()
                    badgeDot.showDot()
                    badgeText.showDot()
                }
                1 -> {
                    badgeView.count = 8
                    badgeDot.count = 3
                    badgeText.count = 12
                }
                2 -> {
                    badgeView.count = 108
                    badgeDot.count = 99
                    badgeText.count = 999
                }
                else -> {
                    badgeView.setBadgeText("NEW")
                    badgeDot.setBadgeText("HOT")
                    badgeText.setBadgeText("VIP")
                }
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_clear).setOnClickListener {
            badgeView.clear()
            badgeDot.clear()
            badgeText.clear()
        }
    }
}
