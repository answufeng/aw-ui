package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.AppCompatTextView
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat
import kotlin.math.max
import kotlin.math.min

class AwBadgeView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatTextView(context, attrs, defStyleAttr) {
        enum class Mode {
            HIDDEN,
            DOT,
            COUNT,
            TEXT,
        }

        private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textBounds = Rect()

        var mode: Mode = Mode.DOT
            private set

        var count: Int = 0
            set(value) {
                field = value
                mode =
                    when {
                        value < 0 -> Mode.HIDDEN
                        value == 0 -> Mode.DOT
                        else -> Mode.COUNT
                    }
                updateVisibilityAndAccessibility()
                requestLayout()
                invalidate()
            }

        var textBadge: String? = null
            private set

        var maxCount: Int = 99
            set(value) {
                field = value.coerceAtLeast(1)
                requestLayout()
                invalidate()
            }

        var badgeColor: Int
            get() = backgroundPaint.color
            set(value) {
                backgroundPaint.color = value
                invalidate()
            }

        var badgeTextColor: Int
            get() = badgeTextPaint.color
            set(value) {
                badgeTextPaint.color = value
                invalidate()
            }

        var badgeTextSizePx: Float
            get() = badgeTextPaint.textSize
            set(value) {
                badgeTextPaint.textSize = value.coerceAtLeast(8f.dpFloat)
                requestLayout()
                invalidate()
            }

        var dotSizePx: Float = 8f.dpFloat
            set(value) {
                field = value.coerceAtLeast(4f.dpFloat)
                requestLayout()
                invalidate()
            }

        var minBadgeHeightPx: Float = 18f.dpFloat
            set(value) {
                field = value.coerceAtLeast(dotSizePx)
                requestLayout()
                invalidate()
            }

        var horizontalPaddingPx: Float = 6f.dpFloat
            set(value) {
                field = value.coerceAtLeast(2f.dpFloat)
                requestLayout()
                invalidate()
            }

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBadgeView)
            badgeColor =
                ta.getColor(
                    R.styleable.AwBadgeView_badge_bgColor,
                    Color.RED,
                )
            badgeTextColor =
                ta.getColor(
                    R.styleable.AwBadgeView_badge_textColor,
                    Color.WHITE,
                )
            badgeTextSizePx = ta.getDimension(R.styleable.AwBadgeView_badge_textSize, 10f.sp())
            dotSizePx = ta.getDimension(R.styleable.AwBadgeView_badge_dotSize, 8f.dpFloat)
            minBadgeHeightPx = ta.getDimension(R.styleable.AwBadgeView_badge_minHeight, 18f.dpFloat)
            horizontalPaddingPx = ta.getDimension(R.styleable.AwBadgeView_badge_horizontalPadding, 6f.dpFloat)
            val textValue = ta.getString(R.styleable.AwBadgeView_badge_text)
            val countValue = ta.getInteger(R.styleable.AwBadgeView_badge_count, 0)
            ta.recycle()

            badgeTextPaint.textAlign = Paint.Align.CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

            if (!textValue.isNullOrBlank()) {
                setBadgeText(textValue)
            } else {
                count = countValue
            }
        }

        fun showDot() {
            textBadge = null
            count = 0
        }

        fun setBadgeText(text: String?) {
            if (text.isNullOrBlank()) {
                clear()
                return
            }
            textBadge = text
            mode = Mode.TEXT
            updateVisibilityAndAccessibility()
            requestLayout()
            invalidate()
        }

        fun increment(delta: Int = 1) {
            textBadge = null
            count = (if (mode == Mode.HIDDEN) 0 else count) + delta
        }

        fun decrement(delta: Int = 1) {
            textBadge = null
            count = (count - delta).coerceAtLeast(-1)
        }

        fun clear() {
            textBadge = null
            count = -1
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val (desiredWidth, desiredHeight) =
                when (mode) {
                    Mode.HIDDEN -> 0 to 0
                    Mode.DOT -> {
                        val size = dotSizePx.toInt()
                        size to size
                    }
                    Mode.COUNT, Mode.TEXT -> {
                        val text = displayText()
                        badgeTextPaint.getTextBounds(text, 0, text.length, textBounds)
                        val height = minBadgeHeightPx.toInt()
                        val width = max(height, (textBounds.width() + horizontalPaddingPx * 2).toInt())
                        width to height
                    }
                }

            setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec),
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            when (mode) {
                Mode.HIDDEN -> Unit
                Mode.DOT -> canvas.drawCircle(width / 2f, height / 2f, min(width, height) / 2f, backgroundPaint)
                Mode.COUNT, Mode.TEXT -> {
                    val radius = height / 2f
                    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, backgroundPaint)
                    val text = displayText()
                    val baseline = height / 2f - (badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f
                    canvas.drawText(text, width / 2f, baseline, badgeTextPaint)
                }
            }
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
                putInt(KEY_COUNT, count)
                putString(KEY_TEXT, textBadge)
                putString(KEY_MODE, mode.name)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state !is Bundle) {
                super.onRestoreInstanceState(state)
                return
            }
            val superState =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    state.getParcelable(KEY_SUPER_STATE, Parcelable::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    state.getParcelable(KEY_SUPER_STATE)
                }
            super.onRestoreInstanceState(superState)
            val restoredMode = state.getString(KEY_MODE)?.let { Mode.valueOf(it) } ?: Mode.DOT
            val restoredText = state.getString(KEY_TEXT)
            if (restoredMode == Mode.TEXT && !restoredText.isNullOrBlank()) {
                setBadgeText(restoredText)
            } else {
                count = state.getInt(KEY_COUNT, 0)
            }
        }

        private fun displayText(): String {
            return when (mode) {
                Mode.TEXT -> textBadge.orEmpty()
                Mode.COUNT -> if (count > maxCount) "$maxCount+" else count.toString()
                else -> ""
            }
        }

        private fun updateVisibilityAndAccessibility() {
            visibility = if (mode == Mode.HIDDEN) GONE else VISIBLE
            contentDescription =
                when (mode) {
                    Mode.HIDDEN -> null
                    Mode.DOT -> "有新提醒"
                    Mode.COUNT -> "${displayText()}条新消息"
                    Mode.TEXT -> displayText()
                }
            if (visibility == VISIBLE && isAttachedToWindow) {
                sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
            }
        }

        private fun Float.sp(): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, resources.displayMetrics)

        companion object {
            private const val KEY_SUPER_STATE = "superState"
            private const val KEY_COUNT = "count"
            private const val KEY_TEXT = "text"
            private const val KEY_MODE = "mode"
        }
    }
