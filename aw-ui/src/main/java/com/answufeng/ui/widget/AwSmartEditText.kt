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
 * A smart EditText with built-in validation, input filtering, and auto-formatting.
 *
 * Features:
 * - Real-time validation via [addValidator]
 * - Input length limit via [maxLength]
 * - Character-level input filtering via [inputFilter]
 * - Auto-formatting callback via [onFormat] (e.g., phone number formatting)
 * - Error message display below the EditText
 * - [isValid] property and [validate] method for programmatic checks
 *
 * ### XML Usage
 * ```xml
 * <com.answufeng.ui.widget.AwSmartEditText
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:smart_maxLength="20"
 *     app:smart_errorColor="#FF0000" />
 * ```
 *
 * ### Code Usage
 * ```kotlin
 * smartEditText.maxLength = 11
 * smartEditText.inputFilter = { it.isDigit() }
 * smartEditText.onFormat = { raw -> phoneFormat(raw) }
 * smartEditText.addValidator("phone", { it.length == 11 }, "Please enter 11 digits")
 * val valid = smartEditText.validate()
 * ```
 *
 * | XML Attribute | Description | Default |
 * |---|---|---|
 * | `smart_maxLength` | Maximum input length | 0 (no limit) |
 * | `smart_errorColor` | Error message text color | Red |
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

    /**
     * Maximum input length. Set to 0 for no limit.
     * When set, an [InputFilter.LengthFilter] is automatically applied.
     */
    var maxLength: Int = 0
        set(value) {
            field = value
            updateLengthFilter()
        }

    /**
     * Error message text color.
     */
    var errorColor: Int = Color.RED
        set(value) {
            field = value
            errorTextView?.setTextColor(value)
        }

    /**
     * Character-level input filter. Return `true` to allow the character, `false` to reject it.
     * Set to `null` to disable character filtering.
     *
     * Example: `inputFilter = { it.isDigit() }` allows only digits.
     */
    var inputFilter: ((Char) -> Boolean)? = null
        set(value) {
            field = value
            updateCharFilter()
        }

    /**
     * Auto-formatting callback. Invoked on every text change with the raw input.
     * Return the formatted string to replace the current text.
     * Set to `null` to disable auto-formatting.
     *
     * Example: `onFormat = { raw -> raw.chunked(4).joinToString("-") }`
     */
    var onFormat: ((String) -> String)? = null

    /**
     * Whether all validators currently pass.
     * This is updated in real-time as the user types.
     */
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
                    setText(formatted)
                    setSelection(formatted.length.coerceAtMost(selEnd.coerceAtLeast(0)))
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
        ensureErrorView()
    }

    /**
     * Adds a named validator.
     *
     * @param name      A unique name for this validator (used for deduplication).
     * @param predicate Returns `true` if the input is valid.
     * @param errorMsg  The error message to display when validation fails.
     */
    fun addValidator(name: String, predicate: (String) -> Boolean, errorMsg: String) {
        validators.removeAll { it.name == name }
        validators.add(Validator(name, predicate, errorMsg))
    }

    /**
     * Removes a previously added validator by name.
     *
     * @param name The validator name to remove.
     */
    fun removeValidator(name: String) {
        validators.removeAll { it.name == name }
    }

    /**
     * Removes all validators.
     */
    fun clearValidators() {
        validators.clear()
        hideError()
    }

    /**
     * Runs all validators and returns `true` if all pass.
     * Displays the first failing validator's error message.
     *
     * @return `true` if all validators pass, `false` otherwise.
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
                gravity = Gravity.BOTTOM or Gravity.START
                marginStart = paddingStart
                bottomMargin = 0
            }
            tv.layoutParams = lp
            (parent as ViewGroup).addView(tv)
        } else if (parent is ViewGroup) {
            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            )
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
