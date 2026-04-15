package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.answufeng.ui.R

/**
 * Auto-scrolling marquee text view.
 *
 * When the text content overflows the view width, the text scrolls
 * automatically in the configured [direction]. The animation pauses
 * at each end for [pauseDuration] milliseconds before restarting.
 *
 * Starts automatically when attached to a window and stops when detached.
 * Uses [postInvalidateDelayed] for the animation loop.
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwMarqueeTextView
 *     android:layout_width="200dp"
 *     android:layout_height="wrap_content"
 *     android:text="This is a very long text that will scroll"
 *     app:marquee_speed="1"
 *     app:marquee_pauseDuration="1000"
 *     app:marquee_direction="right_to_left" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * marqueeView.speed = 2f
 * marqueeView.direction = AwMarqueeTextView.Direction.RIGHT_TO_LEFT
 * ```
 *
 * | XML attribute | Description | Default |
 * |---|---|---|
 * | `marquee_speed` | Scroll speed in pixels per frame | 1 |
 * | `marquee_pauseDuration` | Pause at end before restarting (ms) | 1000 |
 * | `marquee_direction` | Scroll direction enum (left_to_right / right_to_left) | right_to_left |
 */
class AwMarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * Scroll direction.
     */
    enum class Direction {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    /** Scroll speed in pixels per frame. */
    var speed: Float = 1f

    /** Pause duration at each end before restarting, in milliseconds. */
    var pauseDuration: Long = 1000L

    /** Scroll direction. */
    var direction: Direction = Direction.RIGHT_TO_LEFT
        set(value) {
            field = value
            resetScroll()
        }

    private var textWidth: Float = 0f

    private var currentOffset: Float = 0f

    private var isScrolling: Boolean = false

    private var isPaused: Boolean = false

    private val frameDelay = 16L

    private val scrollRunnable = Runnable { advanceScroll() }

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
        currentOffset = 0f
        isPaused = false
        invalidate()
    }

    private fun startScrolling() {
        if (textWidth <= width) return
        isScrolling = true
        isPaused = false
        postDelayed(scrollRunnable, frameDelay)
    }

    private fun stopScrolling() {
        isScrolling = false
        isPaused = false
        removeCallbacks(scrollRunnable)
    }

    private fun advanceScroll() {
        if (!isScrolling || !isAttachedToWindow) return

        if (isPaused) return

        val overflow = textWidth - width
        if (overflow <= 0f) return

        currentOffset += speed

        if (currentOffset >= overflow) {
            currentOffset = overflow
            isPaused = true
            postDelayed({
                currentOffset = 0f
                isPaused = false
                postInvalidateDelayed(frameDelay)
                postDelayed(scrollRunnable, frameDelay)
            }, pauseDuration)
            invalidate()
            return
        }

        invalidate()
        postInvalidateDelayed(frameDelay)
    }
}
