package com.answufeng.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.TextView
import com.answufeng.ui.R
import com.answufeng.ui.widget.AwLoadingView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 通用对话框封装，基于 MaterialAlertDialogBuilder。
 *
 * 支持标题、消息、自定义视图、确定/取消按钮、取消和关闭回调。
 *
 * ### 用法
 * ```kotlin
 * AwDialog.Builder(context)
 *     .title("提示")
 *     .message("确定删除吗？")
 *     .positiveButton("确定") { doDelete() }
 *     .negativeButton("取消")
 *     .show()
 *
 * // 快捷方法
 * AwDialog.showMessage(context, "提示", "操作成功")
 * AwDialog.showConfirm(context, "确认", "确定退出吗？") { exit() }
 * ```
 */
class AwDialog private constructor(
    private val builder: Builder,
) {
    private var dialog: Dialog? = null

    fun show() {
        dismiss()
        val dialogBuilder = MaterialAlertDialogBuilder(builder.context)
        builder.title?.let { dialogBuilder.setTitle(it) }
        builder.message?.let { dialogBuilder.setMessage(it) }
        builder.contentView?.let { dialogBuilder.setView(it) }

        dialogBuilder.setPositiveButton(builder.positiveText) { _, _ ->
            builder.onPositiveClick?.invoke()
        }
        builder.negativeText?.let {
            dialogBuilder.setNegativeButton(it) { _, _ ->
                builder.onNegativeClick?.invoke()
            }
        }
        builder.cancelable?.let { dialogBuilder.setCancelable(it) }
        builder.onCancel?.let { dialogBuilder.setOnCancelListener(it) }
        builder.onDismiss?.let { dialogBuilder.setOnDismissListener(it) }

        dialog = dialogBuilder.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true

    class Builder(val context: Context) {
        internal var title: String? = null
            private set
        internal var message: String? = null
            private set
        internal var positiveText: String = "确定"
            private set
        internal var negativeText: String? = null
            private set
        internal var onPositiveClick: (() -> Unit)? = null
            private set
        internal var onNegativeClick: (() -> Unit)? = null
            private set
        internal var contentView: View? = null
            private set
        internal var cancelable: Boolean? = null
            private set
        internal var onCancel: DialogInterface.OnCancelListener? = null
            private set
        internal var onDismiss: DialogInterface.OnDismissListener? = null
            private set

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun message(message: String): Builder {
            this.message = message
            return this
        }

        fun positiveButton(
            text: String = "确定",
            onClick: () -> Unit = {},
        ): Builder {
            this.positiveText = text
            this.onPositiveClick = onClick
            return this
        }

        fun negativeButton(
            text: String = "取消",
            onClick: () -> Unit = {},
        ): Builder {
            this.negativeText = text
            this.onNegativeClick = onClick
            return this
        }

        fun contentView(view: View): Builder {
            this.contentView = view
            return this
        }

        fun cancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun onCancel(listener: DialogInterface.OnCancelListener): Builder {
            this.onCancel = listener
            return this
        }

        fun onDismiss(listener: DialogInterface.OnDismissListener): Builder {
            this.onDismiss = listener
            return this
        }

        fun build(): AwDialog = AwDialog(this)

        fun show(): AwDialog {
            val dialog = build()
            dialog.show()
            return dialog
        }
    }

    companion object {
        fun showMessage(
            context: Context,
            title: String,
            message: String,
        ) {
            Builder(context)
                .title(title)
                .message(message)
                .show()
        }

        fun showConfirm(
            context: Context,
            title: String,
            message: String,
            onConfirm: () -> Unit,
        ) {
            Builder(context)
                .title(title)
                .message(message)
                .positiveButton { onConfirm() }
                .negativeButton()
                .show()
        }
    }
}

/**
 * 加载中对话框，显示菊花旋转动画和提示文字，半透明深色背景。
 *
 * 默认使用 [AwLoadingView] 的 FLOWER 样式，窗口背景半透明遮罩。
 *
 * ```kotlin
 * AwLoadingDialog.show(context, "加载中...")
 * // 或
 * AwLoadingDialog(context).showLoading("请稍候")
 * ```
 */
class AwLoadingDialog(context: Context) : AwBaseDialog(context) {
    override val cancelableOnTouchOutside: Boolean = false

    private var loadingMessage: String = "加载中..."
    private var messageTextView: TextView? = null
    private var loadingView: AwLoadingView? = null

    init {
        customContentView =
            android.view.LayoutInflater.from(context)
                .inflate(R.layout.aw_dialog_loading, null)
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        messageTextView = findViewById(R.id.tvLoadingMessage)
        loadingView = findViewById(R.id.loadingView)
    }

    override fun show() {
        super.show()
        updateMessage()
    }

    fun setLoadingMessage(message: String): AwLoadingDialog {
        this.loadingMessage = message
        updateMessage()
        return this
    }

    fun setStyle(style: AwLoadingView.Style): AwLoadingDialog {
        loadingView?.style = style
        return this
    }

    fun setTintColor(color: Int): AwLoadingDialog {
        loadingView?.setColorTint(color)
        return this
    }

    fun showLoading(message: String = "加载中...") {
        this.loadingMessage = message
        show()
    }

    private fun updateMessage() {
        messageTextView?.text = loadingMessage
    }

    companion object {
        fun show(
            context: Context,
            message: String = "加载中...",
        ): AwLoadingDialog {
            return AwLoadingDialog(context).apply {
                setLoadingMessage(message)
                show()
            }
        }
    }
}
