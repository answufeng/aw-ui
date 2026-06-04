package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwVerticalMarqueeView

class VerticalMarqueeDemoActivity : AppCompatActivity() {

    private lateinit var marqueeView: AwVerticalMarqueeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vertical_marquee_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        marqueeView = findViewById(R.id.marquee_view)
        marqueeView.items = listOf(
            "恭喜用户 A 获得一等奖",
            "限时优惠：全场商品 8 折起",
            "新品上线：智能手环 Pro",
            "会员日：双倍积分等你拿",
        )
        marqueeView.setOnItemClickListener { index ->
            Toast.makeText(this, "点击了第 ${index + 1} 条公告", Toast.LENGTH_SHORT).show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_start_marquee).setOnClickListener {
            marqueeView.start()
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_stop_marquee).setOnClickListener {
            marqueeView.stop()
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_update_items).setOnClickListener {
            marqueeView.items = listOf(
                "新公告 1：系统升级完成",
                "新公告 2：积分商城上新",
                "新公告 3：签到领红包",
            )
            marqueeView.start()
        }
    }
}
