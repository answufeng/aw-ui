package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.app.Activity
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.answufeng.ui.R
import kotlin.math.min

/**
 * 提示框箭头指针位置。
 */
enum class ArrowPosition {
    LEFT,
    TOP,
    RIGHT,
    BOTTOM
}

/**
 * 提示框/气泡视图，带有圆角矩形主体和三角形箭头指针。
 *
 * 设计为以弹出覆盖层的形式显示，锚定到目标视图。
 * 箭头根据 [arrowPosition] 指向锚点视图。
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.widget.AwTooltipView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:tooltip_text="Hello tooltip"
 *     app:tooltip_arrowPosition="bottom"
 *     app:tooltip_bgColor="#333333"
 *     app:tooltip_textColor="#FFFFFF" />
 * ```
 *
 * ### 代码用法
 * ```kotlin
 * tooltipView.text = "Hello"
 * tooltipView.arrowPosition = ArrowPosition.BOTTOM
 * tooltipView.show(anchorView)
 * tooltipView.dismiss()
 * ```
 *
 * @property text 提示框内显示的文本
 * @property arrowPosition 箭头指针出现的一侧
 * @property backgroundColor 提示框主体的背景颜色
 * @property textColor 提示框文本的颜色
 * @property textSize 提示框文本的大小（像素）
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `tooltip_text` | 提示框文本 | "" |
 * | `tooltip_arrowPosition` | 箭头方向（left/top/right/bottom） | bottom |
 * | `tooltip_bgColor` | 背景颜色 | #333333 |
 * | `tooltip_textColor` | 文本颜色 | #FFFFFF |
 */
class AwTooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arrowPath = Path()
    private val bodyRectF = RectF()

    /** 提示框内显示的文本 */
    var text: CharSequence = ""
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /** 箭头指针出现的一侧 */
    var arrowPosition: ArrowPosition = ArrowPosition.BOTTOM
        set(value) {
            field = value
            invalidate()
        }

    /** 提示框主体的背景颜色 */
    var tooltipBackgroundColor: Int = Color.parseColor("#333333")
        set(value) {
            field = value
            bgPaint.color = value
            invalidate()
        }

    /** 提示框文本的颜色 */
    var textColor: Int = Color.WHITE
        set(value) {
            field = value
            textPaint.color = value
            invalidate()
        }

    /** 提示框文本的大小（像素） */
    var textSize: Float = 14f * resources.displayMetrics.density
        set(value) {
            field = value
            textPaint.textSize = value
            requestLayout()
            invalidate()
        }

    private val arrowSize: Float = 10f * resources.displayMetrics.density
    private val cornerRadius: Float = 8f * resources.displayMetrics.density
    private val paddingH: Float = 16f * resources.displayMetrics.density
    private val paddingV: Float = 10f * resources.displayMetrics.density

    private var anchorView: View? = null
    private var parentOverlay: FrameLayout? = null
    private var autoDismissRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    var autoDismissDelay: Long = 0L

    init {
        val density = resources.displayMetrics.density
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwTooltipView)
        text = ta.getString(R.styleable.AwTooltipView_tooltip_text) ?: ""
        val positionInt = ta.getInt(R.styleable.AwTooltipView_tooltip_arrowPosition, 3)
        arrowPosition = ArrowPosition.entries.getOrElse(positionInt) { ArrowPosition.BOTTOM }
        tooltipBackgroundColor = ta.getColor(R.styleable.AwTooltipView_tooltip_bgColor, Color.parseColor("#333333"))
        textColor = ta.getColor(R.styleable.AwTooltipView_tooltip_textColor, Color.WHITE)
        ta.recycle()

        bgPaint.color = tooltipBackgroundColor
        bgPaint.style = Paint.Style.FILL
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER

        visibility = View.GONE
    }

    override fun onDetachedFromWindow() {
        autoDismissRunnable?.let { handler.removeCallbacks(it) }
        autoDismissRunnable = null
        super.onDetachedFromWindow()
    }

    /**
     * 显示提示框并锚定到给定的 [anchor] 视图。
     *
     * 提示框挂在 Activity 的 `android.R.id.content` 下，不依赖锚点父级是否为 [FrameLayout]。
     */
    fun show(anchor: View) {
        val act = anchor.context.findHostActivity() ?: return
        val content = act.findViewById<FrameLayout>(android.R.id.content) ?: return
        this.anchorView = anchor
        this.parentOverlay = content
        if (content.indexOfChild(this) == -1) {
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            elevation = 12f * resources.displayMetrics.density
            content.addView(this, lp)
        }
        alpha = 0f
        visibility = View.INVISIBLE
        val wSpec = View.MeasureSpec.makeMeasureSpec(content.width, View.MeasureSpec.AT_MOST)
        val hSpec = View.MeasureSpec.makeMeasureSpec(content.height, View.MeasureSpec.AT_MOST)
        measure(wSpec, hSpec)
        anchor.post {
            positionRelativeToAnchor(anchor)
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(200).start()
            if (autoDismissDelay > 0) {
                autoDismissRunnable = Runnable { dismiss() }
                handler.postDelayed(autoDismissRunnable!!, autoDismissDelay)
            }
        }
    }

    /** 关闭提示框，带有淡出动画 */
    fun dismiss() {
        autoDismissRunnable?.let { handler.removeCallbacks(it) }
        autoDismissRunnable = null
        animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                visibility = View.GONE
                parentOverlay?.removeView(this)
                parentOverlay = null
                anchorView = null
            }
            .start()
    }

    private fun positionRelativeToAnchor(anchor: View) {
        val parent = parentOverlay ?: return
        val parentLocation = IntArray(2)
        val anchorLocation = IntArray(2)
        parent.getLocationInWindow(parentLocation)
        anchor.getLocationInWindow(anchorLocation)

        val offsetX = anchorLocation[0] - parentLocation[0]
        val offsetY = anchorLocation[1] - parentLocation[1]

        val lp = layoutParams as? FrameLayout.LayoutParams ?: FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        val tooltipW = measuredWidth.coerceAtLeast(suggestedMinimumWidth)
        val tooltipH = measuredHeight.coerceAtLeast(suggestedMinimumHeight)

        when (arrowPosition) {
            ArrowPosition.TOP -> {
                lp.leftMargin = offsetX + anchor.width / 2 - tooltipW / 2
                lp.topMargin = offsetY + anchor.height
            }
            ArrowPosition.BOTTOM -> {
                lp.leftMargin = offsetX + anchor.width / 2 - tooltipW / 2
                lp.topMargin = offsetY - tooltipH
            }
            ArrowPosition.LEFT -> {
                lp.leftMargin = offsetX + anchor.width
                lp.topMargin = offsetY + anchor.height / 2 - tooltipH / 2
            }
            ArrowPosition.RIGHT -> {
                lp.leftMargin = offsetX - tooltipW
                lp.topMargin = offsetY + anchor.height / 2 - tooltipH / 2
            }
        }

        layoutParams = lp
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val textWidth = if (text.isNotEmpty()) textPaint.measureText(text.toString()) else 0f
        val desiredW = (textWidth + paddingH * 2).toInt()
        val desiredH = (textSize + paddingV * 2 + arrowSize).toInt()
        setMeasuredDimension(
            resolveSize(desiredW, widthMeasureSpec),
            resolveSize(desiredH, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        drawBody(canvas, w, h)
        drawArrow(canvas, w, h)
        drawText(canvas, w, h)
    }

    private fun drawBody(canvas: Canvas, w: Float, h: Float) {
        val left: Float
        val top: Float
        val right: Float
        val bottom: Float

        when (arrowPosition) {
            ArrowPosition.BOTTOM -> {
                left = 0f
                top = 0f
                right = w
                bottom = h - arrowSize
            }
            ArrowPosition.TOP -> {
                left = 0f
                top = arrowSize
                right = w
                bottom = h
            }
            ArrowPosition.LEFT -> {
                left = arrowSize
                top = 0f
                right = w
                bottom = h
            }
            ArrowPosition.RIGHT -> {
                left = 0f
                top = 0f
                right = w - arrowSize
                bottom = h
            }
        }

        bodyRectF.set(left, top, right, bottom)
        canvas.drawRoundRect(bodyRectF, cornerRadius, cornerRadius, bgPaint)
    }

    private fun drawArrow(canvas: Canvas, w: Float, h: Float) {
        arrowPath.reset()
        val centerX = w / 2f
        val centerY = h / 2f

        when (arrowPosition) {
            ArrowPosition.BOTTOM -> {
                arrowPath.moveTo(centerX - arrowSize, h - arrowSize)
                arrowPath.lineTo(centerX, h)
                arrowPath.lineTo(centerX + arrowSize, h - arrowSize)
                arrowPath.close()
            }
            ArrowPosition.TOP -> {
                arrowPath.moveTo(centerX - arrowSize, arrowSize)
                arrowPath.lineTo(centerX, 0f)
                arrowPath.lineTo(centerX + arrowSize, arrowSize)
                arrowPath.close()
            }
            ArrowPosition.LEFT -> {
                arrowPath.moveTo(arrowSize, centerY - arrowSize)
                arrowPath.lineTo(0f, centerY)
                arrowPath.lineTo(arrowSize, centerY + arrowSize)
                arrowPath.close()
            }
            ArrowPosition.RIGHT -> {
                arrowPath.moveTo(w - arrowSize, centerY - arrowSize)
                arrowPath.lineTo(w, centerY)
                arrowPath.lineTo(w - arrowSize, centerY + arrowSize)
                arrowPath.close()
            }
        }

        canvas.drawPath(arrowPath, bgPaint)
    }

    private fun drawText(canvas: Canvas, w: Float, h: Float) {
        if (text.isEmpty()) return
        val textStr = text.toString()

        val textCenterX: Float
        val textCenterY: Float

        when (arrowPosition) {
            ArrowPosition.BOTTOM -> {
                textCenterX = w / 2f
                textCenterY = (h - arrowSize) / 2f
            }
            ArrowPosition.TOP -> {
                textCenterX = w / 2f
                textCenterY = arrowSize + (h - arrowSize) / 2f
            }
            ArrowPosition.LEFT -> {
                textCenterX = arrowSize + (w - arrowSize) / 2f
                textCenterY = h / 2f
            }
            ArrowPosition.RIGHT -> {
                textCenterX = (w - arrowSize) / 2f
                textCenterY = h / 2f
            }
        }

        val y = textCenterY - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(textStr, textCenterX, y, textPaint)
    }
}

private fun android.content.Context.findHostActivity(): Activity? {
    var c: android.content.Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
