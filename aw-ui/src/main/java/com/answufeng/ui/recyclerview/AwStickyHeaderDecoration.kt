package com.answufeng.ui.recyclerview

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 吸顶分组 Header 装饰。
 *
 * ```kotlin
 * recyclerView.addItemDecoration(
 *     AwStickyHeaderDecoration(
 *         isHeader = { pos -> adapter.isHeaderPosition(pos) },
 *         headerLayoutRes = R.layout.item_section_header,
 *     ) { headerView, position ->
 *         headerView.findViewById<TextView>(R.id.tvTitle).text = sections[position]
 *     },
 * )
 * ```
 */
class AwStickyHeaderDecoration(
    private val isHeader: (position: Int) -> Boolean,
    @LayoutRes private val headerLayoutRes: Int,
    private val onBindHeader: (headerView: View, position: Int) -> Unit,
) : RecyclerView.ItemDecoration() {
    private var stickyHeaderView: View? = null

    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.onDrawOver(c, parent, state)
        if (parent.childCount == 0) return

        val topChild = parent.getChildAt(0) ?: return
        val topPosition = parent.getChildAdapterPosition(topChild)
        if (topPosition == RecyclerView.NO_POSITION) return

        val headerPosition = findHeaderPositionForItem(topPosition)
        if (headerPosition == RecyclerView.NO_POSITION) return

        val headerView = getOrCreateHeaderView(parent)
        onBindHeader(headerView, headerPosition)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)
        val childWidthSpec =
            ViewGroup.getChildMeasureSpec(
                widthSpec,
                parent.paddingLeft + parent.paddingRight,
                headerView.layoutParams.width,
            )
        val childHeightSpec =
            ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                headerView.layoutParams.height,
            )
        headerView.measure(childWidthSpec, childHeightSpec)
        headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)

        var contactY = 0
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            if (pos != RecyclerView.NO_POSITION && isHeader(pos) && child.top < headerView.height) {
                contactY = child.top - headerView.height
                break
            }
        }

        c.save()
        c.translate(parent.paddingLeft.toFloat(), contactY.coerceAtLeast(0).toFloat())
        headerView.draw(c)
        c.restore()
    }

    private fun findHeaderPositionForItem(itemPosition: Int): Int {
        var pos = itemPosition
        while (pos >= 0) {
            if (isHeader(pos)) return pos
            pos--
        }
        return RecyclerView.NO_POSITION
    }

    private fun getOrCreateHeaderView(parent: RecyclerView): View {
        stickyHeaderView?.let { return it }
        val view =
            LayoutInflater.from(parent.context).inflate(headerLayoutRes, parent, false)
        stickyHeaderView = view
        return view
    }
}
