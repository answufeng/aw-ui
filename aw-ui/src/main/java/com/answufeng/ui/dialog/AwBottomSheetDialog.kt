package com.answufeng.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.answufeng.ui.R as AwR
import com.answufeng.ui.dpFloat

/**
 * 底部弹出面板（BottomSheet风格）。
 *
 * 从底部滑入，支持自定义内容视图、拖拽手柄、半透明遮罩层。
 * 基于 Dialog 实现，兼容低版本设备。
 *
 * 代码用法：
 * ```kotlin
 * AwBottomSheetDialog(context)
 *     .setContentView(layoutInflater.inflate(R.layout.my_sheet, null))
 *     .setTitle("标题")
 *     .show()
 *
 * // 或链式配置
 * AwBottomSheetDialog(context).apply {
 *     cornerRadius = 16f.dpFloat
 *     dimAmount = 0.5f
 * }.setContentView(myView).show()
 * ```
 */
class AwBottomSheetDialog(context: Context) : android.app.Dialog(context, AwR.style.AwBaseDialog) {

    var cornerRadius: Float = 12f.dpFloat
    var backgroundColor: Int = ContextCompat.getColor(context, AwR.color.aw_color_bottom_sheet_bg)
    var maxHeightRatio: Float = 0.85f  // 屏幕高度的最大占比
    var dragHandleVisible: Boolean = true
    var dragHandleColor: Int = ContextCompat.getColor(context, AwR.color.aw_color_bottom_sheet_drag)
    var dimAmountValue: Float = 0.5f
    var onDismissListener: (() -> Unit)? = null

    private var contentView: View? = null
    private var sheetTitle: String? = null
    private var sheetLayout: LinearLayout? = null

    private val density = context.resources.displayMetrics.density

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val container = ScrollView(context).apply {
            isFillViewport = true
            setBackgroundColor(Color.TRANSPARENT)
        }

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
        }

        val sheetLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createRoundedTopBg(cornerRadius, backgroundColor)
        }
        this.sheetLayout = sheetLayout

        // 拖拽手柄
        if (dragHandleVisible) {
            val handleContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val handle = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (36 * density).toInt(),
                    (4 * density).toInt()
                ).apply {
                    topMargin = (10 * density).toInt()
                    bottomMargin = (6 * density).toInt()
                    gravity = Gravity.CENTER
                }
                background = createRoundedBg((2 * density), dragHandleColor)
            }
            handleContainer.addView(handle)
            sheetLayout.addView(handleContainer)
        }

        // 标题
        sheetTitle?.let {
            val titleView = TextView(context).apply {
                text = it
                setTextColor(ContextCompat.getColor(context, AwR.color.aw_color_stepper_text))
                textSize = 16f
                gravity = Gravity.CENTER
                val padV = (12 * density).toInt()
                val padH = (16 * density).toInt()
                setPadding(padH, padV, padH, padV)
            }
            sheetLayout.addView(titleView)
        }

        // 内容视图
        contentView?.let {
            sheetLayout.addView(it)
        }

        rootLayout.addView(sheetLayout)
        container.addView(rootLayout)
        setContentView(container)
    }

    override fun show() {
        super.show()
        window?.setDimAmount(dimAmountValue)
        // 限制 sheetLayout 最大高度（包含手柄+标题+内容的整体高度）
        sheetLayout?.let {
            val screenHeight = context.resources.displayMetrics.heightPixels
            val maxH = (screenHeight * maxHeightRatio).toInt()
            // 延迟到布局完成后设置，确保已测量
            it.post {
                val currentHeight = it.height
                if (currentHeight > maxH) {
                    it.layoutParams = it.layoutParams?.apply { height = maxH }
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener?.invoke()
    }

    fun setDialogContentView(view: View): AwBottomSheetDialog {
        this.contentView = view
        return this
    }

    fun setTitle(title: String): AwBottomSheetDialog {
        this.sheetTitle = title
        return this
    }

    private fun createRoundedTopBg(radius: Float, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            setColor(color)
        }
    }

    private fun createRoundedBg(radius: Float, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = radius
            setColor(color)
        }
    }

    companion object {
        fun build(context: Context): Builder = Builder(context)
    }

    class Builder(val context: Context) {
        private val dialog = AwBottomSheetDialog(context)

        fun setDialogContentView(view: View): Builder {
            dialog.contentView = view
            return this
        }

        fun setTitle(title: String): Builder {
            dialog.sheetTitle = title
            return this
        }

        fun setCornerRadius(radius: Float): Builder {
            dialog.cornerRadius = radius
            return this
        }

        fun setDimAmount(amount: Float): Builder {
            dialog.dimAmountValue = amount
            return this
        }

        fun setDragHandleVisible(visible: Boolean): Builder {
            dialog.dragHandleVisible = visible
            return this
        }

        fun setOnDismissListener(listener: () -> Unit): Builder {
            dialog.onDismissListener = listener
            return this
        }

        fun show(): AwBottomSheetDialog {
            dialog.show()
            return dialog
        }
    }
}

