package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.demo.databinding.ActivityBannerDemoBinding
import com.answufeng.ui.widget.AwBannerView

class BannerDemoActivity : AppCompatActivity() {

    private lateinit var bannerView: AwBannerView
    private var auto = true

    private val pages = listOf(
        "春季活动" to "#FF8A65",
        "新品上线" to "#4DB6AC",
        "限时折扣" to "#64B5F6",
        "会员专享" to "#BA68C8"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        bannerView = findViewById(R.id.demo_banner)
        bannerView.setData(pages) { container, item, position ->
            container.addView(
                TextView(container.context).apply {
                    text = "${position + 1}. ${item.first}"
                    textSize = 24f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor(item.second))
                    layoutParams = android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            )
        }
        bannerView.setOnPageClickListener {
            Toast.makeText(this, "点击 Banner ${it + 1}", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_toggle_auto).setOnClickListener {
            auto = !auto
            if (auto) bannerView.startAutoScroll() else bannerView.stopAutoScroll()
        }
        findViewById<Button>(R.id.btn_jump_third).setOnClickListener {
            bannerView.setCurrentItem(2, true)
        }
    }
}

