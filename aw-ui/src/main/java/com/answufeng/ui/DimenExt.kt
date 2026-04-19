@file:Suppress("unused")

package com.answufeng.ui

import android.content.res.Resources
import kotlin.math.roundToInt

/** dp 转 px（Int） */
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

/** dp 转 px（Int） */
val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

/** sp 转 px（Int） */
val Int.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

/** sp 转 px（Int） */
val Float.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

/** dp 转 px（Float） */
val Int.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

/** dp 转 px（Float） */
val Float.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

/** px 转 dp */
val Int.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

/** sp 转 px（Float） */
val Int.spToPx: Float
    get() = this * Resources.getSystem().displayMetrics.scaledDensity

/** px 转 sp */
val Int.pxToSp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity
