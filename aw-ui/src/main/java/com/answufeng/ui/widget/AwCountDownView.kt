package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.answufeng.ui.R

/**
 * Countdown timer view that displays remaining seconds.
 *
 * Uses [CountDownTimer] internally. Displays remaining time as "XXs" by default,
 * or a custom format via [formatTime].
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwCountDownView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:countdown_seconds="60"
 *     app:countdown_textColor="#FF0000"
 *     app:countdown_textSize="16sp" />
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * countDownView.formatTime = { seconds -> "${seconds}s remaining" }
 * countDownView.onFinish = { showToast("Done!") }
 * countDownView.onTick = { seconds -> Log.d("TAG", "$seconds left") }
 * countDownView.start(60)
 * ```
 *
 * | XML attribute | Description | Default |
 * |---|---|---|
 * | `countdown_seconds` | Initial countdown duration in seconds | 60 |
 * | `countdown_textColor` | Text color | current text color |
 * | `countdown_textSize` | Text size | 14sp |
 */
class AwCountDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /** Callback invoked when the countdown finishes. */
    var onFinish: (() -> Unit)? = null

    /** Callback invoked on each tick with the remaining seconds. */
    var onTick: ((Int) -> Unit)? = null

    /** Custom time format function. Receives remaining seconds, returns display string. */
    var formatTime: ((Int) -> String)? = null

    /** Whether to auto-disable this view during countdown and re-enable on finish. Default true. */
    var autoDisable: Boolean = true

    /** Initial countdown duration in seconds, used by [reset]. */
    var initialSeconds: Int = 60
        private set

    private var timer: CountDownTimer? = null

    private var remainingSeconds: Int = 0

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwCountDownView)
        initialSeconds = ta.getInt(R.styleable.AwCountDownView_countdown_seconds, 60)
        val textColor = ta.getColor(R.styleable.AwCountDownView_countdown_textColor, currentTextColor)
        val textSize = ta.getDimension(R.styleable.AwCountDownView_countdown_textSize, 14f * resources.displayMetrics.density)
        ta.recycle()

        setTextColor(textColor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        remainingSeconds = initialSeconds
        updateDisplay()
    }

    /**
     * Starts the countdown from the given number of seconds.
     *
     * @param seconds countdown duration in seconds
     */
    fun start(seconds: Int) {
        stop()
        initialSeconds = seconds
        remainingSeconds = seconds
        updateDisplay()
        if (autoDisable) isEnabled = false

        timer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()
                updateDisplay()
                onTick?.invoke(remainingSeconds)
            }

            override fun onFinish() {
                remainingSeconds = 0
                updateDisplay()
                if (autoDisable) isEnabled = true
                onFinish?.invoke()
            }
        }.start()
    }

    /**
     * Stops the countdown without resetting the display.
     */
    fun stop() {
        timer?.cancel()
        timer = null
    }

    /**
     * Resets the view to the initial state, displaying [initialSeconds].
     * Stops any running countdown.
     */
    fun reset() {
        stop()
        remainingSeconds = initialSeconds
        updateDisplay()
        if (autoDisable) isEnabled = true
    }

    private fun updateDisplay() {
        text = formatTime?.invoke(remainingSeconds) ?: "${remainingSeconds}s"
    }
}
