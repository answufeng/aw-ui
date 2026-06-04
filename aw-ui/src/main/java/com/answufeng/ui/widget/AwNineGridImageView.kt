package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.answufeng.ui.R

/**
 * 九宫格图片布局控件。
 *
 * 支持 1~9 张图片的九宫格展示，类似微信朋友圈的图片布局。
 * 当图片数量超过最大显示数量时，最后一张图片上会显示"+N"的覆盖层。
 * 支持通过 XML 属性设置间距、最大数量、单图宽高比和圆角等。
 *
 * XML 属性：
 * - [R.styleable.AwNineGridImageView_ng_spacing] 图片间距
 * - [R.styleable.AwNineGridImageView_ng_maxCount] 最大显示数量
 * - [R.styleable.AwNineGridImageView_ng_singleImageRatio] 单图宽高比
 * - [R.styleable.AwNineGridImageView_ng_corner_radius] 圆角半径
 */
class AwNineGridImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_SPACING_DP = 4f
        private const val DEFAULT_MAX_COUNT = 9
        private const val DEFAULT_SINGLE_IMAGE_RATIO = 2.0f
        private const val DEFAULT_CORNER_RADIUS_DP = 0f
    }

    /** 图片间距（像素） */
    private var spacing: Int = 0

    /** 最大显示图片数量 */
    private var maxCount: Int = DEFAULT_MAX_COUNT

    /** 单图模式下的宽高比（宽/高） */
    private var singleImageRatio: Float = DEFAULT_SINGLE_IMAGE_RATIO

    /** 圆角半径（像素） */
    private var cornerRadius: Int = 0

    /** 图片 URL 列表 */
    var imageUrls: List<String> = emptyList()
        private set

    /** 图片加载器，由外部设置，用于将 URL 加载到 ImageView 中 */
    var imageLoader: ((imageView: ImageView, url: String) -> Unit)? = null

    /** 图片点击监听器 */
    private var onImageClickListener: ((index: Int) -> Unit)? = null

    /** 超出最大数量时，需要额外显示的数量 */
    private var overflowCount: Int = 0

    /** 覆盖层背景画笔 */
    private val overlayBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 覆盖层文字画笔 */
    private val overlayTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 圆角裁剪路径 */
    private val clipPath = Path()

    /** 圆角矩形区域 */
    private val clipRect = RectF()

    /** 最后一张图片的布局矩形，用于绘制覆盖层 */
    private val lastImageRect = RectF()

    private val displayCount: Int
        get() = minOf(imageUrls.size, maxCount)

    private val columnCount: Int
        get() = when (displayCount) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 2
            else -> 3
        }

    private val rowCount: Int
        get() = when (displayCount) {
            0 -> 0
            1 -> 1
            2 -> 1
            3 -> 1
            4 -> 2
            5, 6 -> 2
            else -> 3
        }

    init {
        val density = context.resources.displayMetrics.density
        val defaultSpacing = (DEFAULT_SPACING_DP * density + 0.5f).toInt()
        val defaultCornerRadius = (DEFAULT_CORNER_RADIUS_DP * density + 0.5f).toInt()

        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.AwNineGridImageView,
            defStyleAttr,
            0
        )
        spacing = ta.getDimensionPixelSize(
            R.styleable.AwNineGridImageView_ng_spacing,
            defaultSpacing
        )
        maxCount = ta.getInt(
            R.styleable.AwNineGridImageView_ng_maxCount,
            DEFAULT_MAX_COUNT
        )
        singleImageRatio = ta.getFloat(
            R.styleable.AwNineGridImageView_ng_singleImageRatio,
            DEFAULT_SINGLE_IMAGE_RATIO
        )
        cornerRadius = ta.getDimensionPixelSize(
            R.styleable.AwNineGridImageView_ng_corner_radius,
            defaultCornerRadius
        )
        ta.recycle()

        overlayBgPaint.color = 0x99000000.toInt()
        overlayBgPaint.style = Paint.Style.FILL

        overlayTextPaint.color = Color.WHITE
        overlayTextPaint.textAlign = Paint.Align.CENTER
        val textSize = 16f * density
        overlayTextPaint.textSize = textSize
    }

    /**
     * 设置图片 URL 列表并刷新布局。
     *
     * @param urls 图片 URL 列表
     */
    fun setImageUrls(urls: List<String>) {
        imageUrls = urls
        overflowCount = if (imageUrls.size > maxCount) imageUrls.size - maxCount else 0
        setupChildren()
        requestLayout()
    }

    /**
     * 设置单图模式下的宽高比。
     *
     * @param ratio 宽高比（宽/高），值越大图片越扁
     */
    fun setSingleImageRatio(ratio: Float) {
        singleImageRatio = ratio
        if (displayCount == 1) {
            requestLayout()
        }
    }

    /**
     * 设置图片点击监听器。
     *
     * @param listener 点击回调，参数为被点击图片的索引
     */
    fun setOnImageClickListener(listener: (index: Int) -> Unit) {
        onImageClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        if (widthMode == MeasureSpec.UNSPECIFIED && widthSize == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val count = displayCount
        if (count == 0) {
            setMeasuredDimension(widthSize, 0)
            return
        }

        val cols = columnCount
        val rows = rowCount
        val totalSpacingW = if (cols > 1) spacing * (cols - 1) else 0
        val totalSpacingH = if (rows > 1) spacing * (rows - 1) else 0

        val availableWidth = widthSize - paddingLeft - paddingRight - totalSpacingW
        val cellWidth = if (cols > 0) availableWidth / cols else 0

        val cellHeight: Int = if (count == 1) {
            // 单图模式：高度 = 宽度 / 宽高比
            if (singleImageRatio > 0) (cellWidth / singleImageRatio).toInt() else cellWidth
        } else {
            cellWidth
        }

        val totalHeight = cellHeight * rows + totalSpacingH + paddingTop + paddingBottom

        // 测量每个子 View
        val childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY)
        val childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                child.measure(childWidthSpec, childHeightSpec)
            }
        }

        setMeasuredDimension(widthSize, resolveSize(totalHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = displayCount
        if (count == 0) return

        val cols = columnCount
        val rows = rowCount
        val totalSpacingW = if (cols > 1) spacing * (cols - 1) else 0
        val totalSpacingH = if (rows > 1) spacing * (rows - 1) else 0

        val availableWidth = width - paddingLeft - paddingRight - totalSpacingW
        val cellWidth = if (cols > 0) availableWidth / cols else 0

        val cellHeight: Int = if (count == 1) {
            if (singleImageRatio > 0) (cellWidth / singleImageRatio).toInt() else cellWidth
        } else {
            cellWidth
        }

        var childIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (childIndex >= count || childIndex >= childCount) break

                val child = getChildAt(childIndex)
                if (child.visibility == GONE) {
                    childIndex++
                    continue
                }

                val left = paddingLeft + col * (cellWidth + spacing)
                val top = paddingTop + row * (cellHeight + spacing)
                val right = left + cellWidth
                val bottom = top + cellHeight

                child.layout(left, top, right, bottom)

                // 记录最后一张图片的矩形区域，用于绘制覆盖层
                if (childIndex == count - 1) {
                    lastImageRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
                }

                childIndex++
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (cornerRadius <= 0f) {
            super.dispatchDraw(canvas)
        } else {
            val drawingTime = getDrawingTime()
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.visibility == GONE) continue

                val saveCount = canvas.save()
                clipPath.reset()
                clipRect.set(
                    child.left.toFloat(), child.top.toFloat(),
                    child.right.toFloat(), child.bottom.toFloat()
                )
                clipPath.addRoundRect(clipRect, cornerRadius.toFloat(), cornerRadius.toFloat(), Path.Direction.CW)
                canvas.clipPath(clipPath)
                drawChild(canvas, child, drawingTime)
                canvas.restoreToCount(saveCount)
            }
        }

        // 绘制超出数量的覆盖层
        if (overflowCount > 0 && !lastImageRect.isEmpty) {
            val saveCount = canvas.save()

            // 裁剪圆角
            if (cornerRadius > 0) {
                clipPath.reset()
                clipPath.addRoundRect(
                    lastImageRect,
                    cornerRadius.toFloat(),
                    cornerRadius.toFloat(),
                    Path.Direction.CW
                )
                canvas.clipPath(clipPath)
            }

            // 绘制半透明背景
            canvas.drawRect(lastImageRect, overlayBgPaint)

            // 绘制 "+N" 文字
            val text = "+$overflowCount"
            val textCenterX = lastImageRect.centerX()
            val textCenterY = lastImageRect.centerY() - (overlayTextPaint.descent() + overlayTextPaint.ascent()) / 2f
            canvas.drawText(text, textCenterX, textCenterY, overlayTextPaint)

            canvas.restoreToCount(saveCount)
        }
    }

    /**
     * 根据图片 URL 列表创建或复用子 View。
     */
    private fun setupChildren() {
        removeAllViews()

        val count = displayCount
        for (i in 0 until count) {
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    onImageClickListener?.invoke(i)
                }
            }
            addView(imageView)
            imageLoader?.invoke(imageView, imageUrls[i])
        }
    }
}
