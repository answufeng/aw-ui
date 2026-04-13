package com.answufeng.ui

import android.content.Context
import android.util.TypedValue

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
        android.content.res.Resources.getSystem().displayMetrics
    ).toInt()

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this,
        android.content.res.Resources.getSystem().displayMetrics
    )

val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this.toFloat(),
        android.content.res.Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this,
        android.content.res.Resources.getSystem().displayMetrics
    )

fun Context.dp(value: Int): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
).toInt()

fun Context.sp(value: Int): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, value.toFloat(), resources.displayMetrics
).toInt()
