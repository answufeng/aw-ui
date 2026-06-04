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
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 水平进度条，支持确定模式和不确模式。
 *
 * 确定模式显示当前进度（0~max），支持辅助进度和文本显示。
 * 不确定模式显示来回平移的动画条。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwHorizontalProgressBar
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:hpb_progress="50"
 *     app:hpb_max="100"
 *     app:hpb_showText="true" />
 * ```
 */
class AwHorizontalProgressBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {

        var progress: Int = 0
            set(v) {
                val clamped = v.coerceIn(0, max)
                if (field != clamped) {
                    if (animateChanges) {
                        animateProgress(field, clamped)
                    }
                    field = clamped
                    invalidate()
                    onProgressChange?.invoke(field)
                }
            }

        var max: Int = 100
            set(v) {
                field = v.coerceAtLeast(1)
                progress = progress.coerceIn(0, field)
                invalidate()
            }

        var progressColor: Int = ContextCompat.getColor(context, R.color.aw_color_progress)
            set(v) {
                field = v
                invalidate()
            }

        var trackColor: Int = ContextCompat.getColor(context, R.color.aw_color_progress_track)
            set(v) {
                field = v
                invalidate()
            }

        var progressHeight: Float = 6f.dpFloat
            set(v) {
                field = v
                requestLayout()
            }

        var cornerRadius: Float = 3f.dpFloat
            set(v) {
                field = v
                invalidate()
            }

        var showText: Boolean = false
            set(v) {
                field = v
                requestLayout()
                invalidate()
            }

        var textColor: Int = ContextCompat.getColor(context, R.color.aw_color_progress_text)
            set(v) {
                field = v
                invalidate()
            }

        var textSize: Float = 11f.dpFloat
            set(v) {
                field = v
                invalidate()
            }

        var indeterminate: Boolean = false
            set(v) {
                field = v
                if (v) startIndeterminateAnim() else stopIndeterminateAnim()
                invalidate()
            }

        var indeterminateDuration: Int = 1500
            set(v) {
                field = v.coerceAtLeast(200)
                if (indeterminate) {
                    stopIndeterminateAnim()
                    startIndeterminateAnim()
                }
            }

        var indeterminateColor: Int = ContextCompat.getColor(context, R.color.aw_color_indeterminate)
            set(v) {
                field = v
                invalidate()
            }

        var showProgressOnIndeterminate: Boolean = false
        var animateChanges: Boolean = true
        var animDuration: Int = 300

        var secondaryProgress: Int = 0
            set(v) {
                field = v.coerceIn(0, max)
                invalidate()
            }

        var secondaryProgressColor: Int = ContextCompat.getColor(context, R.color.aw_color_secondary_progress)
            set(v) {
                field = v
                invalidate()
            }

        var onProgressChange: ((Int) -> Unit)? = null

        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val rectF = RectF()

        private var indeterminateAnimator: ValueAnimator? = null
        private var indeterminateOffset: Float = 0f
        private var animatingProgress: Float = 0f
        private var progressAnimator: ValueAnimator? = null

        init {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwHorizontalProgressBar)
            progress = ta.getInt(R.styleable.AwHorizontalProgressBar_hpb_progress, 0)
            max = ta.getInt(R.styleable.AwHorizontalProgressBar_hpb_max, 100)
            progressColor = ta.getColor(R.styleable.AwHorizontalProgressBar_hpb_progressColor, progressColor)
            trackColor = ta.getColor(R.styleable.AwHorizontalProgressBar_hpb_trackColor, trackColor)
            progressHeight = ta.getDimension(R.styleable.AwHorizontalProgressBar_hpb_progressHeight, progressHeight)
            cornerRadius = ta.getDimension(R.styleable.AwHorizontalProgressBar_hpb_cornerRadius, cornerRadius)
            showText = ta.getBoolean(R.styleable.AwHorizontalProgressBar_hpb_showText, false)
            textColor = ta.getColor(R.styleable.AwHorizontalProgressBar_hpb_textColor, textColor)
            textSize = ta.getDimension(R.styleable.AwHorizontalProgressBar_hpb_textSize, textSize)
            indeterminate = ta.getBoolean(R.styleable.AwHorizontalProgressBar_hpb_indeterminate, false)
            indeterminateDuration = ta.getInt(R.styleable.AwHorizontalProgressBar_hpb_indeterminateDuration, 1500)
            indeterminateColor = ta.getColor(R.styleable.AwHorizontalProgressBar_hpb_indeterminateColor, indeterminateColor)
            showProgressOnIndeterminate = ta.getBoolean(R.styleable.AwHorizontalProgressBar_hpb_showProgressOnIndeterminate, false)
            animateChanges = ta.getBoolean(R.styleable.AwHorizontalProgressBar_hpb_animateChanges, true)
            animDuration = ta.getInt(R.styleable.AwHorizontalProgressBar_hpb_animDuration, 300)
            secondaryProgress = ta.getInt(R.styleable.AwHorizontalProgressBar_hpb_secondaryProgress, 0)
            secondaryProgressColor = ta.getColor(R.styleable.AwHorizontalProgressBar_hpb_secondaryProgressColor, secondaryProgressColor)
            ta.recycle()

            animatingProgress = progress.toFloat()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val textHeight = if (showText) textSize + 4f.dpFloat else 0f
            val totalHeight = progressHeight.coerceAtLeast(textHeight) + paddingTop + paddingBottom
            val w = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
            val h = resolveSize(totalHeight.toInt(), heightMeasureSpec)
            setMeasuredDimension(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat() - paddingLeft - paddingRight
            val h = height.toFloat()
            val barTop = (h - progressHeight) / 2f
            val barLeft = paddingLeft.toFloat()

            // 轨道
            rectF.set(barLeft, barTop, barLeft + w, barTop + progressHeight)
            trackPaint.color = trackColor
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, trackPaint)

            if (indeterminate && !showProgressOnIndeterminate) {
                // 不确定模式：动画小条来回滑动
                val barWidth = w * 0.3f
                val offset = indeterminateOffset * (w + barWidth) - barWidth
                rectF.set(barLeft + offset, barTop, barLeft + offset + barWidth, barTop + progressHeight)
                progressPaint.color = indeterminateColor
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, progressPaint)
            } else {
                // 辅助进度
                if (secondaryProgress > 0) {
                    val secW = w * secondaryProgress / max
                    rectF.set(barLeft, barTop, barLeft + secW, barTop + progressHeight)
                    secondaryPaint.color = secondaryProgressColor
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, secondaryPaint)
                }

                // 主进度
                val pW = w * animatingProgress / max
                if (pW > 0) {
                    rectF.set(barLeft, barTop, barLeft + pW, barTop + progressHeight)
                    progressPaint.color = progressColor
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, progressPaint)
                }

                // 文本
                if (showText) {
                    textPaint.color = textColor
                    textPaint.textSize = textSize
                    textPaint.textAlign = Paint.Align.RIGHT
                    val text = "${(animatingProgress / max * 100).toInt()}%"
                    val textY = (h + textSize / 3f) / 2f
                    canvas.drawText(text, barLeft + w, textY, textPaint)
                }
            }
        }

        private fun animateProgress(from: Int, to: Int) {
            progressAnimator?.cancel()
            progressAnimator = ValueAnimator.ofFloat(from.toFloat(), to.toFloat()).apply {
                duration = animDuration.toLong()
                interpolator = DecelerateInterpolator()
                addUpdateListener { anim ->
                    animatingProgress = anim.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        private fun startIndeterminateAnim() {
            stopIndeterminateAnim()
            indeterminateAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = indeterminateDuration.toLong()
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener { anim ->
                    indeterminateOffset = anim.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        private fun stopIndeterminateAnim() {
            indeterminateAnimator?.cancel()
            indeterminateAnimator = null
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putInt("hpb_progress", progress)
                putInt("hpb_max", max)
                putInt("hpb_secondaryProgress", secondaryProgress)
            }
        }

        override fun onRestoreInstanceState(state: Parcelable?) {
            if (state is Bundle) {
                super.onRestoreInstanceState(state.getParcelable("superState"))
                max = state.getInt("hpb_max", 100)
                progress = state.getInt("hpb_progress", 0)
                secondaryProgress = state.getInt("hpb_secondaryProgress", 0)
                animatingProgress = progress.toFloat()
            } else {
                super.onRestoreInstanceState(state)
            }
        }
    }


