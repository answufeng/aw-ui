package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R

/**
 * Banner/carousel view with auto-scroll and indicator dots.
 *
 * Wraps a [ViewPager2] internally and provides auto-scrolling, indicator dots,
 * and page click handling. The indicator dots are displayed at the bottom center.
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwBannerView
 *     android:layout_width="match_parent"
 *     android:layout_height="200dp"
 *     app:banner_interval="3000"
 *     app:banner_indicatorColor="#80FFFFFF"
 *     app:banner_indicatorSelectedColor="#FFFFFF" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * bannerView.setAdapter(myAdapter)
 * bannerView.setOnPageClickListener { position -> ... }
 * bannerView.startAutoScroll()
 * ```
 *
 * @property interval Auto-scroll interval in milliseconds. Default 3000.
 * @property isAutoScrolling Whether auto-scroll is currently active.
 *
 * | XML Attribute | Description | Default |
 * |---|---|---|
 * | `banner_interval` | Auto-scroll interval (ms) | 3000 |
 * | `banner_indicatorColor` | Dot color (unselected) | #80FFFFFF |
 * | `banner_indicatorSelectedColor` | Dot color (selected) | #FFFFFF |
 */
class AwBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var viewPager: ViewPager2
    private val indicatorContainer: LinearLayout

    /**
     * Auto-scroll interval in milliseconds.
     */
    var interval: Long = 3000L
        set(value) {
            field = value
            if (isAutoScrolling) {
                stopAutoScroll()
                startAutoScroll()
            }
        }

    /**
     * Whether auto-scroll is currently active.
     */
    var isAutoScrolling: Boolean = false
        private set

    /**
     * Color for unselected indicator dots.
     */
    var indicatorColor: Int = Color.parseColor("#80FFFFFF")
        set(value) {
            field = value
            updateIndicatorDots()
        }

    /**
     * Color for the selected indicator dot.
     */
    var indicatorSelectedColor: Int = Color.parseColor("#FFFFFF")
        set(value) {
            field = value
            updateIndicatorDots()
        }

    private val handler = Handler(Looper.getMainLooper())
    private var itemCount: Int = 0
    private var pageClickListener: ((Int) -> Unit)? = null

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (isAutoScrolling && itemCount > 1) {
                val next = (viewPager.currentItem + 1) % itemCount
                viewPager.setCurrentItem(next, true)
            }
            handler.postDelayed(this, interval)
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateIndicatorDots(position)
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBannerView)
        interval = ta.getInteger(R.styleable.AwBannerView_banner_interval, 3000).toLong()
        indicatorColor = ta.getColor(R.styleable.AwBannerView_banner_indicatorColor, Color.parseColor("#80FFFFFF"))
        indicatorSelectedColor = ta.getColor(R.styleable.AwBannerView_banner_indicatorSelectedColor, Color.parseColor("#FFFFFF"))
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
    }

    /**
     * Sets the adapter for the internal [ViewPager2].
     *
     * @param adapter The [RecyclerView.Adapter] to supply pages.
     */
    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        viewPager.adapter = adapter
        itemCount = adapter.itemCount
        createIndicatorDots()
    }

    /**
     * Sets a click listener for banner pages.
     *
     * @param listener Lambda receiving the clicked page position.
     */
    fun setOnPageClickListener(listener: (Int) -> Unit) {
        pageClickListener = listener
    }

    /**
     * Starts auto-scrolling to the next page at the configured [interval].
     */
    fun startAutoScroll() {
        if (isAutoScrolling) return
        isAutoScrolling = true
        handler.postDelayed(autoScrollRunnable, interval)
    }

    /**
     * Stops auto-scrolling.
     */
    fun stopAutoScroll() {
        isAutoScrolling = false
        handler.removeCallbacks(autoScrollRunnable)
    }

    private fun createIndicatorDots() {
        indicatorContainer.removeAllViews()
        val dotSize = (6 * resources.displayMetrics.density).toInt()
        val dotMargin = (4 * resources.displayMetrics.density).toInt()

        for (i in 0 until itemCount) {
            val dot = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    leftMargin = dotMargin
                    rightMargin = dotMargin
                }
                setImageDrawable(createDotDrawable(i == 0))
                setOnClickListener {
                    viewPager.setCurrentItem(i, true)
                    pageClickListener?.invoke(i)
                }
            }
            indicatorContainer.addView(dot)
        }
    }

    private fun updateIndicatorDots(selectedPosition: Int = viewPager.currentItem) {
        for (i in 0 until indicatorContainer.childCount) {
            val dot = indicatorContainer.getChildAt(i) as? ImageView ?: continue
            dot.setImageDrawable(createDotDrawable(i == selectedPosition))
        }
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
        if (isAutoScrolling) {
            handler.postDelayed(autoScrollRunnable, interval)
        }
    }

    override fun onDetachedFromWindow() {
        stopAutoScroll()
        super.onDetachedFromWindow()
    }
}
