package com.answufeng.ui.demo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwBottomTabBar
import kotlin.jvm.java

class AwBottomTabBarDemoActivity : AppCompatActivity() {

    private lateinit var tabBar: AwBottomTabBar
    private lateinit var rgTabMode: RadioGroup
    private lateinit var rgLayoutMode: RadioGroup
    private lateinit var rgIndicatorStyle: RadioGroup
    private lateinit var rgIndicatorWidth: RadioGroup
    private lateinit var tvStatus: TextView
    private lateinit var tvItems: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnRemove: Button
    private lateinit var btnReset: Button
    private lateinit var btnBadge: Button
    private lateinit var btnSwitchTab: Button
    private lateinit var btnViewPagerDemo: Button

    private val seedItems = listOf(
        DemoTab("首页", R.drawable.ic_home),
        DemoTab("发现", R.drawable.ic_discover),
        DemoTab("消息", R.drawable.ic_message),
        DemoTab("我的", R.drawable.ic_me)
    )

    private val extraItems = listOf(
        DemoTab("推荐", R.drawable.ic_home),
        DemoTab("动态", R.drawable.ic_discover),
        DemoTab("通知", R.drawable.ic_message),
        DemoTab("设置", R.drawable.ic_me)
    )

    private var addCounter = 0
    private var updateCounter = 0
    private var badgeMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_awbottomtabbar_demo)

        initViews()
        setupWindowInsets()
        setupTabBar()
        setupControls()
        refreshDebugInfo()
    }

    private fun initViews() {
        tabBar = findViewById(R.id.tabBar)
        rgTabMode = findViewById(R.id.rg_tab_mode)
        rgLayoutMode = findViewById(R.id.rg_layout_mode)
        rgIndicatorStyle = findViewById(R.id.rg_indicator_style)
        rgIndicatorWidth = findViewById(R.id.rg_indicator_width)
        tvStatus = findViewById(R.id.tv_status)
        tvItems = findViewById(R.id.tv_items)
        btnAdd = findViewById(R.id.btn_add)
        btnUpdate = findViewById(R.id.btn_update)
        btnRemove = findViewById(R.id.btn_remove)
        btnReset = findViewById(R.id.btn_reset)
        btnBadge = findViewById(R.id.btn_badge)
        btnSwitchTab = findViewById(R.id.btn_switch_tab)
        btnViewPagerDemo = findViewById(R.id.btn_viewpager_demo)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupTabBar() {
        applyItems(seedItems)

        tabBar.selectedColor = Color.parseColor("#FF6B3D")
        tabBar.normalColor = Color.parseColor("#7A7F8F")
        tabBar.indicatorColor = Color.parseColor("#FF6B3D")
        tabBar.tabBackgroundColor = Color.WHITE
        tabBar.cornerRadius = 0f
        tabBar.layoutMode = AwBottomTabBar.LayoutMode.FIXED
        tabBar.tabMinWidth = resources.displayMetrics.density * 72

        tabBar.setOnTabSelectedListener { index ->
            showToast("选中 ${tabTitle(index)}")
            refreshDebugInfo()
        }

        tabBar.setOnTabReselectedListener { index ->
            showToast("重复点击 ${tabTitle(index)}")
            refreshDebugInfo()
        }

        tabBar.setOnTabLongClickListener { index ->
            tabBar.clearBadge(index)
            refreshDebugInfo()
            showToast("已清除 ${tabTitle(index)} 角标")
        }
    }

    private fun setupControls() {
        rgTabMode.setOnCheckedChangeListener { _, checkedId ->
            tabBar.tabMode = when (checkedId) {
                R.id.rb_icon_only -> AwBottomTabBar.TabMode.ICON_ONLY
                R.id.rb_text_only -> AwBottomTabBar.TabMode.TEXT_ONLY
                else -> AwBottomTabBar.TabMode.ICON_TEXT
            }
            refreshDebugInfo()
        }

        rgLayoutMode.setOnCheckedChangeListener { _, checkedId ->
            tabBar.layoutMode = when (checkedId) {
                R.id.rb_layout_fixed -> AwBottomTabBar.LayoutMode.FIXED
                else -> AwBottomTabBar.LayoutMode.SCROLLABLE
            }
            refreshDebugInfo()
        }

        rgIndicatorStyle.setOnCheckedChangeListener { _, checkedId ->
            tabBar.indicatorStyle = when (checkedId) {
                R.id.rb_none -> AwBottomTabBar.IndicatorStyle.NONE
                else -> AwBottomTabBar.IndicatorStyle.LINE
            }
            refreshDebugInfo()
        }

        rgIndicatorWidth.setOnCheckedChangeListener { _, checkedId ->
            tabBar.indicatorWidthMode = when (checkedId) {
                R.id.rb_width_follow -> AwBottomTabBar.IndicatorWidthMode.FOLLOW_TEXT
                else -> AwBottomTabBar.IndicatorWidthMode.MATCH_TAB
            }
            refreshDebugInfo()
        }

        btnAdd.setOnClickListener {
            val insertIndex = (tabBar.getCurrentIndex() + 1).coerceAtMost(tabBar.getItemCount())
            val template = extraItems[addCounter % extraItems.size]
            addCounter += 1
            tabBar.insertItem(insertIndex, AwBottomTabBar.TabItem("${template.title}${addCounter}", iconRes = template.iconRes))
            tabBar.setCurrentIndex(insertIndex, true)
            refreshDebugInfo()
        }

        btnUpdate.setOnClickListener {
            val current = tabBar.getCurrentIndex()
            val oldItem = tabBar.getItem(current) ?: return@setOnClickListener
            updateCounter += 1
            val currentTitle = resolveTitle(oldItem)
            tabBar.updateItem(
                current,
                oldItem.copy(title = "${currentTitle}*${updateCounter}", titleRes = 0)
            )
            refreshDebugInfo()
        }

        btnRemove.setOnClickListener {
            if (tabBar.getItemCount() <= 1) {
                showToast("至少保留一个 Tab")
                return@setOnClickListener
            }
            val removed = tabBar.removeItem(tabBar.getCurrentIndex())
            refreshDebugInfo()
            showToast("已删除 ${removed?.title ?: "当前项"}")
        }

        btnReset.setOnClickListener {
            addCounter = 0
            updateCounter = 0
            badgeMode = 0
            applyItems(seedItems)
            refreshDebugInfo()
        }

        btnBadge.setOnClickListener {
            val current = tabBar.getCurrentIndex()
            badgeMode = (badgeMode + 1) % 4
            when (badgeMode) {
                0 -> tabBar.clearBadge(current)
                1 -> tabBar.showBadgeDot(current)
                2 -> tabBar.setBadgeCount(current, 8)
                else -> tabBar.setBadgeText(current, "NEW")
            }
            refreshDebugInfo()
        }

        btnSwitchTab.setOnClickListener {
            val nextIndex = (tabBar.getCurrentIndex() + 1) % tabBar.getItemCount()
            tabBar.setCurrentIndex(nextIndex, true)
        }

        btnViewPagerDemo.setOnClickListener {
            startActivity(Intent(this, ViewPagerActivity::class.java))
        }
    }

    private fun applyItems(items: List<DemoTab>) {
        tabBar.setItems(items.map { AwBottomTabBar.TabItem(title = it.title, iconRes = it.iconRes) })
        tabBar.setCurrentIndex(0, false)
        tabBar.clearAllBadges()
    }

    private fun refreshDebugInfo() {
        val current = tabBar.getCurrentIndex()
        tvStatus.text = buildString {
            append("当前选中: ")
            append(current + 1)
            append(" / ")
            append(tabBar.getItemCount())
            append("    模式: ")
            append(tabBar.tabMode.name)
            append("    布局: ")
            append(tabBar.layoutMode.name)
            append("    指示器: ")
            append(tabBar.indicatorStyle.name)
        }

        tvItems.text = tabBar.getItems().mapIndexed { index, item ->
            val title = resolveTitle(item)
            val marker = if (index == current) "●" else "○"
            val badge = when {
                tabBar.getBadgeText(index) != null -> " [badge=${tabBar.getBadgeText(index)}]"
                tabBar.hasBadge(index) -> " [badge=dot]"
                else -> ""
            }
            "$marker ${index + 1}. $title$badge"
        }.joinToString("\n")
    }

    private fun tabTitle(index: Int): String {
        val item = tabBar.getItem(index) ?: return "Tab ${index + 1}"
        return resolveTitle(item)
    }

    private fun resolveTitle(item: AwBottomTabBar.TabItem): String {
        return when {
            item.title.isNotBlank() -> item.title
            item.titleRes != 0 -> getString(item.titleRes)
            else -> "未命名"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private data class DemoTab(
        val title: String,
        val iconRes: Int
    )
}
