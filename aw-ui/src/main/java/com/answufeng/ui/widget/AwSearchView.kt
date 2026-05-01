package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.answufeng.ui.R

class AwSearchView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val density = resources.displayMetrics.density

        private val container: FrameLayout

        private val searchIcon: ImageView =
            ImageView(context).apply {
                layoutParams =
                    LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        leftMargin = (12 * density).toInt()
                    }
            }

        private val editText: AppCompatEditText =
            AppCompatEditText(context).apply {
                layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                        leftMargin = (40 * density).toInt()
                        rightMargin = (40 * density).toInt()
                    }
                background = null
                setSingleLine()
                textSize = 14f
            }

        private val clearButton: ImageView =
            ImageView(context).apply {
                layoutParams =
                    LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        rightMargin = (12 * density).toInt()
                    }
                visibility = View.GONE
            }

        var hint: String = context.getString(R.string.aw_search_hint)
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

        var searchIconRes: Int = 0
            set(value) {
                field = value
                if (value != 0) searchIcon.setImageResource(value)
            }

        var clearIconRes: Int = 0
            set(value) {
                field = value
                if (value != 0) clearButton.setImageResource(value)
            }

        var searchBackgroundColor: Int = 0xFFF5F5F5.toInt()
            set(value) {
                field = value
                container.setBackgroundColor(value)
            }

        var searchIconColor: Int = Color.GRAY
            set(value) {
                field = value
                searchIcon.setColorFilter(value)
                clearButton.setColorFilter(value)
            }

        var searchTextColor: Int = Color.BLACK
            set(value) {
                field = value
                editText.setTextColor(value)
            }

        var searchHintColor: Int = Color.GRAY
            set(value) {
                field = value
                editText.setHintTextColor(value)
            }

        var searchCornerRadius: Float = 20f * density
            set(value) {
                field = value
                updateContainerShape()
            }

        var searchHeight: Int = (40 * density).toInt()
            set(value) {
                field = value
                container.layoutParams = container.layoutParams.apply { height = value }
                container.requestLayout()
            }

        var onQueryChange: ((String) -> Unit)? = null
        var onQuerySubmit: ((String) -> Unit)? = null
        var onClearClick: (() -> Unit)? = null
        var onSearchFocusChange: ((Boolean) -> Unit)? = null

        init {
            val defaultBgColor = ContextCompat.getColor(context, R.color.aw_color_search_bg)
            val defaultIconColor = Color.GRAY
            val defaultTextColor = Color.BLACK
            val defaultHintColor = Color.GRAY

            var bgColor = defaultBgColor
            var iconColor = defaultIconColor
            var textColor = defaultTextColor
            var hintColor = defaultHintColor
            var hintStr = context.getString(R.string.aw_search_hint)
            var searchIconDrawableRes = 0
            var clearIconDrawableRes = 0
            var cornerRadiusDimen = 0f
            var heightDimen = 0f

            if (attrs != null) {
                val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSearchView, defStyleAttr, 0)
                try {
                    bgColor = ta.getColor(R.styleable.AwSearchView_search_bgColor, defaultBgColor)
                    iconColor = ta.getColor(R.styleable.AwSearchView_search_iconColor, defaultIconColor)
                    textColor = ta.getColor(R.styleable.AwSearchView_search_textColor, defaultTextColor)
                    hintColor = ta.getColor(R.styleable.AwSearchView_search_hintTextColor, defaultHintColor)
                    val tempHint = ta.getString(R.styleable.AwSearchView_search_hint)
                    if (tempHint != null) hintStr = tempHint
                    searchIconDrawableRes = ta.getResourceId(R.styleable.AwSearchView_search_searchIcon, 0)
                    clearIconDrawableRes = ta.getResourceId(R.styleable.AwSearchView_search_clearIcon, 0)
                    cornerRadiusDimen = ta.getDimension(R.styleable.AwSearchView_search_cornerRadius, 0f)
                    heightDimen = ta.getDimension(R.styleable.AwSearchView_search_height, 0f)
                } catch (_: Exception) {
                } finally {
                    ta.recycle()
                }
            }

            container =
                FrameLayout(context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (40 * density).toInt())
                    background =
                        GradientDrawable().apply {
                            setColor(bgColor)
                            cornerRadius = searchCornerRadius
                        }
                }

            searchBackgroundColor = bgColor
            searchIconColor = iconColor
            searchTextColor = textColor
            searchHintColor = hintColor
            hint = hintStr

            if (searchIconDrawableRes != 0) {
                searchIconRes = searchIconDrawableRes
            } else {
                searchIcon.setImageResource(android.R.drawable.ic_menu_search)
            }

            if (clearIconDrawableRes != 0) {
                clearIconRes = clearIconDrawableRes
            } else {
                clearButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            }

            if (cornerRadiusDimen > 0f) {
                searchCornerRadius = cornerRadiusDimen
            }

            if (heightDimen > 0f) {
                searchHeight = heightDimen.toInt()
            }

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    onQuerySubmit?.invoke(query)
                    true
                } else {
                    false
                }
            }
            editText.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {}

                    override fun afterTextChanged(s: android.text.Editable?) {
                        val text = s.toString()
                        clearButton.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                        onQueryChange?.invoke(text)
                    }
                },
            )
            editText.setOnFocusChangeListener { _, hasFocus ->
                onSearchFocusChange?.invoke(hasFocus)
            }

            clearButton.setOnClickListener {
                editText.text?.clear()
                onClearClick?.invoke()
            }

            container.addView(searchIcon)
            container.addView(editText)
            container.addView(clearButton)
            addView(container)

            isSaveEnabled = true
        }

        fun requestSearchFocus() {
            editText.requestFocus()
        }

        override fun clearFocus() {
            super.clearFocus()
            editText.clearFocus()
        }

        private fun updateContainerShape() {
            (container.background as? GradientDrawable)?.cornerRadius = searchCornerRadius
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putString("query", query)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                val superState: Parcelable? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        state.getParcelable("superState", Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        state.getParcelable("superState")
                    }
                super.onRestoreInstanceState(superState)
                val savedQuery = state.getString("query") ?: ""
                if (savedQuery.isNotEmpty()) {
                    query = savedQuery
                }
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }
