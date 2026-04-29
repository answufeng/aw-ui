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
 * 圆角/圆形 ImageView，将 drawable 裁剪为圆角矩形或圆形。
 *
 * 使用 [BitmapShader] 在圆角或圆形裁剪路径内渲染图片，
 * 可选绘制边框。
 *
 * ### XML 用法
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
 * ### 代码用法
 * ```kotlin
 * roundImageView.isCircle = true
 * roundImageView.radius = 16f
 * roundImageView.borderWidth = 2f
 * roundImageView.borderColor = Color.WHITE
 * ```
 *
 * @property radius 圆角半径（像素）。当 [isCircle] 为 true 时忽略
 * @property isCircle 如果为 true，则裁剪为完美圆形，忽略 [radius]
 * @property borderWidth 边框宽度（像素）
 * @property borderColor 边框颜色
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `roundImg_radius` | 圆角半径 | 0 |
 * | `roundImg_isCircle` | 是否裁剪为圆形 | false |
 * | `roundImg_borderWidth` | 边框宽度 | 0 |
 * | `roundImg_borderColor` | 边框颜色 | #FFFFFF |
 */
class AwRoundImageView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : AppCompatImageView(context, attrs, defStyleAttr) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rectF = RectF()
        private val borderRectF = RectF()
        private val matrix = Matrix()

        private var bitmapShader: BitmapShader? = null
        private var bitmapWidth: Int = 0
        private var bitmapHeight: Int = 0
        private var cachedBitmap: Bitmap? = null
        private var cachedDrawable: android.graphics.drawable.Drawable? = null

        /** 圆角半径（像素）。当 [isCircle] 为 true 时忽略 */
        var radius: Float = 0f
            set(value) {
                field = value
                invalidate()
            }

        /** 如果为 true，则裁剪为完美圆形，忽略 [radius] */
        var isCircle: Boolean = false
            set(value) {
                field = value
                invalidate()
            }

        /** 边框宽度（像素） */
        var borderWidth: Float = 0f
            set(value) {
                field = value
                invalidate()
            }

        /** 边框颜色 */
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

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
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
                cachedDrawable = null
                cachedBitmap?.recycle()
                cachedBitmap = null
                return drawable.bitmap
            }
            if (drawable is android.graphics.drawable.VectorDrawable) {
                cachedBitmap?.recycle()
                val w = if (width > 0) width else drawable.intrinsicWidth.coerceAtLeast(1)
                val h = if (height > 0) height else drawable.intrinsicHeight.coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, w, h)
                drawable.draw(canvas)
                cachedDrawable = drawable.constantState?.newDrawable() ?: drawable
                cachedBitmap = bitmap
                return bitmap
            }
            if (drawable === cachedDrawable && cachedBitmap != null) {
                return cachedBitmap
            }
            cachedBitmap?.recycle()
            val w = if (width > 0) width else drawable.intrinsicWidth.coerceAtLeast(1)
            val h = if (height > 0) height else drawable.intrinsicHeight.coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            cachedDrawable = drawable.constantState?.newDrawable()?.also {
                it.bounds = drawable.bounds
            } ?: drawable
            cachedBitmap = bitmap
            return bitmap
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            cachedBitmap?.recycle()
            cachedBitmap = null
            cachedDrawable = null
        }
    }
