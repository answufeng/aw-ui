package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.AppCompatTextView
import com.answufeng.ui.R
import com.google.android.material.color.MaterialColors
import kotlin.math.min
import kotlin.math.max

/**
 * 角标视图（红点 / 数字 Badge）。
 *
 * 支持三种显示模式：
 * - **count < 0** → 隐藏（GONE）
 * - **count == 0** → 红点（小圆点，无文字）
 * - **count > 0** → 数字角标，超过 99 显示 "99+"
 *
 * 通常配合 [FrameLayout] 叠加在图标右上角使用。
 *
 * ### XML 用法
 * ```xml
 * <FrameLayout ...>
 *     <ImageView ... />
 *     <com.answufeng.ui.widget.AwBadgeView
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         android:layout_gravity="end|top"
 *         app:badge_count="5"
 *         app:badge_bgColor="#FF0000"
 *         app:badge_textColor="#FFFFFF"
 *         app:badge_textSize="10sp" />
 * </FrameLayout>
 * ```
 *
 * ### 代码用法
 * ```kotlin
 * badgeView.count = 3   // 显示数字 "3"
 * badgeView.count = 0   // 显示红点
 * badgeView.count = -1  // 隐藏
 * ```
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `badge_count` | 角标数量 | 0（红点） |
 * | `badge_bgColor` | 背景色 | 红色 |
 * | `badge_textColor` | 文字颜色 | 白色 |
 * | `badge_textSize` | 文字大小 | 10sp |
 */
class AwBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textBounds = Rect()

    /**
     * 角标数量。
     *
     * - 负数：隐藏（GONE）
     * - 0：显示红点
     * - 正数：显示数字，超过 [maxCount] 显示 "${maxCount}+"
     */
    var count: Int = 0
        set(value) {
            field = value
            visibility = if (value < 0) View.GONE else View.VISIBLE
            updateAccessibility(value)
            requestLayout()
            invalidate()
        }

    /**
     * 数字角标显示的最大值，超过后显示 "${maxCount}+"。默认 99。
     */
    var maxCount: Int = 99
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /**
     * 增加角标数量。
     *
     * @param delta 增加值，默认 1
     */
    fun increment(delta: Int = 1) {
        count += delta
    }

    /**
     * 减少角标数量。
     *
     * @param delta 减少值，默认 1
     */
    fun decrement(delta: Int = 1) {
        count = (count - delta).coerceAtLeast(-1)
    }

    /**
     * 清除角标（隐藏）。
     */
    fun clear() {
        count = -1
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwBadgeView)
        count = ta.getInteger(R.styleable.AwBadgeView_badge_count, 0)
        bgPaint.color = ta.getColor(R.styleable.AwBadgeView_badge_bgColor,
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.RED))
        textPaint.color = ta.getColor(R.styleable.AwBadgeView_badge_textColor,
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnError, Color.WHITE))
        textPaint.textSize = ta.getDimension(R.styleable.AwBadgeView_badge_textSize, 10f * resources.displayMetrics.density)
        ta.recycle()

        textPaint.textAlign = Paint.Align.CENTER
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        updateAccessibility(count)
    }

    private fun updateAccessibility(value: Int) {
        contentDescription = when {
            value < 0 -> null
            value == 0 -> "新消息"
            else -> "${value}条新消息"
        }
        if (value >= 0 && isAttachedToWindow) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        if (count == 0) {
            val size = (8 * density).toInt()
            setMeasuredDimension(size, size)
        } else {
            val text = if (count > maxCount) "${maxCount}+" else count.toString()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val h = (16 * density).toInt()
            val w = max(h, textBounds.width() + (10 * density).toInt())
            setMeasuredDimension(w, h)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("count", count)
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
            count = state.getInt("count", 0)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (count == 0) {
            canvas.drawCircle(width / 2f, height / 2f, min(width, height) / 2f, bgPaint)
        } else {
            val text = if (count > maxCount) "${maxCount}+" else count.toString()
            val radius = height / 2f
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, bgPaint)
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, width / 2f, y, textPaint)
        }
    }
}
