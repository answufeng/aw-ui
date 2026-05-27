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
import androidx.core.content.ContextCompat
import com.answufeng.ui.R

class AwSwitchButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        var isChecked: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    animateThumb()
                    invalidate()
                    announceForAccessibility(
                        if (value) "Switch on" else "Switch off",
                    )
                }
            }

        var onCheckedChangeListener: ((Boolean) -> Unit)? = null

        var trackColor: Int = 0xFFCCCCCC.toInt()
            set(value) {
                field = value
                invalidate()
            }

        var trackCheckedColor: Int = 0xFF4CAF50.toInt()
            set(value) {
                field = value
                invalidate()
            }

        var thumbColor: Int = Color.WHITE
            set(value) {
                field = value
                invalidate()
            }

        var thumbCheckedColor: Int = Color.WHITE
            set(value) {
                field = value
                invalidate()
            }

        var thumbShadowEnabled: Boolean = true
            set(value) {
                field = value
                invalidate()
            }

        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val shadowPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x33000000
                style = Paint.Style.FILL
            }
        private val trackRect = RectF()

        private var thumbPosition: Float = 0f

        private var thumbAnimator: ValueAnimator? = null

        private val animDuration = 250L

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSwitchButton)
            isChecked = ta.getBoolean(R.styleable.AwSwitchButton_switch_checked, false)
            trackColor =
                ta.getColor(
                    R.styleable.AwSwitchButton_switch_trackColor,
                    ContextCompat.getColor(context, R.color.aw_color_switch_track),
                )
            trackCheckedColor =
                ta.getColor(
                    R.styleable.AwSwitchButton_switch_trackCheckedColor,
                    ContextCompat.getColor(context, R.color.aw_color_switch_checked),
                )
            thumbColor = ta.getColor(R.styleable.AwSwitchButton_switch_thumbColor, Color.WHITE)
            thumbCheckedColor = ta.getColor(R.styleable.AwSwitchButton_switch_thumbCheckedColor, Color.WHITE)
            thumbShadowEnabled = ta.getBoolean(R.styleable.AwSwitchButton_switch_thumbShadowEnabled, true)
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
                val superState: Parcelable? =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
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

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val desiredWidth = (52 * resources.displayMetrics.density).toInt()
            val desiredHeight = (28 * resources.displayMetrics.density).toInt()

            val w = resolveSize(desiredWidth, widthMeasureSpec)
            val h = resolveSize(desiredHeight, heightMeasureSpec)
            setMeasuredDimension(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val tp = trackPadding
            trackRect.set(tp, tp, width - tp, height - tp)
            val cornerRadius = (height - 2 * tp) / 2f

            trackPaint.color = blendColor(trackColor, trackCheckedColor, thumbPosition)
            canvas.drawRoundRect(trackRect, cornerRadius, cornerRadius, trackPaint)

            val ti = thumbInset
            val tr = thumbRadius
            val thumbCx = tp + ti + tr + thumbPosition * (width - 2 * tp - 2 * ti - 2 * tr)
            val thumbCy = height / 2f

            if (thumbShadowEnabled) {
                val shadowOffset = 0.8f * resources.displayMetrics.density
                val shadowRadius = tr + 0.5f * resources.displayMetrics.density
                shadowPaint.alpha = 40
                canvas.drawCircle(thumbCx, thumbCy + shadowOffset, shadowRadius, shadowPaint)
            }

            thumbPaint.color = blendColor(thumbColor, thumbCheckedColor, thumbPosition)
            canvas.drawCircle(thumbCx, thumbCy, tr, thumbPaint)
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

        private val trackPadding: Float
            get() = height * 0.10f

        private val thumbInset: Float
            get() = height * 0.10f

        private val thumbRadius: Float
            get() = (height - 2 * trackPadding) / 2f - thumbInset

        private fun animateThumb() {
            thumbAnimator?.cancel()

            val start = thumbPosition
            val end = if (isChecked) 1f else 0f

            thumbAnimator =
                ValueAnimator.ofFloat(start, end).apply {
                    duration = animDuration
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        thumbPosition = animation.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
        }

        private fun blendColor(
            fromColor: Int,
            toColor: Int,
            ratio: Float,
        ): Int {
            val invRatio = 1f - ratio
            val r = (Color.red(fromColor) * invRatio + Color.red(toColor) * ratio).toInt()
            val g = (Color.green(fromColor) * invRatio + Color.green(toColor) * ratio).toInt()
            val b = (Color.blue(fromColor) * invRatio + Color.blue(toColor) * ratio).toInt()
            val a = (Color.alpha(fromColor) * invRatio + Color.alpha(toColor) * ratio).toInt()
            return Color.argb(a, r, g, b)
        }
    }
