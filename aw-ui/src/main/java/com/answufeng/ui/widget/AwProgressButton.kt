package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.answufeng.ui.R

/**
 * 带进度指示器的按钮控件。
 *
 * 适用于登录、下载、上传等需要展示进度的场景。
 * 支持确定进度模式（0~100）和不确定进度模式（动画条纹滚动）。
 * 当进度达到 100 时，会自动触发 [onProgressCompleteListener] 回调。
 */
class AwProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_TEXT_SIZE_SP = 14f
        private const val DEFAULT_CORNER_RADIUS_DP = 8f
        private const val INDETERMINATE_STRIP_WIDTH_RATIO = 0.3f
        private const val INDETERMINATE_ANIMATION_DURATION = 1200L
    }

    // region 属性

    /** 按钮文本 */
    var text: String = ""
        set(value) {
            field = value
            invalidate()
        }

    /** 进度值，范围 0~100 */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
            if (field >= 100f && !hasCompleted) {
                hasCompleted = true
                onProgressCompleteListener?.invoke()
            } else if (field < 100f) {
                hasCompleted = false
            }
        }

    /** 是否为不确定进度模式 */
    var isIndeterminate: Boolean = false
        set(value) {
            field = value
            if (value) startProgress() else stopProgress()
            invalidate()
        }

    // endregion

    // region 画笔与内部变量

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()

    private var cornerRadius: Float = 0f
    private var onProgressCompleteListener: (() -> Unit)? = null
    private var indeterminateAnimator: ValueAnimator? = null
    private var indeterminateOffset: Float = 0f
    private var isIndeterminateRunning: Boolean = false
    private var hasCompleted: Boolean = false

    // endregion

    // region 初始化

    init {
        val density = resources.displayMetrics.density
        val defaultTextSize = DEFAULT_TEXT_SIZE_SP * density
        val defaultCornerRadius = DEFAULT_CORNER_RADIUS_DP * density

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.AwProgressButton, defStyleAttr, 0
        )

        try {
            text = typedArray.getString(R.styleable.AwProgressButton_pb_text) ?: ""
            val textColor = typedArray.getColor(
                R.styleable.AwProgressButton_pb_textColor, 0xFFFFFFFF.toInt()
            )
            val textSize = typedArray.getDimension(
                R.styleable.AwProgressButton_pb_textSize, defaultTextSize
            )
            val progressColor = typedArray.getColor(
                R.styleable.AwProgressButton_pb_progressColor, 0xFF1E88E5.toInt()
            )
            val backgroundColor = typedArray.getColor(
                R.styleable.AwProgressButton_pb_backgroundColor, 0xFFBDBDBD.toInt()
            )
            cornerRadius = typedArray.getDimension(
                R.styleable.AwProgressButton_pb_cornerRadius, defaultCornerRadius
            )
            progress = typedArray.getFloat(R.styleable.AwProgressButton_pb_progress, 0f)
            isIndeterminate = typedArray.getBoolean(
                R.styleable.AwProgressButton_pb_indeterminate, false
            )

            backgroundPaint.color = backgroundColor
            progressPaint.color = progressColor
            textPaint.color = textColor
            textPaint.textSize = textSize
        } finally {
            typedArray.recycle()
        }

        isClickable = true
    }

    // endregion

    // region 绘制

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        rectF.set(0f, 0f, width, height)

        // 1. 绘制圆角矩形背景
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)

        // 2. 绘制进度填充
        if (isIndeterminate && isIndeterminateRunning) {
            drawIndeterminateProgress(canvas, width, height)
        } else if (progress > 0f) {
            drawDeterminantProgress(canvas, width, height)
        }

        // 3. 绘制居中文本
        if (text.isNotEmpty()) {
            val textY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, width / 2f, textY, textPaint)
        }
    }

    private fun drawDeterminantProgress(canvas: Canvas, width: Float, height: Float) {
        val progressWidth = width * (progress / 100f)
        canvas.save()
        rectF.set(0f, 0f, progressWidth, height)
        canvas.clipRect(0f, 0f, progressWidth, height)
        canvas.drawRoundRect(
            RectF(0f, 0f, width, height),
            cornerRadius, cornerRadius, progressPaint
        )
        canvas.restore()
    }

    private fun drawIndeterminateProgress(canvas: Canvas, width: Float, height: Float) {
        val stripWidth = width * INDETERMINATE_STRIP_WIDTH_RATIO
        val offset = indeterminateOffset * (width + stripWidth)
        val left = -stripWidth + offset
        val right = left + stripWidth

        canvas.save()
        rectF.set(0f, 0f, width, height)
        canvas.clipRect(0f, 0f, width, height)
        canvas.drawRoundRect(
            RectF(left, 0f, right, height),
            cornerRadius, cornerRadius, progressPaint
        )
        canvas.restore()
    }

    // endregion

    // region 公开 API

    /** 开始不确定进度动画 */
    fun startProgress() {
        if (indeterminateAnimator?.isRunning == true) return

        isIndeterminateRunning = true
        indeterminateAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = INDETERMINATE_ANIMATION_DURATION
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animation ->
                indeterminateOffset = animation.animatedValue as Float
                invalidate()
            }
        }
        indeterminateAnimator?.start()
    }

    /** 停止不确定进度动画 */
    fun stopProgress() {
        indeterminateAnimator?.cancel()
        indeterminateAnimator = null
        isIndeterminateRunning = false
        indeterminateOffset = 0f
        invalidate()
    }

    /** 设置进度完成监听器，当进度达到 100 时自动回调 */
    fun setOnProgressCompleteListener(listener: (() -> Unit)?) {
        onProgressCompleteListener = listener
    }

    /** 设置点击监听器 */
    fun setOnClickListener(listener: (() -> Unit)?) {
        setOnClickListener(View.OnClickListener { listener?.invoke() })
    }

    // endregion

    // region 生命周期

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopProgress()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE && isIndeterminateRunning) {
            stopProgress()
        } else if (visibility == VISIBLE && isIndeterminate) {
            startProgress()
        }
    }

    // endregion
}
