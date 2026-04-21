@file:Suppress("unused")

package com.answufeng.ui

import android.content.res.Resources
import kotlin.math.roundToInt

/**
 * 将 dp 值转换为 px（Int），适合用于需要整数像素的场景。
 */
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * 将 dp 值转换为 px（Int），适合用于需要整数像素的场景。
 */
val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * 将 sp 值转换为 px（Int），适合用于文字大小设置。
 */
val Int.spToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

/**
 * 将 sp 值转换为 px（Int），适合用于文字大小设置。
 */
val Float.spToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

/**
 * 将 px 值转换为 dp（Float），适合用于比例计算。
 */
val Int.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * 将 px 值转换为 sp（Float），适合用于文字大小比例计算。
 */
val Int.pxToSp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity
