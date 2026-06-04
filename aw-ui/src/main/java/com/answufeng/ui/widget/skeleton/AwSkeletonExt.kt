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
 *
 * 通过反射获取 ViewPager2 内部的 RecyclerView，避免直接依赖未公开的实现细节。
 * 若反射失败，可改用 [RecyclerView.applyAwSkeleton] 手动传入 ViewPager2 的子 RecyclerView。
 */
fun ViewPager2.applyAwSkeleton(
    @LayoutRes itemLayout: Int,
    itemCount: Int = 3,
    config: AwSkeletonConfig = AwSkeletonConfig.default(context),
): AwSkeleton {
    val rv = getInternalRecyclerView()
        ?: throw IllegalStateException(
            "ViewPager2 internal RecyclerView not found. " +
                "Use RecyclerView.applyAwSkeleton() on the child RecyclerView directly.",
        )
    return AwSkeletonRecyclerController(rv, itemLayout, itemCount, config)
}

/** 设置真实 adapter，供 [applyAwSkeleton] 恢复时使用 */
fun AwSkeleton.setContentAdapter(adapter: RecyclerView.Adapter<*>) {
    (this as? AwSkeletonRecyclerController)?.setContentAdapter(adapter)
}

/**
 * 通过反射获取 ViewPager2 内部的 RecyclerView，加 try-catch 保护。
 * 若未来 ViewPager2 内部结构变更导致反射失败，返回 null。
 */
private fun ViewPager2.getInternalRecyclerView(): RecyclerView? {
    return try {
        // 优先尝试直接获取（当前版本兼容）
        getChildAt(0) as? RecyclerView
    } catch (_: Exception) {
        null
    } ?: try {
        // 回退到反射获取
        val field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        field.isAccessible = true
        field.get(this) as? RecyclerView
    } catch (_: Exception) {
        null
    }
}
