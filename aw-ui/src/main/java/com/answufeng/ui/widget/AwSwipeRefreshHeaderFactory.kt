package com.answufeng.ui.widget

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import com.answufeng.ui.widget.AwSwipeRefreshLayout.RefreshStyle

/**
 * 下拉刷新 Header 创建策略（内部使用）。
 *
 * 将 Header 创建逻辑从 [AwSwipeRefreshLayout] 中抽离，便于后续替换/扩展实现。
 */
internal object AwSwipeRefreshHeaderFactory {
    data class Config(
        @ColorInt val tintColor: Int,
        val refreshText: String,
        @ColorInt val refreshTextColor: Int,
        val refreshTextSize: Int,
    )

    fun create(
        context: Context,
        style: RefreshStyle,
        config: Config,
    ): View {
        return when (style) {
            RefreshStyle.SYSTEM ->
                AwSwipeRefreshLayout.SystemRefreshHeaderView(context).apply {
                    tintColor = config.tintColor
                }
            RefreshStyle.FLOWER ->
                AwSwipeRefreshLayout.FlowerRefreshHeaderView(context).apply {
                    tintColor = config.tintColor
                }
            RefreshStyle.ARROW ->
                AwSwipeRefreshLayout.ArrowRefreshHeaderView(context).apply {
                    tintColor = config.tintColor
                }
            RefreshStyle.TEXT ->
                AwSwipeRefreshLayout.TextRefreshHeaderView(context).apply {
                    tintColor = config.tintColor
                    text = config.refreshText
                    if (config.refreshTextColor != 0) textColor = config.refreshTextColor
                    if (config.refreshTextSize > 0) textSize = config.refreshTextSize
                }
        }
    }
}
