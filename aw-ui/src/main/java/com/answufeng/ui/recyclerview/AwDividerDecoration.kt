package com.answufeng.ui.recyclerview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * RecyclerView 通用分割线装饰器。
 *
 * 仅在相邻 item 之间绘制分割线，最后一个 item 下方不绘制。
 *
 * ```kotlin
 * val r = recyclerView.resources
 * recyclerView.addItemDecoration(
 *     AwDividerDecoration(
 *         height = r.dpToPx(1),
 *         color = Color.LTGRAY,
 *         paddingStart = r.dpToPx(16),
 *         paddingEnd = r.dpToPx(16)
 *     )
 * )
 * ```
 * 其中 `r.dpToPx(n)` 为 `com.answufeng.ui` 包中对 [android.content.res.Resources] 的扩展，**n 为 dp**。
 *
 * @param height       分割线高度（px）
 * @param color        分割线颜色，默认 #E0E0E0
 * @param paddingStart 左侧缩进（px）
 * @param paddingEnd   右侧缩进（px）
 */
class AwDividerDecoration(
    private val height: Int = 1,
    @ColorInt private val color: Int = 0xFFE0E0E0.toInt(),
    private val paddingStart: Int = 0,
    private val paddingEnd: Int = 0
) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@AwDividerDecoration.color
        style = Paint.Style.FILL
    }

    private val tempRect = RectF()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        // NO_POSITION 表示 ViewHolder 正在被移除或无效，跳过
        if (position == RecyclerView.NO_POSITION) return
        val itemCount = parent.adapter?.itemCount ?: 0

        when (val lm = parent.layoutManager) {
            is GridLayoutManager -> {
                val spanCount = lm.spanCount
                val column = position % spanCount
                outRect.left = height * column / spanCount
                outRect.right = height * (spanCount - 1 - column) / spanCount
                val lastRowStart = ((itemCount - 1) / spanCount) * spanCount
                if (position >= lastRowStart) return
                outRect.bottom = height
            }
            is StaggeredGridLayoutManager -> {
                val lp = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                val spanIndex = lp?.spanIndex ?: 0
                val spanCount = lm.spanCount
                // 水平方向：均分间距
                outRect.left = height * spanIndex / spanCount
                outRect.right = height * (spanCount - 1 - spanIndex) / spanCount
                // 垂直方向：除最后一行外添加底部间距
                if (position < itemCount - 1) {
                    outRect.bottom = height
                }
            }
            else -> {
                if (position < itemCount - 1) {
                    outRect.bottom = height
                }
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        when (parent.layoutManager) {
            is GridLayoutManager -> drawGrid(c, parent)
            is StaggeredGridLayoutManager -> drawStaggeredGrid(c, parent)
            else -> drawLinear(c, parent)
        }
    }

    private fun drawLinear(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingStart + paddingStart
        val right = parent.width - parent.paddingEnd - paddingEnd
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            tempRect.set(left.toFloat(), top.toFloat(), right.toFloat(), (top + height).toFloat())
            c.drawRect(tempRect, paint)
        }
    }

    private fun drawGrid(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue
            val params = child.layoutParams as RecyclerView.LayoutParams
            val bottom = child.bottom + params.bottomMargin
            tempRect.set(child.left.toFloat(), bottom.toFloat(), child.right.toFloat(), (bottom + height).toFloat())
            c.drawRect(tempRect, paint)
            val right = child.right + params.rightMargin
            tempRect.set(right.toFloat(), child.top.toFloat(), (right + height).toFloat(), child.bottom.toFloat())
            c.drawRect(tempRect, paint)
        }
    }

    private fun drawStaggeredGrid(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue
            val params = child.layoutParams as RecyclerView.LayoutParams
            val bottom = child.bottom + params.bottomMargin
            tempRect.set(child.left.toFloat(), bottom.toFloat(), child.right.toFloat(), (bottom + height).toFloat())
            c.drawRect(tempRect, paint)
            val right = child.right + params.rightMargin
            tempRect.set(right.toFloat(), child.top.toFloat(), (right + height).toFloat(), child.bottom.toFloat())
            c.drawRect(tempRect, paint)
        }
    }
}
