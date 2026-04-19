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
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import com.answufeng.ui.R

/**
 * 带可见性切换和强度指示器的密码输入视图。
 *
 * 功能：
 * - 通过眼睛图标按钮切换密码可见性
 * - 密码强度指示条（弱 / 中 / 强），以彩色条显示
 * - 强度计算基于长度、大写字母、小写字母、数字和特殊字符
 * - [password] 属性获取/设置当前密码
 * - [strength] 枚举属性反映当前强度级别
 * - [onStrengthChange] 强度变化回调
 *
 * ### XML 用法
 * ```xml
 * <com.answufeng.ui.widget.AwPasswordInputView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:password_hint="Enter password"
 *     app:password_showToggle="true"
 *     app:password_showStrength="true" />
 * ```
 *
 * ### 代码用法
 * ```kotlin
 * passwordInput.password = "MyP@ss1"
 * val strength = passwordInput.strength
 * passwordInput.onStrengthChange = { s -> updateStrengthLabel(s) }
 * ```
 *
 * | XML 属性 | 说明 | 默认值 |
 * |---|---|---|
 * | `password_hint` | EditText 提示文本 | "Password" |
 * | `password_showToggle` | 是否显示可见性切换按钮 | true |
 * | `password_showStrength` | 是否显示强度指示条 | true |
 */
class AwPasswordInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** 密码强度级别 */
    enum class Strength {
        /** 弱：得分 0-1 */
        WEAK,
        /** 中：得分 2-3 */
        MEDIUM,
        /** 强：得分 4-5 */
        STRONG
    }

    private val density = resources.displayMetrics.density

    private val editText: EditText
    private val toggleButton: ImageView
    private lateinit var strengthBarView: StrengthBarView

    private var isPasswordVisible = false

    private var eyeOpenDrawable: android.graphics.drawable.Drawable? = null
    private var eyeClosedDrawable: android.graphics.drawable.Drawable? = null

    /** 密码 EditText 的提示文本 */
    var hint: String = "Password"
        set(value) {
            field = value
            editText.hint = value
        }

    /** 是否显示可见性切换按钮 */
    var showToggle: Boolean = true
        set(value) {
            field = value
            toggleButton.visibility = if (value) View.VISIBLE else View.GONE
        }

    /** 是否显示密码强度指示条 */
    var showStrength: Boolean = true
        set(value) {
            field = value
            strengthBarView.visibility = if (value) View.VISIBLE else View.GONE
        }

    /** 当前密码文本 */
    var password: String
        get() = editText.text.toString()
        set(value) {
            editText.setText(value)
        }

    /** 当前密码强度级别 */
    val strength: Strength
        get() = calculateStrength(password)

    /** 密码强度变化回调 */
    var onStrengthChange: ((Strength) -> Unit)? = null

    private var lastStrength: Strength = Strength.WEAK

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val newStrength = strength
            if (newStrength != lastStrength) {
                lastStrength = newStrength
                strengthBarView.currentStrength = newStrength
                onStrengthChange?.invoke(newStrength)
            }
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AwPasswordInputView)
        val xmlHint = ta.getString(R.styleable.AwPasswordInputView_password_hint) ?: "Password"
        val xmlShowToggle = ta.getBoolean(R.styleable.AwPasswordInputView_password_showToggle, true)
        val xmlShowStrength = ta.getBoolean(R.styleable.AwPasswordInputView_password_showStrength, true)
        ta.recycle()

        editText = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = xmlHint
            setSingleLine()
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (40 * density).toInt()
            }
        }

        toggleButton = ImageView(context).apply {
            layoutParams = LayoutParams(
                (32 * density).toInt(),
                (32 * density).toInt()
            ).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = (8 * density).toInt()
            }
            setImageDrawable(getEyeDrawable(false))
            setOnClickListener { togglePasswordVisibility() }
        }

        strengthBarView = StrengthBarView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                (4 * density).toInt()
            ).apply {
                gravity = Gravity.BOTTOM
                val editHeight = (48 * density).toInt()
                bottomMargin = editHeight / 2 - (2 * density).toInt()
            }
            visibility = if (xmlShowStrength) View.VISIBLE else View.GONE
        }

        addView(editText)
        addView(toggleButton)
        addView(strengthBarView)

        showToggle = xmlShowToggle
        showStrength = xmlShowStrength
        hint = xmlHint

        editText.addTextChangedListener(textWatcher)
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val selection = editText.selectionEnd
        if (isPasswordVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(selection.coerceIn(0, editText.text.length))
        toggleButton.setImageDrawable(getEyeDrawable(isPasswordVisible))
    }

    private fun getEyeDrawable(visible: Boolean): android.graphics.drawable.Drawable {
        if (visible) {
            return eyeOpenDrawable ?: createEyeDrawable(true).also { eyeOpenDrawable = it }
        }
        return eyeClosedDrawable ?: createEyeDrawable(false).also { eyeClosedDrawable = it }
    }

    private fun createEyeDrawable(visible: Boolean): android.graphics.drawable.Drawable {
        val size = (24 * density).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 2 * density
        }
        val cx = size / 2f
        val cy = size / 2f
        val eyeRadius = size * 0.3f
        canvas.drawOval(
            RectF(cx - eyeRadius * 1.4f, cy - eyeRadius * 0.7f, cx + eyeRadius * 1.4f, cy + eyeRadius * 0.7f),
            paint
        )
        canvas.drawCircle(cx, cy, eyeRadius * 0.45f, paint)

        if (!visible) {
            paint.strokeWidth = 2 * density
            val slashMargin = eyeRadius * 0.9f
            canvas.drawLine(
                cx - slashMargin, cy - slashMargin,
                cx + slashMargin, cy + slashMargin,
                paint
            )
        }

        return android.graphics.drawable.BitmapDrawable(resources, bitmap)
    }

    private fun calculateStrength(pwd: String): Strength {
        if (pwd.isEmpty()) return Strength.WEAK
        var score = 0
        if (pwd.length >= 8) score++
        if (pwd.any { it.isUpperCase() }) score++
        if (pwd.any { it.isLowerCase() }) score++
        if (pwd.any { it.isDigit() }) score++
        if (pwd.any { !it.isLetterOrDigit() }) score++
        return when {
            score <= 1 -> Strength.WEAK
            score <= 3 -> Strength.MEDIUM
            else -> Strength.STRONG
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putBoolean("isPasswordVisible", isPasswordVisible)
            putInt("strength", lastStrength.ordinal)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState: Parcelable? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                state.getParcelable("superState", Parcelable::class.java)
            } else {
                @Suppress("DEPRECATION")
                state.getParcelable("superState")
            }
            super.onRestoreInstanceState(superState)
            val savedVisible = state.getBoolean("isPasswordVisible", false)
            val savedStrength = try {
                Strength.valueOf(state.getString("strength") ?: "WEAK")
            } catch (_: IllegalArgumentException) {
                Strength.WEAK
            }
            editText.removeTextChangedListener(textWatcher)
            isPasswordVisible = savedVisible
            if (isPasswordVisible) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            lastStrength = savedStrength
            strengthBarView.currentStrength = savedStrength
            toggleButton.setImageDrawable(getEyeDrawable(isPasswordVisible))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class StrengthBarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private val density = resources.displayMetrics.density
        private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
        }
        private val rect = RectF()

        var currentStrength: Strength = Strength.WEAK
            set(value) {
                field = value
                invalidate()
            }

        private val strengthColor: Int
            get() = when (currentStrength) {
                Strength.WEAK -> Color.parseColor("#F44336")
                Strength.MEDIUM -> Color.parseColor("#FF9800")
                Strength.STRONG -> Color.parseColor("#4CAF50")
            }

        private val strengthFraction: Float
            get() = when (currentStrength) {
                Strength.WEAK -> 1f / 3f
                Strength.MEDIUM -> 2f / 3f
                Strength.STRONG -> 1f
            }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val radius = h / 2f

            rect.set(0f, 0f, w, h)
            canvas.drawRoundRect(rect, radius, radius, bgPaint)

            barPaint.color = strengthColor
            val fillWidth = w * strengthFraction
            rect.set(0f, 0f, fillWidth, h)
            canvas.drawRoundRect(rect, radius, radius, barPaint)
        }
    }
}
