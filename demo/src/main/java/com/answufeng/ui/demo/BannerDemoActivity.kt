package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityBannerDemoBinding

class BannerDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBannerDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBannerDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.setOnBackClickListener { finish() }

        val items = listOf(
            BannerItem("版本更新", "更快、更稳、更美观", Color.parseColor("#4F46E5")),
            BannerItem("组件展示", "Banner / StateLayout / Dialog", Color.parseColor("#0EA5E9")),
            BannerItem("暗色适配", "Material3 DayNight", Color.parseColor("#10B981"))
        )

        binding.bannerView.setData(items) { container, item, _ ->
            val tv = TextView(container.context).apply {
                text = "${item.title}\n${item.subtitle}"
                setTextColor(Color.WHITE)
                textSize = 18f
                gravity = Gravity.CENTER
                setBackgroundColor(item.bg)
            }
            (container as android.widget.FrameLayout).addView(tv)
        }

        binding.bannerView.setOnPageClickListener { index ->
            val item = items[index]
            Toast.makeText(this, "${item.title}: ${item.subtitle}", Toast.LENGTH_SHORT).show()
        }

        binding.bannerView.startAutoScroll()

        binding.btnToggleAuto.setOnClickListener {
            if (binding.bannerView.isAutoScrolling) {
                binding.bannerView.stopAutoScroll()
                binding.btnToggleAuto.text = "开始自动滚动"
            } else {
                binding.bannerView.startAutoScroll()
                binding.btnToggleAuto.text = "停止自动滚动"
            }
        }
    }

    private data class BannerItem(
        val title: String,
        val subtitle: String,
        val bg: Int
    )
}

