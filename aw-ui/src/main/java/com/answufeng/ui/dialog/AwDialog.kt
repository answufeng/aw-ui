package com.answufeng.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Material Design 风格的通用对话框快捷构建器。
 *
 * 封装了常见对话框场景，基于 [MaterialAlertDialogBuilder] 实现，
 * 无需手动管理 Builder 链。所有方法均返回 [Dialog] 实例，便于后续控制。
 *
 * ### 确认对话框
 * ```kotlin
 * AwDialog.confirm(context, "提示", "确定删除吗？") {
 *     deleteItem()
 * }
 * ```
 *
 * ### 输入对话框
 * ```kotlin
 * AwDialog.input(context, "备注", hint = "请输入备注") { text ->
 *     saveRemark(text)
 * }
 * ```
 *
 * ### 自定义布局
 * ```kotlin
 * AwDialog.custom(context, "设置", R.layout.dialog_settings) { view ->
 *     val switch = view.findViewById<Switch>(R.id.switchDarkMode)
 *     switch.isChecked = isDarkMode
 * }
 * ```
 */
object AwDialog {

    /**
     * 显示确认对话框（确定 + 取消）。
     *
     * @param context       上下文
     * @param title         标题
     * @param message       消息正文
     * @param positiveText  确认按钮文案，默认 "确定"
     * @param negativeText  取消按钮文案，默认 "取消"
     * @param onCancel      取消按钮回调
     * @param onConfirm     确认按钮回调
     * @return [Dialog] 实例
     */
    fun confirm(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "确定",
        negativeText: String = "取消",
        onCancel: (() -> Unit)? = null,
        onConfirm: () -> Unit
    ): Dialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ -> onConfirm() }
            .setNegativeButton(negativeText) { _, _ -> onCancel?.invoke() }
            .show()
    }

    /**
     * 显示纯提示对话框（仅一个确认按钮）。
     *
     * @param context    上下文
     * @param title      标题
     * @param message    消息正文
     * @param buttonText 按钮文案，默认 "确定"
     * @param onDismiss  按钮点击回调
     * @return [Dialog] 实例
     */
    fun alert(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "确定",
        onDismiss: (() -> Unit)? = null
    ): Dialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText) { _, _ -> onDismiss?.invoke() }
            .show()
    }

    /**
     * 显示带文本输入框的对话框。
     *
     * 使用 [TextInputEditText][com.google.android.material.textfield.TextInputEditText]
     * 作为输入控件，输入内容自动 trim。
     *
     * @param context       上下文
     * @param title         标题
     * @param hint          输入框 hint 提示
     * @param prefill       预填充文本
     * @param positiveText  确认按钮文案
     * @param negativeText  取消按钮文案
     * @param onInput       确认后回调，参数为输入文本（已 trim）
     * @return [Dialog] 实例
     */
    fun input(
        context: Context,
        title: String,
        hint: String = "",
        prefill: String = "",
        positiveText: String = "确定",
        negativeText: String = "取消",
        onInput: (String) -> Unit
    ): Dialog {
        val textInputLayout = com.google.android.material.textfield.TextInputLayout(context).apply {
            val density = resources.displayMetrics.density
            val h = (24 * density).toInt()
            val v = (8 * density).toInt()
            setPadding(h, v, h, v)
        }
        val editText = com.google.android.material.textfield.TextInputEditText(context).apply {
            this.hint = hint
            setText(prefill)
        }
        textInputLayout.addView(editText)
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(textInputLayout)
            .setPositiveButton(positiveText) { _, _ ->
                onInput(editText.text?.toString()?.trim() ?: "")
            }
            .setNegativeButton(negativeText, null)
            .show()
    }

    /**
     * 显示列表选择对话框。
     *
     * @param context  上下文
     * @param title    标题
     * @param items    选项列表
     * @param onSelect 选中回调，参数为选中项索引
     * @return [Dialog] 实例
     */
    fun list(
        context: Context,
        title: String,
        items: List<String>,
        onSelect: (Int) -> Unit
    ): Dialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setItems(items.toTypedArray()) { _, which -> onSelect(which) }
            .show()
    }

    /**
     * 显示底部弹出的列表选择对话框。
     *
     * Dialog 窗口 gravity 设为 [Gravity.BOTTOM]，宽度撑满屏幕。
     *
     * @param context  上下文
     * @param title    可选标题，null 时不显示标题栏
     * @param items    选项列表
     * @param onSelect 选中回调，参数为选中项索引
     * @return [Dialog] 实例
     */
    fun bottomList(
        context: Context,
        title: String? = null,
        items: List<String>,
        onSelect: (Int) -> Unit
    ): Dialog {
        val builder = MaterialAlertDialogBuilder(context)
        title?.let { builder.setTitle(it) }
        builder.setItems(items.toTypedArray()) { _, which -> onSelect(which) }
        val dialog = builder.create()
        dialog.window?.apply {
            setGravity(Gravity.BOTTOM)
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
        return dialog
    }

    /**
     * 显示自定义布局对话框。
     *
     * 通过布局资源 ID inflate 自定义视图，并在 [onBind] 中配置视图内容。
     *
     * @param context       上下文
     * @param title         可选标题，null 时不显示标题栏
     * @param layoutRes     自定义布局资源 ID
     * @param positiveText  确认按钮文案，默认 "确定"
     * @param negativeText  取消按钮文案，默认 "取消"，null 时不显示取消按钮
     * @param onConfirm     确认按钮回调
     * @param onBind        视图配置回调，参数为 inflate 后的根 View
     * @return [Dialog] 实例
     */
    fun custom(
        context: Context,
        title: String? = null,
        @LayoutRes layoutRes: Int,
        positiveText: String = "确定",
        negativeText: String? = "取消",
        onConfirm: (() -> Unit)? = null,
        onBind: ((View) -> Unit)? = null
    ): Dialog {
        val view = LayoutInflater.from(context).inflate(layoutRes, null)
        onBind?.invoke(view)
        val builder = MaterialAlertDialogBuilder(context)
            .setView(view)
        title?.let { builder.setTitle(it) }
        onConfirm?.let { builder.setPositiveButton(positiveText) { _, _ -> it() } }
        negativeText?.let { builder.setNegativeButton(it, null) }
        return builder.show()
    }

    /**
     * 显示自定义 View 实例对话框。
     *
     * @param context       上下文
     * @param title         可选标题
     * @param view          自定义 View 实例
     * @param positiveText  确认按钮文案，默认 "确定"
     * @param negativeText  取消按钮文案，默认 "取消"，null 时不显示取消按钮
     * @param onConfirm     确认按钮回调
     * @return [Dialog] 实例
     */
    fun custom(
        context: Context,
        title: String? = null,
        view: View,
        positiveText: String = "确定",
        negativeText: String? = "取消",
        onConfirm: (() -> Unit)? = null
    ): Dialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setView(view)
        title?.let { builder.setTitle(it) }
        onConfirm?.let { builder.setPositiveButton(positiveText) { _, _ -> it() } }
        negativeText?.let { builder.setNegativeButton(it, null) }
        return builder.show()
    }
}
