package com.answufeng.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 下拉选择菜单。
 *
 * 点击后弹出下拉列表，支持单选，选中后回调通知。
 * 可在 XML 中通过属性配置样式。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwDropDownMenu
 *     android:layout_width="match_parent"
 *     android:layout_height="48dp"
 *     app:ddm_hint="请选择"
 *     app:ddm_cornerRadius="4dp" />
 * ```
 */
class AwDropDownMenu
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {

        var items: List<String> = emptyList()
            set(v) {
                field = v
                updateDisplayText()
            }

        var selectedIndex: Int = -1
            set(v) {
                field = v
                updateDisplayText()
            }

        var hint: String = ""
            set(v) {
                field = v
                updateDisplayText()
            }

        var textColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_text)
        var hintTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_dropdown_hint)
        var textSize: Float = 14f.dpFloat
        var bgColor: Int = ContextCompat.getColor(context, R.color.aw_color_dropdown_bg)
        var cornerRadius: Float = 4f.dpFloat
        var borderColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_border)
        var borderWidth: Float = 1f.dpFloat
        var dropdownIcon: Drawable? = null
        var iconTint: Int = ContextCompat.getColor(context, R.color.aw_color_dropdown_icon)
        var dropdownHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
        var itemTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_stepper_text)
        var itemTextSize: Float = 14f.dpFloat
        var itemBgColor: Int = ContextCompat.getColor(context, R.color.aw_color_dropdown_bg)
        var itemSelectedColor: Int = ContextCompat.getColor(context, R.color.aw_color_dropdown_item_selected)
        var itemHeight: Float = 44f.dpFloat

        var onItemSelected: ((index: Int, text: String) -> Unit)? = null

        private val displayText: TextView

        private var popupWindow: PopupWindow? = null

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12f.dpFloat.toInt(), 0, 12f.dpFloat.toInt(), 0)
            setBackgroundColor(bgColor)
            setClickable(true)

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwDropDownMenu)
            hint = ta.getString(R.styleable.AwDropDownMenu_ddm_hint) ?: ""
            textColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_textColor, textColor)
            hintTextColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_hintTextColor, hintTextColor)
            textSize = ta.getDimension(R.styleable.AwDropDownMenu_ddm_textSize, textSize)
            bgColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_bgColor, bgColor)
            cornerRadius = ta.getDimension(R.styleable.AwDropDownMenu_ddm_cornerRadius, cornerRadius)
            borderColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_borderColor, borderColor)
            borderWidth = ta.getDimension(R.styleable.AwDropDownMenu_ddm_borderWidth, borderWidth)
            val iconRes = ta.getResourceId(R.styleable.AwDropDownMenu_ddm_dropdownIcon, 0)
            dropdownHeight = ta.getDimensionPixelSize(R.styleable.AwDropDownMenu_ddm_dropdownHeight, dropdownHeight)
            itemTextColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_itemTextColor, itemTextColor)
            itemTextSize = ta.getDimension(R.styleable.AwDropDownMenu_ddm_itemTextSize, itemTextSize)
            itemBgColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_itemBgColor, itemBgColor)
            itemSelectedColor = ta.getColor(R.styleable.AwDropDownMenu_ddm_itemSelectedColor, itemSelectedColor)
            itemHeight = ta.getDimension(R.styleable.AwDropDownMenu_ddm_itemHeight, itemHeight)
            ta.recycle()

            // 文本
            displayText = TextView(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER_VERTICAL
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize)
                isSingleLine = true
            }
            addView(displayText)
            updateDisplayText()

            // 下拉箭头
            val icon = if (iconRes != 0) {
                ContextCompat.getDrawable(context, iconRes)
            } else {
                ContextCompat.getDrawable(context, R.drawable.aw_ic_arrow_down)
            }
            dropdownIcon = icon?.mutate()?.also { DrawableCompat.setTint(it, iconTint) }
            if (dropdownIcon != null) {
                val iconView = TextView(context).apply {
                    layoutParams = LayoutParams(
                        (24f.dpFloat).toInt(),
                        (24f.dpFloat).toInt()
                    )
                    gravity = Gravity.CENTER
                    setCompoundDrawablesWithIntrinsicBounds(null, null, dropdownIcon, null)
                }
                addView(iconView)
            }

            setOnClickListener { showDropDown() }
        }

        private fun updateDisplayText() {
            if (selectedIndex >= 0 && selectedIndex < items.size) {
                displayText.text = items[selectedIndex]
                displayText.setTextColor(textColor)
            } else {
                displayText.text = hint.ifEmpty { context.getString(R.string.aw_dropdown_hint) }
                displayText.setTextColor(hintTextColor)
            }
        }

        private fun showDropDown() {
            if (items.isEmpty()) return
            dismissDropDown()

            val listView = LinearLayout(context).apply {
                orientation = VERTICAL
                setBackgroundColor(itemBgColor)
                elevation = 4f.dpFloat
            }

            items.forEachIndexed { index, item ->
                val row = TextView(context).apply {
                    text = item
                    setTextColor(itemTextColor)
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, itemTextSize)
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(16f.dpFloat.toInt(), 0, 16f.dpFloat.toInt(), 0)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        itemHeight.toInt()
                    )
                    if (index == selectedIndex) {
                        setBackgroundColor(itemSelectedColor)
                    }
                    setOnClickListener {
                        selectedIndex = index
                        onItemSelected?.invoke(index, item)
                        dismissDropDown()
                    }
                }
                listView.addView(row)

                if (index < items.size - 1) {
                    val divider = View(context).apply {
                        setBackgroundColor(borderColor)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, (0.5f.dpFloat).toInt()
                        )
                    }
                    listView.addView(divider)
                }
            }

            popupWindow = PopupWindow(
                listView,
                width,
                if (dropdownHeight > 0) dropdownHeight else ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                isOutsideTouchable = true
                isFocusable = true
                elevation = 4f.dpFloat
                showAsDropDown(this@AwDropDownMenu, 0, 0)
            }
        }

        fun dismissDropDown() {
            popupWindow?.dismiss()
            popupWindow = null
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            dismissDropDown()
        }
    }

