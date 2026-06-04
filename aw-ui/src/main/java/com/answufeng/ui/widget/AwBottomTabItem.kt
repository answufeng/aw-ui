package com.answufeng.ui.widget

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * [AwBottomTabBar] 单个 Tab 的数据模型。
 */
data class AwBottomTabItem(
    val title: String = "",
    val icon: Drawable? = null,
    @DrawableRes val iconRes: Int = 0,
    @StringRes val titleRes: Int = 0,
)
