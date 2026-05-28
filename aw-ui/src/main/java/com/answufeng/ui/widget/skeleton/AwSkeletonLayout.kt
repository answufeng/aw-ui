package com.answufeng.ui.widget.skeleton

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.answufeng.ui.R

/**
 * 骨架屏容器：包裹真实 content layout，按子 View bounds 自动遮罩。
 *
 * ```xml
 * <com.answufeng.ui.widget.skeleton.AwSkeletonLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:skeleton_autoShow="true">
 *     <include layout="@layout/item_feed" />
 * </com.answufeng.ui.widget.skeleton.AwSkeletonLayout>
 * ```
 *
 * ```kotlin
 * skeletonLayout.showSkeleton()
 * // 数据就绪
 * skeletonLayout.showContent()
 * ```
 */
class AwSkeletonLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr),
        AwSkeleton {
        private var contentRoot: ViewGroup? = null
        private var maskView: AwSkeletonMaskView? = null
        private var showingSkeleton = false
        private var autoShow = false

        override var config: AwSkeletonConfig = AwSkeletonConfig.default(context)
            set(value) {
                field = value
                maskView?.applyConfig(value)
            }

        override val isShowingSkeleton: Boolean get() = showingSkeleton

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSkeletonLayout)
            val density = resources.displayMetrics.density
            val maskColor =
                ta.getColor(
                    R.styleable.AwSkeletonLayout_skeleton_maskColor,
                    ContextCompat.getColor(context, R.color.aw_color_skeleton_base),
                )
            val shimmerColor =
                ta.getColor(
                    R.styleable.AwSkeletonLayout_skeleton_shimmerColor,
                    ContextCompat.getColor(context, R.color.aw_color_skeleton_highlight),
                )
            val cornerRadius =
                ta.getDimension(
                    R.styleable.AwSkeletonLayout_skeleton_maskCornerRadius,
                    4f * density,
                )
            val duration = ta.getInteger(R.styleable.AwSkeletonLayout_skeleton_shimmerDuration, 1500).toLong()
            val showShimmer = ta.getBoolean(R.styleable.AwSkeletonLayout_skeleton_showShimmer, true)
            autoShow = ta.getBoolean(R.styleable.AwSkeletonLayout_skeleton_autoShow, false)
            ta.recycle()
            config =
                AwSkeletonConfig(
                    maskColor = maskColor,
                    shimmerColor = shimmerColor,
                    maskCornerRadiusPx = cornerRadius,
                    shimmerDurationMs = duration,
                    showShimmer = showShimmer,
                )
        }

        override fun onFinishInflate() {
            super.onFinishInflate()
            if (childCount > 0) {
                contentRoot = getChildAt(0) as? ViewGroup ?: wrapSingleChild(getChildAt(0))
            }
            ensureMaskView()
        }

        private fun wrapSingleChild(child: View): ViewGroup {
            val wrapper =
                FrameLayout(context).apply {
                    layoutParams =
                        LayoutParams(
                            child.layoutParams?.width ?: LayoutParams.MATCH_PARENT,
                            child.layoutParams?.height ?: LayoutParams.WRAP_CONTENT,
                        )
                    addView(child)
                }
            removeView(child)
            addView(wrapper, 0)
            return wrapper
        }

        private fun ensureMaskView() {
            if (maskView != null) return
            maskView =
                AwSkeletonMaskView(context).apply {
                    applyConfig(config)
                    visibility = GONE
                    isClickable = true
                }
            addView(
                maskView,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
            )
        }

        override fun showSkeleton() {
            if (showingSkeleton) {
                rebuildMask()
                return
            }
            showingSkeleton = true
            contentRoot?.isEnabled = false
            maskView?.visibility = VISIBLE
            maskView?.bringToFront()
            rebuildMask()
            contentRoot?.visibility = INVISIBLE
            maskView?.startShimmer()
        }

        override fun showContent(animate: Boolean) {
            if (!showingSkeleton) return
            showingSkeleton = false
            maskView?.stopShimmer()
            maskView?.visibility = GONE
            contentRoot?.visibility = VISIBLE
            contentRoot?.isEnabled = true
            if (animate) {
                contentRoot?.let { fadeInContent(it, 200L) }
            } else {
                contentRoot?.alpha = 1f
            }
        }

        private fun rebuildMask() {
            val content = contentRoot ?: return
            val mask = maskView ?: return
            content.post {
                // layout 完成后先收集 bounds（此时 content 仍可见），再隐藏 content
                val targets = AwSkeletonMaskCollector.collect(content, mask, config.maskCornerRadiusPx)
                mask.setTargets(targets)
                if (showingSkeleton) {
                    content.visibility = INVISIBLE
                }
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (autoShow) showSkeleton()
        }

        /** 获取内容根 View，用于数据绑定 */
        fun getContentRoot(): View? = contentRoot

        /** 对内容根执行绑定 */
        fun bindContent(block: (View) -> Unit) {
            contentRoot?.let(block)
        }
    }
