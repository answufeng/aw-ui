package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.R
import kotlin.math.max

/**
 * ViewPager2 圆点指示器。
 *
 * 用于配合 ViewPager2 显示当前页面位置的圆点指示器。
 * 选中的圆点会以更大的尺寸和不同的颜色突出显示，
 * 页面切换时圆点的大小和颜色会有平滑的过渡动画。
 *
 * 支持通过 XML 属性自定义圆点数量、半径、间距、颜色、动画时长等。
 *
 * XML 属性：
 * - di_dotCount：圆点数量，默认 3
 * - di_dotRadius：未选中圆点半径，默认 4dp
 * - di_selectedDotRadius：选中圆点半径，默认 5dp
 * - di_dotSpacing：圆点间距，默认 8dp
 * - di_selectedDotColor：选中圆点颜色，默认 accent
 * - di_unselectedDotColor：未选中圆点颜色，默认灰色
 * - di_animationDuration：动画时长，默认 200ms
 */
class AwDotIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_DOT_COUNT = 3
        private const val DEFAULT_DOT_RADIUS_DP = 4f
        private const val DEFAULT_SELECTED_DOT_RADIUS_DP = 5f
        private const val DEFAULT_DOT_SPACING_DP = 8f
        private const val DEFAULT_ANIMATION_DURATION = 200
        private val DEFAULT_SELECTED_DOT_COLOR = Color.parseColor("#FF1E88E5")
        private val DEFAULT_UNSELECTED_DOT_COLOR = Color.parseColor("#FFBDBDBD")
    }

    /** 圆点数量 */
    var dotCount: Int = DEFAULT_DOT_COUNT
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /** 当前选中位置 */
    var selectedPosition: Int = 0
        set(value) {
            val clamped = value.coerceIn(0, max(0, dotCount - 1))
            if (field != clamped) {
                field = clamped
                invalidate()
            }
        }

    /** 未选中圆点半径（像素） */
    var dotRadius: Float = 0f
        private set

    /** 选中圆点半径（像素） */
    var selectedDotRadius: Float = 0f
        private set

    /** 圆点间距（像素） */
    var dotSpacing: Float = 0f
        private set

    /** 选中圆点颜色 */
    var selectedDotColor: Int = 0
        private set

    /** 未选中圆点颜色 */
    var unselectedDotColor: Int = 0
        private set

    /** 动画时长（毫秒） */
    var animationDuration: Long = 0L
        private set

    private val unselectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null

    /** 动画进度：0 = 完全未选中，1 = 完全选中 */
    private var animProgress = 1f

    /** 正在执行离开选中动画的位置（-1 表示无） */
    private var leavingPosition = -1

    /** 正在执行离开选中动画的进度：1 = 刚离开，0 = 完全离开 */
    private var leavingProgress = 0f

    private var viewPager2: ViewPager2? = null
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    private var adapterDataObserver: RecyclerView.AdapterDataObserver? = null
    private var onPositionChangedListener: ((position: Int) -> Unit)? = null

    init {
        val density = context.resources.displayMetrics.density
        var dotRadiusPx = DEFAULT_DOT_RADIUS_DP * density
        var selectedDotRadiusPx = DEFAULT_SELECTED_DOT_RADIUS_DP * density
        var dotSpacingPx = DEFAULT_DOT_SPACING_DP * density
        var selColor = DEFAULT_SELECTED_DOT_COLOR
        var unselColor = DEFAULT_UNSELECTED_DOT_COLOR
        var animDuration = DEFAULT_ANIMATION_DURATION.toLong()
        var count = DEFAULT_DOT_COUNT

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AwDotIndicator, defStyleAttr, 0)
            try {
                count = a.getInt(R.styleable.AwDotIndicator_di_dotCount, DEFAULT_DOT_COUNT)
                dotRadiusPx = a.getDimension(R.styleable.AwDotIndicator_di_dotRadius, dotRadiusPx)
                selectedDotRadiusPx = a.getDimension(
                    R.styleable.AwDotIndicator_di_selectedDotRadius,
                    selectedDotRadiusPx
                )
                dotSpacingPx = a.getDimension(R.styleable.AwDotIndicator_di_dotSpacing, dotSpacingPx)
                selColor = a.getColor(R.styleable.AwDotIndicator_di_selectedDotColor, selColor)
                unselColor = a.getColor(R.styleable.AwDotIndicator_di_unselectedDotColor, unselColor)
                animDuration = a.getInt(
                    R.styleable.AwDotIndicator_di_animationDuration,
                    DEFAULT_ANIMATION_DURATION
                ).toLong()
            } finally {
                a.recycle()
            }
        }

        this.dotCount = count
        this.dotRadius = dotRadiusPx
        this.selectedDotRadius = selectedDotRadiusPx
        this.dotSpacing = dotSpacingPx
        this.selectedDotColor = selColor
        this.unselectedDotColor = unselColor
        this.animationDuration = animDuration

        unselectedPaint.color = unselectedDotColor
        selectedPaint.color = selectedDotColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = if (dotCount <= 0) {
            0f
        } else {
            dotCount * dotRadius * 2 + (dotCount - 1) * dotSpacing
        }
        val maxHeight = selectedDotRadius * 2

        val width = resolveSize((totalWidth + paddingLeft + paddingRight).toInt(), widthMeasureSpec)
        val height = resolveSize((maxHeight + paddingTop + paddingBottom).toInt(), heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dotCount <= 0) return

        val totalWidth = dotCount * dotRadius * 2 + (dotCount - 1) * dotSpacing
        val startX = (width - totalWidth) / 2f
        val centerY = height / 2f

        for (i in 0 until dotCount) {
            val cx = startX + dotRadius + i * (dotRadius * 2 + dotSpacing)

            when {
                i == selectedPosition && i == leavingPosition -> {
                    // 选中位置同时是离开位置：先缩小再放大
                    val radius = dotRadius + (selectedDotRadius - dotRadius) * animProgress
                    val color = blendColor(unselectedDotColor, selectedDotColor, animProgress)
                    selectedPaint.color = color
                    canvas.drawCircle(cx, centerY, radius, selectedPaint)
                }
                i == selectedPosition -> {
                    val radius = dotRadius + (selectedDotRadius - dotRadius) * animProgress
                    val color = blendColor(unselectedDotColor, selectedDotColor, animProgress)
                    selectedPaint.color = color
                    canvas.drawCircle(cx, centerY, radius, selectedPaint)
                }
                i == leavingPosition -> {
                    val radius = selectedDotRadius - (selectedDotRadius - dotRadius) * (1f - leavingProgress)
                    val color = blendColor(selectedDotColor, unselectedDotColor, 1f - leavingProgress)
                    selectedPaint.color = color
                    canvas.drawCircle(cx, centerY, radius, selectedPaint)
                }
                else -> {
                    canvas.drawCircle(cx, centerY, dotRadius, unselectedPaint)
                }
            }
        }
    }

    /**
     * 绑定 ViewPager2，自动监听页面切换并更新指示器。
     *
     * @param viewPager2 要绑定的 ViewPager2 实例
     */
    fun bindViewPager2(viewPager2: ViewPager2) {
        // 先解绑旧的
        this.viewPager2?.let { old ->
            pageChangeCallback?.let { old.unregisterOnPageChangeCallback(it) }
            adapterDataObserver?.let { old.adapter?.unregisterAdapterDataObserver(it) }
        }

        this.viewPager2 = viewPager2
        this.dotCount = viewPager2.adapter?.itemCount ?: 0
        this.selectedPosition = viewPager2.currentItem

        val callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                animateToPosition(position)
                onPositionChangedListener?.invoke(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // 页面滚动状态变化，无需处理
            }
        }
        pageChangeCallback = callback
        viewPager2.registerOnPageChangeCallback(callback)

        // 监听 adapter 数据变化
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                dotCount = viewPager2.adapter?.itemCount ?: 0
                selectedPosition = viewPager2.currentItem.coerceIn(0, max(0, dotCount - 1))
            }
        }
        adapterDataObserver = observer
        viewPager2.adapter?.registerAdapterDataObserver(observer)

        invalidate()
    }

    /**
     * 设置页面位置变化监听器。
     *
     * @param listener 位置变化回调，参数为新的选中位置
     */
    fun setOnPositionChangedListener(listener: (position: Int) -> Unit) {
        onPositionChangedListener = listener
    }

    private fun animateToPosition(newPosition: Int) {
        if (newPosition == selectedPosition) return

        animator?.cancel()

        val oldPosition = selectedPosition
        leavingPosition = oldPosition
        leavingProgress = 1f
        animProgress = 0f

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                animProgress = fraction
                leavingProgress = 1f - fraction
                invalidate()
            }
            start()
        }

        selectedPosition = newPosition
    }

    /** 混合两种颜色 */
    private fun blendColor(fromColor: Int, toColor: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val a = (Color.alpha(fromColor) * inverseRatio + Color.alpha(toColor) * ratio).toInt()
        val r = (Color.red(fromColor) * inverseRatio + Color.red(toColor) * ratio).toInt()
        val g = (Color.green(fromColor) * inverseRatio + Color.green(toColor) * ratio).toInt()
        val b = (Color.blue(fromColor) * inverseRatio + Color.blue(toColor) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        viewPager2?.let { vp ->
            pageChangeCallback?.let { vp.unregisterOnPageChangeCallback(it) }
            adapterDataObserver?.let { vp.adapter?.unregisterAdapterDataObserver(it) }
        }
        viewPager2 = null
        pageChangeCallback = null
        adapterDataObserver = null
    }
}
