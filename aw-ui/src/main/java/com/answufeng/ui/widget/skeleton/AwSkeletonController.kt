package com.answufeng.ui.widget.skeleton

import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout

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

    override val isShowingSkeleton: Boolean get() = showingSkeleton

    init {
        maskView.applyConfig(config)
        maskView.visibility = View.GONE
        if (maskView.parent == null && host !== contentRoot) {
            val lp = contentRoot.layoutParams
            host.addView(maskView, lp)
        } else if (maskView.parent == null) {
            host.addView(
                maskView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
    }

    override fun showSkeleton() {
        if (showingSkeleton) {
            rebuildMask()
            return
        }
        showingSkeleton = true
        contentRoot.isEnabled = false
        maskView.visibility = View.VISIBLE
        maskView.bringToFront()
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
            val targets = AwSkeletonMaskCollector.collect(contentRoot, maskView, config.maskCornerRadiusPx)
            maskView.setTargets(targets)
            if (showingSkeleton) {
                setContentVisibleForSkeleton(false)
            }
        }
    }

    fun dispose() {
        maskView.stopShimmer()
        (maskView.parent as? ViewGroup)?.removeView(maskView)
    }
}
