package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import com.answufeng.ui.R

class AwTagView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AwFlowLayout(context, attrs, defStyleAttr) {
        var tags: List<String> = emptyList()
            set(value) {
                field = value
                rebuildTags()
            }

        var selectedTags: Set<String> = emptySet()
            private set

        var selectionMode: SelectionMode = SelectionMode.SINGLE
            set(value) {
                field = value
                if (value == SelectionMode.NONE) {
                    selectedTags = emptySet()
                }
                rebuildTags()
            }

        var maxSelectCount: Int = Int.MAX_VALUE

        var onTagClick: ((String, Boolean) -> Unit)? = null
        var onSelectionChange: ((Set<String>) -> Unit)? = null

        var tagTextColor: Int = Color.BLACK
        var tagSelectedTextColor: Int = Color.WHITE
        var tagBgColor: Int = 0xFFF0F0F0.toInt()
        var tagSelectedBgColor: Int = Color.BLUE
        var tagTextSize: Float = 14f
        var tagPaddingH: Int = (12 * resources.displayMetrics.density).toInt()
        var tagPaddingV: Int = (6 * resources.displayMetrics.density).toInt()
        var tagCornerRadius: Float = 4f * resources.displayMetrics.density

        enum class SelectionMode { NONE, SINGLE, MULTI }

        init {
            flowGravity = Gravity.START

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwTagView)
            tagTextColor = ta.getColor(R.styleable.AwTagView_tag_textColor, tagTextColor)
            tagSelectedTextColor = ta.getColor(R.styleable.AwTagView_tag_selectedTextColor, tagSelectedTextColor)
            tagBgColor = ta.getColor(R.styleable.AwTagView_tag_bgColor, tagBgColor)
            tagSelectedBgColor = ta.getColor(R.styleable.AwTagView_tag_selectedBgColor, tagSelectedBgColor)
            val scaledDensity = resources.displayMetrics.scaledDensity
            tagTextSize =
                ta.getDimension(R.styleable.AwTagView_tag_textSize, tagTextSize * scaledDensity) /
                scaledDensity
            tagPaddingH = ta.getDimensionPixelSize(R.styleable.AwTagView_tag_paddingH, tagPaddingH)
            tagPaddingV = ta.getDimensionPixelSize(R.styleable.AwTagView_tag_paddingV, tagPaddingV)
            tagCornerRadius = ta.getDimension(R.styleable.AwTagView_tag_cornerRadius, tagCornerRadius)
            selectionMode =
                when (ta.getInt(R.styleable.AwTagView_tag_selectionMode, 1)) {
                    0 -> SelectionMode.NONE
                    2 -> SelectionMode.MULTI
                    else -> SelectionMode.SINGLE
                }
            maxSelectCount = ta.getInt(R.styleable.AwTagView_tag_maxSelectCount, Int.MAX_VALUE)
            ta.recycle()

            isSaveEnabled = true
        }

        fun setTagSelected(
            tag: String,
            selected: Boolean,
        ) {
            if (selected) {
                selectedTags =
                    when (selectionMode) {
                        SelectionMode.SINGLE -> setOf(tag)
                        SelectionMode.MULTI -> selectedTags + tag
                        SelectionMode.NONE -> return
                    }
            } else {
                selectedTags = selectedTags - tag
            }
            updateTagStyles()
            onSelectionChange?.invoke(selectedTags)
        }

        fun clearSelection() {
            selectedTags = emptySet()
            updateTagStyles()
            onSelectionChange?.invoke(selectedTags)
        }

        private fun rebuildTags() {
            removeAllViews()
            for (tag in tags) {
                val textView = createTagView(tag)
                addView(textView)
            }
        }

        private fun createTagView(tag: String): TextView {
            val isSelected = selectedTags.contains(tag)
            return TextView(context).apply {
                text = tag
                setTextSize(TypedValue.COMPLEX_UNIT_SP, tagTextSize)
                setTextColor(if (isSelected) tagSelectedTextColor else tagTextColor)
                setPadding(tagPaddingH, tagPaddingV, tagPaddingH, tagPaddingV)
                background = createTagBackground(isSelected)
                setOnClickListener { handleTagClick(tag) }
                layoutParams =
                    MarginLayoutParams(
                        MarginLayoutParams.WRAP_CONTENT,
                        MarginLayoutParams.WRAP_CONTENT,
                    )
            }
        }

        private fun createTagBackground(isSelected: Boolean): android.graphics.drawable.GradientDrawable {
            return android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = tagCornerRadius
                setColor(if (isSelected) tagSelectedBgColor else tagBgColor)
            }
        }

        private fun handleTagClick(tag: String) {
            val wasSelected = selectedTags.contains(tag)
            when (selectionMode) {
                SelectionMode.NONE -> {
                    onTagClick?.invoke(tag, false)
                }
                SelectionMode.SINGLE -> {
                    if (wasSelected) {
                        selectedTags = emptySet()
                    } else {
                        selectedTags = setOf(tag)
                    }
                }
                SelectionMode.MULTI -> {
                    selectedTags =
                        if (wasSelected) {
                            selectedTags - tag
                        } else {
                            if (selectedTags.size >= maxSelectCount) {
                                onTagClick?.invoke(tag, false)
                                onSelectionChange?.invoke(selectedTags)
                                return
                            }
                            selectedTags + tag
                        }
                }
            }
            updateTagStyles()
            onTagClick?.invoke(tag, selectedTags.contains(tag))
            onSelectionChange?.invoke(selectedTags)
        }

        private fun updateTagStyles() {
            for (i in 0 until childCount) {
                val child = getChildAt(i) as? TextView ?: continue
                val tag = child.text.toString()
                val isSelected = selectedTags.contains(tag)
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, tagTextSize)
                child.setTextColor(if (isSelected) tagSelectedTextColor else tagTextColor)
                child.background = createTagBackground(isSelected)
            }
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putStringArrayList("selectedTags", ArrayList(selectedTags))
                putString("selectionMode", selectionMode.name)
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
                val savedTags = state.getStringArrayList("selectedTags") ?: emptyList()
                selectedTags = savedTags.toSet()
                updateTagStyles()
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }
