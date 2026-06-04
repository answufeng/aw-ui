package com.answufeng.ui.widget.skeleton

import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.doOnAttach

/**
 * 对任意 [ViewGroup] 内容根节点挂载遮罩层的骨架控制器。
 */
internal class AwSkeletonController(
    private val contentRoot: ViewGroup,
    initialConfig: AwSkeletonConfig,
) : AwSkeleton {
    override var config: AwSkeletonConfig = initialConfig
        set(value) {
            field = value
            maskView.applyConfig(value)
        }

    private val host: ViewGroup = contentRoot.parent as? ViewGroup ?: contentRoot

    private val maskView: AwSkeletonMaskView = AwSkeletonMaskView(contentRoot.context)

    private var showingSkeleton = false
    private var contentHidden = false
    private var maskAttached = false

    override val isShowingSkeleton: Boolean get() = showingSkeleton

    init {
        maskView.applyConfig(config)
        maskView.visibility = View.GONE
        attachMaskView()
    }

    /**
     * 将 maskView 添加到 host 中。
     * 若 contentRoot 尚未挂载（parent == null），则延迟到 attach 后再添加，
     * 避免 host 回退为 contentRoot 自身导致 maskView 作为子 View 被一同隐藏。
     */
    private fun attachMaskView() {
        if (maskView.parent != null) return
        if (host !== contentRoot) {
            val lp = contentRoot.layoutParams
            host.addView(maskView, lp)
            maskAttached = true
        } else {
            // contentRoot 尚未挂载到父 ViewGroup，延迟添加
            contentRoot.doOnAttach {
                if (maskView.parent != null) return@doOnAttach
                val parent = contentRoot.parent as? ViewGroup ?: return@doOnAttach
                parent.addView(maskView, contentRoot.layoutParams)
                maskAttached = true
                if (showingSkeleton) {
                    maskView.visibility = View.VISIBLE
                    maskView.bringToFront()
                    rebuildMask()
                }
            }
        }
    }

    override fun showSkeleton() {
        if (showingSkeleton) {
            rebuildMask()
            return
        }
        showingSkeleton = true
        contentRoot.isEnabled = false
        if (maskAttached) {
            maskView.visibility = View.VISIBLE
            maskView.bringToFront()
        }
        rebuildMask()
        maskView.startShimmer()
    }

    override fun showContent(animate: Boolean) {
        if (!showingSkeleton && !contentHidden) return
        showingSkeleton = false
        maskView.stopShimmer()
        maskView.visibility = View.GONE
        setContentVisibleForSkeleton(true)
        if (animate) {
            fadeInContent(contentRoot, 200L)
        } else {
            contentRoot.alpha = 1f
        }
    }

    private fun setContentVisibleForSkeleton(show: Boolean) {
        contentHidden = !show
        contentRoot.visibility = if (show) View.VISIBLE else View.INVISIBLE
        contentRoot.isEnabled = show
    }

    private fun rebuildMask() {
        contentRoot.doOnLayout {
            // 检查是否仍处于骨架态，避免 showContent() 后 doOnLayout 回调仍执行
            if (!showingSkeleton) return@doOnLayout
            val targets = AwSkeletonMaskCollector.collect(contentRoot, maskView, config.maskCornerRadiusPx)
            maskView.setTargets(targets)
            setContentVisibleForSkeleton(false)
        }
    }

    override fun dispose() {
        maskView.stopShimmer()
        (maskView.parent as? ViewGroup)?.removeView(maskView)
        // 恢复内容可见性
        if (showingSkeleton) {
            setContentVisibleForSkeleton(true)
            showingSkeleton = false
            contentHidden = false
        }
    }
}
