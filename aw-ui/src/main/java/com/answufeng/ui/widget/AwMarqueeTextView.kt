package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.answufeng.ui.R

/**
 * 跑马灯文本视图，支持水平滚动和自定义速度/方向/暂停时长。
 *
 * ```xml
 * <com.answufeng.ui.widget.AwMarqueeTextView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:marquee_speed="2"
 *     app:marquee_pauseDuration="2000"
 *     app:marquee_direction="left_to_right" />
 * ```
 */
class AwMarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    enum class Direction {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    var speed: Float = 1f

    var pauseDuration: Long = 1000L

    var direction: Direction = Direction.RIGHT_TO_LEFT
        set(value) {
            field = value
            resetScroll()
        }

    private var textWidth: Float = 0f
    private var currentOffset: Float = 0f
    private var isScrolling: Boolean = false
    private var scrollAnimator: ValueAnimator? = null

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwMarqueeTextView)
        speed = ta.getFloat(R.styleable.AwMarqueeTextView_marquee_speed, 1f)
        pauseDuration = ta.getInt(R.styleable.AwMarqueeTextView_marquee_pauseDuration, 1000).toLong()
        direction = when (ta.getInt(R.styleable.AwMarqueeTextView_marquee_direction, 1)) {
            0 -> Direction.LEFT_TO_RIGHT
            else -> Direction.RIGHT_TO_LEFT
        }
        ta.recycle()

        isSingleLine = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startScrolling()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScrolling()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculate()
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        super.onTextChanged(text, start, before, count)
        recalculate()
    }

    override fun onDraw(canvas: Canvas) {
        if (textWidth <= width || !isScrolling) {
            super.onDraw(canvas)
            return
        }

        canvas.save()
        if (direction == Direction.RIGHT_TO_LEFT) {
            canvas.translate(-currentOffset, 0f)
        } else {
            canvas.translate(currentOffset, 0f)
        }
        super.onDraw(canvas)
        canvas.restore()
    }

    private fun recalculate() {
        paint.textSize = textSize
        textWidth = paint.measureText(text.toString())
        resetScroll()
    }

    private fun resetScroll() {
        scrollAnimator?.cancel()
        scrollAnimator = null
        currentOffset = 0f
        invalidate()
    }

    private fun startScrolling() {
        if (textWidth <= width) return
        isScrolling = true
        startScrollCycle()
    }

    private fun stopScrolling() {
        isScrolling = false
        scrollAnimator?.cancel()
        scrollAnimator = null
    }

    private fun startScrollCycle() {
        if (!isScrolling || !isAttachedToWindow) return

        val overflow = textWidth - width
        if (overflow <= 0f) return

        val durationPerPx = 16L / speed.coerceAtLeast(0.1f)
        val scrollDuration = (overflow * durationPerPx).toLong()

        scrollAnimator?.cancel()
        scrollAnimator = ValueAnimator.ofFloat(0f, overflow).apply {
            duration = scrollDuration
            addUpdateListener { animator ->
                currentOffset = animator.animatedValue as Float
                invalidate()
            }
            start()
        }

        postDelayed({
            if (!isScrolling || !isAttachedToWindow) return@postDelayed
            scrollAnimator?.cancel()
            scrollAnimator = null
            currentOffset = 0f
            invalidate()
            postDelayed({
                if (isScrolling && isAttachedToWindow) {
                    startScrollCycle()
                }
            }, pauseDuration)
        }, scrollDuration)
    }
}
