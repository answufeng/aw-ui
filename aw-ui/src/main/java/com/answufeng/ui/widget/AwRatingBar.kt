package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 星级评分控件，支持点击与半星（步进 0.5）。
 */
class AwRatingBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        var maxStars: Int = 5
            set(value) {
                field = value.coerceAtLeast(1)
                requestLayout()
                invalidate()
            }

        var rating: Float = 0f
            set(value) {
                field = value.coerceIn(0f, maxStars.toFloat())
                invalidate()
                onRatingChange?.invoke(field)
            }

        var starSize: Float = 24f.dpFloat
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var starSpacing: Float = 4f.dpFloat
            set(value) {
                field = value
                requestLayout()
                invalidate()
            }

        var filledColor: Int = ContextCompat.getColor(context, R.color.aw_color_primary)
            set(value) {
                field = value
                filledPaint.color = value
                invalidate()
            }

        var emptyColor: Int = 0xFFE0E0E0.toInt()
            set(value) {
                field = value
                emptyPaint.color = value
                invalidate()
            }

        var isIndicator: Boolean = false
            set(value) {
                field = value
            }

        var stepSize: Float = 1f
            set(value) {
                field = value.coerceIn(0.5f, 1f)
            }

        var onRatingChange: ((Float) -> Unit)? = null

        private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val starPath = Path()

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwRatingBar)
            maxStars = ta.getInt(R.styleable.AwRatingBar_rating_maxStars, 5)
            rating = ta.getFloat(R.styleable.AwRatingBar_rating_value, 0f)
            starSize = ta.getDimension(R.styleable.AwRatingBar_rating_starSize, starSize)
            starSpacing = ta.getDimension(R.styleable.AwRatingBar_rating_starSpacing, starSpacing)
            filledColor = ta.getColor(R.styleable.AwRatingBar_rating_filledColor, filledColor)
            emptyColor = ta.getColor(R.styleable.AwRatingBar_rating_emptyColor, emptyColor)
            isIndicator = ta.getBoolean(R.styleable.AwRatingBar_rating_isIndicator, false)
            stepSize = ta.getFloat(R.styleable.AwRatingBar_rating_stepSize, 1f)
            ta.recycle()
            filledPaint.color = filledColor
            emptyPaint.color = emptyColor
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val desiredWidth =
                (maxStars * starSize + (maxStars - 1) * starSpacing + paddingLeft + paddingRight).toInt()
            val desiredHeight = (starSize + paddingTop + paddingBottom).toInt()
            setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec),
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            for (i in 0 until maxStars) {
                val left = paddingLeft + i * (starSize + starSpacing)
                val top = paddingTop.toFloat()
                val fillRatio = (rating - i).coerceIn(0f, 1f)
                drawStar(canvas, left, top, starSize, emptyPaint)
                if (fillRatio > 0f) {
                    canvas.save()
                    canvas.clipRect(left, top, left + starSize * fillRatio, top + starSize)
                    drawStar(canvas, left, top, starSize, filledPaint)
                    canvas.restore()
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (isIndicator || !isEnabled) return super.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    val x = event.x - paddingLeft
                    val starWidth = starSize + starSpacing
                    var index = (x / starWidth).toInt().coerceIn(0, maxStars - 1)
                    val inStarOffset = x - index * starWidth
                    val fraction = if (stepSize < 1f && inStarOffset < starSize / 2f) 0.5f else 1f
                    rating = (index + fraction).coerceAtMost(maxStars.toFloat())
                    if (event.action == MotionEvent.ACTION_UP) {
                        performClick()
                    }
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        private fun drawStar(
            canvas: Canvas,
            left: Float,
            top: Float,
            size: Float,
            paint: Paint,
        ) {
            starPath.reset()
            val cx = left + size / 2f
            val cy = top + size / 2f
            val outer = size / 2f
            val inner = outer * 0.4f
            for (i in 0 until 10) {
                val radius = if (i % 2 == 0) outer else inner
                val angle = Math.toRadians(-90.0 + i * 36.0)
                val x = cx + (radius * Math.cos(angle)).toFloat()
                val y = cy + (radius * Math.sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()
            canvas.drawPath(starPath, paint)
        }
    }
