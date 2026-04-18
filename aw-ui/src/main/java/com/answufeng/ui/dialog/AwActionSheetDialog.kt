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
 * iOS-style action sheet dialog displayed from the bottom of the screen.
 *
 * Shows a list of action items with a title header, optional destructive (red) item,
 * and a cancel button separated by a gap. The dialog has rounded top corners and
 * slides up from the bottom.
 *
 * ### Programmatic usage
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
 * @property title Optional title displayed at the top of the action sheet.
 * @property items List of action labels to display.
 * @property destructiveIndex Index of the destructive (red) item. Default -1 (none).
 * @property cancelText Label for the cancel button. Default "取消".
 * @property onSelect Callback invoked when an action item is selected. Receives the item index.
 * @property onCancel Callback invoked when the cancel button is pressed.
 */
class AwActionSheetDialog(context: Context) : Dialog(context, R.style.AwActionSheetDialog) {

    /**
     * Optional title displayed at the top of the action sheet.
     */
    var title: String? = null

    /**
     * List of action labels to display.
     */
    var items: List<String> = emptyList()

    /**
     * Index of the destructive (red) item. Default -1 (none).
     */
    var destructiveIndex: Int = -1

    /**
     * Label for the cancel button. Default "取消".
     */
    var cancelText: String = "取消"

    /**
     * Callback invoked when an action item is selected. Receives the item index.
     */
    var onSelect: ((Int) -> Unit)? = null

    /**
     * Callback invoked when the cancel button is pressed.
     */
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
     * Sets the title for the action sheet.
     *
     * @param title The title text.
     * @return This dialog for chaining.
     */
    fun setTitle(title: String): AwActionSheetDialog {
        this.title = title
        return this
    }

    /**
     * Sets the action items.
     *
     * @param items List of action labels.
     * @return This dialog for chaining.
     */
    fun setItems(items: List<String>): AwActionSheetDialog {
        this.items = items
        return this
    }

    /**
     * Sets the destructive item index (displayed in red).
     *
     * @param index Zero-based index of the destructive item. -1 for none.
     * @return This dialog for chaining.
     */
    fun setDestructiveIndex(index: Int): AwActionSheetDialog {
        this.destructiveIndex = index
        return this
    }

    /**
     * Sets the cancel button text.
     *
     * @param text Cancel button label.
     * @return This dialog for chaining.
     */
    fun setCancelText(text: String): AwActionSheetDialog {
        this.cancelText = text
        return this
    }

    /**
     * Sets the selection callback.
     *
     * @param listener Callback receiving the selected item index.
     * @return This dialog for chaining.
     */
    fun setOnSelect(listener: (Int) -> Unit): AwActionSheetDialog {
        this.onSelect = listener
        return this
    }

    /**
     * Sets the cancel callback.
     *
     * @param listener Callback invoked on cancel.
     * @return This dialog for chaining.
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
