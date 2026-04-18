@file:Suppress("unused")

package com.answufeng.ui

import android.content.res.Resources
import kotlin.math.roundToInt

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Int.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

val Float.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

val Int.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Float.dpToPx: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Int.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

val Int.spToPx: Float
    get() = this * Resources.getSystem().displayMetrics.scaledDensity

val Int.pxToSp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity
