package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.answufeng.ui.R

/**
 * 智能 EditText，内置验证、输入过滤和自动格式化功能。
 *
 * 功能：
 * - 通过 [addValidator] 实时验证
 * - 通过 [maxLength] 限制输入长度
 * - 通过 [inputFilter] 字符级输入过滤
 * - 通过 [onFormat] 自动格式化回调（如手机号格式化）
 * - EditText 下方显示错误信息
 * - [isValid] 属性和 [validate] 方法用于编程式检查
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.widget.AwSmartEditText
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:smart_maxLength="20"
 *     app:smart_errorColor="#FF0000" />
 * ```
 *
 * ### 代码用法
 * ```kotlin
 * smartEditText.maxLength = 11
 * smartEditText.inputFilter = { it.isDigit() }
 * smartEditText.onFormat = { raw -> phoneFormat(raw) }
 * smartEditText.addValidator("phone", { it.length == 11 }, "Please enter 11 digits")
 * val valid = smartEditText.validate()
 * ```
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `smart_maxLength` | 最大输入长度 | 0（无限制） |
 * | `smart_errorColor` | 错误信息文本颜色 | Red |
 */
class AwSmartEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private data class Validator(
        val name: String,
        val predicate: (String) -> Boolean,
        val errorMsg: String
    )

    private val validators = mutableListOf<Validator>()
    private var errorTextView: TextView? = null

    /** 最大输入长度。设为 0 表示无限制。设置后自动应用 [InputFilter.LengthFilter] */
    var maxLength: Int = 0
        set(value) {
            field = value
            updateLengthFilter()
        }

    /** 错误信息文本颜色 */
    var errorColor: Int = Color.RED
        set(value) {
            field = value
            errorTextView?.setTextColor(value)
        }

    /** 字符级输入过滤器。返回 `true` 允许该字符，`false` 拒绝。设为 `null` 禁用字符过滤。例如：`inputFilter = { it.isDigit() }` 仅允许数字 */
    var inputFilter: ((Char) -> Boolean)? = null
        set(value) {
            field = value
            updateCharFilter()
        }

    /** 自动格式化回调。每次文本变化时以原始输入调用，返回格式化后的字符串替换当前文本。设为 `null` 禁用自动格式化。例如：`onFormat = { raw -> raw.chunked(4).joinToString("-") }` */
    var onFormat: ((String) -> String)? = null

    /** 当前所有验证器是否通过，随用户输入实时更新 */
    val isValid: Boolean
        get() = validators.all { it.predicate(text.toString()) }

    private var isFormatting = false

    private val charInputFilter = InputFilter { source, _, _, _, _, _ ->
        val filter = inputFilter ?: return@InputFilter source
        val filtered = source.filter { filter(it) }
        if (filtered.length == source.length) source else filtered
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isFormatting) return
            val formatter = onFormat
            if (formatter != null) {
                val raw = s.toString()
                val formatted = formatter(raw)
                if (raw != formatted) {
                    isFormatting = true
                    val selEnd = selectionEnd
                    val diff = formatted.length - raw.length
                    setText(formatted)
                    val newSel = (selEnd + diff).coerceIn(0, formatted.length)
                    setSelection(newSel)
                    isFormatting = false
                }
            }
            runValidators()
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSmartEditText)
        maxLength = ta.getInt(R.styleable.AwSmartEditText_smart_maxLength, 0)
        errorColor = ta.getColor(R.styleable.AwSmartEditText_smart_errorColor, Color.RED)
        ta.recycle()

        addTextChangedListener(textWatcher)
        updateCharFilter()
    }

    /**
     * 添加命名验证器。
     *
     * @param name      验证器唯一名称（用于去重）
     * @param predicate 输入有效时返回 `true`
     * @param errorMsg  验证失败时显示的错误信息
     */
    fun addValidator(name: String, predicate: (String) -> Boolean, errorMsg: String) {
        validators.removeAll { it.name == name }
        validators.add(Validator(name, predicate, errorMsg))
    }

    /**
     * 按名称移除之前添加的验证器。
     *
     * @param name 要移除的验证器名称
     */
    fun removeValidator(name: String) {
        validators.removeAll { it.name == name }
    }

    /** 移除所有验证器 */
    fun clearValidators() {
        validators.clear()
        hideError()
    }

    /**
     * 运行所有验证器，全部通过则返回 `true`。
     * 显示第一个失败验证器的错误信息。
     *
     * @return 所有验证器通过返回 `true`，否则返回 `false`
     */
    fun validate(): Boolean {
        val input = text.toString()
        val failed = validators.firstOrNull { !it.predicate(input) }
        if (failed != null) {
            showError(failed.errorMsg)
            return false
        }
        hideError()
        return true
    }

    private fun runValidators() {
        val input = text.toString()
        val failed = validators.firstOrNull { !it.predicate(input) }
        if (failed != null) {
            showError(failed.errorMsg)
        } else {
            hideError()
        }
    }

    private fun showError(msg: String) {
        val tv = ensureErrorView()
        tv.text = msg
        tv.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorTextView?.visibility = View.GONE
    }

    private fun ensureErrorView(): TextView {
        errorTextView?.let { return it }
        val tv = TextView(context).apply {
            setTextColor(errorColor)
            textSize = 12f
            visibility = View.GONE
            val pad = (4 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, 0, 0)
        }
        val parent = parent
        if (parent is FrameLayout) {
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
                marginStart = paddingStart
                topMargin = bottom + paddingBottom
            }
            tv.layoutParams = lp
            (parent as ViewGroup).addView(tv)
        } else if (parent is ViewGroup) {
            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            )
            lp.topMargin = bottom + paddingBottom
            tv.layoutParams = lp
            (parent as ViewGroup).addView(tv)
        }
        errorTextView = tv
        return tv
    }

    private fun updateLengthFilter() {
        val current = filters.filterNot { it is InputFilter.LengthFilter }.toTypedArray()
        filters = if (maxLength > 0) {
            current + InputFilter.LengthFilter(maxLength)
        } else {
            current
        }
    }

    private fun updateCharFilter() {
        val current = filters.filterNot { it === charInputFilter }.toTypedArray()
        filters = if (inputFilter != null) {
            current + charInputFilter
        } else {
            current
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ensureErrorView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        errorTextView = null
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("maxLength", maxLength)
            putBoolean("hasError", errorTextView?.visibility == View.VISIBLE)
            putString("errorMsg", errorTextView?.text?.toString())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState: Parcelable? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                state.getParcelable("superState", Parcelable::class.java)
            } else {
                @Suppress("DEPRECATION")
                state.getParcelable("superState")
            }
            super.onRestoreInstanceState(superState)
            maxLength = state.getInt("maxLength", 0)
            val hasError = state.getBoolean("hasError", false)
            val errorMsg = state.getString("errorMsg")
            if (hasError && errorMsg != null) {
                showError(errorMsg)
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
