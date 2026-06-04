package com.answufeng.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 带密码可见切换按钮的输入框。
 *
 * 在输入框右侧显示眼睛图标，点击切换密码可见/隐藏。
 * 可在 XML 中通过属性配置可见/隐藏图标、大小、颜色。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwPasswordEditText
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:hint="请输入密码"
 *     app:pwdToggleSize="20dp" />
 * ```
 */
class AwPasswordEditText
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatEditText(context, attrs, defStyleAttr) {

        private var isPasswordVisible: Boolean = false

        private var toggleIconOn: Drawable? = null
        private var toggleIconOff: Drawable? = null
        private var toggleTint: Int = ContextCompat.getColor(context, R.color.aw_color_pwd_toggle)
        private var toggleSize: Float = 20f.dpFloat

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwPasswordEditText)
            val onRes = ta.getResourceId(R.styleable.AwPasswordEditText_pwdToggleIconOn, 0)
            if (onRes != 0) {
                toggleIconOn = ContextCompat.getDrawable(context, onRes)
            } else {
                toggleIconOn = ContextCompat.getDrawable(context, R.drawable.aw_ic_visibility_on)
            }
            val offRes = ta.getResourceId(R.styleable.AwPasswordEditText_pwdToggleIconOff, 0)
            if (offRes != 0) {
                toggleIconOff = ContextCompat.getDrawable(context, offRes)
            } else {
                toggleIconOff = ContextCompat.getDrawable(context, R.drawable.aw_ic_visibility_off)
            }
            toggleTint = ta.getColor(R.styleable.AwPasswordEditText_pwdToggleTint, toggleTint)
            toggleSize = ta.getDimension(R.styleable.AwPasswordEditText_pwdToggleSize, toggleSize)
            ta.recycle()

            // 默认密码模式
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            updateToggleIcon()
        }

        private fun updateToggleIcon() {
            val icon = if (isPasswordVisible) toggleIconOn else toggleIconOff
            if (icon != null) {
                val tinted = icon.mutate()
                DrawableCompat.setTint(tinted, toggleTint)
                tinted.setBounds(0, 0, toggleSize.toInt(), toggleSize.toInt())
                setCompoundDrawables(null, null, tinted, null)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP && compoundDrawables[2] != null) {
                val drawableWidth = compoundDrawables[2].bounds.width()
                val padding = 24f.dpFloat
                val totalTapArea = drawableWidth + padding
                val touchX = event.x
                val isTapOnToggle =
                    touchX >= (width - totalTapArea - paddingEnd) && touchX <= (width - paddingEnd)
                if (isTapOnToggle) {
                    isPasswordVisible = !isPasswordVisible
                    // 切换输入类型
                    val cursorPosition = selectionStart
                    inputType = if (isPasswordVisible) {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    setSelection(cursorPosition.coerceIn(0, text?.length ?: 0))
                    updateToggleIcon()
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

