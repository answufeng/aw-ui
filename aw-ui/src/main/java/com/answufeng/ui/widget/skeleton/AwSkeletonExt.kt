package com.answufeng.ui.widget.skeleton

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * 为任意 [ViewGroup] 创建骨架遮罩控制器。
 */
fun ViewGroup.createAwSkeleton(config: AwSkeletonConfig = AwSkeletonConfig.default(context)): AwSkeleton {
    return AwSkeletonController(this, config)
}

/**
 * 为 [View] 创建骨架控制器；若非 [ViewGroup] 则要求其已挂载到 [ViewGroup]。
 */
fun View.createAwSkeleton(config: AwSkeletonConfig = AwSkeletonConfig.default(context)): AwSkeleton {
    if (this is AwSkeletonLayout) return this
    if (this is ViewGroup) return createAwSkeleton(config)
    val parent = parent as? ViewGroup
        ?: throw IllegalArgumentException("View must be attached to a ViewGroup")
    return parent.createAwSkeleton(config)
}

/**
 * RecyclerView 骨架：临时替换 adapter 为占位 item。
 */
fun RecyclerView.applyAwSkeleton(
    @LayoutRes itemLayout: Int,
    itemCount: Int = AwSkeletonConfig.default(context).itemCount,
    config: AwSkeletonConfig = AwSkeletonConfig.default(context),
): AwSkeleton {
    return AwSkeletonRecyclerController(this, itemLayout, itemCount, config)
}

/**
 * ViewPager2 骨架：对其内部 RecyclerView 应用 [applyAwSkeleton]。
 */
fun ViewPager2.applyAwSkeleton(
    @LayoutRes itemLayout: Int,
    itemCount: Int = 3,
    config: AwSkeletonConfig = AwSkeletonConfig.default(context),
): AwSkeleton {
    val rv = getChildAt(0) as? RecyclerView
        ?: throw IllegalStateException("ViewPager2 internal RecyclerView not found")
    return AwSkeletonRecyclerController(rv, itemLayout, itemCount, config)
}

/** 设置真实 adapter，供 [applyAwSkeleton] 恢复时使用 */
fun AwSkeleton.setContentAdapter(adapter: RecyclerView.Adapter<*>) {
    (this as? AwSkeletonRecyclerController)?.setContentAdapter(adapter)
}
