package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 滚轮选择器（单列），类似 iOS PickerView。
 *
 * 支持滚动选择、循环模式和弹性回弹。
 * 可作为日期、时间、地区等选择的基座组件。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwPickerView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:pv_visibleItemCount="5"
 *     app:pv_textSize="16sp" />
 * ```
 *
 * 代码用法：
 * ```kotlin
 * pickerView.items = listOf("Spring", "Summer", "Autumn", "Winter")
 * pickerView.onSelectedListener = { index, text -> /* ... */ }
 * pickerView.setSelectedIndex(2, animated = true)
 * ```
 */
class AwPickerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {

        var items: List<String> = emptyList()
            set(v) {
                field = v
                selectedIndex = 0
                totalScrollY = 0f
                requestLayout()
                invalidate()
            }

        var textColor: Int = Color.parseColor("#999999")
            set(v) {
                field = v
                invalidate()
            }

        var selectedTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_picker_selected_text)
            set(v) {
                field = v
                invalidate()
            }

        var visibleItemCount: Int = 5
            set(v) {
                field = v.coerceAtLeast(1)
                requestLayout()
                invalidate()
            }

        var textSize: Float = 16f.dpFloat
            set(v) {
                field = v
                requestLayout()
                invalidate()
            }

        /** 是否循环滚动，默认 true */
        var cyclic: Boolean = true
            set(v) {
                field = v
                invalidate()
            }

        var selectedIndex: Int = 0
            set(v) {
                field = if (items.isEmpty()) 0 else v.coerceIn(0, items.size - 1)
                invalidate()
            }

        var onSelectedListener: ((index: Int, text: String) -> Unit)? = null

        // 每个选项的高度
        private var itemHeight: Float = 40f.dpFloat
        // 当前偏移量
        private var totalScrollY: Float = 0f

        // 触摸相关
        private var lastDownY: Float = 0f
        private var lastMoveY: Float = 0f
        private var isDragging: Boolean = false
        private var velocityTracker: VelocityTracker? = null
        private val minimumVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        private val maximumVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        private var flingAnimator: android.animation.ValueAnimator? = null
        private var scrollAnimator: android.animation.ValueAnimator? = null

        private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }
        private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }
        private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#1A000000")
        }
        private val textRect = Rect()

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwPickerView)
            textColor = ta.getColor(R.styleable.AwPickerView_pv_textColor, textColor)
            selectedTextColor = ta.getColor(R.styleable.AwPickerView_pv_selectedTextColor, selectedTextColor)
            visibleItemCount = ta.getInt(R.styleable.AwPickerView_pv_visibleItemCount, 5)
            textSize = ta.getDimension(R.styleable.AwPickerView_pv_textSize, textSize)
            ta.recycle()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val h = (itemHeight * visibleItemCount).toInt() + paddingTop + paddingBottom
            val w = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
            setMeasuredDimension(w, resolveSize(h, heightMeasureSpec).coerceAtLeast(h))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (items.isEmpty()) return
            val w = width.toFloat()
            val h = height.toFloat()
            val halfH = h / 2f
            val centerIndexFloat = totalScrollY / itemHeight
            val firstVisibleIndex = (centerIndexFloat - visibleItemCount / 2f).toInt() - 1

            // 选中指示器背景
            val indicatorTop = halfH - itemHeight / 2f
            canvas.drawRect(0f, indicatorTop, w, indicatorTop + itemHeight, indicatorPaint)

            for (i in firstVisibleIndex..firstVisibleIndex + visibleItemCount + 2) {
                val posY = (i - centerIndexFloat) * itemHeight + halfH
                if (posY < -itemHeight || posY > h + itemHeight) continue

                val realIndex = toRealIndex(i)
                val isCenter = i == getCenterVirtualIndex()
                val paint = if (isCenter) selectedPaint else normalPaint

                paint.textSize = textSize
                paint.color = if (isCenter) selectedTextColor else textColor

                // 缩放效果：中间大，两端小
                val distance = abs(posY - halfH) / halfH
                val scale = (1f - distance * 0.3f).coerceIn(0.7f, 1f)
                paint.textSize = textSize * scale

                val text = items[realIndex]
                paint.getTextBounds(text, 0, text.length, textRect)
                canvas.drawText(text, w / 2f, posY + textRect.height() / 2f, paint)
            }
        }

        /** 将虚拟索引转换为真实索引（0..items.size-1） */
        private fun toRealIndex(virtualIndex: Int): Int {
            if (items.isEmpty()) return 0
            return if (cyclic) {
                ((virtualIndex % items.size) + items.size) % items.size
            } else {
                virtualIndex.coerceIn(0, items.size - 1)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (items.isEmpty()) return super.onTouchEvent(event)

            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain()
            }
            velocityTracker!!.addMovement(event)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    lastDownY = event.y
                    lastMoveY = event.y
                    isDragging = false
                    cancelAnimations()
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = event.y - lastMoveY
                    if (!isDragging && abs(dy) > ViewConfiguration.get(context).scaledTouchSlop) {
                        isDragging = true
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    if (isDragging) {
                        totalScrollY -= dy
                        if (cyclic) {
                            // 循环模式：不限制滚动范围
                        } else {
                            // 非循环模式：限制边界
                            val maxScroll = (items.size - 1) * itemHeight
                            totalScrollY = totalScrollY.coerceIn(0f, maxScroll)
                        }
                        invalidate()
                    }
                    lastMoveY = event.y
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    if (isDragging) {
                        velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                        val velocityY = -velocityTracker!!.yVelocity
                        if (abs(velocityY) > minimumVelocity) {
                            doFling(velocityY)
                        } else {
                            snapToNearest()
                        }
                    } else {
                        val clickedIndex = getIndexAtY(event.y)
                        if (clickedIndex in items.indices) {
                            smoothScrollToIndex(clickedIndex)
                        }
                    }
                    isDragging = false
                    releaseVelocityTracker()
                    performClick()
                }
            }
            return true
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        /** 获取当前中心虚拟索引（四舍五入到最近整数） */
        private fun getCenterVirtualIndex(): Int = (totalScrollY / itemHeight).roundToInt()

        /** 获取当前中心真实索引 */
        private fun getCenterIndex(): Int = toRealIndex(getCenterVirtualIndex())

        private fun getIndexAtY(y: Float): Int {
            val centerY = height / 2f
            val dy = y - centerY
            val offset = totalScrollY - dy
            val virtualIndex = (offset / itemHeight).roundToInt()
            return toRealIndex(virtualIndex)
        }

        private fun doFling(velocityY: Float) {
            val endScroll = totalScrollY + velocityY * 0.3f
            flingAnimator = android.animation.ValueAnimator.ofFloat(totalScrollY, endScroll).apply {
                duration = 300
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { anim ->
                    totalScrollY = anim.animatedValue as Float
                    if (!cyclic) {
                        val maxScroll = (items.size - 1) * itemHeight
                        totalScrollY = totalScrollY.coerceIn(0f, maxScroll)
                    }
                    invalidate()
                }
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        snapToNearest()
                    }
                })
                start()
            }
        }

        private fun snapToNearest() {
            if (items.isEmpty()) return
            val currentVirtualIndex = totalScrollY / itemHeight
            val targetVirtualIndex = currentVirtualIndex.roundToInt()
            val targetScroll = targetVirtualIndex * itemHeight

            if (cyclic) {
                // 循环模式：直接吸附到最近的虚拟索引
                scrollAnimator = android.animation.ValueAnimator.ofFloat(totalScrollY, targetScroll).apply {
                    duration = 200
                    interpolator = android.view.animation.DecelerateInterpolator()
                    addUpdateListener { anim ->
                        totalScrollY = anim.animatedValue as Float
                        invalidate()
                    }
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            selectedIndex = getCenterIndex()
                            if (selectedIndex in items.indices) {
                                onSelectedListener?.invoke(selectedIndex, items[selectedIndex])
                            }
                        }
                    })
                    start()
                }
            } else {
                // 非循环模式：限制边界
                val clampedIndex = targetVirtualIndex.coerceIn(0, items.size - 1)
                smoothScrollToIndex(clampedIndex)
            }
        }

        private fun smoothScrollToIndex(index: Int) {
            if (items.isEmpty()) return
            val target = index.coerceIn(0, items.size - 1)

            if (cyclic) {
                // 循环模式：找最短路径
                val currentVirtualIndex = (totalScrollY / itemHeight).roundToInt()
                val currentRealIndex = toRealIndex(currentVirtualIndex)

                // 计算从当前位置到目标的最短距离
                var diff = target - currentRealIndex
                if (diff > items.size / 2) diff -= items.size
                if (diff < -items.size / 2) diff += items.size

                val targetVirtualIndex = currentVirtualIndex + diff
                val targetScroll = targetVirtualIndex * itemHeight
                scrollAnimator = android.animation.ValueAnimator.ofFloat(totalScrollY, targetScroll).apply {
                    duration = 200
                    interpolator = android.view.animation.DecelerateInterpolator()
                    addUpdateListener { anim ->
                        totalScrollY = anim.animatedValue as Float
                        invalidate()
                    }
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            selectedIndex = target
                            if (selectedIndex in items.indices) {
                                onSelectedListener?.invoke(selectedIndex, items[selectedIndex])
                            }
                        }
                    })
                    start()
                }
            } else {
                val targetScroll = target * itemHeight
                scrollAnimator = android.animation.ValueAnimator.ofFloat(totalScrollY, targetScroll).apply {
                    duration = 200
                    interpolator = android.view.animation.DecelerateInterpolator()
                    addUpdateListener { anim ->
                        totalScrollY = anim.animatedValue as Float
                        invalidate()
                    }
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            selectedIndex = target
                            if (selectedIndex in items.indices) {
                                onSelectedListener?.invoke(selectedIndex, items[selectedIndex])
                            }
                        }
                    })
                    start()
                }
            }
        }

        private fun cancelAnimations() {
            flingAnimator?.cancel()
            scrollAnimator?.cancel()
        }

        private fun releaseVelocityTracker() {
            velocityTracker?.recycle()
            velocityTracker = null
        }

        @Deprecated("Use items property instead", ReplaceWith("items = data"))
        fun setData(data: List<String>) {
            items = data
        }

        fun setSelectedIndex(index: Int, animated: Boolean = false) {
            if (items.isEmpty()) return
            val target = index.coerceIn(0, items.size - 1)
            if (animated) {
                smoothScrollToIndex(target)
            } else {
                totalScrollY = (target * itemHeight).toFloat()
                selectedIndex = target
                invalidate()
            }
        }
    }
