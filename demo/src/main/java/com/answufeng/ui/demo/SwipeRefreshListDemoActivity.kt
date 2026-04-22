package com.answufeng.ui.demo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.demo.databinding.ActivitySwipeRefreshDemoBinding
import com.answufeng.ui.demo.databinding.ItemSwipeDemoRowBinding
import com.answufeng.ui.recyclerview.AwSimpleAdapter

private data class DemoRow(val id: Long, val title: String)

class SwipeRefreshListDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySwipeRefreshDemoBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private var nextId = 4L

    private val diff = object : DiffUtil.ItemCallback<DemoRow>() {
        override fun areItemsTheSame(oldItem: DemoRow, newItem: DemoRow) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DemoRow, newItem: DemoRow) = oldItem == newItem
    }

    private val listAdapter = AwSimpleAdapter(
        inflate = ItemSwipeDemoRowBinding::inflate,
        diffCallback = diff,
        bind = { rowBinding, item, _ ->
            rowBinding.tvTitle.text = item.title
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipeRefreshDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.setOnBackClickListener { finish() }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = listAdapter
        listAdapter.submitList(
            listOf(
                DemoRow(1, "下拉触发刷新（模拟 1.2s）"),
                DemoRow(2, "可与 AwLoadMoreAdapter 组合：刷新重置页码，加载更多追加尾部"),
                DemoRow(3, "列表为 RecyclerView 单 child，符合 SwipeRefresh 手势要求")
            )
        )

        binding.swipeRefresh.setOnRefreshListener {
            mainHandler.postDelayed({
                val current = listAdapter.currentList.toMutableList()
                current.add(0, DemoRow(nextId++, "刷新于 ${System.currentTimeMillis() % 100000}"))
                listAdapter.submitList(current)
                binding.swipeRefresh.isRefreshing = false
            }, 1200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
