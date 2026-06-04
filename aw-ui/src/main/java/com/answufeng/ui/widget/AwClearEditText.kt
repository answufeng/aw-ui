package com.answufeng.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 带一键清除按钮的输入框。
 *
 * 当输入框有内容时，右侧显示清除（X）按钮，点击按钮清空输入内容。
 * 可在 XML 中通过属性配置清除图标及其大小、颜色。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwClearEditText
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:hint="请输入"
 *     app:clearIconSize="18dp"
 *     app:clearIconTint="#999999" />
 * ```
 */
class AwClearEditText
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatEditText(context, attrs, defStyleAttr) {

        private var clearIcon: Drawable? = null
        private var clearIconTint: Int = ContextCompat.getColor(context, R.color.aw_color_clear_icon)
        private var clearIconSize: Float = 18f.dpFloat
        private var showClearAlways: Boolean = false

        private var customClearIcon: Drawable? = null

        private val clearTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateClearButton()
            }
        }

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwClearEditText)
            val clearDrawableRes = ta.getResourceId(R.styleable.AwClearEditText_clearIcon, 0)
            if (clearDrawableRes != 0) {
                customClearIcon = ContextCompat.getDrawable(context, clearDrawableRes)
            }
            clearIconTint = ta.getColor(R.styleable.AwClearEditText_clearIconTint, clearIconTint)
            clearIconSize = ta.getDimension(R.styleable.AwClearEditText_clearIconSize, clearIconSize)
            showClearAlways = ta.getBoolean(R.styleable.AwClearEditText_showClearAlways, false)
            ta.recycle()

            addTextChangedListener(clearTextWatcher)
            updateClearButton()
        }

        private fun updateClearButton() {
            if (showClearAlways || text?.isNotEmpty() == true) {
                showClearIcon()
            } else {
                setCompoundDrawables(null, null, null, null)
            }
        }

        private fun showClearIcon() {
            val icon = customClearIcon ?: ContextCompat.getDrawable(context, R.drawable.aw_ic_clear)
            if (icon != null) {
                val tinted = icon.mutate()
                DrawableCompat.setTint(tinted, clearIconTint)
                tinted.setBounds(0, 0, clearIconSize.toInt(), clearIconSize.toInt())
                setCompoundDrawables(null, null, tinted, null)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP && compoundDrawables[2] != null) {
                val drawableWidth = compoundDrawables[2].bounds.width()
                val padding = 24f.dpFloat
                val totalTapArea = drawableWidth + padding
                val touchX = event.x
                val isTapOnClear =
                    touchX >= (width - totalTapArea - paddingEnd) && touchX <= (width - paddingEnd)
                if (isTapOnClear) {
                    text?.clear()
                    setText("")
                    performClick()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean {
            return super.performClick()
        }
    }
