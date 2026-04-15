package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.answufeng.ui.R
import kotlin.math.min

/**
 * Round/circle image view that clips the drawable to a rounded rectangle or circle shape.
 *
 * Uses [BitmapShader] to render the image within a rounded or circular clip path,
 * with optional border drawing.
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwRoundImageView
 *     android:layout_width="80dp"
 *     android:layout_height="80dp"
 *     android:src="@mipmap/avatar"
 *     app:roundImg_radius="12dp"
 *     app:roundImg_isCircle="false"
 *     app:roundImg_borderWidth="2dp"
 *     app:roundImg_borderColor="#FFFFFF" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * roundImageView.isCircle = true
 * roundImageView.radius = 16f
 * roundImageView.borderWidth = 2f
 * roundImageView.borderColor = Color.WHITE
 * ```
 *
 * @property radius Corner radius in pixels. Ignored when [isCircle] is true.
 * @property isCircle If true, clips to a perfect circle regardless of [radius].
 * @property borderWidth Border width in pixels.
 * @property borderColor Border color.
 *
 * | XML Attribute | Description | Default |
 * |---|---|---|
 * | `roundImg_radius` | Corner radius | 0 |
 * | `roundImg_isCircle` | Clip to circle | false |
 * | `roundImg_borderWidth` | Border width | 0 |
 * | `roundImg_borderColor` | Border color | #FFFFFF |
 */
class AwRoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private val borderRectF = RectF()
    private val matrix = Matrix()

    private var bitmapShader: BitmapShader? = null
    private var bitmapWidth: Int = 0
    private var bitmapHeight: Int = 0

    /**
     * Corner radius in pixels. Ignored when [isCircle] is true.
     */
    var radius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * If true, clips to a perfect circle regardless of [radius].
     */
    var isCircle: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Border width in pixels.
     */
    var borderWidth: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Border color.
     */
    var borderColor: Int = Color.WHITE
        set(value) {
            field = value
            borderPaint.color = value
            invalidate()
        }

    init {
        val density = resources.displayMetrics.density
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwRoundImageView)
        radius = ta.getDimension(R.styleable.AwRoundImageView_roundImg_radius, 0f)
        isCircle = ta.getBoolean(R.styleable.AwRoundImageView_roundImg_isCircle, false)
        borderWidth = ta.getDimension(R.styleable.AwRoundImageView_roundImg_borderWidth, 0f)
        borderColor = ta.getColor(R.styleable.AwRoundImageView_roundImg_borderColor, Color.WHITE)
        ta.recycle()

        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupShader()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        setupShader()
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmapShader == null) {
            super.onDraw(canvas)
            return
        }

        paint.shader = bitmapShader

        if (isCircle) {
            val cx = width / 2f
            val cy = height / 2f
            val r = min(width, height) / 2f - borderWidth / 2f
            canvas.drawCircle(cx, cy, r, paint)
            if (borderWidth > 0f) {
                canvas.drawCircle(cx, cy, r, borderPaint)
            }
        } else {
            val halfBorder = borderWidth / 2f
            rectF.set(halfBorder, halfBorder, width - halfBorder, height - halfBorder)
            canvas.drawRoundRect(rectF, radius, radius, paint)
            if (borderWidth > 0f) {
                borderRectF.set(halfBorder, halfBorder, width - halfBorder, height - halfBorder)
                canvas.drawRoundRect(borderRectF, radius, radius, borderPaint)
            }
        }
    }

    private fun setupShader() {
        val drawable = drawable ?: return
        if (width == 0 || height == 0) return

        val bitmap = drawableToBitmap(drawable) ?: return
        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height
        bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        updateShaderMatrix()
        invalidate()
    }

    private fun updateShaderMatrix() {
        val shader = bitmapShader ?: return
        val scaleX: Float
        val scaleY: Float

        if (isCircle) {
            val viewSize = min(width, height).toFloat()
            scaleX = viewSize / bitmapWidth.toFloat()
            scaleY = viewSize / bitmapHeight.toFloat()
            val scale = maxOf(scaleX, scaleY)
            val dx = (width - bitmapWidth * scale) / 2f
            val dy = (height - bitmapHeight * scale) / 2f
            matrix.setScale(scale, scale)
            matrix.postTranslate(dx, dy)
        } else {
            scaleX = width.toFloat() / bitmapWidth.toFloat()
            scaleY = height.toFloat() / bitmapHeight.toFloat()
            val scale = maxOf(scaleX, scaleY)
            val dx = (width - bitmapWidth * scale) / 2f
            val dy = (height - bitmapHeight * scale) / 2f
            matrix.setScale(scale, scale)
            matrix.postTranslate(dx, dy)
        }

        shader.setLocalMatrix(matrix)
    }

    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap? {
        if (drawable is android.graphics.drawable.BitmapDrawable) {
            return drawable.bitmap
        }
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }
}
