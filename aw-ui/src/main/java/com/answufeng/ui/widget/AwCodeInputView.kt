package com.answufeng.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.answufeng.ui.R

/**
 * 验证码输入视图，每位数字显示在独立的方框中。
 *
 * 功能：
 * - 通过 [codeLength] 配置验证码位数（默认 6）
 * - 输入后自动跳转到下一个方框
 * - 自动聚焦第一个空方框
 * - 支持粘贴完整验证码
 * - [onCodeComplete] 所有数字输入完成回调
 * - [code] 属性获取/设置完整验证码
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.widget.AwCodeInputView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:code_length="6"
 *     app:code_boxSize="48dp"
 *     app:code_boxSpacing="8dp"
 *     app:code_boxStrokeColor="#CCCCCC"
 *     app:code_boxStrokeWidth="2dp"
 *     app:code_textColor="#000000"
 *     app:code_textSize="18sp" />
 * ```
 *
 * ### 代码用法
 * ```kotlin
 * codeInputView.codeLength = 4
 * codeInputView.onCodeComplete = { code -> verifyCode(code) }
 * val entered = codeInputView.code
 * ```
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `code_length` | 验证码位数 | 6 |
 * | `code_boxSize` | 每个方框尺寸 | 48dp |
 * | `code_boxSpacing` | 方框间距 | 8dp |
 * | `code_boxStrokeColor` | 方框边框颜色 | #CCCCCC |
 * | `code_boxStrokeWidth` | 方框边框宽度 | 2dp |
 * | `code_textColor` | 数字文字颜色 | #000000 |
 * | `code_textSize` | 数字文字大小 | 18sp |
 */
class AwCodeInputView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        private val density = resources.displayMetrics.density

        /** 验证码位数，修改此值会重建方框视图 */
        var codeLength: Int = 6
            set(value) {
                field = value.coerceAtLeast(1)
                rebuildBoxes()
            }

        /** 每个方框的尺寸（px） */
        var boxSize: Int = (48 * density).toInt()
            set(value) {
                field = value
                rebuildBoxes()
            }

        /** 方框间距（px） */
        var boxSpacing: Int = (8 * density).toInt()
            set(value) {
                field = value
                rebuildBoxes()
            }

        /** 方框边框颜色 */
        var boxStrokeColor: Int = 0xFFCCCCCC.toInt()
            set(value) {
                field = value
                invalidateBoxes()
            }

        /** 方框边框宽度（px） */
        var boxStrokeWidth: Float = 2 * density
            set(value) {
                field = value
                invalidateBoxes()
            }

        /** 数字文字颜色 */
        var codeTextColor: Int = Color.BLACK
            set(value) {
                field = value
                invalidateBoxes()
            }

        /** 数字文字大小（px） */
        var codeTextSize: Float = 18 * density
            set(value) {
                field = value
                invalidateBoxes()
            }

        /** 所有数字输入完成回调 */
        var onCodeComplete: ((String) -> Unit)? = null

        var codeInputType: Int = InputType.TYPE_CLASS_NUMBER

        /** 当前已输入的完整验证码，设置此属性可程序化填充方框 */
        var code: String
            get() =
                buildString {
                    for (i in 0 until childCount) {
                        val et = getChildAt(i) as? CodeEditText ?: continue
                        append(et.text.toString())
                    }
                }
            set(value) {
                val digits = value.take(codeLength)
                for (i in 0 until childCount) {
                    val et = getChildAt(i) as? CodeEditText ?: continue
                    et.removeTextChangedListener(et.internalWatcher)
                    if (i < digits.length) {
                        et.setText(digits[i].toString())
                    } else {
                        et.setText("")
                    }
                    et.addTextChangedListener(et.internalWatcher)
                }
                focusFirstEmpty()
                if (digits.length == codeLength) {
                    onCodeComplete?.invoke(digits)
                }
            }

        private val boxPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
            }
        private val focusedBoxPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
        private val boxRect = RectF()

        init {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwCodeInputView)
            codeLength = ta.getInt(R.styleable.AwCodeInputView_code_length, 6)
            boxSize = ta.getDimensionPixelSize(R.styleable.AwCodeInputView_code_boxSize, (48 * density).toInt())
            boxSpacing = ta.getDimensionPixelSize(R.styleable.AwCodeInputView_code_boxSpacing, (8 * density).toInt())
            boxStrokeColor =
                ta.getColor(
                    R.styleable.AwCodeInputView_code_boxStrokeColor,
                    ContextCompat.getColor(context, R.color.aw_color_code_input_stroke),
                )
            boxStrokeWidth = ta.getDimension(R.styleable.AwCodeInputView_code_boxStrokeWidth, 2 * density)
            codeTextColor = ta.getColor(R.styleable.AwCodeInputView_code_textColor, Color.BLACK)
            codeTextSize = ta.getDimension(R.styleable.AwCodeInputView_code_textSize, 18 * density)
            ta.recycle()

            isSaveEnabled = true
            rebuildBoxes()
        }

        private fun rebuildBoxes() {
            removeAllViews()
            for (i in 0 until codeLength) {
                val et =
                    CodeEditText(context, this, i).apply {
                        inputType = codeInputType
                        isCursorVisible = false
                        setTextColor(codeTextColor)
                        textSize = codeTextSize / density
                        gravity = Gravity.CENTER
                        layoutParams =
                            LayoutParams(boxSize, boxSize).apply {
                                if (i < codeLength - 1) {
                                    marginEnd = boxSpacing
                                }
                            }
                    }
                addView(et)
            }
        }

        private fun invalidateBoxes() {
            for (i in 0 until childCount) {
                val et = getChildAt(i) as? CodeEditText ?: continue
                et.setTextColor(codeTextColor)
                et.textSize = codeTextSize / density
            }
            invalidate()
        }

        internal fun onDigitEntered(index: Int) {
            val nextIndex = index + 1
            if (nextIndex < childCount) {
                getChildAt(nextIndex).requestFocus()
            } else {
                val currentCode = code
                if (currentCode.length == codeLength) {
                    onCodeComplete?.invoke(currentCode)
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(windowToken, 0)
                }
            }
        }

        internal fun onDigitDeleted(index: Int) {
            if (index > 0) {
                val prevEt = getChildAt(index - 1) as? CodeEditText ?: return
                prevEt.setText("")
                prevEt.requestFocus()
            }
        }

        internal fun onDigitCleared(index: Int) {
            if (index > 0) {
                val prevEt = getChildAt(index - 1) as? CodeEditText ?: return
                prevEt.setText("")
                prevEt.requestFocus()
            }
        }

        internal fun onPasteFullCode(pasted: String) {
            code = pasted
        }

        private fun focusFirstEmpty() {
            for (i in 0 until childCount) {
                val et = getChildAt(i) as? CodeEditText ?: continue
                if (et.text.isEmpty()) {
                    et.requestFocus()
                    return
                }
            }
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val totalWidth = boxSize * codeLength + boxSpacing * (codeLength - 1) + paddingStart + paddingEnd
            val totalHeight = boxSize + paddingTop + paddingBottom
            setMeasuredDimension(
                resolveSize(totalWidth, widthMeasureSpec),
                resolveSize(totalHeight, heightMeasureSpec),
            )
            measureChildren(
                MeasureSpec.makeMeasureSpec(boxSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(boxSize, MeasureSpec.EXACTLY),
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            boxPaint.color = boxStrokeColor
            boxPaint.strokeWidth = boxStrokeWidth

            val focusedColor = boxStrokeColor
            focusedBoxPaint.color = focusedColor
            focusedBoxPaint.alpha = 30

            for (i in 0 until childCount) {
                val child = getChildAt(i)
                boxRect.set(child.left.toFloat(), child.top.toFloat(), child.right.toFloat(), child.bottom.toFloat())
                val radius = 8 * density

                if (child.hasFocus()) {
                    canvas.drawRoundRect(boxRect, radius, radius, focusedBoxPaint)
                }
                canvas.drawRoundRect(boxRect, radius, radius, boxPaint)
            }
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putString("code", code)
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
                val savedCode = state.getString("code") ?: ""
                for (i in 0 until childCount.coerceAtMost(savedCode.length)) {
                    val et = getChildAt(i) as? CodeEditText ?: continue
                    et.removeTextChangedListener(et.internalWatcher)
                    et.setText(savedCode[i].toString())
                    et.addTextChangedListener(et.internalWatcher)
                }
            } else {
                super.onRestoreInstanceState(state)
            }
        }

        private class CodeEditText(
            context: Context,
            private val parent: AwCodeInputView,
            private val index: Int,
        ) : EditText(context) {
            val internalWatcher =
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {}

                    override fun afterTextChanged(s: Editable?) {
                        val text = s.toString()
                        if (text.length > 1) {
                            val pasted = text.filter { it.isLetterOrDigit() }
                            if (pasted.length > 1) {
                                removeTextChangedListener(this)
                                setText("")
                                addTextChangedListener(this)
                                parent.onPasteFullCode(pasted)
                                return
                            }
                        }
                        if (text.isNotEmpty()) {
                            val digit = text.last().toString()
                            removeTextChangedListener(this)
                            setText(digit)
                            setSelection(digit.length)
                            addTextChangedListener(this)
                            parent.onDigitEntered(index)
                        } else {
                            parent.onDigitCleared(index)
                        }
                    }
                }

            init {
                addTextChangedListener(internalWatcher)
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.action == android.view.KeyEvent.ACTION_DOWN) {
                        if (text.isEmpty()) {
                            parent.onDigitDeleted(index)
                        }
                        false
                    } else {
                        false
                    }
                }
                setOnFocusChangeListener { _, hasFocus ->
                    parent.invalidate()
                }
            }
        }
    }
