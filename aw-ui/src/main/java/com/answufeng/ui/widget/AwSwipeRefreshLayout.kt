package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

class AwSwipeRefreshLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : FrameLayout(context, attrs), NestedScrollingParent3, NestedScrollingChild3 {
        companion object {
            private const val DRAG_RATE = 0.62f
            private const val ANIM_DURATION_MS = 280L
        }

        enum class RefreshStyle {
            SYSTEM,
            FLOWER,
            ARROW,
            TEXT,
        }

        var refreshStyle: RefreshStyle = RefreshStyle.SYSTEM
            set(value) {
                if (field == value) return
                field = value
                rebuildHeaderView()
            }

        var refreshTintColor: Int = 0
            set(value) {
                field = value
                applyTintColor()
            }

        var refreshText: String = context.getString(R.string.aw_swipe_refresh_pull_down)
            set(value) {
                field = value
                (headerView as? TextRefreshHeaderView)?.text = value
            }

        var refreshTextSize: Int = 0
            set(value) {
                field = value
                if (value > 0) {
                    (headerView as? TextRefreshHeaderView)?.textSize = value
                }
            }

        var refreshTextColor: Int = 0
            set(value) {
                field = value
                if (value != 0) {
                    (headerView as? TextRefreshHeaderView)?.textColor = value
                }
            }

        var enableRefresh: Boolean = true

        var refreshListener: (() -> Unit)? = null

        var isRefreshing: Boolean = false
            private set

        private val parentHelper = NestedScrollingParentHelper(this)
        private val childHelper = NestedScrollingChildHelper(this)

        private var headerView: View
        private var contentView: View? = null

        private var headerHeight: Int = 0
        private var currentOffset: Int = 0
        private var touchSlop: Int = 0
        private var isBeingDragged: Boolean = false
        private var activePointerId: Int = MotionEvent.INVALID_POINTER_ID
        private var initialDownY: Float = 0f
        private var initialMoveY: Float = 0f
        private var nestedScrollAccepted: Boolean = false

        private val maxDragOffset: Int
            get() = headerHeight * 2

        private val triggerOffset: Int
            get() = headerHeight

        private var animator: ValueAnimator? = null

        init {
            refreshTintColor = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)

            touchSlop = ViewConfiguration.get(context).scaledTouchSlop

            var customLayoutRes = 0
            context.obtainStyledAttributes(attrs, R.styleable.AwSwipeRefreshLayout).apply {
                try {
                    if (hasValue(R.styleable.AwSwipeRefreshLayout_customRefreshHeaderLayout)) {
                        customLayoutRes = getResourceId(R.styleable.AwSwipeRefreshLayout_customRefreshHeaderLayout, 0)
                    }
                    val styleIndex = getInt(R.styleable.AwSwipeRefreshLayout_refreshStyle, 0)
                    refreshStyle =
                        when (styleIndex) {
                            1 -> RefreshStyle.FLOWER
                            2 -> RefreshStyle.ARROW
                            3 -> RefreshStyle.TEXT
                            else -> RefreshStyle.SYSTEM
                        }
                    if (hasValue(R.styleable.AwSwipeRefreshLayout_refreshTintColor)) {
                        refreshTintColor = getColor(R.styleable.AwSwipeRefreshLayout_refreshTintColor, refreshTintColor)
                    }
                    refreshText =
                        getString(R.styleable.AwSwipeRefreshLayout_refreshText)
                            ?: context.getString(R.string.aw_swipe_refresh_pull_down)
                    if (hasValue(R.styleable.AwSwipeRefreshLayout_refreshTextColor)) {
                        refreshTextColor = getColor(R.styleable.AwSwipeRefreshLayout_refreshTextColor, 0)
                    }
                    if (hasValue(R.styleable.AwSwipeRefreshLayout_refreshTextSize)) {
                        refreshTextSize = getDimensionPixelSize(R.styleable.AwSwipeRefreshLayout_refreshTextSize, 0)
                    }
                } finally {
                    recycle()
                }
            }

            headerView =
                if (customLayoutRes != 0) {
                    LayoutInflater.from(context).inflate(customLayoutRes, this, false)
                } else {
                    createHeaderView()
                }
            addView(headerView)

            isNestedScrollingEnabled = true
            setWillNotDraw(false)
        }

        fun setCustomHeaderView(view: View) {
            removeView(headerView)
            headerView = view
            addView(headerView, 0)
            measureHeader()
            applyTintColor()
            updateHeaderState()
        }

        fun setCustomHeaderView(
            @LayoutRes layoutRes: Int,
        ) {
            val view = LayoutInflater.from(context).inflate(layoutRes, this, false)
            setCustomHeaderView(view)
        }

        fun startRefresh() {
            if (isRefreshing) return
            isRefreshing = true
            updateHeaderState()
            animateTo(triggerOffset)
        }

        fun finishRefresh() {
            if (!isRefreshing) return
            isRefreshing = false
            updateHeaderState()
            animateTo(0)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            measureHeader()
        }

        override fun onFinishInflate() {
            super.onFinishInflate()
            resolveContentView()
        }

        override fun addView(child: View?) {
            super.addView(child)
            if (child !== headerView && contentView == null) {
                contentView = child
            }
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            measureHeader()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        override fun onLayout(
            changed: Boolean,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
        ) {
            val w = right - left
            val h = bottom - top

            headerView.layout(0, currentOffset - headerHeight, w, currentOffset)

            contentView?.layout(0, currentOffset, w, h + currentOffset)
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            if (!enableRefresh || isRefreshing || canChildScrollUp()) {
                return false
            }

            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    activePointerId = ev.getPointerId(0)
                    isBeingDragged = false
                    initialDownY = ev.getY(0)
                    initialMoveY = initialDownY
                }

                MotionEvent.ACTION_MOVE -> {
                    if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                        return false
                    }
                    val pointerIndex = ev.findPointerIndex(activePointerId)
                    if (pointerIndex < 0) return false
                    val y = ev.getY(pointerIndex)
                    val dy = y - initialDownY
                    if (dy > touchSlop && !isBeingDragged) {
                        initialMoveY = y
                        isBeingDragged = true
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isBeingDragged = false
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
            }

            return isBeingDragged
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            if (!enableRefresh || isRefreshing) {
                return false
            }

            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    activePointerId = ev.getPointerId(0)
                    initialDownY = ev.getY(0)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val pointerIndex = ev.findPointerIndex(activePointerId)
                    if (pointerIndex < 0) return false
                    val y = ev.getY(pointerIndex)
                    var dy = y - initialMoveY
                    if (!isBeingDragged && dy > touchSlop) {
                        isBeingDragged = true
                        dy -= touchSlop
                    }
                    if (isBeingDragged) {
                        val targetOffset = computeTargetOffset(dy)
                        updateOffset(targetOffset)
                        initialMoveY = y
                        updateHeaderState()
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (isBeingDragged) {
                        isBeingDragged = false
                        releaseDrag()
                    }
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (isBeingDragged) {
                        isBeingDragged = false
                        animateTo(0)
                    }
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                    return true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    onSecondaryPointerUp(ev)
                    return true
                }
            }

            return false
        }

        override fun onStartNestedScroll(
            child: View,
            target: View,
            axes: Int,
            type: Int,
        ): Boolean {
            return enableRefresh && axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        }

        override fun onNestedScrollAccepted(
            child: View,
            target: View,
            axes: Int,
            type: Int,
        ) {
            parentHelper.onNestedScrollAccepted(child, target, axes, type)
            nestedScrollAccepted = true
        }

        override fun onStopNestedScroll(
            target: View,
            type: Int,
        ) {
            parentHelper.onStopNestedScroll(target, type)
            nestedScrollAccepted = false
            if (isBeingDragged) {
                isBeingDragged = false
                releaseDrag()
            } else if (currentOffset > 0 && !isRefreshing) {
                animateTo(0)
            }
        }

        override fun onNestedPreScroll(
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
            type: Int,
        ) {
            if (dy > 0 && currentOffset > 0) {
                val consumedY = dy.coerceAtMost(currentOffset)
                updateOffset(currentOffset - consumedY)
                consumed[1] = consumedY
                updateHeaderState()
            }
        }

        override fun onNestedScroll(
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
            consumed: IntArray,
        ) {
            if (type != ViewCompat.TYPE_TOUCH) return
            if (dyUnconsumed < 0 && !isRefreshing && enableRefresh) {
                val newOffset = computeTargetOffset(-dyUnconsumed.toFloat())
                updateOffset(newOffset)
                updateHeaderState()
                consumed[1] = dyUnconsumed
            }
        }

        override fun onNestedScroll(
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
        ) {
        }

        override fun onNestedPreScroll(
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
        ) {
            onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH)
        }

        override fun onStartNestedScroll(
            child: View,
            target: View,
            axes: Int,
        ): Boolean {
            return onStartNestedScroll(child, target, axes, ViewCompat.TYPE_TOUCH)
        }

        override fun onNestedScrollAccepted(
            child: View,
            target: View,
            axes: Int,
        ) {
            onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
        }

        override fun onStopNestedScroll(target: View) {
            onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
        }

        override fun onNestedScroll(
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
        ) {
        }

        override fun onNestedPreFling(
            target: View,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            return false
        }

        override fun onNestedFling(
            target: View,
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean,
        ): Boolean {
            return false
        }

        override fun getNestedScrollAxes(): Int = parentHelper.nestedScrollAxes

        override fun setNestedScrollingEnabled(enabled: Boolean) {
            childHelper.isNestedScrollingEnabled = enabled
        }

        override fun isNestedScrollingEnabled(): Boolean = childHelper.isNestedScrollingEnabled

        override fun startNestedScroll(
            axes: Int,
            type: Int,
        ): Boolean {
            return childHelper.startNestedScroll(axes, type)
        }

        override fun stopNestedScroll(type: Int) {
            childHelper.stopNestedScroll(type)
        }

        override fun hasNestedScrollingParent(type: Int): Boolean {
            return childHelper.hasNestedScrollingParent(type)
        }

        override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?,
            type: Int,
            consumed: IntArray,
        ) {
            childHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow,
                type,
                consumed,
            )
        }

        override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?,
            type: Int,
        ): Boolean {
            return childHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow,
                type,
            )
        }

        override fun dispatchNestedPreScroll(
            dx: Int,
            dy: Int,
            consumed: IntArray?,
            offsetInWindow: IntArray?,
            type: Int,
        ): Boolean {
            return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
        }

        override fun startNestedScroll(axes: Int): Boolean {
            return startNestedScroll(axes, ViewCompat.TYPE_TOUCH)
        }

        override fun stopNestedScroll() {
            stopNestedScroll(ViewCompat.TYPE_TOUCH)
        }

        override fun hasNestedScrollingParent(): Boolean {
            return hasNestedScrollingParent(ViewCompat.TYPE_TOUCH)
        }

        override fun dispatchNestedScroll(
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            offsetInWindow: IntArray?,
        ): Boolean {
            return childHelper.dispatchNestedScroll(
                dxConsumed,
                dyConsumed,
                dxUnconsumed,
                dyUnconsumed,
                offsetInWindow,
            )
        }

        override fun dispatchNestedPreScroll(
            dx: Int,
            dy: Int,
            consumed: IntArray?,
            offsetInWindow: IntArray?,
        ): Boolean {
            return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, ViewCompat.TYPE_TOUCH)
        }

        override fun dispatchNestedFling(
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean,
        ): Boolean {
            return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
        }

        override fun dispatchNestedPreFling(
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            return childHelper.dispatchNestedPreFling(velocityX, velocityY)
        }

        private fun resolveContentView() {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !== headerView && contentView == null) {
                    contentView = child
                }
            }
        }

        private fun measureHeader() {
            val wSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            headerView.measure(wSpec, hSpec)
            headerHeight = headerView.measuredHeight
            if (headerHeight == 0) {
                headerHeight = (56 * resources.displayMetrics.density).toInt()
            }
        }

        private fun updateOffset(offset: Int) {
            if (currentOffset == offset) return
            currentOffset = offset
            requestLayout()
        }

        private fun animateTo(targetOffset: Int) {
            animator?.cancel()
            if (currentOffset == targetOffset) return
            animator =
                ValueAnimator.ofInt(currentOffset, targetOffset).apply {
                    duration = ANIM_DURATION_MS
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        currentOffset = animation.animatedValue as Int
                        requestLayout()
                        updateHeaderState()
                    }
                    start()
                }
        }

        private fun updateHeaderState() {
            val progress =
                if (triggerOffset > 0) {
                    (currentOffset.toFloat() / triggerOffset).coerceIn(0f, 1f)
                } else {
                    0f
                }

            val hv = headerView
            if (hv is RefreshHeaderView) {
                hv.onRefreshStateChanged(progress, isRefreshing)
            }
        }

        private fun applyTintColor() {
            val hv = headerView
            if (hv is RefreshHeaderView) {
                hv.onTintColorChanged(refreshTintColor)
            }
        }

        private fun rebuildHeaderView() {
            val oldHeader = headerView
            removeView(oldHeader)
            headerView = createHeaderView()
            addView(headerView, 0)
            measureHeader()
            updateHeaderState()
        }

        private fun createHeaderView(): View {
            return when (refreshStyle) {
                RefreshStyle.SYSTEM ->
                    SystemRefreshHeaderView(context).apply {
                        tintColor = refreshTintColor
                    }
                RefreshStyle.FLOWER ->
                    FlowerRefreshHeaderView(context).apply {
                        tintColor = refreshTintColor
                    }
                RefreshStyle.ARROW ->
                    ArrowRefreshHeaderView(context).apply {
                        tintColor = refreshTintColor
                    }
                RefreshStyle.TEXT ->
                    TextRefreshHeaderView(context).apply {
                        tintColor = refreshTintColor
                        text = refreshText
                        if (refreshTextColor != 0) textColor = refreshTextColor
                        if (refreshTextSize > 0) textSize = refreshTextSize
                    }
            }
        }

        private fun canChildScrollUp(): Boolean {
            val child = contentView ?: return false
            return ViewCompat.canScrollVertically(child, -1)
        }

        private fun computeTargetOffset(dy: Float): Int {
            val resistance = dragResistance(currentOffset)
            return (currentOffset + dy * DRAG_RATE * resistance).toInt().coerceIn(0, maxDragOffset)
        }

        private fun dragResistance(offset: Int): Float {
            if (maxDragOffset <= 0) return 1f
            val ratio = (offset.toFloat() / maxDragOffset).coerceIn(0f, 1f)
            // Stronger damping when pulling further to avoid "rubber-band explosion".
            return 1f - 0.55f * ratio
        }

        private fun releaseDrag() {
            if (currentOffset >= triggerOffset) {
                if (!isRefreshing) {
                    isRefreshing = true
                    refreshListener?.invoke()
                }
                updateHeaderState()
                animateTo(triggerOffset)
            } else {
                animateTo(0)
            }
        }

        private fun onSecondaryPointerUp(ev: MotionEvent) {
            val pointerIndex = ev.actionIndex
            val pointerId = ev.getPointerId(pointerIndex)
            if (pointerId == activePointerId) {
                val newPointerIndex = if (pointerIndex == 0) 1 else 0
                if (newPointerIndex < ev.pointerCount) {
                    activePointerId = ev.getPointerId(newPointerIndex)
                    initialMoveY = ev.getY(newPointerIndex)
                } else {
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
            }
        }

        override fun onDetachedFromWindow() {
            animator?.cancel()
            animator = null
            super.onDetachedFromWindow()
        }

        interface RefreshHeaderView {
            fun onRefreshStateChanged(
                pullProgress: Float,
                isRefreshing: Boolean,
            )

            fun onTintColorChanged(
                @ColorInt color: Int,
            )
        }

        internal class SystemRefreshHeaderView
            @JvmOverloads
            constructor(
                context: Context,
                attrs: AttributeSet? = null,
            ) : FrameLayout(context, attrs), RefreshHeaderView {
                private val loadingView: FlowerIndicatorView

                var tintColor: Int = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)
                    set(value) {
                        field = value
                        loadingView.tintColor = value
                    }

                var progress: Float = 0f
                    set(value) {
                        field = value
                        loadingView.alpha = value.coerceIn(0.3f, 1f)
                    }

                var isRefreshing: Boolean = false
                    set(value) {
                        field = value
                        if (value) loadingView.startSpin() else loadingView.stopSpin()
                    }

                override fun onRefreshStateChanged(
                    pullProgress: Float,
                    refreshing: Boolean,
                ) {
                    progress = pullProgress
                    isRefreshing = refreshing
                }

                override fun onTintColorChanged(color: Int) {
                    tintColor = color
                }

                init {
                    val size = (32 * resources.displayMetrics.density).toInt()
                    loadingView =
                        FlowerIndicatorView(context).apply {
                            tintColor = this@SystemRefreshHeaderView.tintColor
                            layoutParams =
                                LayoutParams(size, size).apply {
                                    gravity = Gravity.CENTER
                                }
                        }
                    addView(loadingView)
                    val h = (56 * resources.displayMetrics.density).toInt()
                    minimumHeight = h
                }
            }

        internal class FlowerRefreshHeaderView
            @JvmOverloads
            constructor(
                context: Context,
                attrs: AttributeSet? = null,
            ) : FrameLayout(context, attrs), RefreshHeaderView {
                private val indicatorView: FlowerIndicatorView

                var tintColor: Int = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)
                    set(value) {
                        field = value
                        indicatorView.tintColor = value
                    }

                var progress: Float = 0f
                    set(value) {
                        field = value
                        indicatorView.alpha = value.coerceIn(0.3f, 1f)
                    }

                var isRefreshing: Boolean = false
                    set(value) {
                        field = value
                        if (value) indicatorView.startSpin() else indicatorView.stopSpin()
                    }

                override fun onRefreshStateChanged(
                    pullProgress: Float,
                    refreshing: Boolean,
                ) {
                    progress = pullProgress
                    isRefreshing = refreshing
                }

                override fun onTintColorChanged(color: Int) {
                    tintColor = color
                }

                init {
                    val size = (32 * resources.displayMetrics.density).toInt()
                    indicatorView =
                        FlowerIndicatorView(context).apply {
                            tintColor = this@FlowerRefreshHeaderView.tintColor
                            layoutParams =
                                LayoutParams(size, size).apply {
                                    gravity = Gravity.CENTER
                                }
                        }
                    addView(indicatorView)
                    minimumHeight = (56 * resources.displayMetrics.density).toInt()
                }
            }

        internal class ArrowRefreshHeaderView
            @JvmOverloads
            constructor(
                context: Context,
                attrs: AttributeSet? = null,
            ) : FrameLayout(context, attrs), RefreshHeaderView {
                private val arrowView: ArrowIndicatorView

                var tintColor: Int = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)
                    set(value) {
                        field = value
                        arrowView.tintColor = value
                    }

                var progress: Float = 0f
                    set(value) {
                        field = value
                        arrowView.progress = value
                    }

                var isRefreshing: Boolean = false
                    set(value) {
                        field = value
                        arrowView.isRefreshing = value
                    }

                override fun onRefreshStateChanged(
                    pullProgress: Float,
                    refreshing: Boolean,
                ) {
                    progress = pullProgress
                    isRefreshing = refreshing
                }

                override fun onTintColorChanged(color: Int) {
                    tintColor = color
                }

                init {
                    val size = (32 * resources.displayMetrics.density).toInt()
                    arrowView =
                        ArrowIndicatorView(context).apply {
                            tintColor = this@ArrowRefreshHeaderView.tintColor
                            layoutParams =
                                LayoutParams(size, size).apply {
                                    gravity = Gravity.CENTER
                                }
                        }
                    addView(arrowView)
                    minimumHeight = (56 * resources.displayMetrics.density).toInt()
                }
            }

        internal class TextRefreshHeaderView
            @JvmOverloads
            constructor(
                context: Context,
                attrs: AttributeSet? = null,
            ) : LinearLayout(context, attrs), RefreshHeaderView {
                private val indicatorView: FlowerIndicatorView
                private val textView: TextView

                var tintColor: Int = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)
                    set(value) {
                        field = value
                        indicatorView.tintColor = value
                        if (textColor == 0) textView.setTextColor(value)
                    }

                var text: String = context.getString(R.string.aw_swipe_refresh_pull_down)
                    set(value) {
                        field = value
                        updateDisplay()
                    }

                var textColor: Int = 0
                    set(value) {
                        field = value
                        if (value != 0) {
                            textView.setTextColor(value)
                        } else {
                            textView.setTextColor(tintColor)
                        }
                    }

                var textSize: Int = 0
                    set(value) {
                        field = value
                        if (value > 0) textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, value.toFloat())
                    }

                var progress: Float = 0f
                    set(value) {
                        field = value
                        indicatorView.alpha = value.coerceIn(0.3f, 1f)
                        updateDisplay()
                    }

                var isRefreshing: Boolean = false
                    set(value) {
                        field = value
                        if (value) indicatorView.startSpin() else indicatorView.stopSpin()
                        updateDisplay()
                    }

                override fun onRefreshStateChanged(
                    pullProgress: Float,
                    refreshing: Boolean,
                ) {
                    progress = pullProgress
                    isRefreshing = refreshing
                }

                override fun onTintColorChanged(color: Int) {
                    tintColor = color
                }

                private fun updateDisplay() {
                    textView.text =
                        when {
                            isRefreshing -> context.getString(R.string.aw_swipe_refresh_refreshing)
                            progress >= 1f -> context.getString(R.string.aw_swipe_refresh_release)
                            else -> text
                        }
                }

                init {
                    orientation = HORIZONTAL
                    gravity = Gravity.CENTER

                    val density = resources.displayMetrics.density
                    val indicatorSize = (20 * density).toInt()

                    indicatorView =
                        FlowerIndicatorView(context).apply {
                            tintColor = this@TextRefreshHeaderView.tintColor
                            layoutParams =
                                LayoutParams(indicatorSize, indicatorSize).apply {
                                    marginEnd = (8 * density).toInt()
                                }
                        }
                    addView(indicatorView)

                    textView =
                        TextView(context).apply {
                            textSize = 14f
                            setTextColor(if (textColor != 0) textColor else tintColor)
                        }
                    addView(textView)

                    minimumHeight = (56 * density).toInt()
                }
            }

        internal class FlowerIndicatorView
            @JvmOverloads
            constructor(
                context: Context,
                attrs: AttributeSet? = null,
            ) : View(context, attrs) {
                private val petalCount = 8
                private val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        strokeCap = Paint.Cap.ROUND
                    }
                private var phase = 0f
                private var isSpinning = false
                private val strokeWidth = 2.5f.dpFloat

                var tintColor: Int = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)
                    set(value) {
                        field = value
                        paint.color = value
                        invalidate()
                    }

                private val animator =
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 750L
                        repeatCount = ValueAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        addUpdateListener {
                            phase = it.animatedValue as Float
                            invalidate()
                        }
                    }

                fun startSpin() {
                    if (isSpinning) return
                    isSpinning = true
                    if (!animator.isStarted) animator.start()
                }

                fun stopSpin() {
                    isSpinning = false
                    animator.cancel()
                    phase = 0f
                    invalidate()
                }

                override fun onDraw(canvas: Canvas) {
                    val cx = width / 2f
                    val cy = height / 2f
                    val radius = minOf(width, height) / 2f - strokeWidth
                    val innerR = radius * 0.56f
                    val petalLen = radius * 0.34f

                    for (i in 0 until petalCount) {
                        val offset = (i.toFloat() / petalCount - phase + 1f) % 1f
                        val alpha =
                            when {
                                offset < 0.5f -> 0.08f + 0.92f * (offset * 2f).coerceIn(0f, 1f)
                                else -> 0.08f + 0.92f * ((1f - offset) * 2f).coerceIn(0f, 1f)
                            }
                        paint.alpha = (255 * alpha).toInt()
                        paint.strokeWidth = strokeWidth

                        val angle = Math.PI * 2 * i / petalCount - Math.PI / 2
                        val x1 = cx + (kotlin.math.cos(angle) * innerR).toFloat()
                        val y1 = cy + (kotlin.math.sin(angle) * innerR).toFloat()
                        val x2 = cx + (kotlin.math.cos(angle) * (innerR + petalLen)).toFloat()
                        val y2 = cy + (kotlin.math.sin(angle) * (innerR + petalLen)).toFloat()
                        canvas.drawLine(x1, y1, x2, y2, paint)
                    }
                }

                override fun onMeasure(
                    widthMeasureSpec: Int,
                    heightMeasureSpec: Int,
                ) {
                    val size = (36 * resources.displayMetrics.density).toInt()
                    setMeasuredDimension(size, size)
                }

                override fun onAttachedToWindow() {
                    super.onAttachedToWindow()
                    if (isSpinning) startSpin()
                }

                override fun onDetachedFromWindow() {
                    animator.cancel()
                    super.onDetachedFromWindow()
                }
            }

        internal class ArrowIndicatorView
            @JvmOverloads
            constructor(
                context: Context,
                attrs: AttributeSet? = null,
            ) : View(context, attrs) {
                private val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                private val path = Path()

                var tintColor: Int = ContextCompat.getColor(context, R.color.aw_color_swipe_refresh)
                    set(value) {
                        field = value
                        paint.color = value
                        invalidate()
                    }

                var progress: Float = 0f
                    set(value) {
                        field = value
                        invalidate()
                    }

                var isRefreshing: Boolean = false
                    set(value) {
                        field = value
                        if (value) startSpin() else stopSpin()
                    }

                private var spinAngle = 0f
                private var spinSweep = 270f
                private var isSpinning = false

                private val animator =
                    ValueAnimator.ofFloat(0f, 360f).apply {
                        duration = 800L
                        repeatCount = ValueAnimator.INFINITE
                        interpolator = LinearInterpolator()
                        addUpdateListener {
                            val value = it.animatedValue as Float
                            spinAngle = value
                            // Slight breathing sweep makes the refresh spinner less rigid.
                            val phase = Math.toRadians(value.toDouble())
                            spinSweep = 210f + (kotlin.math.sin(phase).toFloat() + 1f) * 60f
                            invalidate()
                        }
                    }

                fun startSpin() {
                    if (isSpinning) return
                    isSpinning = true
                    if (!animator.isStarted) animator.start()
                }

                fun stopSpin() {
                    isSpinning = false
                    animator.cancel()
                    spinAngle = 0f
                    invalidate()
                }

                override fun onDraw(canvas: Canvas) {
                    val cx = width / 2f
                    val cy = height / 2f
                    val r = minOf(width, height) / 2f
                    val strokeWidth = 2.5f.dpFloat
                    paint.strokeWidth = strokeWidth
                    val arcRadius = r * 0.68f
                    val arcBoundsLeft = cx - arcRadius
                    val arcBoundsTop = cy - arcRadius
                    val arcBoundsRight = cx + arcRadius
                    val arcBoundsBottom = cy + arcRadius

                    if (isRefreshing) {
                        paint.alpha = 255
                        canvas.save()
                        canvas.rotate(spinAngle, cx, cy)
                        canvas.drawArc(
                            arcBoundsLeft,
                            arcBoundsTop,
                            arcBoundsRight,
                            arcBoundsBottom,
                            -90f,
                            spinSweep,
                            false,
                            paint,
                        )
                        canvas.restore()
                    } else {
                        val p = progress.coerceIn(0f, 1f)
                        paint.alpha = (255 * p.coerceIn(0.2f, 1f)).toInt()

                        // Material-like indicator: ring grows with pull progress.
                        val progressSweep = 24f + 282f * p
                        canvas.drawArc(
                            arcBoundsLeft,
                            arcBoundsTop,
                            arcBoundsRight,
                            arcBoundsBottom,
                            -90f,
                            progressSweep,
                            false,
                            paint,
                        )

                        // Replace previous arc-end arrow with a clearer center chevron arrow.
                        val flipProgress = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)
                        val rotation = 180f * flipProgress
                        val arrowSize = r * 0.42f
                        val shaftLen = arrowSize * 0.62f
                        val wing = arrowSize * 0.44f

                        canvas.save()
                        canvas.rotate(rotation, cx, cy)
                        path.reset()
                        path.moveTo(cx, cy + arrowSize * 0.56f)
                        path.lineTo(cx, cy - shaftLen * 0.12f)
                        path.moveTo(cx, cy + arrowSize * 0.56f)
                        path.lineTo(cx - wing, cy + arrowSize * 0.16f)
                        path.moveTo(cx, cy + arrowSize * 0.56f)
                        path.lineTo(cx + wing, cy + arrowSize * 0.16f)
                        canvas.drawPath(path, paint)
                        canvas.restore()
                    }
                }

                override fun onMeasure(
                    widthMeasureSpec: Int,
                    heightMeasureSpec: Int,
                ) {
                    val size = (36 * resources.displayMetrics.density).toInt()
                    setMeasuredDimension(size, size)
                }

                override fun onAttachedToWindow() {
                    super.onAttachedToWindow()
                    if (isSpinning) startSpin()
                }

                override fun onDetachedFromWindow() {
                    animator.cancel()
                    super.onDetachedFromWindow()
                }
            }
    }
