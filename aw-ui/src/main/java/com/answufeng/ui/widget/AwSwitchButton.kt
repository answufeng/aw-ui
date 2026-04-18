package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.answufeng.ui.R

/**
 * Custom switch button with smooth thumb animation.
 *
 * Draws a rounded track and a circular thumb in [onDraw].
 * The track color transitions between checked and unchecked states,
 * and the thumb slides with a [ValueAnimator].
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwSwitchButton
 *     android:layout_width="52dp"
 *     android:layout_height="28dp"
 *     app:switch_checked="false"
 *     app:switch_trackColor="#CCCCCC"
 *     app:switch_trackCheckedColor="#4CAF50"
 *     app:switch_thumbColor="#FFFFFF"
 *     app:switch_thumbCheckedColor="#FFFFFF" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * switchButton.isChecked = true
 * switchButton.onCheckedChangeListener = { checked -> ... }
 * ```
 *
 * | XML attribute | Description | Default |
 * |---|---|---|
 * | `switch_checked` | Initial checked state | false |
 * | `switch_trackColor` | Track color when unchecked | #CCCCCC |
 * | `switch_trackCheckedColor` | Track color when checked | #4CAF50 |
 * | `switch_thumbColor` | Thumb color when unchecked | white |
 * | `switch_thumbCheckedColor` | Thumb color when checked | white |
 */
class AwSwitchButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Whether the switch is currently checked. */
    var isChecked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                animateThumb()
                invalidate()
                announceForAccessibility(
                    if (value) "Switch on" else "Switch off"
                )
            }
        }

    /** Callback invoked when the checked state changes. */
    var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    /** Track color when unchecked. */
    var trackColor: Int = Color.parseColor("#CCCCCC")
        set(value) {
            field = value
            invalidate()
        }

    /** Track color when checked. */
    var trackCheckedColor: Int = Color.parseColor("#4CAF50")
        set(value) {
            field = value
            invalidate()
        }

    /** Thumb color when unchecked. */
    var thumbColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    /** Thumb color when checked. */
    var thumbCheckedColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackRect = RectF()

    private var thumbPosition: Float = 0f

    private var thumbAnimator: ValueAnimator? = null

    private val animDuration = 250L

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSwitchButton)
        isChecked = ta.getBoolean(R.styleable.AwSwitchButton_switch_checked, false)
        trackColor = ta.getColor(R.styleable.AwSwitchButton_switch_trackColor, Color.parseColor("#CCCCCC"))
        trackCheckedColor = ta.getColor(R.styleable.AwSwitchButton_switch_trackCheckedColor, Color.parseColor("#4CAF50"))
        thumbColor = ta.getColor(R.styleable.AwSwitchButton_switch_thumbColor, Color.WHITE)
        thumbCheckedColor = ta.getColor(R.styleable.AwSwitchButton_switch_thumbCheckedColor, Color.WHITE)
        ta.recycle()

        thumbPosition = if (isChecked) 1f else 0f

        isClickable = true
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putBoolean("isChecked", isChecked)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState: Parcelable? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                state.getParcelable("superState", Parcelable::class.java)
            } else {
                @Suppress("DEPRECATION")
                state.getParcelable("superState")
            }
            super.onRestoreInstanceState(superState)
            isChecked = state.getBoolean("isChecked", false)
            thumbPosition = if (isChecked) 1f else 0f
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (52 * resources.displayMetrics.density).toInt()
        val desiredHeight = (28 * resources.displayMetrics.density).toInt()

        val w = resolveSize(desiredWidth, widthMeasureSpec)
        val h = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = thumbRadius() * 0.1f
        trackRect.set(padding, padding, width - padding, height - padding)
        val cornerRadius = (height - 2 * padding) / 2f

        trackPaint.color = blendColor(trackColor, trackCheckedColor, thumbPosition)
        canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, trackPaint)

        thumbPaint.color = blendColor(thumbColor, thumbCheckedColor, thumbPosition)

        val thumbCx = padding + thumbRadius() + thumbPosition * (width - 2 * padding - 2 * thumbRadius())
        val thumbCy = height / 2f
        canvas.drawCircle(thumbCx, thumbCy, thumbRadius(), thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                isChecked = !isChecked
                onCheckedChangeListener?.invoke(isChecked)
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun thumbRadius(): Float {
        val padding = height * 0.05f
        return (height - 2 * padding) / 2f * 0.85f
    }

    private fun animateThumb() {
        thumbAnimator?.cancel()

        val start = thumbPosition
        val end = if (isChecked) 1f else 0f

        thumbAnimator = ValueAnimator.ofFloat(start, end).apply {
            duration = animDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                thumbPosition = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun blendColor(fromColor: Int, toColor: Int, ratio: Float): Int {
        val invRatio = 1f - ratio
        val r = (Color.red(fromColor) * invRatio + Color.red(toColor) * ratio).toInt()
        val g = (Color.green(fromColor) * invRatio + Color.green(toColor) * ratio).toInt()
        val b = (Color.blue(fromColor) * invRatio + Color.blue(toColor) * ratio).toInt()
        val a = (Color.alpha(fromColor) * invRatio + Color.alpha(toColor) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }
}
