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
import android.widget.TextView
import com.answufeng.ui.R

/**
 * iOS-style segmented control.
 *
 * Displays a horizontal row of segments with a sliding highlight that animates
 * smoothly between selections using [View.animate].
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwSegmentedControl
 *     android:layout_width="wrap_content"
 *     android:layout_height="40dp"
 *     app:seg_items="@array/my_segments"
 *     app:seg_selectedIndex="0"
 *     app:seg_selectedColor="#FFFFFFFF"
 *     app:seg_textColor="#99000000"
 *     app:seg_selectedTextColor="#FF000000" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * val control = AwSegmentedControl(context)
 * control.items = listOf("Tab 1", "Tab 2", "Tab 3")
 * control.onSelectionChange = { index -> ... }
 * ```
 *
 * | XML attribute | Description | Default |
 * |---|---|---|
 * | `seg_items` | String array reference for segment labels | empty |
 * | `seg_selectedIndex` | Initially selected index | 0 |
 * | `seg_selectedColor` | Highlight / thumb background color | white |
 * | `seg_textColor` | Unselected segment text color | gray |
 * | `seg_selectedTextColor` | Selected segment text color | black |
 */
class AwSegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val container = android.widget.LinearLayout(context).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private val highlightView = View(context)

    private val segmentViews = mutableListOf<TextView>()

    /** Segment labels. */
    var items: List<String> = emptyList()
        set(value) {
            field = value
            rebuildSegments()
        }

    /** Currently selected segment index. */
    var selectedIndex: Int = 0
        set(value) {
            val clamped = value.coerceIn(0, (items.size - 1).coerceAtLeast(0))
            if (field != clamped) {
                field = clamped
                updateHighlight(animated = true)
                updateTextColors()
            }
        }

    /** Callback invoked when the selected segment changes. */
    var onSelectionChange: ((Int) -> Unit)? = null

    /** Background color of the sliding highlight. */
    var selectedColor: Int = Color.WHITE
        set(value) {
            field = value
            (highlightView.background as? GradientDrawable)?.setColor(value)
        }

    /** Text color for unselected segments. */
    var textColor: Int = Color.parseColor("#99000000")
        set(value) {
            field = value
            updateTextColors()
        }

    /** Text color for the selected segment. */
    var selectedTextColor: Int = Color.parseColor("#FF000000")
        set(value) {
            field = value
            updateTextColors()
        }

    private var highlightDrawable: GradientDrawable = GradientDrawable()

    private var containerDrawable: GradientDrawable = GradientDrawable()

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSegmentedControl)
        val itemsResId = ta.getResourceId(R.styleable.AwSegmentedControl_seg_items, 0)
        val itemsFromXml = if (itemsResId != 0) resources.getStringArray(itemsResId).toList() else emptyList()
        selectedIndex = ta.getInt(R.styleable.AwSegmentedControl_seg_selectedIndex, 0)
        selectedColor = ta.getColor(R.styleable.AwSegmentedControl_seg_selectedColor, Color.WHITE)
        textColor = ta.getColor(R.styleable.AwSegmentedControl_seg_textColor, Color.parseColor("#99000000"))
        selectedTextColor = ta.getColor(R.styleable.AwSegmentedControl_seg_selectedTextColor, Color.parseColor("#FF000000"))
        ta.recycle()

        highlightDrawable.setColor(selectedColor)
        highlightDrawable.cornerRadius = 0f
        highlightView.background = highlightDrawable

        containerDrawable.setColor(Color.parseColor("#1A000000"))
        containerDrawable.cornerRadius = 0f
        container.background = containerDrawable

        container.addView(highlightView, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT))
        addView(container, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        clipChildren = false
        clipToPadding = false

        if (itemsFromXml.isNotEmpty()) {
            items = itemsFromXml
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cornerRadius = h / 2f
        highlightDrawable.cornerRadius = cornerRadius
        containerDrawable.cornerRadius = cornerRadius
        updateHighlight(animated = false)
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("selectedIndex", selectedIndex)
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
            selectedIndex = state.getInt("selectedIndex", 0)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun rebuildSegments() {
        segmentViews.clear()
        container.removeAllViews()

        val lp = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        for ((index, label) in items.withIndex()) {
            val tv = TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                setTextColor(if (index == selectedIndex) selectedTextColor else textColor)
                setOnClickListener {
                    if (selectedIndex != index) {
                        selectedIndex = index
                        onSelectionChange?.invoke(index)
                    }
                }
            }
            segmentViews.add(tv)
            container.addView(tv, lp)
        }

        val highlightLp = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        container.addView(highlightView, highlightLp)

        updateTextColors()
        post { updateHighlight(animated = false) }
    }

    private fun updateHighlight(animated: Boolean) {
        if (items.isEmpty() || width == 0) return

        val segmentWidth = width.toFloat() / items.size
        val targetX = selectedIndex * segmentWidth

        val lp = highlightView.layoutParams as android.widget.LinearLayout.LayoutParams
        lp.width = segmentWidth.toInt()
        lp.weight = 0f
        highlightView.layoutParams = lp

        if (animated) {
            highlightView.animate()
                .translationX(targetX)
                .setDuration(250)
                .start()
        } else {
            highlightView.translationX = targetX
        }

        highlightView.bringToFront()
    }

    private fun updateTextColors() {
        for ((index, tv) in segmentViews.withIndex()) {
            tv.setTextColor(if (index == selectedIndex) selectedTextColor else textColor)
        }
    }
}
