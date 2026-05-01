package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R
import com.answufeng.ui.dp
import com.answufeng.ui.dpFloat
import androidx.core.content.ContextCompat

class AwBannerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private enum class PagerEngine {
            VIEW_PAGER,
            VIEW_PAGER2,
        }

        private val handler = Handler(Looper.getMainLooper())
        private lateinit var indicatorContainer: LinearLayout
        private lateinit var viewPager2: ViewPager2
        /** 僅在使用 [setPagerAdapter] 時建立，避免預設路徑同時掛載兩套 Pager。 */
        private var legacyViewPager: ViewPager? = null

        private var pagerEngine = PagerEngine.VIEW_PAGER2
        private var lifecycleObserver: LifecycleEventObserver? = null
        private var wasAutoScrollingBeforePause = false
        private var normalDotDrawable: GradientDrawable? = null
        private var selectedDotDrawable: GradientDrawable? = null
        private var realItemCount = 0
        private var pageClickListener: ((Int) -> Unit)? = null
        private var indicatorClickListener: ((Int) -> Unit)? = null

        var interval: Long = 3000L
            set(value) {
                field = value.coerceAtLeast(1000L)
                restartAutoScrollIfNeeded()
            }

        var isAutoScrolling: Boolean = false
            private set

        var autoStart: Boolean = true

        var isInfiniteLoop: Boolean = true
            set(value) {
                if (field == value) return
                field = value
                when (pagerEngine) {
                    PagerEngine.VIEW_PAGER2 -> viewPager2.adapter?.let { setAdapter(it, realItemCount) }
                    PagerEngine.VIEW_PAGER -> legacyViewPager?.adapter?.let { setPagerAdapter(it, realItemCount) }
                }
            }

        var showIndicators: Boolean = true
            set(value) {
                if (field == value) return
                field = value
                updateIndicatorVisibility()
            }

        var indicatorColor: Int = 0x80FFFFFF.toInt()
            set(value) {
                field = value
                normalDotDrawable = null
                updateIndicatorDots()
            }

        var indicatorSelectedColor: Int = Color.WHITE
            set(value) {
                field = value
                selectedDotDrawable = null
                updateIndicatorDots()
            }

        var indicatorSize: Float = 6f.dpFloat
            set(value) {
                field = value
                createIndicatorDots()
            }

        var indicatorSpacing: Float = 4f.dpFloat
            set(value) {
                field = value
                createIndicatorDots()
            }

        enum class IndicatorShape { CIRCLE, RECT }

        var indicatorShape: IndicatorShape = IndicatorShape.CIRCLE
            set(value) {
                field = value
                normalDotDrawable = null
                selectedDotDrawable = null
                createIndicatorDots()
            }

        enum class IndicatorGravity { CENTER, START, END }

        var indicatorGravity: IndicatorGravity = IndicatorGravity.CENTER
            set(value) {
                field = value
                updateIndicatorContainerGravity()
            }

        var indicatorMarginBottom: Float = 12f.dpFloat
            set(value) {
                field = value
                (indicatorContainer.layoutParams as? LayoutParams)?.bottomMargin = value.toInt()
            }

        private val autoScrollRunnable =
            object : Runnable {
                override fun run() {
                    if (isAutoScrolling && realItemCount > 1) {
                        val shouldScheduleNext = when (pagerEngine) {
                            PagerEngine.VIEW_PAGER2 -> {
                                viewPager2.setCurrentItem(viewPager2.currentItem + 1, true)
                                true
                            }
                            PagerEngine.VIEW_PAGER -> {
                                val lp = legacyViewPager
                                if (lp != null) {
                                    lp.currentItem = lp.currentItem + 1
                                    true
                                } else {
                                    false
                                }
                            }
                        }
                        if (shouldScheduleNext) {
                            handler.postDelayed(this, interval)
                        }
                    }
                }
            }

        private val pageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIndicatorDots(toRealPosition(position))
                }
            }

        private val legacyPageChangeListener =
            object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    updateIndicatorDots(toRealPosition(position))
                }
            }

        init {
            viewPager2 =
                ViewPager2(context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    offscreenPageLimit = 1
                    registerOnPageChangeCallback(pageChangeCallback)
                }
            addView(viewPager2)

            indicatorContainer =
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutParams =
                        LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                            bottomMargin = 12.dp
                        }
                }
            addView(indicatorContainer)

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBannerView)
            interval = ta.getInteger(R.styleable.AwBannerView_banner_interval, 3000).toLong()
            indicatorColor = ta.getColor(R.styleable.AwBannerView_banner_indicatorColor, ContextCompat.getColor(context, R.color.aw_color_banner_indicator))
            indicatorSelectedColor = ta.getColor(R.styleable.AwBannerView_banner_indicatorSelectedColor, Color.WHITE)
            showIndicators = ta.getBoolean(R.styleable.AwBannerView_banner_showIndicators, true)
            autoStart = ta.getBoolean(R.styleable.AwBannerView_banner_autoStart, true)
            isInfiniteLoop = ta.getBoolean(R.styleable.AwBannerView_banner_infiniteLoop, true)
            indicatorSize = ta.getDimension(R.styleable.AwBannerView_banner_indicatorSize, 6f.dpFloat)
            indicatorSpacing = ta.getDimension(R.styleable.AwBannerView_banner_indicatorSpacing, 4f.dpFloat)
            indicatorShape =
                when (ta.getInt(R.styleable.AwBannerView_banner_indicatorShape, 0)) {
                    1 -> IndicatorShape.RECT
                    else -> IndicatorShape.CIRCLE
                }
            indicatorGravity =
                when (ta.getInt(R.styleable.AwBannerView_banner_indicatorGravity, 0)) {
                    1 -> IndicatorGravity.START
                    2 -> IndicatorGravity.END
                    else -> IndicatorGravity.CENTER
                }
            indicatorMarginBottom = ta.getDimension(R.styleable.AwBannerView_banner_indicatorMarginBottom, 12f.dpFloat)
            ta.recycle()
        }

        private fun ensureLegacyViewPager(): ViewPager {
            legacyViewPager?.let { return it }
            val vp =
                ViewPager(context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    visibility = GONE
                    addOnPageChangeListener(legacyPageChangeListener)
                }
            addView(vp, 1)
            legacyViewPager = vp
            return vp
        }

        @JvmOverloads
        fun setAdapter(
            adapter: RecyclerView.Adapter<*>,
            knownItemCount: Int? = null,
        ) {
            stopAutoScroll()
            pagerEngine = PagerEngine.VIEW_PAGER2
            viewPager2.visibility = VISIBLE
            legacyViewPager?.visibility = GONE
            viewPager2.adapter = adapter
            realItemCount = resolveRealCount(adapter.itemCount, knownItemCount)
            createIndicatorDots()
            moveToInitialPosition()
            if (autoStart && realItemCount > 1) startAutoScroll()
        }

        @JvmOverloads
        fun setPagerAdapter(
            adapter: PagerAdapter,
            knownItemCount: Int? = null,
        ) {
            stopAutoScroll()
            pagerEngine = PagerEngine.VIEW_PAGER
            val vp = ensureLegacyViewPager()
            vp.visibility = VISIBLE
            viewPager2.visibility = GONE
            vp.adapter = adapter
            realItemCount = resolveRealCount(adapter.count, knownItemCount)
            createIndicatorDots()
            moveToInitialPosition()
            if (autoStart && realItemCount > 1) startAutoScroll()
        }

        fun <T> setData(
            items: List<T>,
            bind: (ViewGroup, T, Int) -> Unit,
        ) {
            val adapter =
                object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int,
                    ): RecyclerView.ViewHolder {
                        val container =
                            FrameLayout(parent.context).apply {
                                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                            }
                        return object : RecyclerView.ViewHolder(container) {}
                    }

                    override fun onBindViewHolder(
                        holder: RecyclerView.ViewHolder,
                        position: Int,
                    ) {
                        val container = holder.itemView as FrameLayout
                        container.removeAllViews()
                        val realPosition = toRealPosition(position)
                        bind(container, items[realPosition], realPosition)
                        container.setOnClickListener { pageClickListener?.invoke(realPosition) }
                    }

                    override fun getItemCount(): Int {
                        return if (isInfiniteLoop && items.size > 1) Int.MAX_VALUE else items.size
                    }
                }
            setAdapter(adapter, items.size)
        }

        fun setCurrentItem(
            index: Int,
            smoothScroll: Boolean = true,
        ) {
            if (realItemCount == 0 || index !in 0 until realItemCount) return
            when (pagerEngine) {
                PagerEngine.VIEW_PAGER2 -> {
                    val target =
                        if (isInfiniteLoop && realItemCount > 1) {
                            val base = viewPager2.currentItem - (viewPager2.currentItem % realItemCount)
                            base + index
                        } else {
                            index
                        }
                    viewPager2.setCurrentItem(target, smoothScroll)
                }
                PagerEngine.VIEW_PAGER -> {
                    val vp = legacyViewPager ?: return
                    val target =
                        if (isInfiniteLoop && realItemCount > 1) {
                            val base = vp.currentItem - (vp.currentItem % realItemCount)
                            base + index
                        } else {
                            index
                        }
                    vp.setCurrentItem(target, smoothScroll)
                }
            }
        }

        fun getCurrentRealItem(): Int {
            val current =
                when (pagerEngine) {
                    PagerEngine.VIEW_PAGER2 -> viewPager2.currentItem
                    PagerEngine.VIEW_PAGER -> legacyViewPager?.currentItem ?: 0
                }
            return toRealPosition(current)
        }

        fun setOnPageClickListener(listener: (Int) -> Unit) {
            pageClickListener = listener
        }

        fun setOnIndicatorClickListener(listener: (Int) -> Unit) {
            indicatorClickListener = listener
        }

        fun startAutoScroll() {
            if (isAutoScrolling || realItemCount <= 1) return
            isAutoScrolling = true
            handler.removeCallbacks(autoScrollRunnable)
            handler.postDelayed(autoScrollRunnable, interval)
        }

        fun stopAutoScroll() {
            isAutoScrolling = false
            handler.removeCallbacks(autoScrollRunnable)
        }

        fun toRealPosition(position: Int): Int {
            if (realItemCount <= 0) return 0
            return if (isInfiniteLoop) position % realItemCount else position.coerceIn(0, realItemCount - 1)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            bindLifecycle()
            restartAutoScrollIfNeeded()
        }

        override fun onDetachedFromWindow() {
            stopAutoScroll()
            unbindLifecycle()
            super.onDetachedFromWindow()
        }

        private fun resolveRealCount(
            rawCount: Int,
            knownItemCount: Int?,
        ): Int {
            return when {
                knownItemCount != null -> knownItemCount
                rawCount in 0..500_000 -> rawCount
                else -> 0
            }
        }

        private fun restartAutoScrollIfNeeded() {
            if (isAutoScrolling) {
                stopAutoScroll()
                startAutoScroll()
            }
        }

        private fun moveToInitialPosition() {
            if (realItemCount <= 1) {
                when (pagerEngine) {
                    PagerEngine.VIEW_PAGER2 -> viewPager2.setCurrentItem(0, false)
                    PagerEngine.VIEW_PAGER -> legacyViewPager?.setCurrentItem(0, false)
                }
                return
            }
            val start =
                if (isInfiniteLoop) {
                    val middle = Int.MAX_VALUE / 2
                    middle - (middle % realItemCount)
                } else {
                    0
                }
            when (pagerEngine) {
                PagerEngine.VIEW_PAGER2 -> viewPager2.setCurrentItem(start, false)
                PagerEngine.VIEW_PAGER -> legacyViewPager?.setCurrentItem(start, false)
            }
        }

        private fun createIndicatorDots() {
            indicatorContainer.removeAllViews()
            normalDotDrawable = null
            selectedDotDrawable = null
            updateIndicatorVisibility()
            if (realItemCount <= 0) return

            val dotSize = indicatorSize.toInt()
            val margin = indicatorSpacing.toInt()
            repeat(realItemCount) { index ->
                val dot =
                    ImageView(context).apply {
                        layoutParams =
                            LinearLayout.LayoutParams(dotSize, dotSize).apply {
                                marginStart = margin
                                marginEnd = margin
                            }
                        setImageDrawable(getDotDrawable(index == 0))
                        setOnClickListener {
                            setCurrentItem(index, true)
                            indicatorClickListener?.invoke(index)
                        }
                    }
                indicatorContainer.addView(dot)
            }
        }

        private fun updateIndicatorVisibility() {
            indicatorContainer.visibility = if (showIndicators && realItemCount > 1) VISIBLE else GONE
        }

        private fun updateIndicatorDots(selectedPosition: Int = getCurrentRealItem()) {
            for (i in 0 until indicatorContainer.childCount) {
                (indicatorContainer.getChildAt(i) as? ImageView)?.setImageDrawable(getDotDrawable(i == selectedPosition))
            }
        }

        private fun getDotDrawable(selected: Boolean): GradientDrawable {
            return if (selected) {
                selectedDotDrawable ?: createDotDrawable(true).also { selectedDotDrawable = it }
            } else {
                normalDotDrawable ?: createDotDrawable(false).also { normalDotDrawable = it }
            }
        }

        private fun createDotDrawable(selected: Boolean): GradientDrawable {
            val size = indicatorSize.toInt()
            return GradientDrawable().apply {
                if (indicatorShape == IndicatorShape.RECT) {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = size / 2f
                    val width = if (selected) size * 2 else size
                    setSize(width, size)
                } else {
                    shape = GradientDrawable.OVAL
                    setSize(size, size)
                }
                setColor(if (selected) indicatorSelectedColor else indicatorColor)
            }
        }

        private fun updateIndicatorContainerGravity() {
            val lp = indicatorContainer.layoutParams as? LayoutParams ?: return
            lp.gravity = Gravity.BOTTOM or
                when (indicatorGravity) {
                    IndicatorGravity.START -> Gravity.START
                    IndicatorGravity.END -> Gravity.END
                    IndicatorGravity.CENTER -> Gravity.CENTER_HORIZONTAL
                }
            indicatorContainer.layoutParams = lp
        }

        private fun bindLifecycle() {
            unbindLifecycle()
            val owner = findViewTreeLifecycleOwner() ?: return
            lifecycleObserver =
                LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> {
                            wasAutoScrollingBeforePause = isAutoScrolling
                            stopAutoScroll()
                        }
                        Lifecycle.Event.ON_RESUME -> {
                            if (wasAutoScrollingBeforePause && autoStart) startAutoScroll()
                        }
                        else -> Unit
                    }
                }
            owner.lifecycle.addObserver(lifecycleObserver!!)
        }

        private fun unbindLifecycle() {
            lifecycleObserver?.let { observer ->
                findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(observer)
            }
            lifecycleObserver = null
        }
    }
