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

class AwBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private enum class PagerEngine {
        VIEW_PAGER, VIEW_PAGER2
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPager: ViewPager

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
                PagerEngine.VIEW_PAGER -> viewPager.adapter?.let { setPagerAdapter(it, realItemCount) }
            }
        }

    var showIndicators: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            updateIndicatorVisibility()
        }

    var indicatorColor: Int = Color.parseColor("#80FFFFFF")
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

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (isAutoScrolling && realItemCount > 1) {
                when (pagerEngine) {
                    PagerEngine.VIEW_PAGER2 -> viewPager2.setCurrentItem(viewPager2.currentItem + 1, true)
                    PagerEngine.VIEW_PAGER -> viewPager.currentItem = viewPager.currentItem + 1
                }
                handler.postDelayed(this, interval)
            }
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateIndicatorDots(toRealPosition(position))
        }
    }

    private val legacyPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateIndicatorDots(toRealPosition(position))
        }
    }

    init {
        viewPager2 = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            offscreenPageLimit = 1
            registerOnPageChangeCallback(pageChangeCallback)
        }
        addView(viewPager2)

        viewPager = ViewPager(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            visibility = GONE
            addOnPageChangeListener(legacyPageChangeListener)
        }
        addView(viewPager)

        indicatorContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 12f.dp().toInt()
            }
        }
        addView(indicatorContainer)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBannerView)
        interval = ta.getInteger(R.styleable.AwBannerView_banner_interval, 3000).toLong()
        indicatorColor = ta.getColor(R.styleable.AwBannerView_banner_indicatorColor, Color.parseColor("#80FFFFFF"))
        indicatorSelectedColor = ta.getColor(R.styleable.AwBannerView_banner_indicatorSelectedColor, Color.WHITE)
        showIndicators = ta.getBoolean(R.styleable.AwBannerView_banner_showIndicators, true)
        autoStart = ta.getBoolean(R.styleable.AwBannerView_banner_autoStart, true)
        isInfiniteLoop = ta.getBoolean(R.styleable.AwBannerView_banner_infiniteLoop, true)
        ta.recycle()
    }

    @JvmOverloads
    fun setAdapter(adapter: RecyclerView.Adapter<*>, knownItemCount: Int? = null) {
        stopAutoScroll()
        pagerEngine = PagerEngine.VIEW_PAGER2
        viewPager2.visibility = VISIBLE
        viewPager.visibility = GONE
        viewPager2.adapter = adapter
        realItemCount = resolveRealCount(adapter.itemCount, knownItemCount)
        createIndicatorDots()
        moveToInitialPosition()
        if (autoStart && realItemCount > 1) startAutoScroll()
    }

    @JvmOverloads
    fun setPagerAdapter(adapter: PagerAdapter, knownItemCount: Int? = null) {
        stopAutoScroll()
        pagerEngine = PagerEngine.VIEW_PAGER
        viewPager.visibility = VISIBLE
        viewPager2.visibility = GONE
        viewPager.adapter = adapter
        realItemCount = resolveRealCount(adapter.count, knownItemCount)
        createIndicatorDots()
        moveToInitialPosition()
        if (autoStart && realItemCount > 1) startAutoScroll()
    }

    fun <T> setData(items: List<T>, bind: (ViewGroup, T, Int) -> Unit) {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val container = FrameLayout(parent.context).apply {
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                }
                return object : RecyclerView.ViewHolder(container) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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

    fun setCurrentItem(index: Int, smoothScroll: Boolean = true) {
        if (realItemCount == 0 || index !in 0 until realItemCount) return
        when (pagerEngine) {
            PagerEngine.VIEW_PAGER2 -> {
                val target = if (isInfiniteLoop && realItemCount > 1) {
                    val base = viewPager2.currentItem - (viewPager2.currentItem % realItemCount)
                    base + index
                } else {
                    index
                }
                viewPager2.setCurrentItem(target, smoothScroll)
            }
            PagerEngine.VIEW_PAGER -> {
                val target = if (isInfiniteLoop && realItemCount > 1) {
                    val base = viewPager.currentItem - (viewPager.currentItem % realItemCount)
                    base + index
                } else {
                    index
                }
                viewPager.setCurrentItem(target, smoothScroll)
            }
        }
    }

    fun getCurrentRealItem(): Int {
        val current = when (pagerEngine) {
            PagerEngine.VIEW_PAGER2 -> viewPager2.currentItem
            PagerEngine.VIEW_PAGER -> viewPager.currentItem
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

    private fun resolveRealCount(rawCount: Int, knownItemCount: Int?): Int {
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
                PagerEngine.VIEW_PAGER -> viewPager.setCurrentItem(0, false)
            }
            return
        }
        val start = if (isInfiniteLoop) {
            val middle = Int.MAX_VALUE / 2
            middle - (middle % realItemCount)
        } else {
            0
        }
        when (pagerEngine) {
            PagerEngine.VIEW_PAGER2 -> viewPager2.setCurrentItem(start, false)
            PagerEngine.VIEW_PAGER -> viewPager.setCurrentItem(start, false)
        }
    }

    private fun createIndicatorDots() {
        indicatorContainer.removeAllViews()
        normalDotDrawable = null
        selectedDotDrawable = null
        updateIndicatorVisibility()
        if (realItemCount <= 0) return

        val dotSize = 6f.dp().toInt()
        val margin = 4f.dp().toInt()
        repeat(realItemCount) { index ->
            val dot = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
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
        val size = 6f.dp().toInt()
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setSize(size, size)
            setColor(if (selected) indicatorSelectedColor else indicatorColor)
        }
    }

    private fun bindLifecycle() {
        unbindLifecycle()
        val owner = findViewTreeLifecycleOwner() ?: return
        lifecycleObserver = LifecycleEventObserver { _, event ->
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

    private fun Float.dp(): Float = this * resources.displayMetrics.density
}
