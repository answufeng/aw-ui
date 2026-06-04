package com.answufeng.ui.dialog

import android.content.Context
import androidx.fragment.app.Fragment

/** 显示仅含确定按钮的提示对话框。 */
fun Context.showAwMessage(
    title: String,
    message: String,
): AwDialog {
    return AwDialog.showMessage(this, title, message)
}

/** 显示确认对话框，点击确定后执行 [onConfirm]。 */
fun Context.showAwConfirm(
    title: String,
    message: String,
    onConfirm: () -> Unit,
): AwDialog {
    return AwDialog.showConfirm(this, title, message, onConfirm)
}

/** [Fragment] 便捷封装，安全获取 Context。 */
fun Fragment.showAwMessage(
    title: String,
    message: String,
): AwDialog? {
    val ctx = context ?: return null
    return ctx.showAwMessage(title, message)
}

/** [Fragment] 便捷封装，安全获取 Context。 */
fun Fragment.showAwConfirm(
    title: String,
    message: String,
    onConfirm: () -> Unit,
): AwDialog? {
    val ctx = context ?: return null
    return ctx.showAwConfirm(title, message, onConfirm)
}
