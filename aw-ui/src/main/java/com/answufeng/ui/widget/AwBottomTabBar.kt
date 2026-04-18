package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R
import com.google.android.material.tabs.TabLayoutMediator

class AwBottomTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class TabMode {
        ICON_TEXT, ICON_ONLY, TEXT_ONLY
    }

    enum class IndicatorStyle {
        LINE, DOT, BLOCK, NONE
    }

    data class TabItem(
        val title: String = "",
        val icon: Drawable? = null,
        @DrawableRes val iconRes: Int = 0,
        @DrawableRes val titleRes: Int = 0
    )

    private val tabs = mutableListOf<TabItem>()
    private val tabViews = mutableListOf<View>()
    private var currentIndex = 0
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var indicatorRect = RectF()
    private var indicatorAnimator: ValueAnimator? = null
    private var boundViewPager: ViewPager2? = null
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    private var tabClickListener: ((Int) -> Unit)? = null
    private var tabReselectedListener: ((Int) -> Unit)? = null
    private var tabLongClickListener: ((Int) -> Unit)? = null
    private val badgeCounts = mutableMapOf<Int, Int>()
    private val badgeTexts = mutableMapOf<Int, String>()

    var tabMode: TabMode = TabMode.ICON_TEXT
    var selectedColor: Int = 0
    var normalColor: Int = 0
    var indicatorStyle: IndicatorStyle = IndicatorStyle.LINE
    var indicatorColor: Int = 0
    var indicatorHeight: Float = 3f.dp()
    var indicatorMarginTop: Float = 0f
    var iconSize: Float = 24f.dp()
    var textSize: Float = 12f.sp()
    var tabMargin: Float = 0f
    var autoTintIcon: Boolean = true
    var selectedScale: Float = 1.1f
    var indicatorWidthFollowsText: Boolean = false
    var indicatorAnimatorDuration: Long = 300L
    var badgeBackgroundColor: Int = 0

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        selectedColor = ContextCompat.getColor(context, R.color.tab_selected_default)
        normalColor = ContextCompat.getColor(context, R.color.tab_normal_default)
        indicatorColor = ContextCompat.getColor(context, R.color.tab_selected_default)
        badgeBackgroundColor = ContextCompat.getColor(context, R.color.tab_badge_default)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBottomTabBar)

        tabMode = TabMode.entries.getOrElse(ta.getInt(R.styleable.AwBottomTabBar_tab_mode, 0)) { TabMode.ICON_TEXT }
        selectedColor = ta.getColor(R.styleable.AwBottomTabBar_tab_selected_color, selectedColor)
        normalColor = ta.getColor(R.styleable.AwBottomTabBar_tab_normal_color, normalColor)
        indicatorStyle = IndicatorStyle.entries.getOrElse(ta.getInt(R.styleable.AwBottomTabBar_indicator_style, 0)) { IndicatorStyle.LINE }
        indicatorColor = ta.getColor(R.styleable.AwBottomTabBar_indicator_color, indicatorColor)
        indicatorHeight = ta.getDimension(R.styleable.AwBottomTabBar_indicator_height, 3f.dp())
        indicatorMarginTop = ta.getDimension(R.styleable.AwBottomTabBar_indicator_marginTop, 0f)
        iconSize = ta.getDimension(R.styleable.AwBottomTabBar_tab_icon_size, 24f.dp())
        textSize = ta.getDimension(R.styleable.AwBottomTabBar_tab_text_size, 12f.sp())
        tabMargin = ta.getDimension(R.styleable.AwBottomTabBar_tab_margin, 0f)
        autoTintIcon = ta.getBoolean(R.styleable.AwBottomTabBar_tab_auto_tint, true)
        selectedScale = ta.getFloat(R.styleable.AwBottomTabBar_tab_selected_scale, 1.1f)
        indicatorWidthFollowsText = ta.getBoolean(R.styleable.AwBottomTabBar_indicator_width_follows_text, false)
        indicatorAnimatorDuration = ta.getInteger(R.styleable.AwBottomTabBar_indicator_anim_duration, 300).toLong()
        badgeBackgroundColor = ta.getColor(R.styleable.AwBottomTabBar_badge_background_color, badgeBackgroundColor)

        val titlesRes = ta.getResourceId(R.styleable.AwBottomTabBar_tab_titles, 0)
        val iconsRes = ta.getResourceId(R.styleable.AwBottomTabBar_tab_icons, 0)

        if (titlesRes != 0) {
            val titles = context.resources.getStringArray(titlesRes)
            val icons = if (iconsRes != 0) context.resources.obtainTypedArray(iconsRes) else null
            titles.forEachIndexed { index, title ->
                val icon = icons?.getDrawableOrNull(index)
                tabs.add(TabItem(title = title, icon = icon))
            }
            icons?.recycle()
        }

        ta.recycle()

        indicatorPaint.color = indicatorColor
        indicatorPaint.style = Paint.Style.FILL
        badgePaint.color = badgeBackgroundColor
        badgePaint.style = Paint.Style.FILL

        clipChildren = false
        clipToPadding = false
    }

    private fun TypedArray.getDrawableOrNull(index: Int): Drawable? {
        return try {
            getDrawable(index)
        } catch (e: Exception) {
            null
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (tabs.isNotEmpty() && childCount == 0) {
            rebuildTabs()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (indicatorStyle != IndicatorStyle.NONE && tabViews.isNotEmpty()) {
            post { moveIndicatorTo(currentIndex, false) }
        }
    }

    fun setItems(items: List<TabItem>) {
        tabs.clear()
        tabs.addAll(items)
        rebuildTabs()
    }

    fun addItem(item: TabItem) {
        tabs.add(item)
        addTabView(item, tabs.size - 1)
        requestLayout()
    }

    fun removeItem(index: Int) {
        if (index < 0 || index >= tabs.size) return
        tabs.removeAt(index)
        removeViewAt(index)
        tabViews.removeAt(index)
        if (currentIndex >= tabs.size) {
            currentIndex = (tabs.size - 1).coerceAtLeast(0)
        }
        updateTabColors()
    }

    fun clearItems() {
        tabs.clear()
        removeAllViews()
        tabViews.clear()
        currentIndex = 0
    }

    private fun rebuildTabs() {
        removeAllViews()
        tabViews.clear()
        tabs.forEachIndexed { index, item ->
            addTabView(item, index)
        }
        updateTabColors()
        post {
            if (indicatorStyle != IndicatorStyle.NONE) {
                moveIndicatorTo(currentIndex, false)
            }
        }
    }

    private fun addTabView(item: TabItem, index: Int) {
        val tabView = createTabView(item, index)
        val params = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
        if (index > 0) {
            (params as MarginLayoutParams).leftMargin = tabMargin.toInt()
        }
        addView(tabView, params)
        tabViews.add(tabView)
    }

    private fun createTabView(item: TabItem, index: Int): View {
        return when (tabMode) {
            TabMode.ICON_ONLY -> createIconOnlyTabView(item, index)
            TabMode.TEXT_ONLY -> createTextOnlyTabView(item, index)
            TabMode.ICON_TEXT -> createIconTextTabView(item, index)
        }.also { view ->
            view.setOnClickListener { handleTabClick(index) }
            view.setOnLongClickListener {
                tabLongClickListener?.invoke(index)
                true
            }
        }
    }

    private fun createIconOnlyTabView(item: TabItem, index: Int): View {
        val container = FrameLayout(context)
        container.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)

        val iconView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(iconSize.toInt(), iconSize.toInt()).apply {
                gravity = Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(item.icon ?: (if (item.iconRes != 0) ContextCompat.getDrawable(context, item.iconRes) else null))
            if (autoTintIcon) {
                imageTintList = ColorStateList.valueOf(normalColor)
            }
            tag = "icon"
        }
        container.addView(iconView)
        addBadgeToContainer(container, iconView, index)
        return container
    }

    private fun createTextOnlyTabView(item: TabItem, index: Int): View {
        return TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.CENTER
            }
            text = if (item.titleRes != 0) context.getString(item.titleRes) else item.title
            textSize = textSize.spToPx()
            setTextColor(normalColor)
            gravity = Gravity.CENTER
            maxLines = 1
            tag = "text"
        }
    }

    private fun createIconTextTabView(item: TabItem, index: Int): View {
        val container = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

        val iconContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        }

        val iconView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(iconSize.toInt(), iconSize.toInt()).apply {
                gravity = Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageDrawable(item.icon ?: (if (item.iconRes != 0) ContextCompat.getDrawable(context, item.iconRes) else null))
            if (autoTintIcon) {
                imageTintList = ColorStateList.valueOf(normalColor)
            }
            tag = "icon"
        }
        iconContainer.addView(iconView)
        container.addView(iconContainer)

        val titleText = if (item.titleRes != 0) context.getString(item.titleRes) else item.title
        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (2 * resources.displayMetrics.density).toInt()
                gravity = Gravity.CENTER
            }
            text = titleText
            textSize = textSize.spToPx()
            setTextColor(normalColor)
            gravity = Gravity.CENTER
            maxLines = 1
            tag = "text"
        }
        container.addView(textView)

        addBadgeToContainer(iconContainer, iconView, index)
        return container
    }

    private fun addBadgeToContainer(parent: ViewGroup, anchor: View, index: Int) {
        val density = resources.displayMetrics.density

        val badgeDot = View(context).apply {
            layoutParams = FrameLayout.LayoutParams((8 * density).toInt(), (8 * density).toInt()).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = (-2 * density).toInt()
                rightMargin = (-6 * density).toInt()
            }
            background = createBadgeDrawable()
            visibility = View.GONE
            tag = "badge_dot"
        }
        parent.addView(badgeDot)

        val badgeText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = (-2 * density).toInt()
                rightMargin = (-2 * density).toInt()
            }
            textSize = 9f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            visibility = View.GONE
            tag = "badge_text"
        }
        parent.addView(badgeText)
    }

    private fun createBadgeDrawable(): Drawable {
        val drawable = StateListDrawable()
        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.OVAL
        shape.setColor(badgeBackgroundColor)
        drawable.addState(intArrayOf(), shape)
        return drawable
    }

    private fun handleTabClick(index: Int) {
        if (index == currentIndex) {
            tabReselectedListener?.invoke(index)
            return
        }
        setCurrentIndex(index, true)
    }

    fun setCurrentIndex(index: Int, animate: Boolean = true) {
        if (index < 0 || index >= tabs.size) return
        currentIndex = index
        updateTabColors()
        if (indicatorStyle != IndicatorStyle.NONE) {
            moveIndicatorTo(index, animate)
        }
        boundViewPager?.setCurrentItem(index, animate)
        tabClickListener?.invoke(index)
    }

    fun getCurrentIndex(): Int = currentIndex

    private fun updateTabColors() {
        tabViews.forEachIndexed { index, view ->
            val isSelected = index == currentIndex
            updateViewColor(view, if (isSelected) selectedColor else normalColor, isSelected)
        }
    }

    private fun updateViewColor(view: View, color: Int, isSelected: Boolean) {
        when (view) {
            is ImageView -> {
                if (autoTintIcon) {
                    view.imageTintList = ColorStateList.valueOf(color)
                }
                if (isSelected && selectedScale != 1f) {
                    animateScale(view, selectedScale)
                } else if (!isSelected) {
                    animateScale(view, 1f)
                }
            }
            is TextView -> {
                view.setTextColor(color)
                if (isSelected && selectedScale != 1f) {
                    animateScale(view, selectedScale)
                } else if (!isSelected) {
                    animateScale(view, 1f)
                }
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    updateViewColor(view.getChildAt(i), color, isSelected)
                }
            }
        }
    }

    private fun animateScale(view: View, scale: Float) {
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(200)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun moveIndicatorTo(index: Int, animate: Boolean) {
        if (tabViews.isEmpty() || index < 0 || index >= tabViews.size) return
        val targetTab = tabViews[index]
        val targetLeft = targetTab.left.toFloat()
        val targetRight = targetTab.right.toFloat()

        val indicatorLeft: Float
        val indicatorRight: Float

        when (indicatorStyle) {
            IndicatorStyle.LINE -> {
                if (indicatorWidthFollowsText) {
                    val textView = findTextViewIn(targetTab)
                    if (textView != null) {
                        indicatorLeft = textView.left.toFloat() + targetTab.left
                        indicatorRight = textView.right.toFloat() + targetTab.left
                    } else {
                        val center = (targetLeft + targetRight) / 2
                        val halfWidth = (targetRight - targetLeft) * 0.4f
                        indicatorLeft = center - halfWidth
                        indicatorRight = center + halfWidth
                    }
                } else {
                    indicatorLeft = targetLeft
                    indicatorRight = targetRight
                }
            }
            IndicatorStyle.DOT -> {
                val center = (targetLeft + targetRight) / 2
                val dotSize = 6 * resources.displayMetrics.density
                indicatorLeft = center - dotSize / 2
                indicatorRight = center + dotSize / 2
            }
            IndicatorStyle.BLOCK -> {
                indicatorLeft = targetLeft + 4 * resources.displayMetrics.density
                indicatorRight = targetRight - 4 * resources.displayMetrics.density
            }
            IndicatorStyle.NONE -> return
        }

        val targetTop = height - indicatorHeight - indicatorMarginTop
        val targetBottom = height - indicatorMarginTop

        if (!animate) {
            indicatorRect.set(indicatorLeft, targetTop, indicatorRight, targetBottom)
            invalidate()
            return
        }

        indicatorAnimator?.cancel()
        val startRect = RectF(indicatorRect)
        if (startRect.isEmpty) {
            indicatorRect.set(indicatorLeft, targetTop, indicatorRight, targetBottom)
            invalidate()
            return
        }

        indicatorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = indicatorAnimatorDuration
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                indicatorRect.set(
                    lerp(startRect.left, indicatorLeft, fraction),
                    lerp(startRect.top, targetTop, fraction),
                    lerp(startRect.right, indicatorRight, fraction),
                    lerp(startRect.bottom, targetBottom, fraction)
                )
                invalidate()
            }
            start()
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    private fun findTextViewIn(view: View): TextView? {
        return when (view) {
            is TextView -> view
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    val found = findTextViewIn(view.getChildAt(i))
                    if (found != null) return found
                }
                null
            }
            else -> null
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (indicatorStyle != IndicatorStyle.NONE && !indicatorRect.isEmpty) {
            when (indicatorStyle) {
                IndicatorStyle.LINE -> {
                    val radius = indicatorHeight / 2
                    canvas.drawRoundRect(indicatorRect, radius, radius, indicatorPaint)
                }
                IndicatorStyle.DOT -> {
                    val cx = (indicatorRect.left + indicatorRect.right) / 2
                    val cy = (indicatorRect.top + indicatorRect.bottom) / 2
                    val radius = (indicatorRect.right - indicatorRect.left) / 2
                    canvas.drawCircle(cx, cy, radius, indicatorPaint)
                }
                IndicatorStyle.BLOCK -> {
                    val radius = 8 * resources.displayMetrics.density
                    canvas.drawRoundRect(indicatorRect, radius, radius, indicatorPaint)
                }
                IndicatorStyle.NONE -> {}
            }
        }
        super.dispatchDraw(canvas)
    }

    fun bindViewPager(viewPager: ViewPager2) {
        unbindViewPager()
        boundViewPager = viewPager

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            private var lastState = ViewPager2.SCROLL_STATE_IDLE

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (lastState == ViewPager2.SCROLL_STATE_DRAGGING && positionOffset != 0f) {
                    val from = currentIndex
                    val to = if (positionOffset < 0.5f) position else position + 1
                    if (from != to && to in tabs.indices) {
                        animateIndicatorScroll(from, to, positionOffset)
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                currentIndex = position
                updateTabColors()
                if (indicatorStyle != IndicatorStyle.NONE) {
                    moveIndicatorTo(position, true)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                lastState = state
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    currentIndex = viewPager.currentItem
                    updateTabColors()
                    if (indicatorStyle != IndicatorStyle.NONE) {
                        moveIndicatorTo(currentIndex, false)
                    }
                }
            }
        }
        viewPager.registerOnPageChangeCallback(pageChangeCallback!!)
    }

    fun bindFragments(activity: FragmentActivity, fragments: List<Fragment>) {
        val fragmentAdapter = object : FragmentStateAdapter(activity) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        val vp = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            adapter = fragmentAdapter
        }
        bindViewPager(vp)
    }

    private var pagerAdapter: RecyclerView.Adapter<*>? = null

    fun bindAdapter(adapter: RecyclerView.Adapter<*>) {
        this.pagerAdapter = adapter
    }

    private fun animateIndicatorScroll(fromIndex: Int, toIndex: Int, offset: Float) {
        if (tabViews.isEmpty() || fromIndex !in tabViews.indices || toIndex !in tabViews.indices) return

        val fromTab = tabViews[fromIndex]
        val toTab = tabViews[toIndex]

        val fromLeft = fromTab.left.toFloat()
        val toLeft = toTab.left.toFloat()
        val fromRight = fromTab.right.toFloat()
        val toRight = toTab.right.toFloat()

        val targetTop = height - indicatorHeight - indicatorMarginTop
        val targetBottom = height - indicatorMarginTop

        val currentLeft = fromLeft + (toLeft - fromLeft) * offset
        val currentRight = fromRight + (toRight - fromRight) * offset

        indicatorRect.set(currentLeft, targetTop, currentRight, targetBottom)
        invalidate()
    }

    fun unbindViewPager() {
        pageChangeCallback?.let { callback ->
            boundViewPager?.unregisterOnPageChangeCallback(callback)
        }
        boundViewPager = null
        pageChangeCallback = null
    }

    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        tabClickListener = listener
    }

    fun setOnTabReselectedListener(listener: (Int) -> Unit) {
        tabReselectedListener = listener
    }

    fun setOnTabLongClickListener(listener: (Int) -> Unit) {
        tabLongClickListener = listener
    }

    fun setBadgeCount(index: Int, count: Int) {
        if (index < 0 || index >= tabViews.size) return
        badgeCounts[index] = count
        updateBadgeInView(tabViews[index], count, badgeTexts[index])
    }

    fun setBadgeText(index: Int, text: String?) {
        if (index < 0 || index >= tabViews.size) return
        if (text != null) badgeTexts[index] = text else badgeTexts.remove(index)
        updateBadgeInView(tabViews[index], badgeCounts[index] ?: 0, text)
    }

    fun clearBadge(index: Int) {
        if (index < 0 || index >= tabViews.size) return
        badgeCounts.remove(index)
        badgeTexts.remove(index)
        hideBadgeInView(tabViews[index])
    }

    private fun updateBadgeInView(tabView: View, count: Int, text: String?) {
        val badgeDot = findViewWithTagInView(tabView, "badge_dot") as? View
        val badgeTextView = findViewWithTagInView(tabView, "badge_text") as? TextView

        when {
            text != null -> {
                badgeTextView?.text = text
                badgeTextView?.visibility = View.VISIBLE
                badgeDot?.visibility = View.VISIBLE
                updateBadgeSize(badgeDot, badgeTextView, true)
            }
            count > 0 -> {
                badgeTextView?.text = if (count > 99) "99+" else count.toString()
                badgeTextView?.visibility = View.VISIBLE
                badgeDot?.visibility = View.VISIBLE
                updateBadgeSize(badgeDot, badgeTextView, count > 9)
            }
            else -> {
                hideBadgeInView(tabView)
            }
        }
    }

    private fun hideBadgeInView(tabView: View) {
        findViewWithTagInView(tabView, "badge_dot")?.visibility = View.GONE
        (findViewWithTagInView(tabView, "badge_text") as? TextView)?.visibility = View.GONE
    }

    private fun findViewWithTagInView(view: View, tag: String): View? {
        if (view.tag == tag) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findViewWithTagInView(view.getChildAt(i), tag)
                if (found != null) return found
            }
        }
        return null
    }

    private fun updateBadgeSize(badgeDot: View?, badgeText: TextView?, large: Boolean) {
        val density = resources.displayMetrics.density
        val baseSize = (8 * density).toInt()
        val largeSize = (16 * density).toInt()
        val size = if (large) largeSize else baseSize
        badgeDot?.let {
            val params = it.layoutParams
            params.width = size
            params.height = size
            it.layoutParams = params
        }
        badgeText?.textSize = if (large) 10f else 8f
    }

    override fun onDetachedFromWindow() {
        indicatorAnimator?.cancel()
        unbindViewPager()
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("currentIndex", currentIndex)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            currentIndex = state.getInt("currentIndex", 0)
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onInitializeAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = "AwBottomTabBar"
        event.itemCount = tabs.size
        event.currentItemIndex = currentIndex
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density
    private fun Float.sp(): Float = this * resources.displayMetrics.scaledDensity
    private fun Float.spToPx(): Float = this / resources.displayMetrics.scaledDensity
}
