package com.answufeng.ui.widget

import androidx.annotation.DrawableRes

/**
 * 分段项：可仅文字、仅图标，或**图标+文字**（与微信底栏类似：**上图标、下文字**，由 [AwSegmentedControl] 竖向排列绘制）。
 */
data class SegmentTab(
    val label: String = "",
    @DrawableRes val iconRes: Int = 0,
) {
    val hasIcon: Boolean get() = iconRes != 0
    val hasLabel: Boolean get() = label.isNotEmpty()
}
