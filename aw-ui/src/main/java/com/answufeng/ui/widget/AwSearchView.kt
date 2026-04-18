package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import com.answufeng.ui.R
import com.google.android.material.color.MaterialColors

class AwSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val searchIcon: ImageView
    private val editText: EditText
    private val clearButton: ImageView

    var hint: String = "搜索"
        set(value) {
            field = value
            editText.hint = value
        }

    var query: String
        get() = editText.text.toString()
        set(value) {
            editText.setText(value)
            clearButton.visibility = if (value.isNotEmpty()) View.VISIBLE else View.GONE
        }

    var onQueryChange: ((String) -> Unit)? = null
    var onQuerySubmit: ((String) -> Unit)? = null
    var onClearClick: (() -> Unit)? = null

    init {
        val bgColor = MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorSurfaceVariant, Color.parseColor("#F5F5F5")
        )
        val iconColor = MaterialColors.getColor(
            context, android.R.attr.textColorSecondary, Color.GRAY
        )

        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (40 * density).toInt())
            setBackgroundColor(bgColor)
        }

        searchIcon = ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                leftMargin = (12 * density).toInt()
            }
            setImageResource(android.R.drawable.ic_menu_search)
            setColorFilter(iconColor)
        }

        editText = EditText(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                leftMargin = (40 * density).toInt()
                rightMargin = (40 * density).toInt()
            }
            hint = this@AwSearchView.hint
            background = null
            setSingleLine()
            textSize = 14f
            setTextColor(MaterialColors.getColor(context, android.R.attr.textColorPrimary, Color.BLACK))
            setHintTextColor(MaterialColors.getColor(context, android.R.attr.textColorSecondary, Color.GRAY))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    onQuerySubmit?.invoke(text.toString())
                    true
                } else false
            }
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    val text = s.toString()
                    clearButton.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                    onQueryChange?.invoke(text)
                }
            })
        }

        clearButton = ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                rightMargin = (12 * density).toInt()
            }
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(iconColor)
            visibility = View.GONE
            setOnClickListener {
                editText.text.clear()
                onClearClick?.invoke()
            }
        }

        container.addView(searchIcon)
        container.addView(editText)
        container.addView(clearButton)
        addView(container)
    }

    fun requestSearchFocus() {
        editText.requestFocus()
    }

    fun clearFocus() {
        editText.clearFocus()
    }
}
