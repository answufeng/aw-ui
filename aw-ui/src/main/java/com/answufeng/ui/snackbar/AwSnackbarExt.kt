package com.answufeng.ui.snackbar

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/** 显示普通 Snackbar 提示。 */
fun Context.showAwSnackbar(
    text: String,
    duration: Int = Snackbar.LENGTH_SHORT,
): AwSnackbar {
    return AwSnackbar.make(this).text(text).duration(duration).show()
}

/** 显示带操作按钮的 Snackbar。 */
fun Context.showAwSnackbar(
    text: String,
    actionText: String,
    action: () -> Unit,
): AwSnackbar {
    return AwSnackbar.make(this).text(text).action(actionText, action).show()
}

/** 显示成功提示（绿色）。 */
fun Context.showAwSuccess(text: String): AwSnackbar {
    return AwSnackbar.success(this, text)
}

/** 显示警告提示（橙色）。 */
fun Context.showAwWarning(text: String): AwSnackbar {
    return AwSnackbar.warning(this, text)
}

/** 显示错误提示（红色）。 */
fun Context.showAwError(text: String): AwSnackbar {
    return AwSnackbar.error(this, text)
}

/** 显示信息提示（蓝色）。 */
fun Context.showAwInfo(text: String): AwSnackbar {
    return AwSnackbar.info(this, text)
}

// ──────────────────────────────────────────────────────────
// View 扩展
// ──────────────────────────────────────────────────────────

/** 显示普通 Snackbar 提示。 */
fun View.showAwSnackbar(
    text: String,
    duration: Int = Snackbar.LENGTH_SHORT,
): AwSnackbar {
    return AwSnackbar.make(this).text(text).duration(duration).show()
}

/** 显示带操作按钮的 Snackbar。 */
fun View.showAwSnackbar(
    text: String,
    actionText: String,
    action: () -> Unit,
): AwSnackbar {
    return AwSnackbar.make(this).text(text).action(actionText, action).show()
}

/** 显示成功提示（绿色）。 */
fun View.showAwSuccess(text: String): AwSnackbar {
    return AwSnackbar.success(this, text)
}

/** 显示警告提示（橙色）。 */
fun View.showAwWarning(text: String): AwSnackbar {
    return AwSnackbar.warning(this, text)
}

/** 显示错误提示（红色）。 */
fun View.showAwError(text: String): AwSnackbar {
    return AwSnackbar.error(this, text)
}

/** 显示信息提示（蓝色）。 */
fun View.showAwInfo(text: String): AwSnackbar {
    return AwSnackbar.info(this, text)
}

// ──────────────────────────────────────────────────────────
// Fragment 扩展（安全获取 Context）
// ──────────────────────────────────────────────────────────

/** 显示普通 Snackbar 提示。Context 为空时返回 null。 */
fun Fragment.showAwSnackbar(
    text: String,
    duration: Int = Snackbar.LENGTH_SHORT,
): AwSnackbar? {
    val ctx = context ?: return null
    return ctx.showAwSnackbar(text, duration)
}

/** 显示成功提示（绿色）。Context 为空时返回 null。 */
fun Fragment.showAwSuccess(text: String): AwSnackbar? {
    val ctx = context ?: return null
    return ctx.showAwSuccess(text)
}

/** 显示警告提示（橙色）。Context 为空时返回 null。 */
fun Fragment.showAwWarning(text: String): AwSnackbar? {
    val ctx = context ?: return null
    return ctx.showAwWarning(text)
}

/** 显示错误提示（红色）。Context 为空时返回 null。 */
fun Fragment.showAwError(text: String): AwSnackbar? {
    val ctx = context ?: return null
    return ctx.showAwError(text)
}

/** 显示信息提示（蓝色）。Context 为空时返回 null。 */
fun Fragment.showAwInfo(text: String): AwSnackbar? {
    val ctx = context ?: return null
    return ctx.showAwInfo(text)
}
