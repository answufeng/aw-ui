package com.answufeng.ui.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.answufeng.ui.R
import com.google.android.material.color.MaterialColors

/**
 * 支持上拉加载更多的 RecyclerView 适配器，基于 ViewBinding + [ListAdapter]。
 *
 * 内置底部加载状态视图（加载中 / 无更多数据 / 加载失败），
 * 自动检测滚动到底部并触发加载回调。
 * 使用 [AsyncListDiffer][ListAdapter] 进行差量更新，避免闪烁和不必要的 rebind。
 *
 * ### 基本用法
 * ```kotlin
 * val adapter = AwLoadMoreAdapter<ItemBinding, Article>(
 *     inflate = ItemBinding::inflate,
 *     diffCallback = object : DiffUtil.ItemCallback<Article>() {
 *         override fun areItemsTheSame(old: Article, new: Article) = old.id == new.id
 *         override fun areContentsTheSame(old: Article, new: Article) = old == new
 *     }
 * ) { binding, item, _ ->
 *     binding.tvTitle.text = item.title
 * }
 *
 * adapter.setOnLoadMoreListener { adapter.loadMore(nextPageData) }
 * // 或 adapter.noMore() 当没有更多数据时
 * // 或 adapter.loadFailed() 加载失败时（可点击重试）
 *
 * recyclerView.adapter = adapter
 * adapter.submitList(firstPageData)
 * ```
 *
 * ### 加载更多状态
 * - **IDLE**：空闲状态，等待触发
 * - **LOADING**：正在加载中
 * - **NO_MORE**：已加载全部数据
 * - **FAILED**：加载失败，点击可重试
 *
 * ### 注意
 * - 滚动检测仅支持 [LinearLayoutManager]，其他 LayoutManager 不会自动触发加载
 * - 所有方法必须在主线程调用
 *
 * @param VB   ViewBinding 类型
 * @param T    数据类型
 * @param inflate       ViewBinding 的 inflate 函数引用
 * @param diffCallback  DiffUtil 比较回调
 * @param bind          数据绑定回调 (binding, item, position)
 */
class AwLoadMoreAdapter<VB : ViewBinding, T>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    diffCallback: DiffUtil.ItemCallback<T>,
    private val bind: (VB, T, Int) -> Unit
) : ListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    /** 加载更多状态 */
    enum class LoadState {
        IDLE, LOADING, NO_MORE, FAILED
    }

    private var loadState = LoadState.IDLE
    private var onLoadMore: (() -> Unit)? = null
    private var onItemClick: ((T, Int) -> Unit)? = null
    private var onItemLongClick: ((T, Int) -> Boolean)? = null
    private var scrollListener: RecyclerView.OnScrollListener? = null

    /** 加载中提示文字 */
    var loadingText: String = "正在加载..."

    /** 无更多数据提示文字 */
    var noMoreText: String = "—— 没有更多了 ——"

    /** 加载失败提示文字 */
    var failedText: String = "加载失败，点击重试"

    /** 距离底部还有多少个 item 时开始预加载，默认 3 */
    var preloadOffset: Int = 3

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < super.getItemCount()) VIEW_TYPE_ITEM else VIEW_TYPE_FOOTER
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (showFooter()) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ItemViewHolder(binding)
            holder.itemView.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < currentList.size) {
                    onItemClick?.invoke(currentList[pos], pos)
                }
            }
            holder.itemView.setOnLongClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < currentList.size) {
                    onItemLongClick?.invoke(currentList[pos], pos) ?: false
                } else false
            }
            holder
        } else {
            FooterViewHolder.create(parent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder<*>) {
            val item = currentList[position]
            bind(holder.binding as VB, item, position)
        } else if (holder is FooterViewHolder) {
            holder.bind(loadState, loadingText, noMoreText, failedText) {
                if (loadState == LoadState.FAILED) {
                    loadState = LoadState.LOADING
                    notifyItemChanged(currentList.size)
                    onLoadMore?.invoke()
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        scrollListener?.let { recyclerView.removeOnScrollListener(it) }
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                if (loadState != LoadState.IDLE) return

                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount

                if (lastVisible >= total - 1 - preloadOffset) {
                    loadState = LoadState.LOADING
                    if (showFooter()) notifyItemChanged(currentList.size)
                    else notifyItemInserted(currentList.size)
                    onLoadMore?.invoke()
                }
            }
        }
        scrollListener = listener
        recyclerView.addOnScrollListener(listener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        scrollListener?.let { recyclerView.removeOnScrollListener(it) }
        scrollListener = null
    }

    /**
     * 提交首页数据（清空旧数据）。
     *
     * 使用 [ListAdapter] 的 [AsyncListDiffer] 自动进行差量计算。
     *
     * @param list 第一页数据
     */
    fun submitInitialList(list: List<T>) {
        loadState = LoadState.IDLE
        super.submitList(list)
    }

    fun appendList(list: List<T>, commitCallback: Runnable? = null) {
        loadMore(list, commitCallback)
    }

    fun refreshAll(list: List<T>, commitCallback: Runnable? = null) {
        loadState = LoadState.IDLE
        super.submitList(list, commitCallback)
    }

    /**
     * 追加下一页数据（加载更多成功时调用）。
     *
     * 通过合并当前列表和新数据再 [submitList]，DiffUtil 自动计算增量。
     *
     * @param list 新增数据
     */
    fun loadMore(list: List<T>, commitCallback: Runnable? = null) {
        val combined = currentList.toMutableList().apply { addAll(list) }
        loadState = LoadState.IDLE
        super.submitList(combined, commitCallback)
    }

    /**
     * 标记为没有更多数据。
     */
    fun noMore() {
        val hadFooter = showFooter()
        loadState = LoadState.NO_MORE
        if (hadFooter) notifyItemChanged(currentList.size)
        else notifyItemInserted(currentList.size)
    }

    /**
     * 标记加载失败（用户可点击 footer 重试）。
     */
    fun loadFailed() {
        val hadFooter = showFooter()
        loadState = LoadState.FAILED
        if (hadFooter) notifyItemChanged(currentList.size)
        else notifyItemInserted(currentList.size)
    }

    /**
     * 重置加载状态为 IDLE。
     */
    fun resetLoadState() {
        val hadFooter = showFooter()
        loadState = LoadState.IDLE
        if (hadFooter && !showFooter()) {
            notifyItemRemoved(currentList.size)
        }
    }

    /**
     * 设置加载更多监听器。
     *
     * @param listener 触发加载更多时的回调
     */
    fun setOnLoadMoreListener(listener: () -> Unit) {
        onLoadMore = listener
    }

    /**
     * 设置 item 点击监听器。
     */
    fun setOnItemClickListener(listener: (T, Int) -> Unit) {
        onItemClick = listener
    }

    /**
     * 设置 item 长按监听器。
     */
    fun setOnItemLongClickListener(listener: (T, Int) -> Boolean) {
        onItemLongClick = listener
    }

    private fun showFooter(): Boolean = loadState != LoadState.IDLE

    private class ItemViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    private class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadMoreProgress)
        private val textView: TextView = itemView.findViewById(R.id.loadMoreText)

        fun bind(
            state: LoadState,
            loadingText: String,
            noMoreText: String,
            failedText: String,
            onRetry: () -> Unit
        ) {
            when (state) {
                LoadState.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    textView.text = loadingText
                    itemView.setOnClickListener(null)
                    itemView.isClickable = false
                }
                LoadState.NO_MORE -> {
                    progressBar.visibility = View.GONE
                    textView.text = noMoreText
                    itemView.setOnClickListener(null)
                    itemView.isClickable = false
                }
                LoadState.FAILED -> {
                    progressBar.visibility = View.GONE
                    textView.text = failedText
                    itemView.setOnClickListener { onRetry() }
                }
                LoadState.IDLE -> {
                    progressBar.visibility = View.GONE
                    textView.text = ""
                    itemView.setOnClickListener(null)
                    itemView.isClickable = false
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup): FooterViewHolder {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                val textColor = MaterialColors.getColor(
                    context, android.R.attr.textColorSecondary, 0xFF999999.toInt()
                )

                val container = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (48 * density).toInt()
                    )
                    setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
                }

                val progress = ProgressBar(context).apply {
                    id = R.id.loadMoreProgress
                    val size = (24 * density).toInt()
                    layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                        marginEnd = (8 * density).toInt()
                    }
                }

                val text = TextView(context).apply {
                    id = R.id.loadMoreText
                    textSize = 14f
                    setTextColor(textColor)
                }

                container.addView(progress)
                container.addView(text)
                return FooterViewHolder(container)
            }
        }
    }
}

fun <VB : ViewBinding, T> awLoadMoreAdapter(
    diffCallback: DiffUtil.ItemCallback<T>,
    inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
    bind: (VB, T, Int) -> Unit
): AwLoadMoreAdapter<VB, T> {
    return AwLoadMoreAdapter(inflate, diffCallback, bind)
}
