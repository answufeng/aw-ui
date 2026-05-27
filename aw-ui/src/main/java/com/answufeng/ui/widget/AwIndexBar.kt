package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 侧边字母索引条，常用于联系人、城市列表等场景。
 *
 * ```kotlin
 * indexBar.letters = listOf("A", "B", "C", "#")
 * indexBar.onLetterSelected = { letter, index ->
 *     scrollToSection(letter)
 * }
 * ```
 */
class AwIndexBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        var letters: List<String> =
            ('A'..'Z').map { it.toString() } + listOf("#")
            set(value) {
                field = value.ifEmpty { listOf("#") }
                requestLayout()
                invalidate()
            }

        var onLetterSelected: ((letter: String, index: Int) -> Unit)? = null

        var normalTextColor: Int = 0xFF666666.toInt()
            set(value) {
                field = value
                normalPaint.color = value
                invalidate()
            }

        var selectedTextColor: Int = ContextCompat.getColor(context, R.color.aw_color_primary)
            set(value) {
                field = value
                selectedPaint.color = value
                invalidate()
            }

        var textSizePx: Float = 11f.dpFloat
            set(value) {
                field = value
                normalPaint.textSize = value
                selectedPaint.textSize = value
                requestLayout()
                invalidate()
            }

        private var selectedIndex: Int = -1

        private val normalPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
            }
        private val selectedPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwIndexBar)
            normalTextColor = ta.getColor(R.styleable.AwIndexBar_index_textColor, normalTextColor)
            selectedTextColor = ta.getColor(R.styleable.AwIndexBar_index_selectedTextColor, selectedTextColor)
            textSizePx = ta.getDimension(R.styleable.AwIndexBar_index_textSize, textSizePx)
            ta.recycle()
            normalPaint.color = normalTextColor
            normalPaint.textSize = textSizePx
            selectedPaint.color = selectedTextColor
            selectedPaint.textSize = textSizePx
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val letterHeight = textSizePx * 1.6f
            val desiredHeight = (letters.size * letterHeight + paddingTop + paddingBottom).toInt()
            val desiredWidth = (textSizePx * 2f + paddingLeft + paddingRight).toInt()
            setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec),
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (letters.isEmpty()) return
            val letterHeight = (height - paddingTop - paddingBottom).toFloat() / letters.size
            val cx = paddingLeft + (width - paddingLeft - paddingRight) / 2f
            letters.forEachIndexed { index, letter ->
                val cy = paddingTop + letterHeight * index + letterHeight / 2f
                val paint = if (index == selectedIndex) selectedPaint else normalPaint
                val fm = paint.fontMetrics
                val baseline = cy - (fm.ascent + fm.descent) / 2f
                canvas.drawText(letter, cx, baseline, paint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val index = letterIndexAt(event.y)
                    if (index in letters.indices && index != selectedIndex) {
                        selectedIndex = index
                        invalidate()
                        onLetterSelected?.invoke(letters[index], index)
                    }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    selectedIndex = -1
                    invalidate()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        private fun letterIndexAt(y: Float): Int {
            val contentHeight = height - paddingTop - paddingBottom
            if (contentHeight <= 0) return -1
            val relativeY = (y - paddingTop).coerceIn(0f, contentHeight.toFloat())
            return (relativeY / contentHeight * letters.size).toInt().coerceIn(0, letters.lastIndex)
        }
    }
