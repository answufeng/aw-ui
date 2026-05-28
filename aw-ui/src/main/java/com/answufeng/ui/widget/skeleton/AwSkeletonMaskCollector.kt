package com.answufeng.ui.widget.skeleton

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import com.answufeng.ui.R

internal object AwSkeletonMaskCollector {
    fun collect(
        root: View,
        overlay: View,
        defaultCornerRadiusPx: Float,
    ): List<AwSkeletonMaskTarget> {
        val targets = mutableListOf<AwSkeletonMaskTarget>()
        collectRecursive(root, overlay, defaultCornerRadiusPx, targets)
        return targets
    }

    private fun collectRecursive(
        view: View,
        overlay: View,
        defaultCornerRadiusPx: Float,
        out: MutableList<AwSkeletonMaskTarget>,
    ) {
        if (view === overlay || view is AwSkeletonMaskView) return
        // 仅跳过 GONE；INVISIBLE 仍参与 layout，需收集 bounds 供遮罩绘制
        if (view.visibility == View.GONE) return

        val tagIgnore = view.getTag(R.id.aw_skeleton_ignore) as? Boolean
        if (tagIgnore == true) return

        val tagMask = view.getTag(R.id.aw_skeleton_mask) as? Boolean
        val isLeaf = view !is ViewGroup || !hasVisibleChildren(view)

        if (isLeaf || tagMask == true) {
            if (view.width > 0 && view.height > 0) {
                val rect = RectF()
                if (!mapRectToOverlay(view, overlay, rect)) return
                if (rect.width() > 0f && rect.height() > 0f) {
                    val radius =
                        view.getTag(R.id.aw_skeleton_corner_radius) as? Float
                            ?: defaultCornerRadiusPx
                    out.add(AwSkeletonMaskTarget(rect, radius))
                }
            }
            if (tagMask == true) return
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectRecursive(view.getChildAt(i), overlay, defaultCornerRadiusPx, out)
            }
        }
    }

    private fun hasVisibleChildren(group: ViewGroup): Boolean {
        for (i in 0 until group.childCount) {
            if (group.getChildAt(i).visibility != View.GONE) return true
        }
        return false
    }

    private fun mapRectToOverlay(
        view: View,
        overlay: View,
        out: RectF,
    ): Boolean {
        val loc = IntArray(2)
        val overlayLoc = IntArray(2)
        view.getLocationOnScreen(loc)
        overlay.getLocationOnScreen(overlayLoc)
        val left = (loc[0] - overlayLoc[0]).toFloat()
        val top = (loc[1] - overlayLoc[1]).toFloat()
        out.set(left, top, left + view.width, top + view.height)
        return true
    }
}
