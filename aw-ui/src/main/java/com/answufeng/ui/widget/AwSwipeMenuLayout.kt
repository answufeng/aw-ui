package com.answufeng.ui.widget

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.OverScroller
import com.answufeng.ui.R

/**
 * 左滑菜单布局。
 *
 * 布局包含两个子视图：第一个为内容视图，第二个为菜单视图。
 * 用户在内容上向左滑动时，隐藏的菜单按钮（如删除、编辑）将被露出。
 *
 * ### XML 使用
 * ```xml
 * <com.answufeng.ui.widget.AwSwipeMenuLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:sm_menuWidth="200dp">
 *
 *     <!-- 内容视图（第一个子视图） -->
 *     <LinearLayout ... />
 *
 *     <!-- 菜单视图（第二个子视图） -->
 *     <LinearLayout ... />
 *
 * </com.answufeng.ui.widget.AwSwipeMenuLayout>
 * ```
 *
 * ### 编程使用
 * ```kotlin
 * swipeMenuLayout.openMenu()
 * swipeMenuLayout.closeMenu()
 * swipeMenuLayout.isMenuOpen
 * swipeMenuLayout.onMenuOpenListener = { ... }
 * swipeMenuLayout.onMenuCloseListener = { ... }
 * ```
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `sm_menuWidth` | 菜单宽度 | 200dp |
 */
class AwSwipeMenuLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {

        /** 菜单宽度（像素） */
        var menuWidth: Int = 0
            set(value) {
                field = value
                requestLayout()
            }

        /** 菜单是否处于打开状态 */
        var isMenuOpen: Boolean = false
            private set

        /** 菜单打开监听器 */
        var onMenuOpenListener: (() -> Unit)? = null

        /** 菜单关闭监听器 */
        var onMenuCloseListener: (() -> Unit)? = null

        private val scroller = OverScroller(context)
        private var velocityTracker: VelocityTracker? = null

        private val touchSlop: Int
        private val minVelocity: Int
        private val maxVelocity: Int

        private var downX = 0f
        private var downY = 0f
        private var lastX = 0f
        private var isDragging = false
        private var isSwiping = false

        private var contentView: View? = null
        private var menuView: View? = null

        private var scrollOffset = 0
        /** 记录上次 applyScrollOffset 时 content 的 left 偏移，用于 offsetLeftAndRight 增量更新 */
        private var lastContentLeft = 0
        private var lastMenuLeft = 0

        init {
            val vc = ViewConfiguration.get(context)
            touchSlop = vc.scaledTouchSlop
            minVelocity = vc.scaledMinimumFlingVelocity
            maxVelocity = vc.scaledMaximumFlingVelocity

            val defaultMenuWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                200f,
                resources.displayMetrics,
            ).toInt()

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSwipeMenuLayout)
            menuWidth = ta.getDimensionPixelSize(
                R.styleable.AwSwipeMenuLayout_sm_menuWidth,
                defaultMenuWidth,
            )
            ta.recycle()
        }

        override fun onFinishInflate() {
            super.onFinishInflate()
            ensureChildren()
        }

        /** 确保子视图引用已初始化（兼容 XML 声明和编程添加两种方式） */
        private fun ensureChildren() {
            if (contentView == null && childCount >= 1) {
                contentView = getChildAt(0)
            }
            if (menuView == null && childCount >= 2) {
                menuView = getChildAt(1)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            ensureChildren()
            val content = contentView
            val menu = menuView
            if (content == null || menu == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                return
            }

            // 测量内容视图：宽度为父布局宽度，高度自适应
            val contentWidthSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.EXACTLY,
            )
            measureChildWithMargins(content, contentWidthSpec, 0, heightMeasureSpec, 0)

            // 测量菜单视图：强制宽度为 menuWidth，高度与内容视图一致
            val menuWidthSpec = MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.EXACTLY)
            val menuHeightSpec = MeasureSpec.makeMeasureSpec(
                content.measuredHeight,
                MeasureSpec.EXACTLY,
            )
            menu.measure(menuWidthSpec, menuHeightSpec)

            val contentLp = content.layoutParams as? MarginLayoutParams
            val vm = contentLp?.let { it.topMargin + it.bottomMargin } ?: 0
            val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
            val totalHeight = content.measuredHeight + vm + paddingTop + paddingBottom

            setMeasuredDimension(totalWidth, resolveSize(totalHeight, heightMeasureSpec))
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val content = contentView
            val menu = menuView
            if (content == null || menu == null) return

            val contentLp = content.layoutParams as? MarginLayoutParams
            val clm = contentLp?.leftMargin ?: 0
            val ctm = contentLp?.topMargin ?: 0

            val contentLeft = paddingLeft + clm + scrollOffset
            val contentTop = paddingTop + ctm
            content.layout(contentLeft, contentTop, contentLeft + content.measuredWidth, contentTop + content.measuredHeight)

            val menuLp = menu.layoutParams as? MarginLayoutParams
            val mlm = menuLp?.leftMargin ?: 0
            val mtm = menuLp?.topMargin ?: 0

            val menuLeft = contentLeft + content.measuredWidth + (contentLp?.rightMargin ?: 0) + mlm
            val menuTop = paddingTop + mtm
            menu.layout(menuLeft, menuTop, menuLeft + menu.measuredWidth, menuTop + menu.measuredHeight)

            // 更新偏移记录
            lastContentLeft = contentLeft
            lastMenuLeft = menuLeft
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = ev.x
                    downY = ev.y
                    lastX = ev.x
                    isDragging = false
                    isSwiping = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) return true
                    val dx = ev.x - downX
                    val dy = ev.y - downY
                    if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                        isDragging = true
                        isSwiping = true
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isSwiping = false
                    isDragging = false
                }
            }
            return super.onInterceptTouchEvent(ev)
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            initVelocityTracker(ev)

            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!scroller.isFinished) {
                        scroller.abortAnimation()
                    }
                    downX = ev.x
                    downY = ev.y
                    lastX = ev.x
                    isDragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.x - lastX
                    lastX = ev.x

                    val newOffset = (scrollOffset + dx).toInt().coerceIn(-menuWidth, 0)
                    if (newOffset != scrollOffset) {
                        scrollOffset = newOffset
                        isDragging = true
                        applyScrollOffset()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    velocityTracker?.computeCurrentVelocity(1000, maxVelocity.toFloat())
                    val velocityX = velocityTracker?.xVelocity ?: 0f

                    if (isDragging || isSwiping) {
                        if (shouldOpenMenu(velocityX)) {
                            openMenu()
                        } else {
                            closeMenu()
                        }
                    } else if (isMenuOpen) {
                        // 菜单打开时，点击内容区域关闭菜单
                        val content = contentView
                        if (content != null) {
                            val hitRect = android.graphics.Rect()
                            content.getHitRect(hitRect)
                            if (!hitRect.contains(ev.x.toInt(), ev.y.toInt())) {
                                // 点击在菜单区域，不关闭
                            } else {
                                closeMenu()
                            }
                        } else {
                            closeMenu()
                        }
                    }

                    isSwiping = false
                    isDragging = false
                    releaseVelocityTracker()
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (scrollOffset < -menuWidth / 2) {
                        openMenu()
                    } else {
                        closeMenu()
                    }
                    isSwiping = false
                    isDragging = false
                    releaseVelocityTracker()
                }
            }
            return true
        }

        override fun computeScroll() {
            if (scroller.computeScrollOffset()) {
                scrollOffset = scroller.currX
                applyScrollOffset()
                postInvalidateOnAnimation()
            } else {
                val wasOpen = isMenuOpen
                isMenuOpen = scrollOffset <= -menuWidth
                if (isMenuOpen && !wasOpen) {
                    onMenuOpenListener?.invoke()
                } else if (!isMenuOpen && wasOpen) {
                    onMenuCloseListener?.invoke()
                }
            }
        }

        /**
         * 打开菜单，带平滑动画。
         */
        fun openMenu() {
            val startOffset = scrollOffset
            val endOffset = -menuWidth
            if (startOffset == endOffset) {
                val wasOpen = isMenuOpen
                isMenuOpen = true
                if (!wasOpen) onMenuOpenListener?.invoke()
                return
            }
            scroller.startScroll(startOffset, 0, endOffset - startOffset, 0, SCROLL_DURATION)
            postInvalidateOnAnimation()
        }

        /**
         * 关闭菜单，带平滑动画。
         */
        fun closeMenu() {
            val startOffset = scrollOffset
            val endOffset = 0
            if (startOffset == endOffset) {
                val wasOpen = isMenuOpen
                isMenuOpen = false
                if (wasOpen) onMenuCloseListener?.invoke()
                return
            }
            scroller.startScroll(startOffset, 0, endOffset - startOffset, 0, SCROLL_DURATION)
            postInvalidateOnAnimation()
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putInt("scrollOffset", scrollOffset)
                putBoolean("isMenuOpen", isMenuOpen)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                val superState: Parcelable? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        state.getParcelable("superState", Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        state.getParcelable("superState")
                    }
                super.onRestoreInstanceState(superState)
                scrollOffset = state.getInt("scrollOffset", 0)
                isMenuOpen = state.getBoolean("isMenuOpen", false)
                applyScrollOffset()
            } else {
                super.onRestoreInstanceState(state)
            }
        }

        override fun onDetachedFromWindow() {
            scroller.abortAnimation()
            releaseVelocityTracker()
            super.onDetachedFromWindow()
        }

        private fun shouldOpenMenu(velocityX: Float): Boolean {
            if (velocityX < -minVelocity) return true
            if (velocityX > minVelocity) return false
            return -scrollOffset > menuWidth * 0.5f
        }

        /**
         * 通过 requestLayout 重新布局子视图，使触摸区域跟随视觉位置移动。
         * 不使用 translationX，因为 translationX 不会更新触摸命中区域。
         */
        private fun applyScrollOffset() {
            requestLayout()
        }

        private fun initVelocityTracker(ev: MotionEvent) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain()
            }
            velocityTracker?.addMovement(ev)
        }

        private fun releaseVelocityTracker() {
            velocityTracker?.recycle()
            velocityTracker = null
        }

        companion object {
            private const val SCROLL_DURATION = 300
        }
    }
