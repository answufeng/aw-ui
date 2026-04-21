package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.answufeng.ui.R
import com.google.android.material.color.MaterialColors

/**
 * 标签选择视图，支持单选/多选模式和最大选择数限制。
 *
 * 支持自定义背景色、文字颜色、圆角等属性，
 * 常用于分类筛选、标签展示等场景。
 *
 * ### XML 属性
 * - `tag_mode`: 标签模式（FLOW/GRID）
 *
 * ### 用法
 * ```kotlin
 * tagView.tags = listOf("Kotlin", "Java", "Python", "Go")
 * tagView.selectionMode = AwTagView.SelectionMode.MULTI
 * tagView.maxSelectCount = 3
 * tagView.onSelectionChange = { selected -> updateSelection(selected) }
 * ```
 */
class AwTagView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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

    var tagTextColor: Int = MaterialColors.getColor(context, android.R.attr.textColorPrimary, Color.BLACK)
    var tagSelectedTextColor: Int = Color.WHITE
    var tagBgColor: Int = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, Color.parseColor("#F0F0F0"))
    var tagSelectedBgColor: Int = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.BLUE)
    var tagTextSize: Float = 14f
    var tagPaddingH: Int = (12 * resources.displayMetrics.density).toInt()
    var tagPaddingV: Int = (6 * resources.displayMetrics.density).toInt()
    var tagCornerRadius: Float = 4f * resources.displayMetrics.density

    enum class SelectionMode { NONE, SINGLE, MULTI }

    init {
        flowGravity = Gravity.START
    }

    fun setTagSelected(tag: String, selected: Boolean) {
        if (selected) {
            selectedTags = when (selectionMode) {
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
            textSize = tagTextSize
            setTextColor(if (isSelected) tagSelectedTextColor else tagTextColor)
            setPadding(tagPaddingH, tagPaddingV, tagPaddingH, tagPaddingV)
            background = createTagBackground(isSelected)
            setOnClickListener { handleTagClick(tag) }
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
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
                selectedTags = if (wasSelected) {
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
            child.setTextColor(if (isSelected) tagSelectedTextColor else tagTextColor)
            child.background = createTagBackground(isSelected)
        }
    }
}
