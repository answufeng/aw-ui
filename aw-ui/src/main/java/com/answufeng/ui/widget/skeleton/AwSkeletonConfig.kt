package com.answufeng.ui.widget.skeleton

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.answufeng.ui.R

/**
 * 骨架屏全局配置：遮罩颜色、闪光参数、列表占位条数等。
 */
data class AwSkeletonConfig(
    /** 遮罩背景色 */
    @ColorInt val maskColor: Int,
    /** 闪光高亮色 */
    @ColorInt val shimmerColor: Int,
    /** 遮罩圆角半径（px） */
    val maskCornerRadiusPx: Float,
    /** 闪光动画周期（毫秒） */
    val shimmerDurationMs: Long,
    /** 是否显示闪光动画 */
    val showShimmer: Boolean,
    /** RecyclerView/ViewPager2 骨架占位 item 数量，默认 6 */
    val itemCount: Int = 6,
) {
    companion object {
        fun default(context: Context): AwSkeletonConfig {
            val density = context.resources.displayMetrics.density
            return AwSkeletonConfig(
                maskColor = ContextCompat.getColor(context, R.color.aw_color_skeleton_base),
                shimmerColor = ContextCompat.getColor(context, R.color.aw_color_skeleton_highlight),
                maskCornerRadiusPx = 4f * density,
                shimmerDurationMs = 1500L,
                showShimmer = true,
            )
        }
    }
}
