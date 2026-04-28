package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R

class AwBottomTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    enum class TabMode {
        ICON_TEXT, ICON_ONLY, TEXT_ONLY
    }

    enum class IndicatorStyle {
        LINE, NONE
    }

    enum class IndicatorWidthMode {
        MATCH_TAB, FOLLOW_TEXT
    }

    enum class LayoutMode {
        FIXED, SCROLLABLE
    }

    data class TabItem(
        val title: String = "",
        val icon: Drawable? = null,
        @DrawableRes val iconRes: Int = 0,
        @DrawableRes val titleRes: Int = 0
    )

    private enum class BadgeType {
        NONE, DOT, TEXT
    }

    private data class BadgeState(
        val type: BadgeType = BadgeType.NONE,
        val text: String? = null
    )

    private val tabContainer = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        clipChildren = false
        clipToPadding = false
    }

    private val tabs = mutableListOf<TabItem>()
    private val tabViews = mutableListOf<View>()
    private val badgeStates = mutableMapOf<Int, BadgeState>()
    private val badgeTextColors = mutableMapOf<Int, Int>()
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val indicatorRect = RectF()
    private val drawingRect = RectF()
    private val tempRect = Rect()

    private var currentIndex = 0
    private var pendingRestoreIndex = 0
    private var indicatorAnimator: ValueAnimator? = null
    private var boundViewPager: ViewPager2? = null
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    private var boundLegacyViewPager: ViewPager? = null
    private var legacyPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var ignorePagerCallback = false

    private var tabSelectedListener: ((Int) -> Unit)? = null
    private var tabReselectedListener: ((Int) -> Unit)? = null
    private var tabLongClickListener: ((Int) -> Unit)? = null

    private val defaultSelectedColor = ContextCompat.getColor(context, R.color.tab_selected_default)
    private val defaultNormalColor = ContextCompat.getColor(context, R.color.tab_normal_default)
    private val defaultIndicatorColor = ContextCompat.getColor(context, R.color.tab_selected_default)
    private val defaultBadgeBackgroundColor = ContextCompat.getColor(context, R.color.tab_badge_default)
    private val defaultBadgeTextColor = 0xFFFFFFFF.toInt()

    var tabMode: TabMode = TabMode.ICON_TEXT
        set(value) {
            if (field == value) return
            field = value
            rebuildTabs()
        }

    var layoutMode: LayoutMode = LayoutMode.FIXED
        set(value) {
            if (field == value) return
            field = value
            rebuildTabs()
        }

    var selectedColor: Int = defaultSelectedColor
        set(value) {
            if (field == value) return
            field = value
            updateTabColors()
        }

    var normalColor: Int = defaultNormalColor
        set(value) {
            if (field == value) return
            field = value
            updateTabColors()
        }

    var indicatorStyle: IndicatorStyle = IndicatorStyle.NONE
        set(value) {
            if (field == value) return
            field = value
            if (value == IndicatorStyle.NONE) {
                indicatorAnimator?.cancel()
                indicatorRect.setEmpty()
                invalidate()
            } else {
                refreshIndicatorPosition(false)
            }
        }

    var indicatorWidthMode: IndicatorWidthMode = IndicatorWidthMode.MATCH_TAB
        set(value) {
            if (field == value) return
            field = value
            refreshIndicatorPosition(false)
        }

    var indicatorColor: Int = defaultIndicatorColor
        set(value) {
            if (field == value) return
            field = value
            indicatorPaint.color = value
            invalidate()
        }

    var indicatorHeight: Float = 3f.dp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            refreshIndicatorPosition(false)
        }

    var indicatorMarginTop: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            refreshIndicatorPosition(false)
        }

    var indicatorCornerRadius: Float = -1f
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var iconSize: Float = 24f.dp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            rebuildTabs()
        }

    var iconHeight: Float = 0f
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            rebuildTabs()
        }

    var textSize: Float = 12f.sp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            rebuildTabs()
        }

    var iconTextGap: Float = 4f.dp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            rebuildTabs()
        }

    var tabMargin: Float = 0f
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            rebuildTabs()
        }

    var tabMinWidth: Float = 56f.dp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            rebuildTabs()
        }

    var autoTintIcon: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            updateTabColors()
        }

    var selectedScale: Float = 1.1f
        set(value) {
            val newValue = value.coerceAtLeast(1f)
            if (field == newValue) return
            field = newValue
            updateTabColors()
        }

    var scaleDuration: Long = 200L
    var scaleInterpolator: Interpolator = OvershootInterpolator()
    var indicatorAnimatorDuration: Long = 300L
    var indicatorInterpolator: Interpolator = OvershootInterpolator()

    var badgeBackgroundColor: Int = defaultBadgeBackgroundColor
        set(value) {
            if (field == value) return
            field = value
            refreshBadgeAppearance()
        }

    var badgeTextColor: Int = defaultBadgeTextColor
        set(value) {
            if (field == value) return
            field = value
            refreshBadgeAppearance()
        }

    var badgeMinWidth: Float = 16f.dp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            refreshBadgeAppearance()
        }

    var badgePadding: Float = 4f.dp()
        set(value) {
            val newValue = value.coerceAtLeast(0f)
            if (field == newValue) return
            field = newValue
            refreshBadgeAppearance()
        }

    var enableScrollSync: Boolean = true

    var tabBackgroundColor: Int = 0
        set(value) {
            if (field == value) return
            field = value
            applyContainerBackground()
        }

    var enableRippleEffect: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            rebuildTabs()
        }

    var rippleColor: Int = 0x33000000
        set(value) {
            if (field == value) return
            field = value
            if (enableRippleEffect) rebuildTabs()
        }

    var tabBarElevation: Float = 8f.dp()
        set(value) {
            if (field == value) return
            field = value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = value
            }
        }

    var cornerRadius: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            applyContainerBackground()
        }

    init {
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        clipChildren = false
        clipToPadding = false
        isFillViewport = true
        setWillNotDraw(false)

        addView(
            tabContainer,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBottomTabBar)
        applyAttributes(ta)
        ta.recycle()

        indicatorPaint.color = indicatorColor
        applyContainerBackground()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = tabBarElevation
        }
    }

    private fun applyAttributes(ta: TypedArray) {
        tabMode = TabMode.values().getOrElse(ta.getInt(R.styleable.AwBottomTabBar_tab_mode, 0)) { TabMode.ICON_TEXT }
        layoutMode = LayoutMode.values().getOrElse(ta.getInt(R.styleable.AwBottomTabBar_tab_layout_mode, 0)) { LayoutMode.FIXED }
        selectedColor = ta.getColor(R.styleable.AwBottomTabBar_tab_selected_color, defaultSelectedColor)
        normalColor = ta.getColor(R.styleable.AwBottomTabBar_tab_normal_color, defaultNormalColor)
        indicatorStyle = IndicatorStyle.values().getOrElse(ta.getInt(R.styleable.AwBottomTabBar_indicator_style, 1)) { IndicatorStyle.NONE }
        indicatorWidthMode = IndicatorWidthMode.values().getOrElse(ta.getInt(R.styleable.AwBottomTabBar_indicator_width_mode, 0)) { IndicatorWidthMode.MATCH_TAB }
        indicatorColor = ta.getColor(R.styleable.AwBottomTabBar_indicator_color, defaultIndicatorColor)
        indicatorHeight = ta.getDimension(R.styleable.AwBottomTabBar_indicator_height, 3f.dp())
        indicatorMarginTop = ta.getDimension(R.styleable.AwBottomTabBar_indicator_marginTop, 0f)
        indicatorCornerRadius = ta.getDimension(R.styleable.AwBottomTabBar_indicator_corner_radius, -1f)
        iconSize = ta.getDimension(R.styleable.AwBottomTabBar_tab_icon_size, 24f.dp())
        iconHeight = ta.getDimension(R.styleable.AwBottomTabBar_tab_icon_height, 0f)
        textSize = ta.getDimension(R.styleable.AwBottomTabBar_tab_text_size, 12f.sp())
        iconTextGap = ta.getDimension(R.styleable.AwBottomTabBar_tab_icon_text_gap, 4f.dp())
        tabMargin = ta.getDimension(R.styleable.AwBottomTabBar_tab_margin, 0f)
        tabMinWidth = ta.getDimension(R.styleable.AwBottomTabBar_tab_min_width, 56f.dp())
        autoTintIcon = ta.getBoolean(R.styleable.AwBottomTabBar_tab_auto_tint, true)
        selectedScale = ta.getFloat(R.styleable.AwBottomTabBar_tab_selected_scale, 1.1f)
        scaleDuration = ta.getInteger(R.styleable.AwBottomTabBar_tab_scale_duration, 200).toLong()
        indicatorAnimatorDuration = ta.getInteger(R.styleable.AwBottomTabBar_indicator_anim_duration, 300).toLong()
        badgeBackgroundColor = ta.getColor(R.styleable.AwBottomTabBar_badge_background_color, defaultBadgeBackgroundColor)
        badgeTextColor = ta.getColor(R.styleable.AwBottomTabBar_badge_text_color, defaultBadgeTextColor)
        badgeMinWidth = ta.getDimension(R.styleable.AwBottomTabBar_badge_min_width, 16f.dp())
        badgePadding = ta.getDimension(R.styleable.AwBottomTabBar_badge_padding, 4f.dp())
        enableScrollSync = ta.getBoolean(R.styleable.AwBottomTabBar_enable_scroll_sync, true)
        tabBackgroundColor = ta.getColor(R.styleable.AwBottomTabBar_background_color, 0)
        enableRippleEffect = ta.getBoolean(R.styleable.AwBottomTabBar_enable_ripple, true)
        rippleColor = ta.getColor(R.styleable.AwBottomTabBar_ripple_color, 0x33000000)
        tabBarElevation = ta.getDimension(R.styleable.AwBottomTabBar_tabbar_elevation, 8f.dp())
        cornerRadius = ta.getDimension(R.styleable.AwBottomTabBar_corner_radius, 0f)

        val titlesRes = ta.getResourceId(R.styleable.AwBottomTabBar_tab_titles, 0)
        val iconsRes = ta.getResourceId(R.styleable.AwBottomTabBar_tab_icons, 0)
        if (titlesRes != 0) {
            val titles = context.resources.getStringArray(titlesRes)
            val iconArray = if (iconsRes != 0) context.resources.obtainTypedArray(iconsRes) else null
            val initialItems = titles.mapIndexed { index, title ->
                TabItem(title = title, icon = iconArray?.getDrawableOrNull(index))
            }
            iconArray?.recycle()
            tabs.clear()
            tabs.addAll(initialItems)
        }
    }

    private fun TypedArray.getDrawableOrNull(index: Int): Drawable? {
        return runCatching { getDrawable(index) }.getOrNull()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (tabs.isNotEmpty() && tabViews.isEmpty()) {
            rebuildTabs()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshIndicatorPosition(false)
    }

    fun setItems(items: List<TabItem>) {
        tabs.clear()
        tabs.addAll(items)
        trimBadgeMaps()
        currentIndex = currentIndex.coerceInTabRange()
        pendingRestoreIndex = currentIndex
        rebuildTabs()
    }

    fun addItem(item: TabItem) {
        insertItem(tabs.size, item)
    }

    fun insertItem(index: Int, item: TabItem) {
        val safeIndex = index.coerceIn(0, tabs.size)
        tabs.add(safeIndex, item)
        shiftStateForInsert(safeIndex)
        if (safeIndex <= currentIndex && tabs.size > 1) {
            currentIndex += 1
        }
        pendingRestoreIndex = currentIndex.coerceInTabRange()
        rebuildTabs()
    }

    fun updateItem(index: Int, item: TabItem): Boolean {
        if (index !in tabs.indices) return false
        tabs[index] = item
        rebuildTabs()
        return true
    }

    fun removeItem(index: Int): TabItem? {
        if (index !in tabs.indices) return null
        val removed = tabs.removeAt(index)
        shiftStateForRemove(index)
        currentIndex = when {
            tabs.isEmpty() -> 0
            index < currentIndex -> currentIndex - 1
            currentIndex >= tabs.size -> tabs.lastIndex
            else -> currentIndex
        }
        pendingRestoreIndex = currentIndex
        rebuildTabs()
        return removed
    }

    fun clearItems() {
        tabs.clear()
        tabViews.clear()
        badgeStates.clear()
        badgeTextColors.clear()
        currentIndex = 0
        pendingRestoreIndex = 0
        indicatorAnimator?.cancel()
        indicatorRect.setEmpty()
        tabContainer.removeAllViews()
        invalidate()
    }

    fun getItemCount(): Int = tabs.size
    fun getItem(index: Int): TabItem? = tabs.getOrNull(index)
    fun getItems(): List<TabItem> = tabs.toList()
    fun containsItem(index: Int): Boolean = index in tabs.indices

    private fun rebuildTabs() {
        tabContainer.removeAllViews()
        tabViews.clear()
        configureContainerWidth()

        tabs.forEachIndexed { index, item ->
            val tabView = createTabView(item, index)
            tabContainer.addView(tabView, createTabLayoutParams(index))
            tabViews.add(tabView)
        }

        currentIndex = currentIndex.coerceInTabRange()
        pendingRestoreIndex = pendingRestoreIndex.coerceInTabRange()
        applyAllBadges()
        updateTabColors()
        refreshIndicatorPosition(false)
    }

    private fun configureContainerWidth() {
        val params = tabContainer.layoutParams as LayoutParams
        if (layoutMode == LayoutMode.FIXED) {
            params.width = LayoutParams.MATCH_PARENT
            tabContainer.gravity = Gravity.CENTER
        } else {
            params.width = LayoutParams.WRAP_CONTENT
            tabContainer.gravity = Gravity.CENTER_VERTICAL
        }
        tabContainer.layoutParams = params
    }

    private fun createTabLayoutParams(index: Int): LinearLayout.LayoutParams {
        return if (layoutMode == LayoutMode.FIXED) {
            LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                if (index > 0) marginStart = tabMargin.toInt()
            }
        } else {
            LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                if (index > 0) marginStart = tabMargin.toInt()
            }
        }
    }

    private fun createTabView(item: TabItem, index: Int): View {
        val content = when (tabMode) {
            TabMode.ICON_ONLY -> createIconOnlyContent(item)
            TabMode.TEXT_ONLY -> createTextOnlyContent(item)
            TabMode.ICON_TEXT -> createIconTextContent(item)
        }

        return createTabContainer(content).also { container ->
            container.minimumWidth = tabMinWidth.toInt()
            container.setOnClickListener { handleTabClick(index) }
            container.setOnLongClickListener {
                tabLongClickListener?.invoke(index)
                true
            }
            container.contentDescription = resolveTitle(item).ifBlank { "Tab ${index + 1}" }
        }
    }

    private fun createTabContainer(content: View): FrameLayout {
        return FrameLayout(context).apply {
            foregroundGravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
            if (enableRippleEffect && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                foreground = createRippleDrawable()
            } else if (enableRippleEffect) {
                background = createRippleDrawable()
            }
            addView(
                content,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
        }
    }

    private fun createIconOnlyContent(item: TabItem): FrameLayout {
        return FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
            addView(createIconView(item))
            addBadgeViews(this)
        }
    }

    private fun createTextOnlyContent(item: TabItem): FrameLayout {
        return FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
            addView(createTextView(item))
            addBadgeViews(this)
        }
    }

    private fun createIconTextContent(item: TabItem): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false

            val iconHolder = FrameLayout(context).apply {
                clipChildren = false
                clipToPadding = false
                addView(createIconView(item))
                addBadgeViews(this)
            }

            addView(iconHolder)
            addView(
                createTextView(item),
                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    topMargin = iconTextGap.toInt()
                }
            )
        }
    }

    private fun createIconView(item: TabItem): ImageView {
        val actualIconHeight = if (iconHeight > 0f) iconHeight else iconSize
        return ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(iconSize.toInt(), actualIconHeight.toInt(), Gravity.CENTER)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(resolveIcon(item))
            tag = TAG_ICON
        }
    }

    private fun createTextView(item: TabItem): TextView {
        return TextView(context).apply {
            text = resolveTitle(item)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            setTextColor(normalColor)
            gravity = Gravity.CENTER
            maxLines = 1
            tag = TAG_TEXT
        }
    }

    private fun resolveTitle(item: TabItem): String {
        return if (item.titleRes != 0) context.getString(item.titleRes) else item.title
    }

    private fun resolveIcon(item: TabItem): Drawable? {
        return item.icon ?: item.iconRes.takeIf { it != 0 }?.let { ContextCompat.getDrawable(context, it) }
    }

    private fun addBadgeViews(parent: FrameLayout) {
        val offset = 2f.dp().toInt()
        val dotSize = 10f.dp().toInt()

        parent.addView(
            View(context).apply {
                tag = TAG_BADGE_DOT
                visibility = GONE
                background = createBadgeDrawable()
            },
            FrameLayout.LayoutParams(dotSize, dotSize, Gravity.TOP or Gravity.END).apply {
                topMargin = -offset
                marginEnd = -offset
            }
        )

        parent.addView(
            TextView(context).apply {
                tag = TAG_BADGE_TEXT
                visibility = GONE
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(badgeTextColor)
                background = createBadgeDrawable()
                minWidth = badgeMinWidth.toInt()
                minHeight = badgeMinWidth.toInt()
                setPadding(badgePadding.toInt(), 0, badgePadding.toInt(), 0)
            },
            FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, badgeMinWidth.toInt(), Gravity.TOP or Gravity.END).apply {
                topMargin = -offset
                marginEnd = -offset
            }
        )
    }

    private fun createBadgeDrawable(): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = badgeMinWidth / 2f
            setColor(badgeBackgroundColor)
            setStroke(1f.dp().toInt(), 0xFFFFFFFF.toInt())
        }
    }

    private fun createRippleDrawable(): RippleDrawable {
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = this@AwBottomTabBar.cornerRadius
            setColor(0xFFFFFFFF.toInt())
        }
        return RippleDrawable(ColorStateList.valueOf(rippleColor), null, mask)
    }

    private fun handleTabClick(index: Int) {
        if (index == currentIndex) {
            tabReselectedListener?.invoke(index)
            return
        }
        setCurrentIndex(index, animate = true, notifyListener = true, syncPager = true)
    }

    fun setCurrentIndex(@IntRange(from = 0) index: Int, animate: Boolean = true) {
        setCurrentIndex(index, animate, notifyListener = true, syncPager = true)
    }

    private fun setCurrentIndex(index: Int, animate: Boolean, notifyListener: Boolean, syncPager: Boolean) {
        if (index !in tabs.indices) return
        val changed = currentIndex != index
        currentIndex = index
        pendingRestoreIndex = index
        updateTabColors()
        refreshIndicatorPosition(animate)
        ensureTabVisible(index, animate)

        if (syncPager) {
            var changedPager = false
            if (boundViewPager?.currentItem != index) {
                ignorePagerCallback = true
                boundViewPager?.setCurrentItem(index, animate)
                changedPager = true
            }
            if (boundLegacyViewPager?.currentItem != index) {
                ignorePagerCallback = true
                boundLegacyViewPager?.setCurrentItem(index, animate)
                changedPager = true
            }
            if (changedPager) {
                post { ignorePagerCallback = false }
            }
        }

        if (changed && notifyListener) {
            tabSelectedListener?.invoke(index)
        }
    }

    fun getCurrentIndex(): Int = currentIndex

    private fun updateTabColors() {
        tabViews.forEachIndexed { index, view ->
            val selected = index == currentIndex
            val color = if (selected) selectedColor else normalColor
            updateViewState(view, color, selected)
            view.isSelected = selected
        }
    }

    private fun updateViewState(view: View, color: Int, selected: Boolean) {
        when (view) {
            is ImageView -> {
                view.imageTintList = if (autoTintIcon) ColorStateList.valueOf(color) else null
                updateScale(view, selected)
            }
            is TextView -> {
                if (view.tag == TAG_TEXT) view.setTextColor(color)
                updateScale(view, selected)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    updateViewState(view.getChildAt(i), color, selected)
                }
                updateScale(view, selected)
            }
            else -> updateScale(view, selected)
        }
    }

    private fun updateScale(view: View, selected: Boolean) {
        val targetScale = if (selected) selectedScale else 1f
        if (view.scaleX == targetScale && view.scaleY == targetScale) return
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(scaleDuration)
            .setInterpolator(scaleInterpolator)
            .start()
    }

    private fun refreshIndicatorPosition(animate: Boolean) {
        if (indicatorStyle == IndicatorStyle.NONE || tabViews.isEmpty() || currentIndex !in tabViews.indices) {
            indicatorRect.setEmpty()
            invalidate()
            return
        }
        post { moveIndicatorTo(currentIndex, animate) }
    }

    private fun moveIndicatorTo(index: Int, animate: Boolean) {
        if (indicatorStyle == IndicatorStyle.NONE || index !in tabViews.indices) return
        val targetRect = computeIndicatorRect(index) ?: return

        if (!animate || indicatorRect.isEmpty) {
            indicatorAnimator?.cancel()
            indicatorRect.set(targetRect)
            invalidate()
            return
        }

        val startRect = RectF(indicatorRect)
        indicatorAnimator?.cancel()
        indicatorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = indicatorAnimatorDuration
            interpolator = indicatorInterpolator
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                indicatorRect.set(
                    lerp(startRect.left, targetRect.left, fraction),
                    lerp(startRect.top, targetRect.top, fraction),
                    lerp(startRect.right, targetRect.right, fraction),
                    lerp(startRect.bottom, targetRect.bottom, fraction)
                )
                invalidate()
            }
            start()
        }
    }

    private fun computeIndicatorRect(index: Int): RectF? {
        val targetTab = tabViews.getOrNull(index) ?: return null
        if (!targetTab.isLaidOut || targetTab.width == 0) return null

        val left: Float
        val right: Float
        if (indicatorWidthMode == IndicatorWidthMode.FOLLOW_TEXT) {
            val textView = findFirstViewWithTag(targetTab, TAG_TEXT) as? TextView
            if (textView != null && textView.width > 0) {
                val titleWidth = textView.paint.measureText(textView.text.toString())
                val textCenter = viewCenterXInContainer(textView)
                left = textCenter - titleWidth / 2f
                right = textCenter + titleWidth / 2f
            } else {
                val center = viewCenterXInContainer(targetTab)
                val fallbackWidth = targetTab.width * 0.52f
                left = center - fallbackWidth / 2f
                right = center + fallbackWidth / 2f
            }
        } else {
            left = targetTab.left.toFloat()
            right = targetTab.right.toFloat()
        }

        val bottom = height - indicatorMarginTop
        val top = bottom - indicatorHeight
        return RectF(left, top, right, bottom)
    }

    private fun viewCenterXInContainer(view: View): Float {
        view.getDrawingRect(tempRect)
        tabContainer.offsetDescendantRectToMyCoords(view, tempRect)
        return tempRect.exactCenterX()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (indicatorStyle == IndicatorStyle.NONE || indicatorRect.isEmpty) return
        val radius = if (indicatorCornerRadius >= 0f) indicatorCornerRadius else indicatorHeight / 2f
        drawingRect.set(
            indicatorRect.left - scrollX,
            indicatorRect.top,
            indicatorRect.right - scrollX,
            indicatorRect.bottom
        )
        canvas.drawRoundRect(drawingRect, radius, radius, indicatorPaint)
    }

    fun bindViewPager(viewPager: ViewPager2) {
        unbindViewPager()
        boundViewPager = viewPager
        if (viewPager.currentItem in tabs.indices) {
            currentIndex = viewPager.currentItem
            pendingRestoreIndex = currentIndex
        }

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (!enableScrollSync || indicatorStyle == IndicatorStyle.NONE || tabViews.isEmpty()) return
                if (position !in tabs.indices) return
                val fromRect = computeIndicatorRect(position) ?: return
                val nextRect = computeIndicatorRect((position + 1).coerceAtMost(tabs.lastIndex)) ?: fromRect
                val fraction = positionOffset.coerceIn(0f, 1f)
                indicatorRect.set(
                    lerp(fromRect.left, nextRect.left, fraction),
                    lerp(fromRect.top, nextRect.top, fraction),
                    lerp(fromRect.right, nextRect.right, fraction),
                    lerp(fromRect.bottom, nextRect.bottom, fraction)
                )
                if (layoutMode == LayoutMode.SCROLLABLE) {
                    val currentCenter = lerp(fromRect.centerX(), nextRect.centerX(), fraction)
                    smoothScrollToCentered(currentCenter, false)
                }
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                if (ignorePagerCallback || position !in tabs.indices) return
                setCurrentIndex(position, animate = true, notifyListener = true, syncPager = false)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && currentIndex in tabs.indices) {
                    refreshIndicatorPosition(false)
                }
            }
        }

        viewPager.registerOnPageChangeCallback(pageChangeCallback!!)
        setCurrentIndex(currentIndex.coerceInTabRange(), animate = false, notifyListener = false, syncPager = false)
    }

    fun bindViewPager(viewPager: ViewPager) {
        unbindViewPager()
        boundLegacyViewPager = viewPager
        if (viewPager.currentItem in tabs.indices) {
            currentIndex = viewPager.currentItem
            pendingRestoreIndex = currentIndex
        }

        legacyPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (!enableScrollSync || indicatorStyle == IndicatorStyle.NONE || tabViews.isEmpty()) return
                if (position !in tabs.indices) return
                val fromRect = computeIndicatorRect(position) ?: return
                val nextRect = computeIndicatorRect((position + 1).coerceAtMost(tabs.lastIndex)) ?: fromRect
                val fraction = positionOffset.coerceIn(0f, 1f)
                indicatorRect.set(
                    lerp(fromRect.left, nextRect.left, fraction),
                    lerp(fromRect.top, nextRect.top, fraction),
                    lerp(fromRect.right, nextRect.right, fraction),
                    lerp(fromRect.bottom, nextRect.bottom, fraction)
                )
                if (layoutMode == LayoutMode.SCROLLABLE) {
                    val currentCenter = lerp(fromRect.centerX(), nextRect.centerX(), fraction)
                    smoothScrollToCentered(currentCenter, false)
                }
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                if (ignorePagerCallback || position !in tabs.indices) return
                setCurrentIndex(position, animate = true, notifyListener = true, syncPager = false)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE && currentIndex in tabs.indices) {
                    refreshIndicatorPosition(false)
                }
            }
        }

        viewPager.addOnPageChangeListener(legacyPageChangeListener!!)
        setCurrentIndex(currentIndex.coerceInTabRange(), animate = false, notifyListener = false, syncPager = false)
    }

    fun bindFragments(activity: FragmentActivity, fragments: List<Fragment>, viewPager: ViewPager2) {
        viewPager.adapter = object : FragmentStateAdapter(activity) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        bindViewPager(viewPager)
    }

    fun bindFragments(fragmentManager: FragmentManager, fragments: List<Fragment>, viewPager: ViewPager) {
        viewPager.adapter = object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getCount(): Int = fragments.size
            override fun getItem(position: Int): Fragment = fragments[position]
        }
        bindViewPager(viewPager)
    }

    fun unbindViewPager() {
        pageChangeCallback?.let { boundViewPager?.unregisterOnPageChangeCallback(it) }
        legacyPageChangeListener?.let { boundLegacyViewPager?.removeOnPageChangeListener(it) }
        pageChangeCallback = null
        boundViewPager = null
        legacyPageChangeListener = null
        boundLegacyViewPager = null
        ignorePagerCallback = false
    }

    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        tabSelectedListener = listener
    }

    fun setOnTabReselectedListener(listener: (Int) -> Unit) {
        tabReselectedListener = listener
    }

    fun setOnTabLongClickListener(listener: (Int) -> Unit) {
        tabLongClickListener = listener
    }

    fun setBadgeCount(index: Int, count: Int) {
        if (index !in tabs.indices) return
        if (count > 0) {
            badgeStates[index] = BadgeState(BadgeType.TEXT, if (count > 99) "99+" else count.toString())
        } else {
            badgeStates.remove(index)
        }
        applyBadge(index)
    }

    fun setBadgeText(index: Int, text: String?) {
        if (index !in tabs.indices) return
        if (text.isNullOrBlank()) badgeStates.remove(index)
        else badgeStates[index] = BadgeState(BadgeType.TEXT, text)
        applyBadge(index)
    }

    fun showBadgeDot(index: Int) {
        if (index !in tabs.indices) return
        badgeStates[index] = BadgeState(BadgeType.DOT)
        applyBadge(index)
    }

    fun setBadgeTextColor(index: Int, color: Int) {
        if (index !in tabs.indices) return
        badgeTextColors[index] = color
        applyBadge(index)
    }

    fun clearBadge(index: Int) {
        if (index !in tabs.indices) return
        badgeStates.remove(index)
        badgeTextColors.remove(index)
        applyBadge(index)
    }

    fun clearAllBadges() {
        badgeStates.clear()
        badgeTextColors.clear()
        tabViews.forEachIndexed { index, _ -> applyBadge(index) }
    }

    fun getBadgeText(index: Int): String? = badgeStates[index]?.text
    fun hasBadge(index: Int): Boolean = badgeStates[index]?.type?.let { it != BadgeType.NONE } == true

    private fun applyAllBadges() {
        tabViews.forEachIndexed { index, _ -> applyBadge(index) }
    }

    private fun applyBadge(index: Int) {
        val tabView = tabViews.getOrNull(index) ?: return
        val dotView = findFirstViewWithTag(tabView, TAG_BADGE_DOT)
        val textView = findFirstViewWithTag(tabView, TAG_BADGE_TEXT) as? TextView
        val state = badgeStates[index] ?: BadgeState()
        val resolvedTextColor = badgeTextColors[index] ?: badgeTextColor

        dotView?.background = createBadgeDrawable()
        textView?.apply {
            background = createBadgeDrawable()
            setTextColor(resolvedTextColor)
            minWidth = badgeMinWidth.toInt()
            minHeight = badgeMinWidth.toInt()
            setPadding(badgePadding.toInt(), 0, badgePadding.toInt(), 0)
            layoutParams = (layoutParams as FrameLayout.LayoutParams).also { params ->
                params.height = badgeMinWidth.toInt()
            }
        }

        when (state.type) {
            BadgeType.NONE -> {
                dotView?.visibility = GONE
                textView?.visibility = GONE
            }
            BadgeType.DOT -> {
                dotView?.visibility = VISIBLE
                textView?.visibility = GONE
            }
            BadgeType.TEXT -> {
                textView?.text = state.text.orEmpty()
                textView?.visibility = VISIBLE
                dotView?.visibility = GONE
            }
        }
    }

    private fun refreshBadgeAppearance() {
        tabViews.forEachIndexed { index, _ -> applyBadge(index) }
    }

    private fun applyContainerBackground() {
        val color = if (tabBackgroundColor != 0) tabBackgroundColor else 0xFFFFFFFF.toInt()
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = this@AwBottomTabBar.cornerRadius
            setColor(color)
        }
    }

    private fun ensureTabVisible(index: Int, animate: Boolean) {
        if (layoutMode != LayoutMode.SCROLLABLE) return
        val targetTab = tabViews.getOrNull(index) ?: return
        val center = targetTab.left + targetTab.width / 2f
        smoothScrollToCentered(center, animate)
    }

    private fun smoothScrollToCentered(centerX: Float, animate: Boolean) {
        val maxScroll = (tabContainer.width - width).coerceAtLeast(0)
        val targetScroll = (centerX - width / 2f).toInt().coerceIn(0, maxScroll)
        if (animate) smoothScrollTo(targetScroll, 0) else scrollTo(targetScroll, 0)
    }

    private fun findFirstViewWithTag(view: View, tag: String): View? {
        if (view.tag == tag) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findFirstViewWithTag(view.getChildAt(i), tag)
                if (found != null) return found
            }
        }
        return null
    }

    private fun trimBadgeMaps() {
        val valid = tabs.indices.toSet()
        badgeStates.keys.toList().filterNot(valid::contains).forEach(badgeStates::remove)
        badgeTextColors.keys.toList().filterNot(valid::contains).forEach(badgeTextColors::remove)
    }

    private fun shiftStateForInsert(insertIndex: Int) {
        badgeStates.clearAndReplace(shiftMapOnInsert(badgeStates, insertIndex))
        badgeTextColors.clearAndReplace(shiftMapOnInsert(badgeTextColors, insertIndex))
    }

    private fun shiftStateForRemove(removeIndex: Int) {
        badgeStates.clearAndReplace(shiftMapOnRemove(badgeStates, removeIndex))
        badgeTextColors.clearAndReplace(shiftMapOnRemove(badgeTextColors, removeIndex))
    }

    private fun <T> MutableMap<Int, T>.clearAndReplace(newValues: Map<Int, T>) {
        clear()
        putAll(newValues)
    }

    private fun <T> shiftMapOnInsert(source: Map<Int, T>, insertIndex: Int): Map<Int, T> {
        val result = mutableMapOf<Int, T>()
        source.forEach { (key, value) ->
            result[if (key >= insertIndex) key + 1 else key] = value
        }
        return result
    }

    private fun <T> shiftMapOnRemove(source: Map<Int, T>, removeIndex: Int): Map<Int, T> {
        val result = mutableMapOf<Int, T>()
        source.forEach { (key, value) ->
            when {
                key < removeIndex -> result[key] = value
                key > removeIndex -> result[key - 1] = value
            }
        }
        return result
    }

    private fun Int.coerceInTabRange(): Int {
        return if (tabs.isEmpty()) 0 else coerceIn(0, tabs.lastIndex)
    }

    private fun lerp(start: Float, end: Float, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
        return start + (end - start) * fraction
    }

    fun refreshIndicatorColor(color: Int) {
        indicatorColor = color
    }

    fun refreshBadgeColor(color: Int) {
        badgeBackgroundColor = color
    }

    override fun onDetachedFromWindow() {
        indicatorAnimator?.cancel()
        unbindViewPager()
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
            putInt(KEY_CURRENT_INDEX, currentIndex)
            putInt(KEY_SCROLL_X, scrollX)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        val superState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            state.getParcelable(KEY_SUPER_STATE, Parcelable::class.java)
        } else {
            @Suppress("DEPRECATION")
            state.getParcelable(KEY_SUPER_STATE)
        }
        super.onRestoreInstanceState(superState)
        pendingRestoreIndex = state.getInt(KEY_CURRENT_INDEX, 0)
        val restoredScrollX = state.getInt(KEY_SCROLL_X, 0)
        currentIndex = pendingRestoreIndex.coerceInTabRange()
        post {
            currentIndex = pendingRestoreIndex.coerceInTabRange()
            updateTabColors()
            refreshIndicatorPosition(false)
            if (layoutMode == LayoutMode.SCROLLABLE) {
                scrollTo(restoredScrollX, 0)
                ensureTabVisible(currentIndex, false)
            }
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = AwBottomTabBar::class.java.name
        event.itemCount = tabs.size
        event.currentItemIndex = currentIndex
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density
    private fun Float.sp(): Float = this * resources.displayMetrics.scaledDensity

    companion object {
        private const val KEY_SUPER_STATE = "superState"
        private const val KEY_CURRENT_INDEX = "currentIndex"
        private const val KEY_SCROLL_X = "scrollX"

        private const val TAG_ICON = "icon"
        private const val TAG_TEXT = "text"
        private const val TAG_BADGE_DOT = "badge_dot"
        private const val TAG_BADGE_TEXT = "badge_text"
    }
}
