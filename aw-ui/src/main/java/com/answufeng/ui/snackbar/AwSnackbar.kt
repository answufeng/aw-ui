package com.answufeng.ui.snackbar

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.answufeng.ui.R
import com.google.android.material.snackbar.Snackbar

/**
 * Snackbar 封装，替代原生 Toast，解决定制系统上 Toast 不显示的问题。
 *
 * 支持 Builder 链式配置、预设样式（success/warning/error/info）、自动查找锚点 View。
 *
 * ### 用法
 * ```kotlin
 * // 最简用法
 * AwSnackbar.success(view, "保存成功")
 *
 * // Builder 用法
 * AwSnackbar.make(view)
 *     .text("自定义消息")
 *     .duration(Snackbar.LENGTH_INDEFINITE)
 *     .action("重试") { retry() }
 *     .backgroundColor(Color.parseColor("#333333"))
 *     .show()
 *
 * // 自动查找锚点 View（需先调用 AwSnackbarManager.install(application)）
 * AwSnackbar.make(context)
 *     .warning()
 *     .text("注意")
 *     .show()
 * ```
 */
class AwSnackbar private constructor(private val builder: Builder) {

    private var snackbar: Snackbar? = null

    fun show() {
        dismiss()
        val view = builder.view
        val customView = builder.customView

        snackbar = Snackbar.make(view, "", builder.duration).apply {
            val snackbarView = this.view
            val textView = snackbarView.findViewById<TextView>(
                com.google.android.material.R.id.snackbar_text
            )

            if (customView != null) {
                // 自定义 View 模式：隐藏默认文本和操作按钮，插入自定义 View
                textView?.visibility = View.GONE
                // 隐藏 action button（遍历 Snackbar 内部布局）
                val contentParent = textView?.parent as? ViewGroup
                if (contentParent != null) {
                    for (i in 0 until contentParent.childCount) {
                        val child = contentParent.getChildAt(i)
                        if (child is com.google.android.material.button.MaterialButton) {
                            child.visibility = View.GONE
                        }
                    }
                    contentParent.addView(customView, 0, ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ))
                }
            } else {
                // 标准模式
                val text = builder.text ?: ""
                this.setText(text)
                builder.actionText?.let { actionText ->
                    setAction(actionText) { builder.actionListener?.invoke() }
                }
                builder.actionColor?.let { setActionTextColor(it) }
                builder.textColor?.let {
                    textView?.setTextColor(it)
                }
                if (builder.maxLines < Int.MAX_VALUE) {
                    textView?.maxLines = builder.maxLines
                }
            }
            builder.backgroundColor?.let { setBackgroundTint(it) }
            builder.anchorView?.let { anchorView = it }
            addCallback(object : Snackbar.Callback() {
                override fun onShown(sb: Snackbar) {
                    builder.onShow?.invoke()
                }

                override fun onDismissed(sb: Snackbar, event: Int) {
                    builder.onDismiss?.invoke()
                }
            })
            show()
        }
    }

    fun dismiss() {
        snackbar?.dismiss()
        snackbar = null
    }

    fun isShowing(): Boolean = snackbar?.isShown == true

    class Builder internal constructor(internal val view: View) {
        internal var text: String? = null
            private set
        internal var duration: Int = Snackbar.LENGTH_SHORT
            private set
        internal var actionText: String? = null
            private set
        internal var actionListener: (() -> Unit)? = null
            private set
        internal var actionColor: Int? = null
            private set
        internal var backgroundColor: Int? = view.context.getColor(R.color.aw_color_snackbar_bg)
            private set
        internal var textColor: Int? = view.context.getColor(R.color.aw_color_snackbar_text)
            private set
        internal var maxLines: Int = Int.MAX_VALUE
            private set
        internal var anchorView: View? = null
            private set
        internal var customView: View? = null
            private set
        internal var onDismiss: (() -> Unit)? = null
            private set
        internal var onShow: (() -> Unit)? = null
            private set

        fun text(text: String): Builder {
            this.text = text
            return this
        }

        fun text(@StringRes resId: Int): Builder {
            this.text = view.context.getString(resId)
            return this
        }

        fun duration(duration: Int): Builder {
            this.duration = duration
            return this
        }

        fun action(text: String, listener: () -> Unit = {}): Builder {
            this.actionText = text
            this.actionListener = listener
            return this
        }

        fun actionColor(@ColorInt color: Int): Builder {
            this.actionColor = color
            return this
        }

        fun backgroundColor(@ColorInt color: Int): Builder {
            this.backgroundColor = color
            return this
        }

        fun textColor(@ColorInt color: Int): Builder {
            this.textColor = color
            return this
        }

        fun maxLines(maxLines: Int): Builder {
            this.maxLines = maxLines
            return this
        }

        fun anchorView(view: View): Builder {
            this.anchorView = view
            return this
        }

        /** 设置自定义 View，替代默认文本和操作按钮。 */
        fun customView(view: View): Builder {
            this.customView = view
            return this
        }

        fun onDismiss(action: () -> Unit): Builder {
            this.onDismiss = action
            return this
        }

        fun onShow(action: () -> Unit): Builder {
            this.onShow = action
            return this
        }

        /** 绿色背景 + 白色文字 */
        fun success(): Builder {
            backgroundColor(view.context.getColor(R.color.aw_color_snackbar_success))
            textColor(view.context.getColor(R.color.aw_color_snackbar_text))
            return this
        }

        /** 橙色背景 + 白色文字 */
        fun warning(): Builder {
            backgroundColor(view.context.getColor(R.color.aw_color_snackbar_warning))
            textColor(view.context.getColor(R.color.aw_color_snackbar_text))
            return this
        }

        /** 红色背景 + 白色文字 */
        fun error(): Builder {
            backgroundColor(view.context.getColor(R.color.aw_color_snackbar_error))
            textColor(view.context.getColor(R.color.aw_color_snackbar_text))
            return this
        }

        /** 蓝色背景 + 白色文字 */
        fun info(): Builder {
            backgroundColor(view.context.getColor(R.color.aw_color_snackbar_info))
            textColor(view.context.getColor(R.color.aw_color_snackbar_text))
            return this
        }

        fun build(): AwSnackbar = AwSnackbar(this)

        fun show(): AwSnackbar {
            val snackbar = build()
            snackbar.show()
            return snackbar
        }
    }

    companion object {

        /** 传入 View 创建 Builder。 */
        fun make(view: View): Builder = Builder(view)

        /**
         * 传入 Context 创建 Builder，自动查找锚点 View。
         *
         * 查找逻辑：
         * 1. 若 Context 是 Activity → 取 content view
         * 2. 否则通过 [AwSnackbarManager] 获取当前栈顶 Activity 的 content view
         * 3. 都获取不到 → 抛出 IllegalArgumentException
         */
        fun make(context: Context): Builder {
            val view = resolveView(context)
            return Builder(view)
        }

        /** 快捷方法：显示成功提示（绿色）。 */
        fun success(view: View, text: String): AwSnackbar {
            return make(view).success().text(text).show()
        }

        fun success(context: Context, text: String): AwSnackbar {
            return make(context).success().text(text).show()
        }

        /** 快捷方法：显示警告提示（橙色）。 */
        fun warning(view: View, text: String): AwSnackbar {
            return make(view).warning().text(text).show()
        }

        fun warning(context: Context, text: String): AwSnackbar {
            return make(context).warning().text(text).show()
        }

        /** 快捷方法：显示错误提示（红色）。 */
        fun error(view: View, text: String): AwSnackbar {
            return make(view).error().text(text).show()
        }

        fun error(context: Context, text: String): AwSnackbar {
            return make(context).error().text(text).show()
        }

        /** 快捷方法：显示信息提示（蓝色）。 */
        fun info(view: View, text: String): AwSnackbar {
            return make(view).info().text(text).show()
        }

        fun info(context: Context, text: String): AwSnackbar {
            return make(context).info().text(text).show()
        }

        private fun resolveView(context: Context): View {
            if (context is Activity) {
                return AwSnackbarManager.getContentView(context)
            }
            val activity = AwSnackbarManager.getCurrentActivity()
            if (activity != null) {
                return AwSnackbarManager.getContentView(activity)
            }
            throw IllegalArgumentException(
                "Cannot find anchor View for Snackbar. " +
                    "Either pass a View directly, or call AwSnackbarManager.install(application) first."
            )
        }
    }
}
