package com.answufeng.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.answufeng.ui.R
import com.google.android.material.color.MaterialColors

/**
 * iOS 风格底部弹出操作表对话框。
 *
 * 显示操作项列表，带有标题头部、可选的破坏性（红色）项和取消按钮，
 * 取消按钮与操作列表之间有间隔。对话框具有圆角顶部并从底部滑入。
 *
 * ### 代码用法
 * ```kotlin
 * AwActionSheetDialog(context)
 *     .setTitle("Select Action")
 *     .setItems(listOf("Photo", "Camera", "Delete"))
 *     .setDestructiveIndex(2)
 *     .setOnSelect { index -> handleSelection(index) }
 *     .setOnCancel { /* cancelled */ }
 *     .show()
 * ```
 *
 * @property title 操作表顶部显示的可选标题
 * @property items 要显示的操作标签列表
 * @property destructiveIndex 破坏性（红色）项的索引，默认 -1（无）
 * @property cancelText 取消按钮文本，默认 "取消"
 * @property onSelect 选中操作项时的回调，接收项索引
 * @property onCancel 按下取消按钮时的回调
 */
class AwActionSheetDialog(context: Context) : Dialog(context, R.style.AwActionSheetDialog) {

    /** 操作表顶部显示的可选标题 */
    var title: String? = null

    /** 要显示的操作标签列表 */
    var items: List<String> = emptyList()

    /** 破坏性（红色）项的索引，默认 -1（无） */
    var destructiveIndex: Int = -1

    /** 取消按钮文本，默认 "取消" */
    var cancelText: String = "取消"

    /** 选中操作项时的回调，接收项索引 */
    var onSelect: ((Int) -> Unit)? = null

    /** 按下取消按钮时的回调 */
    var onCancel: (() -> Unit)? = null

    private val density = context.resources.displayMetrics.density

    private val surfaceColor: Int by lazy {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.WHITE)
    }
    private val onSurfaceColor: Int by lazy {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
    }
    private val secondaryColor: Int by lazy {
        MaterialColors.getColor(context, android.R.attr.textColorSecondary, Color.parseColor("#8E8E93"))
    }
    private val primaryColor: Int by lazy {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.parseColor("#007AFF"))
    }
    private val separatorColor: Int by lazy {
        val ta = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        val color = try {
            (ta.getDrawable(0) as? android.graphics.drawable.ColorDrawable)?.color
                ?: Color.parseColor("#C6C6C8")
        } finally {
            ta.recycle()
        }
        color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val container = ScrollView(context).apply {
            isFillViewport = true
            setBackgroundColor(Color.TRANSPARENT)
        }

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            val pad = (12 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val sheetLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createRoundedBackground(12f * density, surfaceColor)
        }

        title?.let {
            sheetLayout.addView(createTitleView(it))
            sheetLayout.addView(createSeparator())
        }

        items.forEachIndexed { index, label ->
            sheetLayout.addView(createItemView(label, index))
            if (index < items.size - 1) {
                sheetLayout.addView(createSeparator())
            }
        }

        rootLayout.addView(sheetLayout)

        val cancelView = createCancelView(cancelText)
        val cancelLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val margin = (8 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
            }
            addView(cancelView)
        }
        rootLayout.addView(cancelLayout)

        container.addView(rootLayout)
        setContentView(container)

        window?.apply {
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent)
            val winParams = attributes
            winParams.width = WindowManager.LayoutParams.MATCH_PARENT
            winParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            winParams.windowAnimations = android.R.style.Animation_InputMethod
            attributes = winParams
        }
    }

    /**
     * 设置操作表标题。
     *
     * @param title 标题文本
     * @return 当前对话框，用于链式调用
     */
    fun setTitle(title: String): AwActionSheetDialog {
        this.title = title
        return this
    }

    /**
     * 设置操作项列表。
     *
     * @param items 操作标签列表
     * @return 当前对话框，用于链式调用
     */
    fun setItems(items: List<String>): AwActionSheetDialog {
        this.items = items
        return this
    }

    /**
     * 设置破坏性项索引（显示为红色）。
     *
     * @param index 破坏性项的从零开始的索引，-1 表示无
     * @return 当前对话框，用于链式调用
     */
    fun setDestructiveIndex(index: Int): AwActionSheetDialog {
        this.destructiveIndex = index
        return this
    }

    /**
     * 设置取消按钮文本。
     *
     * @param text 取消按钮标签
     * @return 当前对话框，用于链式调用
     */
    fun setCancelText(text: String): AwActionSheetDialog {
        this.cancelText = text
        return this
    }

    /**
     * 设置选中回调。
     *
     * @param listener 接收选中项索引的回调
     * @return 当前对话框，用于链式调用
     */
    fun setOnSelect(listener: (Int) -> Unit): AwActionSheetDialog {
        this.onSelect = listener
        return this
    }

    /**
     * 设置取消回调。
     *
     * @param listener 取消时的回调
     * @return 当前对话框，用于链式调用
     */
    fun setOnCancel(listener: () -> Unit): AwActionSheetDialog {
        this.onCancel = listener
        return this
    }

    private fun createTitleView(titleText: String): TextView {
        return TextView(context).apply {
            text = titleText
            setTextColor(secondaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            val padV = (14 * density).toInt()
            val padH = (16 * density).toInt()
            setPadding(padH, padV, padH, padV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun createItemView(label: String, index: Int): TextView {
        val isDestructive = index == destructiveIndex
        return TextView(context).apply {
            text = label
            setTextColor(if (isDestructive) Color.parseColor("#FF3B30") else primaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            gravity = Gravity.CENTER
            val padV = (14 * density).toInt()
            val padH = (16 * density).toInt()
            setPadding(padH, padV, padH, padV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                onSelect?.invoke(index)
                dismiss()
            }
        }
    }

    private fun createCancelView(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(primaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            gravity = Gravity.CENTER
            val padV = (14 * density).toInt()
            val padH = (16 * density).toInt()
            setPadding(padH, padV, padH, padV)
            background = createRoundedBackground(12f * density, surfaceColor)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                onCancel?.invoke()
                dismiss()
            }
        }
    }

    private fun createSeparator(): View {
        return View(context).apply {
            setBackgroundColor(separatorColor)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (0.5 * density).toInt()
            )
        }
    }

    private fun createRoundedBackground(radius: Float, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            this.cornerRadius = radius
            setColor(color)
        }
    }
}
