package com.answufeng.ui.demo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.demo.databinding.ActivitySwipeRefreshDemoBinding
import com.answufeng.ui.demo.databinding.ItemSwipeDemoRowBinding
import com.answufeng.ui.recyclerview.AwLoadMoreAdapter
import com.answufeng.ui.widget.setOnRefreshWithStop

private data class DemoRow(val id: Long, val title: String)

/**
 * 演示 AwSwipeRefreshLayout 与 AwLoadMoreAdapter 组合：下拉回第一页、上拉分页，并用 [setOnRefreshWithStop] 结束指示器。
 */
class SwipeRefreshListDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySwipeRefreshDemoBinding
    private val mainHandler = Handler(Looper.getMainLooper())

    private val diff = object : DiffUtil.ItemCallback<DemoRow>() {
        override fun areItemsTheSame(oldItem: DemoRow, newItem: DemoRow) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DemoRow, newItem: DemoRow) = oldItem == newItem
    }

    private val listAdapter = AwLoadMoreAdapter(
        inflate = ItemSwipeDemoRowBinding::inflate,
        diffCallback = diff
    ) { rowBinding, item, _ ->
        rowBinding.tvTitle.text = item.title
    }

    private var nextPageToLoad = 1
    private val lastPageIndex = 2
    private val pageSize = 8

    private fun buildPageItems(pageIndex: Int): List<DemoRow> {
        return (0 until pageSize).map { i ->
            val id = pageIndex * 10_000L + i
            DemoRow(
                id = id,
                title = "第 ${pageIndex + 1} 页 · 第 ${i + 1} 条 · 下拉可刷新回到第 1 页"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipeRefreshDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.setOnBackClickListener { finish() }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = listAdapter

        nextPageToLoad = 1
        listAdapter.submitInitialList(buildPageItems(0))

        listAdapter.setOnLoadMoreListener {
            if (binding.swipeRefresh.isRefreshing) return@setOnLoadMoreListener
            scheduleLoadNextPage()
        }

        binding.swipeRefresh.setOnRefreshWithStop { stopRefreshing ->
            mainHandler.postDelayed({
                nextPageToLoad = 1
                listAdapter.refreshAll(buildPageItems(0))
                stopRefreshing()
            }, 1000L)
        }
    }

    private fun scheduleLoadNextPage() {
        mainHandler.postDelayed({
            if (nextPageToLoad > lastPageIndex) {
                listAdapter.noMore()
                return@postDelayed
            }
            val page = nextPageToLoad
            listAdapter.loadMore(buildPageItems(page))
            nextPageToLoad = page + 1
        }, 700L)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
