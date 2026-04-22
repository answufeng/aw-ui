package com.answufeng.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.MaterialColors

/**
 * 对 [SwipeRefreshLayout] 的轻量封装：默认使用主题 `colorPrimary` 作为刷新指示器着色。
 *
 * ### 与 RecyclerView
 * 将 [androidx.recyclerview.widget.RecyclerView] 作为**唯一子 View** 时，列表须可垂直滚动，
 * 否则下拉手势可能无法触发。刷新结束后务必将 `isRefreshing` 置为 `false`，避免指示器常驻。
 *
 * ### 与 AwLoadMoreAdapter
 * 下拉刷新与加载更多可同时使用：刷新重置页码并 [androidx.recyclerview.widget.ListAdapter.submitList]，
 * 加载更多在底部回调中追加数据；注意避免两次操作并发修改同一数据源。
 */
class AwSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    init {
        val primary = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimary,
            0
        )
        setColorSchemeColors(primary)
    }
}
