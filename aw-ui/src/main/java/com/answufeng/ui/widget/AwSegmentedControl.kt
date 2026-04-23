package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R

/**
 * 类 **微信** 底栏的分段/Tab 控件：底轨为 [FrameLayout]，滑块/下划线在**下方一层**，标题行在上且**背景透明**，
 * 使高亮可见。支持仅文字、仅图标、**上图标+下字**、
 * [SelectionAppearance]（**胶囊** [PILL]、**圆角/直角长方形** [RECT]、下划线、仅文字色）、[AccessoryIndicator]、以及与 [ViewPager2] 联动。
 */
class AwSegmentedControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** 主滑块/下划线高亮；[SelectionAppearance.TEXT_TINT] 时隐藏。 */
    private val track = FrameLayout(context)

    private val segmentRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        isClickable = false
    }

    private val highlightView = View(context)

    /** 底部小圆点，与 [AccessoryIndicator.DOT] 配合。 */
    private val accessoryDot = View(context)

    /** 每格一项： [TextView] 纯文字、 [ImageView] 纯图标、或竖向 [LinearLayout]（上图标+下字）。 */
    private val segmentCells = mutableListOf<View>()

    private var boundViewPager: ViewPager2? = null
    private var viewPagerPageCallback: ViewPager2.OnPageChangeCallback? = null
    private var ignoreViewPagerCallback = false

    private var highlightDrawable: GradientDrawable = GradientDrawable()
    private var containerDrawable: GradientDrawable = GradientDrawable()
    private var dotDrawable: GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
    }

    private var underlineHeightPx: Int = 0
    private var dotSizePx: Int = 0
    private var iconSizePx: Int = 0
    /** [SelectionAppearance.RECT] 时选中块圆角（px，浮点与 [GradientDrawable] 一致） */
    private var rectCornerRadiusPx: Float = 0f
    private var internalIndex: Int = 0

    /** 分段项（图标在字左侧，由 compound drawable 绘制）。 */
    var tabs: List<SegmentTab> = emptyList()
        set(value) {
            field = value
            internalIndex = if (value.isNotEmpty()) {
                internalIndex.coerceIn(0, value.size - 1)
            } else {
                0
            }
            rebuildSegments()
        }

    var items: List<String>
        get() = tabs.map { it.label }
        set(value) {
            tabs = value.map { SegmentTab(label = it) }
        }

    var selectedIndex: Int
        get() = internalIndex
        set(value) = setSelectedIndex(value, animated = true, fromViewPager = false, notify = true)

    var onSelectionChange: ((Int) -> Unit)? = null

    var selectedColor: Int = Color.WHITE
        set(value) {
            field = value
            (highlightView.background as? GradientDrawable)?.setColor(value)
        }

    var textColor: Int = Color.parseColor("#99000000")
        set(value) {
            field = value
            updateTextColors()
        }

    var selectedTextColor: Int = Color.parseColor("#FF000000")
        set(value) {
            field = value
            dotDrawable.setColor(value)
            updateTextColors()
        }

    var selectionAppearance: SelectionAppearance = SelectionAppearance.PILL
        set(value) {
            if (field == value) return
            field = value
            applyHighlightAndAccessoryLayout()
            post { updateHighlight(animated = false) }
        }

    var accessoryIndicator: AccessoryIndicator = AccessoryIndicator.NONE
        set(value) {
            if (field == value) return
            field = value
            applyHighlightAndAccessoryLayout()
            post { updateHighlight(animated = false) }
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSegmentedControl)
        val itemsResId = ta.getResourceId(R.styleable.AwSegmentedControl_seg_items, 0)
        val itemsFromXml = if (itemsResId != 0) resources.getStringArray(itemsResId).toList() else emptyList()
        internalIndex = ta.getInt(R.styleable.AwSegmentedControl_seg_selectedIndex, 0)
        selectedColor = ta.getColor(R.styleable.AwSegmentedControl_seg_selectedColor, Color.WHITE)
        textColor = ta.getColor(R.styleable.AwSegmentedControl_seg_textColor, Color.parseColor("#99000000"))
        selectedTextColor = ta.getColor(
            R.styleable.AwSegmentedControl_seg_selectedTextColor,
            Color.parseColor("#FF000000")
        )
        val appFromXml = ta.getInt(R.styleable.AwSegmentedControl_seg_selectionAppearance, 0)
        val accFromXml = ta.getInt(R.styleable.AwSegmentedControl_seg_accessoryIndicator, 0)
        if (ta.hasValue(R.styleable.AwSegmentedControl_seg_iconSize)) {
            iconSizePx = ta.getDimensionPixelSize(R.styleable.AwSegmentedControl_seg_iconSize, 0)
        }
        if (ta.hasValue(R.styleable.AwSegmentedControl_seg_rectCornerRadius)) {
            rectCornerRadiusPx = ta.getDimension(R.styleable.AwSegmentedControl_seg_rectCornerRadius, 0f)
        } else {
            rectCornerRadiusPx = 4f * resources.displayMetrics.density
        }
        ta.recycle()

        if (iconSizePx <= 0) {
            iconSizePx = (18f * resources.displayMetrics.density).toInt()
        }
        underlineHeightPx = (3f * resources.displayMetrics.density).toInt()
        dotSizePx = (5f * resources.displayMetrics.density).toInt()

        highlightDrawable.setColor(selectedColor)
        highlightDrawable.cornerRadius = 0f
        highlightView.background = highlightDrawable

        // 整轨灰底在 track 上；segmentRow 必须透明，否则盖死下层胶囊/下划线
        containerDrawable.setColor(Color.parseColor("#1A000000"))
        containerDrawable.cornerRadius = 0f
        track.background = containerDrawable
        segmentRow.setBackgroundColor(Color.TRANSPARENT)

        dotDrawable.setColor(selectedTextColor)
        accessoryDot.background = dotDrawable

        track.addView(
            highlightView,
            FrameLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        track.addView(
            accessoryDot,
            FrameLayout.LayoutParams(dotSizePx, dotSizePx).apply {
                gravity = Gravity.BOTTOM or Gravity.START
            }
        )
        track.addView(
            segmentRow,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        addView(track, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        clipChildren = false
        clipToPadding = false

        // 须在 addView 之后：若在 inflation 中过早设 selectionAppearance，会触发 apply 而此时 layoutParams 尚未建立。
        selectionAppearance = SelectionAppearance.values().getOrElse(appFromXml) { SelectionAppearance.PILL }
        accessoryIndicator = AccessoryIndicator.values().getOrElse(accFromXml) { AccessoryIndicator.NONE }
        applyHighlightAndAccessoryLayout()

        if (itemsFromXml.isNotEmpty()) {
            items = itemsFromXml
        }
    }

    /**
     * 与 [viewPager] 的页码双向同步；控件销毁或从窗口分离时会 [unbindViewPager2]（也可手动解绑）。
     * 请保证 **页数** 与 [tabs] 数量一致。
     */
    fun bindViewPager2(viewPager: ViewPager2) {
        unbindViewPager2()
        boundViewPager = viewPager
        val cb = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (ignoreViewPagerCallback) return
                if (position == internalIndex) return
                setSelectedIndex(position, animated = true, fromViewPager = true, notify = true)
            }
        }
        viewPager.registerOnPageChangeCallback(cb)
        viewPagerPageCallback = cb
        if (tabs.isNotEmpty()) {
            val p = viewPager.currentItem.coerceIn(0, tabs.size - 1)
            if (p != internalIndex) {
                setSelectedIndex(p, animated = false, fromViewPager = true, notify = false)
            }
        }
    }

    fun unbindViewPager2() {
        viewPagerPageCallback?.let { c ->
            boundViewPager?.unregisterOnPageChangeCallback(c)
        }
        viewPagerPageCallback = null
        boundViewPager = null
    }

    override fun onDetachedFromWindow() {
        unbindViewPager2()
        super.onDetachedFromWindow()
    }

    private fun applyHighlightAndAccessoryLayout() {
        val hLp = highlightView.layoutParams as? FrameLayout.LayoutParams
        if (hLp == null) {
            return
        }
        when (selectionAppearance) {
            SelectionAppearance.PILL, SelectionAppearance.RECT -> {
                highlightView.visibility = VISIBLE
                val lp = hLp
                lp.height = FrameLayout.LayoutParams.MATCH_PARENT
                lp.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                highlightView.layoutParams = lp
                highlightView.alpha = 1f
            }
            SelectionAppearance.UNDERLINE -> {
                highlightView.visibility = VISIBLE
                val lp = hLp
                lp.height = underlineHeightPx
                lp.gravity = Gravity.BOTTOM or Gravity.START
                highlightView.layoutParams = lp
                highlightView.alpha = 1f
            }
            SelectionAppearance.TEXT_TINT -> {
                highlightView.visibility = GONE
            }
        }
        when (accessoryIndicator) {
            AccessoryIndicator.NONE -> accessoryDot.visibility = GONE
            AccessoryIndicator.DOT -> {
                if (selectionAppearance == SelectionAppearance.TEXT_TINT) {
                    accessoryDot.visibility = VISIBLE
                } else {
                    accessoryDot.visibility = if (selectionAppearance == SelectionAppearance.UNDERLINE) GONE else VISIBLE
                }
            }
        }
        (highlightView.background as? GradientDrawable)?.let { gd ->
            when (selectionAppearance) {
                SelectionAppearance.UNDERLINE -> {
                    val r = underlineHeightPx / 2f
                    gd.cornerRadius = r
                }
                SelectionAppearance.PILL -> {
                    if (height > 0) {
                        gd.cornerRadius = height / 2f
                    }
                }
                SelectionAppearance.RECT -> {
                    gd.cornerRadius = rectCornerRadiusPx
                }
                else -> { }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cornerRadius = h / 2f
        when (selectionAppearance) {
            SelectionAppearance.PILL -> {
                highlightDrawable.cornerRadius = cornerRadius
            }
            SelectionAppearance.RECT -> {
                highlightDrawable.cornerRadius = rectCornerRadiusPx
            }
            else -> { }
        }
        containerDrawable.cornerRadius = cornerRadius
        updateHighlight(animated = false)
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("selectedIndex", internalIndex)
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
            internalIndex = state.getInt("selectedIndex", 0)
        } else {
            super.onRestoreInstanceState(state)
        }
        if (state is Bundle) {
            post {
                if (tabs.isNotEmpty()) {
                    internalIndex = internalIndex.coerceIn(0, tabs.size - 1)
                }
                updateTextColors()
                updateHighlight(animated = false)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else 0.4f
        for (cell in segmentCells) {
            cell.isEnabled = enabled
        }
    }

    private fun setSelectedIndex(
        value: Int,
        animated: Boolean,
        fromViewPager: Boolean,
        notify: Boolean
    ) {
        val count = tabs.size
        if (count == 0) return
        val clamped = value.coerceIn(0, count - 1)
        if (internalIndex == clamped) return
        internalIndex = clamped
        updateHighlight(animated = animated)
        updateTextColors()
        if (notify) {
            onSelectionChange?.invoke(clamped)
        }
        if (!fromViewPager) {
            val vp = boundViewPager
            if (vp != null && vp.currentItem != clamped) {
                ignoreViewPagerCallback = true
                vp.setCurrentItem(clamped, animated)
                ignoreViewPagerCallback = false
            }
        }
    }

    private fun rebuildSegments() {
        segmentCells.clear()
        segmentRow.removeAllViews()
        if (iconSizePx <= 0) {
            iconSizePx = (22f * resources.displayMetrics.density).toInt()
        }

        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        for ((index, tab) in tabs.withIndex()) {
            val cell = buildSegmentCell(index, tab)
            segmentCells.add(cell)
            segmentRow.addView(cell, lp)
        }
        updateTextColors()
        post { updateHighlight(animated = false) }
    }

    private fun buildSegmentCell(index: Int, tab: SegmentTab): View {
        val dm = resources.displayMetrics.density
        val padH = (10f * dm).toInt()
        val padV = (6f * dm).toInt()
        val vGap = (3f * dm).toInt()
        val minTouch = (48f * dm).toInt()
        val onClick = View.OnClickListener {
            if (internalIndex != index) {
                setSelectedIndex(index, animated = true, fromViewPager = false, notify = true)
            }
        }
        return when {
            tab.hasIcon && tab.hasLabel -> {
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(padH, padV, padH, padV)
                    minimumHeight = minTouch
                    val iv = ImageView(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }
                    applyIconToImageView(iv, tab, index)
                    addView(
                        iv,
                        LinearLayout.LayoutParams(iconSizePx, iconSizePx)
                    )
                    val tv = TextView(context).apply {
                        text = tab.label
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                        gravity = Gravity.CENTER
                        maxLines = 1
                    }
                    val tvLp = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    tvLp.topMargin = vGap
                    addView(tv, tvLp)
                    setOnClickListener(onClick)
                }
            }
            tab.hasIcon && !tab.hasLabel -> {
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setPadding(padH, padV, padH, padV)
                    minimumWidth = minTouch
                    minimumHeight = minTouch
                    applyIconToImageView(this, tab, index)
                    setOnClickListener(onClick)
                }
            }
            else -> {
                TextView(context).apply {
                    text = tab.label
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setPadding(padH, padV, padH, padV)
                    minHeight = minTouch
                    setOnClickListener(onClick)
                }
            }
        }
    }

    private fun applyIconToImageView(target: ImageView, tab: SegmentTab, index: Int) {
        val d0 = AppCompatResources.getDrawable(context, tab.iconRes) ?: return
        val w = DrawableCompat.wrap(d0.mutate())
        val c = if (index == internalIndex) selectedTextColor else textColor
        DrawableCompat.setTint(w, c)
        target.setImageDrawable(w)
    }

    private fun updateHighlight(animated: Boolean) {
        if (tabs.isEmpty() || width == 0) return

        val segmentWidth = width.toFloat() / tabs.size
        val targetX = internalIndex * segmentWidth
        val dotX = targetX + (segmentWidth - dotSizePx) / 2f

        if (selectionAppearance != SelectionAppearance.TEXT_TINT) {
            val lp = highlightView.layoutParams as FrameLayout.LayoutParams
            lp.width = segmentWidth.toInt()
            highlightView.layoutParams = lp
            if (animated) {
                highlightView.animate().translationX(targetX).setDuration(250).start()
            } else {
                highlightView.translationX = targetX
            }
        } else {
            highlightView.translationX = 0f
        }

        if (accessoryIndicator == AccessoryIndicator.DOT && accessoryDot.visibility == VISIBLE) {
            val dlp = accessoryDot.layoutParams as FrameLayout.LayoutParams
            dlp.width = dotSizePx
            dlp.height = dotSizePx
            dlp.leftMargin = 0
            dlp.bottomMargin = 0
            dlp.gravity = Gravity.BOTTOM or Gravity.START
            accessoryDot.layoutParams = dlp
            if (animated) {
                accessoryDot.animate().translationX(dotX).setDuration(250).start()
            } else {
                accessoryDot.translationX = dotX
            }
        } else {
            accessoryDot.translationX = 0f
        }
    }

    private fun updateTextColors() {
        for ((index, cell) in segmentCells.withIndex()) {
            val c = if (index == internalIndex) selectedTextColor else textColor
            val tab = tabs.getOrNull(index) ?: continue
            when (cell) {
                is TextView -> {
                    cell.setTextColor(c)
                }
                is ImageView -> {
                    if (tab.hasIcon) {
                        val d0 = AppCompatResources.getDrawable(context, tab.iconRes) ?: continue
                        val w = DrawableCompat.wrap(d0.mutate())
                        DrawableCompat.setTint(w, c)
                        cell.setImageDrawable(w)
                    }
                }
                is LinearLayout -> {
                    if (tab.hasIcon && tab.hasLabel) {
                        (cell.getChildAt(1) as? TextView)?.setTextColor(c)
                        (cell.getChildAt(0) as? ImageView)?.let { iv ->
                            val d0 = AppCompatResources.getDrawable(context, tab.iconRes) ?: return@let
                            val w = DrawableCompat.wrap(d0.mutate())
                            DrawableCompat.setTint(w, c)
                            iv.setImageDrawable(w)
                        }
                    }
                }
                else -> { }
            }
        }
    }

    /**
     * 顺序与 XML `seg_selectionAppearance` 枚举值 0~3 一致，勿改序。
     */
    enum class SelectionAppearance {
        /** 全高**胶囊**（两端半圆，圆角 = 半高）。 */
        PILL,

        /** 底部细下划线。 */
        UNDERLINE,

        /** 无背景/线，仅通过文字色（及可选小圆点）表示选中。 */
        TEXT_TINT,

        /**
         * **圆角/直角长方形** 选中块：与 [PILL] 同为整格滑动背景；圆角由 `app:seg_rectCornerRadius` 控制（未写时约 4dp，0dp 为直角）。
         */
        RECT
    }

    enum class AccessoryIndicator {
        /** 无额外指示。 */
        NONE,

        /**
         * 底部小圆点；与 [SelectionAppearance.UNDERLINE] 同时存在时，下划线以圆点宽呈现（避免与线重复堆叠）。
         */
        DOT
    }
}
