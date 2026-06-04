package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/**
 * 粘性头部容器：将指定的头部 View 固定在顶部，内容区域在头部下方滚动。
 *
 * 第一个子 View 作为粘性头部（始终固定在顶部），第二个子 View 作为可滚动内容。
 * 内容区域的高度会自动减去头部高度，确保内容不会被头部遮挡。
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.widget.AwStickyHeaderLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent">
 *
 *     <!-- 粘性头部（第一个子 View） -->
 *     <TextView
 *         android:layout_width="match_parent"
 *         android:layout_height="48dp"
 *         android:text="Sticky Header" />
 *
 *     <!-- 可滚动内容（第二个子 View） -->
 *     <androidx.core.widget.NestedScrollView
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent">
 *         <!-- 内容 -->
 *     </androidx.core.widget.NestedScrollView>
 *
 * </com.answufeng.ui.widget.AwStickyHeaderLayout>
 * ```
 */
class AwStickyHeaderLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {

        private var stickyHeader: View? = null

        /** 头部粘住/取消粘住时的回调 */
        var onHeaderStickListener: ((stuck: Boolean) -> Unit)? = null

        init {
            orientation = VERTICAL
            setChildrenDrawingOrderEnabled(true)
        }

        /**
         * 手动设置粘性头部 View。
         * 默认使用第一个子 View 作为头部。
         */
        fun setStickyHeader(view: View) {
            stickyHeader = view
        }

        private fun getStickyHeader(): View? {
            if (stickyHeader != null) return stickyHeader
            if (childCount >= 1) return getChildAt(0)
            return null
        }

        private fun getScrollContent(): View? {
            if (childCount >= 2) return getChildAt(1)
            return null
        }

        override fun measureChildWithMargins(
            child: View,
            parentWidthMeasureSpec: Int,
            widthUsed: Int,
            parentHeightMeasureSpec: Int,
            heightUsed: Int,
        ) {
            val header = getStickyHeader()
            if (child !== header && header != null) {
                // 内容区域需要减去头部高度
                val headerHeight = header.measuredHeight
                super.measureChildWithMargins(
                    child, parentWidthMeasureSpec, widthUsed,
                    parentHeightMeasureSpec, heightUsed + headerHeight,
                )
            } else {
                super.measureChildWithMargins(
                    child, parentWidthMeasureSpec, widthUsed,
                    parentHeightMeasureSpec, heightUsed,
                )
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val header = getStickyHeader()
            val content = getScrollContent()

            if (header != null && content != null) {
                val headerHeight = header.measuredHeight

                // 头部始终固定在顶部
                header.layout(
                    paddingLeft, paddingTop,
                    paddingLeft + header.measuredWidth, paddingTop + headerHeight
                )

                // 内容从头部下方开始
                val contentTop = paddingTop + headerHeight
                content.layout(
                    paddingLeft, contentTop,
                    paddingLeft + content.measuredWidth, contentTop + content.measuredHeight
                )

                onHeaderStickListener?.invoke(true)
            } else if (header != null) {
                header.layout(
                    paddingLeft, paddingTop,
                    paddingLeft + header.measuredWidth, paddingTop + header.measuredHeight
                )

                onHeaderStickListener?.invoke(true)
            } else {
                super.onLayout(changed, l, t, r, b)
            }
        }

        override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
            val header = getStickyHeader() ?: return i
            val headerIndex = indexOfChild(header)
            // 让 header 最后绘制（显示在最上层）
            return if (i == childCount - 1) {
                headerIndex
            } else if (i < headerIndex) {
                i
            } else {
                i + 1
            }
        }

        override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
            val header = getStickyHeader()

            // 头部始终绘制在最上层
            if (child === header) {
                return super.drawChild(canvas, child, drawingTime)
            }

            // 内容区域被头部遮挡的部分不绘制（优化性能）
            if (header != null && child === getScrollContent()) {
                val headerBottom = header.measuredHeight.coerceAtLeast(0)
                if (headerBottom > 0) {
                    canvas.save()
                    canvas.clipRect(0, headerBottom, width, height)
                    val result = super.drawChild(canvas, child, drawingTime)
                    canvas.restore()
                    return result
                }
            }

            return super.drawChild(canvas, child, drawingTime)
        }
    }
