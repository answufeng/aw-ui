@file:Suppress("unused")

package com.answufeng.ui

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import kotlin.math.roundToInt

/**
 * 使用 [Resources] 的显示度量将 dp 转为 px，与当前 [android.content.res.Configuration] 一致。
 * 在 View/Context 有 [Resources] 时优先用此类方法，避免 [Int.dp] 等无接收者扩展。
 */
fun Resources.dpToPx(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics).roundToInt()

/**
 * 使用 [Resources] 将 dp 转为 px（浮点 dp 值），与当前 [android.content.res.Configuration] 一致。
 */
fun Resources.dpToPx(dip: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics).roundToInt()

/**
 * 使用 [Resources] 的 scaledDensity 将 sp 转为 px，与系统字体缩放一致。
 */
fun Resources.spToPx(sp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), displayMetrics).roundToInt()

/**
 * 使用 [Resources] 将 sp 转为 px（浮点 sp 值）。
 */
fun Resources.spToPx(sp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics).roundToInt()

/** [View] 的 [View.getResources] 上 dp 转 px 的简写。 */
fun View.dip2px(dip: Int): Int = resources.dpToPx(dip)

/** [View] 的 [View.getResources] 上 sp 转 px 的简写。 */
fun View.sp2px(sp: Int): Int = resources.spToPx(sp)

/**
 * 将整型 dp 值转换为 px（Int）。
 *
 * 基于 [Resources.getSystem]：可能与当前界面的 [Resources] 在部分场景下（多窗口/折叠屏/字体等）存在偏差。
 * 有 [View] 或 [android.content.Context] 时，优先使用 [Resources.dpToPx]。
 */
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

/**
 * 将浮点型 dp 值转换为 px（Int）。
 * @see Int.dp
 */
val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

/**
 * 将浮点型 dp 值转换为 px（Float），保留小数精度。
 *
 * 适用于需要精确浮点 px 值的场景（如 Paint.strokeWidth、Canvas 绘制坐标等）。
 * 基于 [Resources.getSystem]：可能与当前界面的 [Resources] 在部分场景下存在偏差。
 * 有 [View] 或 [android.content.Context] 时，优先使用 [Resources.dpToPx]。
 */
val Float.dpFloat: Float
    get() = this * Resources.getSystem().displayMetrics.density

/**
 * 将整型 sp 值转换为 px（Int）。
 * @see Int.dp
 */
val Int.spToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

/**
 * 将浮点型 sp 值转换为 px（Int）。
 * @see Int.spToPx
 */
val Float.spToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

/**
 * 将整型 sp 值转换为 px（Int），与 [Int.dp] 命名风格统一。
 * @see Int.dp
 */
val Int.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

/**
 * 将浮点型 sp 值转换为 px（Int），与 [Float.dp] 命名风格统一。
 * @see Float.dp
 */
val Float.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

/**
 * 将 px 值转换为 dp（Float）。
 * @see Int.dp
 */
val Int.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * 将浮点 px 值转换为 dp（Float）。
 * @see Int.pxToDp
 */
val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * 将 px 值转换为 sp（Float）。
 * @see Int.spToPx
 */
val Int.pxToSp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity

/**
 * 将浮点 px 值转换为 sp（Float）。
 * @see Int.pxToSp
 */
val Float.pxToSp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity
