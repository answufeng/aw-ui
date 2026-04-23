package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R

/**
 * 轮播图视图，基于 ViewPager2 实现。
 *
 * 支持无限循环、自动滚动、指示器、生命周期感知（自动暂停/恢复）。
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.widget.AwBannerView
 *     android:layout_width="match_parent"
 *     android:layout_height="200dp"
 *     app:banner_interval="3000"
 *     app:banner_indicatorColor="#80FFFFFF"
 *     app:banner_indicatorSelectedColor="#FFFFFF" />
 * ```
 *
 * ### 代码用法
 * ```kotlin
 * bannerView.setData(items) { view, item, position ->
 *     val iv = ImageView(context)
 *     Glide.with(iv).load(item.url).into(iv)
 *     view.addView(iv)
 * }
 * bannerView.startAutoScroll()
 * ```
 */
class AwBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var viewPager: ViewPager2
    private val indicatorContainer: LinearLayout

    var interval: Long = 3000L
        set(value) {
            field = value
            if (isAutoScrolling) {
                stopAutoScroll()
                startAutoScroll()
            }
        }

    var isAutoScrolling: Boolean = false
        private set

    var indicatorColor: Int = Color.parseColor("#80FFFFFF")
        set(value) {
            field = value
            normalDotDrawable = null
            updateIndicatorDots()
        }

    var indicatorSelectedColor: Int = Color.parseColor("#FFFFFF")
        set(value) {
            field = value
            selectedDotDrawable = null
            updateIndicatorDots()
        }

    var isInfiniteLoop: Boolean = true

    private val handler = Handler(Looper.getMainLooper())
    private var realItemCount: Int = 0
    private var pageClickListener: ((Int) -> Unit)? = null
    private var indicatorClickListener: ((Int) -> Unit)? = null
    private var lifecycleObserver: LifecycleEventObserver? = null
    private var wasAutoScrollingBeforePause: Boolean = false
    private var normalDotDrawable: android.graphics.drawable.GradientDrawable? = null
    private var selectedDotDrawable: android.graphics.drawable.GradientDrawable? = null

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (isAutoScrolling && realItemCount > 1) {
                val next = viewPager.currentItem + 1
                viewPager.setCurrentItem(next, true)
            }
            handler.postDelayed(this, interval)
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateIndicatorDots(toRealPosition(position))
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBannerView)
        interval = ta.getInteger(R.styleable.AwBannerView_banner_interval, 3000).toLong()
        val xmlIndicatorColor = ta.getColor(
            R.styleable.AwBannerView_banner_indicatorColor,
            Color.parseColor("#80FFFFFF")
        )
        val xmlIndicatorSelectedColor = ta.getColor(
            R.styleable.AwBannerView_banner_indicatorSelectedColor,
            Color.parseColor("#FFFFFF")
        )
        ta.recycle()

        viewPager = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            offscreenPageLimit = 1
            registerOnPageChangeCallback(pageChangeCallback)
        }
        addView(viewPager)

        indicatorContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            lp.bottomMargin = 12
            layoutParams = lp
        }
        addView(indicatorContainer)

        // 须先完成 viewPager / indicator 再设颜色，否则 setter 会调 updateIndicatorDots 访问未初始化的 viewPager
        indicatorColor = xmlIndicatorColor
        indicatorSelectedColor = xmlIndicatorSelectedColor
    }

    /**
     * @param knownItemCount 当 [RecyclerView.Adapter.getItemCount] 为 [Int.MAX_VALUE] 等“虚假”大数时（如内部无限轮播），必须显式传入真实条数
     */
    @JvmOverloads
    fun setAdapter(adapter: RecyclerView.Adapter<*>, knownItemCount: Int? = null) {
        viewPager.adapter = adapter
        val raw = adapter.itemCount
        realItemCount = when {
            knownItemCount != null -> knownItemCount
            raw in 0..500_000 -> raw
            else -> 0
        }
        createIndicatorDots()
        if (isInfiniteLoop && realItemCount > 1) {
            val mid = Int.MAX_VALUE / 2
            val startPos = mid - (mid % realItemCount)
            viewPager.setCurrentItem(startPos, false)
        }
    }

    fun <T> setData(
        items: List<T>,
        bind: (android.view.View, T, Int) -> Unit
    ) {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val frameLayout = android.widget.FrameLayout(parent.context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                return object : RecyclerView.ViewHolder(frameLayout) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val container = holder.itemView as android.widget.FrameLayout
                container.removeAllViews()
                val realPos = toRealPosition(position)
                bind(container, items[realPos], realPos)
            }

            override fun getItemCount(): Int {
                return if (isInfiniteLoop && items.size > 1) Int.MAX_VALUE else items.size
            }
        }
        setAdapter(adapter, items.size)
    }

    fun setOnPageClickListener(listener: (Int) -> Unit) {
        pageClickListener = listener
    }

    fun setOnIndicatorClickListener(listener: (Int) -> Unit) {
        indicatorClickListener = listener
    }

    fun startAutoScroll() {
        if (isAutoScrolling) return
        isAutoScrolling = true
        handler.postDelayed(autoScrollRunnable, interval)
    }

    fun stopAutoScroll() {
        isAutoScrolling = false
        handler.removeCallbacks(autoScrollRunnable)
    }

    fun toRealPosition(position: Int): Int {
        if (realItemCount == 0) return 0
        return if (isInfiniteLoop) position % realItemCount else position
    }

    private fun createIndicatorDots() {
        indicatorContainer.removeAllViews()
        normalDotDrawable = null
        selectedDotDrawable = null
        val dotSize = (6 * resources.displayMetrics.density).toInt()
        val dotMargin = (4 * resources.displayMetrics.density).toInt()

        for (i in 0 until realItemCount) {
            val dot = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    leftMargin = dotMargin
                    rightMargin = dotMargin
                }
                setImageDrawable(getDotDrawable(i == 0))
                setOnClickListener {
                    val target = if (isInfiniteLoop) {
                        viewPager.currentItem - (viewPager.currentItem % realItemCount) + i
                    } else {
                        i
                    }
                    viewPager.setCurrentItem(target, true)
                    indicatorClickListener?.invoke(i)
                }
            }
            indicatorContainer.addView(dot)
        }
    }

    private fun updateIndicatorDots(selectedPosition: Int = toRealPosition(viewPager.currentItem)) {
        for (i in 0 until indicatorContainer.childCount) {
            val dot = indicatorContainer.getChildAt(i) as? ImageView ?: continue
            dot.setImageDrawable(getDotDrawable(i == selectedPosition))
        }
    }

    private fun getDotDrawable(isSelected: Boolean): android.graphics.drawable.GradientDrawable {
        if (isSelected) {
            return selectedDotDrawable ?: createDotDrawable(true).also { selectedDotDrawable = it }
        }
        return normalDotDrawable ?: createDotDrawable(false).also { normalDotDrawable = it }
    }

    private fun createDotDrawable(isSelected: Boolean): android.graphics.drawable.GradientDrawable {
        val density = resources.displayMetrics.density
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            color = android.content.res.ColorStateList.valueOf(if (isSelected) indicatorSelectedColor else indicatorColor)
            val size = (6 * density).toInt()
            setSize(size, size)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindLifecycle()
        if (isAutoScrolling) {
            handler.removeCallbacks(autoScrollRunnable)
            handler.postDelayed(autoScrollRunnable, interval)
        }
    }

    override fun onDetachedFromWindow() {
        stopAutoScroll()
        unbindLifecycle()
        super.onDetachedFromWindow()
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
                    if (wasAutoScrollingBeforePause) {
                        startAutoScroll()
                    }
                }
                else -> {}
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
