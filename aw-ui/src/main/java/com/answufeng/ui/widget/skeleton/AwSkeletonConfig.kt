package com.answufeng.ui.widget.skeleton

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.answufeng.ui.R

/**
 * 骨架屏全局配置：遮罩颜色、闪光参数、列表占位条数等。
 */
data class AwSkeletonConfig(
    @ColorInt val maskColor: Int,
    @ColorInt val shimmerColor: Int,
    val maskCornerRadiusPx: Float,
    val shimmerDurationMs: Long,
    val showShimmer: Boolean,
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
                itemCount = 6,
            )
        }
    }
}
