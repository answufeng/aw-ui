package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.ui.demo.databinding.ActivitySwipeRefreshDemoBinding
import com.answufeng.ui.demo.databinding.ItemSwipeDemoRowBinding
import com.answufeng.ui.widget.AwSwipeRefreshLayout

private data class DemoRow(val id: Long, val title: String)
private enum class FooterState { HIDDEN, LOADING, NO_MORE }

class SwipeRefreshListDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySwipeRefreshDemoBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private val rows = mutableListOf<DemoRow>()
    private val listAdapter = DemoListAdapter()

    private var nextPageToLoad = 1
    private val lastPageIndex = 3
    private val pageSize = 8
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipeRefreshDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.paddingBottom)
            insets
        }

        binding.titleBar.setOnBackClickListener { finish() }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = listAdapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isLoading || binding.swipeRefresh.isRefreshing) return
                if (nextPageToLoad > lastPageIndex) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = lm.findLastVisibleItemPosition()
                if (lastVisible >= listAdapter.itemCount - 2) {
                    scheduleLoadNextPage()
                }
            }
        })

        loadFirstPage(isPullRefresh = false)

        binding.swipeRefresh.refreshListener = {
            loadFirstPage(isPullRefresh = true)
        }

        binding.chipGroupStyle.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(binding.chipCustom.id)) {
                binding.swipeRefresh.setCustomHeaderView(createCustomHeaderView())
            } else {
                val style = when {
                    checkedIds.contains(binding.chipDefault.id) -> AwSwipeRefreshLayout.RefreshStyle.SYSTEM
                    checkedIds.contains(binding.chipFlower.id) -> AwSwipeRefreshLayout.RefreshStyle.FLOWER
                    checkedIds.contains(binding.chipArrow.id) -> AwSwipeRefreshLayout.RefreshStyle.ARROW
                    checkedIds.contains(binding.chipText.id) -> AwSwipeRefreshLayout.RefreshStyle.TEXT
                    else -> AwSwipeRefreshLayout.RefreshStyle.SYSTEM
                }
                binding.swipeRefresh.refreshStyle = style
            }
        }
    }

    private fun buildPageItems(page: Int): List<DemoRow> {
        val start = page * pageSize + 1L
        return (1..pageSize).map { i ->
            DemoRow(start + i - 1, "列表项 #${start + i - 1}")
        }
    }

    private fun createCustomHeaderView(): View {
        val density = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (56 * density).toInt()
            )
        }
        val progressBar = ProgressBar(this).apply {
            val size = (24 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (10 * density).toInt()
            }
            indeterminateDrawable.setTint(Color.parseColor("#E91E63"))
        }
        val textView = TextView(this).apply {
            text = "自定义刷新中..."
            textSize = 14f
            setTextColor(Color.parseColor("#E91E63"))
        }
        container.addView(progressBar)
        container.addView(textView)
        return container
    }

    private fun loadFirstPage(isPullRefresh: Boolean) {
        isLoading = true
        listAdapter.setFooter(FooterState.HIDDEN)
        mainHandler.postDelayed({
            rows.clear()
            rows.addAll(buildPageItems(1))
            nextPageToLoad = 2
            isLoading = false
            listAdapter.submit(rows, FooterState.HIDDEN)
            if (isPullRefresh) {
                binding.swipeRefresh.finishRefresh()
            }
        }, 500L)
    }

    private fun scheduleLoadNextPage() {
        if (isLoading || nextPageToLoad > lastPageIndex) return
        isLoading = true
        listAdapter.setFooter(FooterState.LOADING)
        mainHandler.postDelayed({
            val page = nextPageToLoad
            rows.addAll(buildPageItems(page))
            nextPageToLoad = page + 1
            isLoading = false
            val footer = if (nextPageToLoad > lastPageIndex) FooterState.NO_MORE else FooterState.HIDDEN
            listAdapter.submit(rows, footer)
        }, 700L)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }

    private class DemoListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val data = mutableListOf<DemoRow>()
        private var footerState: FooterState = FooterState.HIDDEN

        companion object {
            private const val TYPE_ROW = 1
            private const val TYPE_FOOTER = 2
        }

        fun submit(rows: List<DemoRow>, footer: FooterState) {
            data.clear()
            data.addAll(rows)
            footerState = footer
            notifyDataSetChanged()
        }

        fun setFooter(state: FooterState) {
            footerState = state
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return data.size + if (footerState == FooterState.HIDDEN) 0 else 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position < data.size) TYPE_ROW else TYPE_FOOTER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_ROW) {
                val binding = ItemSwipeDemoRowBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                RowHolder(binding)
            } else {
                FooterHolder(createFooterView(parent))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is RowHolder) {
                holder.binding.tvTitle.text = data[position].title
            } else if (holder is FooterHolder) {
                holder.bind(footerState)
            }
        }

        private fun createFooterView(parent: ViewGroup): View {
            val context = parent.context
            val density = context.resources.displayMetrics.density
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (48 * density).toInt()
                )
            }
            val progress = ProgressBar(context).apply {
                val size = (22 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (8 * density).toInt()
                }
            }
            val text = TextView(context).apply {
                textSize = 14f
                setTextColor(0xFF666666.toInt())
            }
            container.addView(progress)
            container.addView(text)
            return container
        }

        private class RowHolder(val binding: ItemSwipeDemoRowBinding) : RecyclerView.ViewHolder(binding.root)

        private class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val container = itemView as LinearLayout
            private val progressBar = container.getChildAt(0) as ProgressBar
            private val textView = container.getChildAt(1) as TextView

            fun bind(state: FooterState) {
                when (state) {
                    FooterState.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        textView.text = "正在加载..."
                    }
                    FooterState.NO_MORE -> {
                        progressBar.visibility = View.GONE
                        textView.text = "—— 没有更多了 ——"
                    }
                    FooterState.HIDDEN -> {
                        progressBar.visibility = View.GONE
                        textView.text = ""
                    }
                }
            }
        }
    }
}
