package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.widget.skeleton.AwSkeletonShimmer

/**
 * 骨架屏加载视图，带有从左到右扫过的闪光/渐变动画。
 *
 * 精细手拼场景使用；常规页面/列表请优先 [com.answufeng.ui.widget.skeleton.AwSkeletonLayout]。
 */
class AwSkeletonView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private val basePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        private val path = Path()
        private val rectF = RectF()
        private val shimmer = AwSkeletonShimmer(0, 0)

        var baseColor: Int = 0xFFE0E0E0.toInt()
            set(value) {
                field = value
                basePaint.color = value
                shimmer.updateColors(value, highlightColor)
                invalidate()
            }

        var highlightColor: Int = 0xFFF5F5F5.toInt()
            set(value) {
                field = value
                shimmer.updateColors(baseColor, value)
                invalidate()
            }

        var cornerRadius: Float = 4f * resources.displayMetrics.density
            set(value) {
                field = value
                invalidate()
            }

        var animationDuration: Long = 1000L
            set(value) {
                field = value
                shimmer.durationMs = value
                if (isShimmering) {
                    stopShimmer()
                    startShimmer()
                }
            }

        var isShimmering: Boolean = false
            private set

        var autoStart: Boolean = true

        init {
            val density = resources.displayMetrics.density
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwSkeletonView)
            baseColor =
                ta.getColor(
                    R.styleable.AwSkeletonView_skeleton_baseColor,
                    ContextCompat.getColor(context, R.color.aw_color_skeleton_base),
                )
            highlightColor =
                ta.getColor(
                    R.styleable.AwSkeletonView_skeleton_highlightColor,
                    ContextCompat.getColor(context, R.color.aw_color_skeleton_highlight),
                )
            cornerRadius = ta.getDimension(R.styleable.AwSkeletonView_skeleton_cornerRadius, 4f * density)
            animationDuration = ta.getInteger(R.styleable.AwSkeletonView_skeleton_duration, 1000).toLong()
            ta.recycle()

            basePaint.color = baseColor
            shimmer.updateColors(baseColor, highlightColor)
            shimmer.durationMs = animationDuration
            shimmer.onInvalidate = { invalidate() }
        }

        fun startShimmer() {
            if (isShimmering) return
            isShimmering = true
            shimmer.start()
        }

        fun stopShimmer() {
            shimmer.stop()
            isShimmering = false
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            rectF.set(0f, 0f, width.toFloat(), height.toFloat())
            path.reset()
            path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
            canvas.drawPath(path, basePaint)
            shimmer.drawShimmerOnPath(canvas, path, width)
        }

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            shimmer.invalidateShader(w)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (autoStart) startShimmer()
        }

        override fun onDetachedFromWindow() {
            stopShimmer()
            super.onDetachedFromWindow()
        }

        override fun setVisibility(visibility: Int) {
            super.setVisibility(visibility)
            if (visibility == VISIBLE) {
                startShimmer()
            } else {
                stopShimmer()
            }
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putBoolean("isShimmering", isShimmering)
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
                if (state.getBoolean("isShimmering", false)) startShimmer()
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }
