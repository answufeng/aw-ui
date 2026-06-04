package com.answufeng.ui.widget.skeleton

/**
 * 骨架屏统一门面：显示遮罩占位或恢复真实内容。
 */
interface AwSkeleton {
    /** 显示骨架遮罩 */
    fun showSkeleton()

    /** 显示真实内容 */
    fun showContent(animate: Boolean = true)

    /** 当前是否处于骨架态 */
    val isShowingSkeleton: Boolean

    /** 运行时配置 */
    var config: AwSkeletonConfig

    /** 清理资源（移除遮罩视图、停止动画），在不再使用骨架时调用 */
    fun dispose()
}
