package com.answufeng.ui.widget

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * 下拉刷新：在 [onRefresh] 中收到 [stopRefreshing]，在数据就绪后调用以关闭指示器。
 *
 * 内部对 [stopRefreshing] 使用 [android.view.View.post] 到本 View，适合从子线程、Handler 或主线程分派结果后结束刷新，减少在非 UI 线程写 [SwipeRefreshLayout.isRefreshing] 的隐患。
 */
fun SwipeRefreshLayout.setOnRefreshWithStop(
    onRefresh: (stopRefreshing: () -> Unit) -> Unit
) {
    setOnRefreshListener {
        onRefresh { post { isRefreshing = false } }
    }
}
