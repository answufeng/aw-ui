package com.answufeng.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.answufeng.ui.R
import java.util.LinkedList

/**
 * 垂直跑马灯视图，用于自动垂直滚动展示文本列表，类似新闻公告轮播效果。
 *
 * 支持通过 XML 属性配置滚动间隔、动画时长、文字大小和颜色。
 * 提供 [start]、[stop] 方法控制轮播，以及 [setOnItemClickListener] 设置点击监听。
 *
 * XML 属性：
 * - vm_interval：轮播间隔时间（毫秒），默认 3000
 * - vm_animDuration：动画时长（毫秒），默认 500
 * - vm_textSize：文字大小，默认 14sp
 * - vm_textColor：文字颜色，默认 Color.BLACK
 */
class AwVerticalMarqueeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_INTERVAL = 3000L
        private const val DEFAULT_ANIM_DURATION = 500L
        private const val DEFAULT_TEXT_SIZE_SP = 14f
    }

    /** 轮播间隔时间（毫秒） */
    var interval: Long = DEFAULT_INTERVAL
        private set

    /** 动画时长（毫秒） */
    var animDuration: Long = DEFAULT_ANIM_DURATION
        private set

    /** 文字大小（px） */
    var textSize: Float = 0f
        private set

    /** 文字颜色 */
    var textColor: Int = Color.BLACK
        private set

    /** 当前展示的数据列表 */
    var items: List<String> = emptyList()
        set(value) {
            field = value
            currentIndex = 0
            if (value.size <= 1) {
                stop()
            }
            updateView()
        }

    private var currentIndex = 0
    private var isRunning = false
    private var runnable: Runnable? = null
    private var onItemClickListener: ((index: Int) -> Unit)? = null

    private val currentTextView: TextView by lazy { createTextView() }
    private val nextTextView: TextView by lazy { createTextView() }

    init {
        // 初始化默认文字大小
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            DEFAULT_TEXT_SIZE_SP,
            resources.displayMetrics
        )

        // 解析 XML 属性
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.AwVerticalMarqueeView)
            interval = ta.getInt(R.styleable.AwVerticalMarqueeView_vm_interval, DEFAULT_INTERVAL.toInt()).toLong()
            animDuration = ta.getInt(R.styleable.AwVerticalMarqueeView_vm_animDuration, DEFAULT_ANIM_DURATION.toInt()).toLong()
            textSize = ta.getDimension(R.styleable.AwVerticalMarqueeView_vm_textSize, textSize)
            textColor = ta.getColor(R.styleable.AwVerticalMarqueeView_vm_textColor, Color.BLACK)
            ta.recycle()
        }

        // 添加两个 TextView 用于交替显示
        addView(currentTextView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(nextTextView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        nextTextView.visibility = View.GONE
    }

    private fun createTextView(): TextView {
        return TextView(context).apply {
            this.textSize = textSize / resources.displayMetrics.scaledDensity
            setTextColor(textColor)
            gravity = Gravity.CENTER_VERTICAL
            setSingleLine(true)
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
    }

    private fun updateView() {
        if (items.isEmpty()) {
            currentTextView.text = ""
            return
        }
        currentTextView.text = items[currentIndex]
        currentTextView.visibility = View.VISIBLE
        nextTextView.visibility = View.GONE
    }

    /** 开始轮播 */
    fun start() {
        if (isRunning || items.size <= 1) return
        isRunning = true
        scheduleNext()
    }

    /** 停止轮播 */
    fun stop() {
        isRunning = false
        runnable?.let { removeCallbacks(it) }
        runnable = null
        currentTextView.animate().cancel()
        nextTextView.animate().cancel()
        currentTextView.translationY = 0f
        currentTextView.alpha = 1f
        nextTextView.translationY = 0f
        nextTextView.alpha = 1f
    }

    /** 设置条目点击监听 */
    fun setOnItemClickListener(listener: (index: Int) -> Unit) {
        onItemClickListener = listener
        val clickListener = OnClickListener {
            if (currentIndex < items.size) {
                onItemClickListener?.invoke(currentIndex)
            }
        }
        currentTextView.setOnClickListener(clickListener)
        nextTextView.setOnClickListener(clickListener)
    }

    private fun scheduleNext() {
        if (!isRunning) return
        runnable?.let { removeCallbacks(it) }
        val r = Runnable { flipToNext() }
        runnable = r
        postDelayed(r, interval)
    }

    private fun flipToNext() {
        if (!isRunning || items.size <= 1) return

        val nextIndex = (currentIndex + 1) % items.size
        nextTextView.text = items[nextIndex]

        // 当前视图向上滑出
        currentTextView.animate()
            .translationY(-height.toFloat())
            .alpha(0f)
            .setDuration(animDuration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // 下一个视图从底部滑入
        nextTextView.translationY = height.toFloat()
        nextTextView.alpha = 0f
        nextTextView.visibility = View.VISIBLE
        nextTextView.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(animDuration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentIndex = nextIndex
                    // 交换引用：next 变为 current
                    currentTextView.text = items[currentIndex]
                    currentTextView.translationY = 0f
                    currentTextView.alpha = 1f
                    currentTextView.visibility = View.VISIBLE

                    nextTextView.visibility = View.GONE
                    nextTextView.animate().setListener(null)

                    scheduleNext()
                }
            })
            .start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (items.size > 1) {
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }
}
