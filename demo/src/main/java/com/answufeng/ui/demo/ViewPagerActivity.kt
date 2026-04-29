package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.widget.AwBottomTabBar

class ViewPagerActivity : AppCompatActivity() {

    private val tabTitles = listOf("首页", "发现", "消息", "我的")
    private val tabColors = listOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"),
        Color.parseColor("#96CEB4")
    )

    private lateinit var tabBar: AwBottomTabBar
    private lateinit var viewPager: ViewPager2
    private lateinit var tvInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager)

        initViews()
        setupWindowInsets()
        setupTabBar()

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
    }

    private fun initViews() {
        tabBar = findViewById(R.id.tabBar)
        viewPager = findViewById(R.id.viewPager)
        tvInfo = findViewById(R.id.tv_info)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupTabBar() {
        tabBar.setItems(
            listOf(
                AwBottomTabBar.TabItem(title = "首页", iconRes = R.drawable.ic_home),
                AwBottomTabBar.TabItem(title = "发现", iconRes = R.drawable.ic_discover),
                AwBottomTabBar.TabItem(title = "消息", iconRes = R.drawable.ic_message),
                AwBottomTabBar.TabItem(title = "我的", iconRes = R.drawable.ic_me)
            )
        )

        val fragments = tabTitles.mapIndexed { index, title ->
            TabFragment.newInstance(title, tabColors[index])
        }
        tabBar.bindFragments(this, fragments, viewPager)
        tabBar.setBadgeText(2, "7")
        tabBar.showBadgeDot(3)

        tabBar.setOnTabSelectedListener { index ->
            tvInfo.text = "选中 Tab ${index + 1}: ${tabTitles[index]}"
        }

        tabBar.setOnTabReselectedListener { index ->
            tvInfo.text = "重新选中 Tab ${index + 1}: ${tabTitles[index]}"
        }
    }
}
