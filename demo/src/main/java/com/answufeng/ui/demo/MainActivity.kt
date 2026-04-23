package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.demo.databinding.ActivityHomeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val adapter = DemoEntryAdapter { entry ->
        startActivity(android.content.Intent(this, entry.activity))
    }

    private lateinit var allEntries: List<DemoEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupList()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.titleBar.title = getString(R.string.demo_home_title)
        binding.titleBar.setRightText(getString(R.string.demo_playbook_action)) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.demo_playbook_title)
                .setMessage(R.string.demo_playbook_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun setupList() {
        allEntries = listOf(
            DemoEntry(
                title = "组件总览",
                desc = "一页快速体验常用控件与交互（输入、进度、Dialog、布局等）",
                activity = ShowcaseActivity::class.java
            ),
            DemoEntry(
                title = "状态页（加载/空/错/自定义）",
                desc = "AwStateLayout 四态与重试；可循环切换无/淡入/交叉/自底（slideFromBottom）过渡",
                activity = StateDemoActivity::class.java
            ),
            DemoEntry(
                title = "下拉刷新 + 列表",
                desc = "AwSwipeRefreshLayout + AwLoadMoreAdapter + setOnRefreshWithStop（上拉分页与下拉回第一页）",
                activity = SwipeRefreshListDemoActivity::class.java
            ),
            DemoEntry(
                title = "轮播 Banner",
                desc = "演示 AwBannerView（自动滚动、指示器、点击回调）",
                activity = BannerDemoActivity::class.java
            ),
            DemoEntry(
                title = "分段控制 Segmented",
                desc = "AwSegmentedControl：文字/图标/指示器样式、bindViewPager2 与独立回调",
                activity = SegmentedControlDemoActivity::class.java
            )
        )

        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter
        adapter.submitList(allEntries)
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { editable ->
            val q = editable?.toString().orEmpty().trim()
            if (q.isEmpty()) {
                adapter.submitList(allEntries)
                return@doAfterTextChanged
            }
            adapter.submitList(
                allEntries.filter {
                    it.title.contains(q, ignoreCase = true) || it.desc.contains(q, ignoreCase = true)
                }
            )
        }
    }
}
