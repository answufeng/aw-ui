package com.answufeng.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.answufeng.ui.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AwDialog(context: Context) : Dialog(context) {

    private var title: String? = null
    private var message: String? = null
    private var positiveText: String = "确定"
    private var negativeText: String? = null
    private var onPositiveClick: (() -> Unit)? = null
    private var onNegativeClick: (() -> Unit)? = null
    private var contentView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    fun setDialogTitle(title: String): AwDialog {
        this.title = title
        return this
    }

    fun setDialogMessage(message: String): AwDialog {
        this.message = message
        return this
    }

    fun setPositiveButton(text: String, onClick: () -> Unit = {}): AwDialog {
        this.positiveText = text
        this.onPositiveClick = onClick
        return this
    }

    fun setNegativeButton(text: String, onClick: () -> Unit = {}): AwDialog {
        this.negativeText = text
        this.onNegativeClick = onClick
        return this
    }

    fun setDialogContentView(view: View): AwDialog {
        this.contentView = view
        return this
    }

    fun showDialog() {
        val builder = MaterialAlertDialogBuilder(context)
        title?.let { builder.setTitle(it) }
        message?.let { builder.setMessage(it) }
        contentView?.let { builder.setView(it) }

        builder.setPositiveButton(positiveText) { _, _ ->
            onPositiveClick?.invoke()
        }
        negativeText?.let {
            builder.setNegativeButton(it) { _, _ ->
                onNegativeClick?.invoke()
            }
        }

        builder.show()
    }

    companion object {
        fun alert(context: Context): AwDialog {
            return AwDialog(context)
        }

        fun showMessage(context: Context, title: String, message: String) {
            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show()
        }

        fun showConfirm(
            context: Context,
            title: String,
            message: String,
            onConfirm: () -> Unit
        ) {
            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定") { _, _ -> onConfirm() }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}

class LoadingDialog(context: Context) : Dialog(context) {

    private var loadingMessage: String = "加载中..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.aw_state_loading)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun setLoadingMessage(message: String): LoadingDialog {
        this.loadingMessage = message
        return this
    }

    fun showLoading(message: String = "加载中...") {
        this.loadingMessage = message
        show()
    }

    companion object {
        fun show(context: Context, message: String = "加载中...") {
            LoadingDialog(context).apply {
                setLoadingMessage(message)
                show()
            }
        }
    }
}