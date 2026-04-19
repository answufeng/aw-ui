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

/**
 * 搜索栏视图，包含搜索图标、输入框和清除按钮。
 *
 * 支持实时搜索回调、提交搜索回调和清除按钮回调。
 *
 * ```xml
 * <com.answufeng.ui.widget.AwSearchView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:search_hint="搜索..." />
 * ```
 *
 * ```kotlin
 * searchView.onQueryChange = { query -> filterList(query) }
 * searchView.onQuerySubmit = { query -> doSearch(query) }
 * ```
 */
class AwSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val clearButton: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            rightMargin = (12 * density).toInt()
        }
        setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        visibility = View.GONE
    }

    private val searchIcon: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            leftMargin = (12 * density).toInt()
        }
        setImageResource(android.R.drawable.ic_menu_search)
    }

    private val editText: EditText = EditText(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            leftMargin = (40 * density).toInt()
            rightMargin = (40 * density).toInt()
        }
        background = null
        setSingleLine()
        textSize = 14f
    }

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

        searchIcon.setColorFilter(iconColor)
        clearButton.setColorFilter(iconColor)

        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (40 * density).toInt())
            setBackgroundColor(bgColor)
        }

        editText.hint = hint
        editText.setTextColor(MaterialColors.getColor(context, android.R.attr.textColorPrimary, Color.BLACK))
        editText.setHintTextColor(MaterialColors.getColor(context, android.R.attr.textColorSecondary, Color.GRAY))
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                onQuerySubmit?.invoke(editText.text.toString())
                true
            } else false
        }
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s.toString()
                clearButton.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                onQueryChange?.invoke(text)
            }
        })

        clearButton.setOnClickListener {
            editText.text.clear()
            onClearClick?.invoke()
        }

        container.addView(searchIcon)
        container.addView(editText)
        container.addView(clearButton)
        addView(container)
    }

    fun requestSearchFocus() {
        editText.requestFocus()
    }

    override fun clearFocus() {
        super.clearFocus()
        editText.clearFocus()
    }
}
