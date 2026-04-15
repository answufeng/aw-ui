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
 * A password input view with visibility toggle and strength indicator.
 *
 * Features:
 * - Toggle password visibility via an eye icon button
 * - Password strength indicator (Weak / Medium / Strong) shown as a colored bar
 * - Strength calculation based on length, uppercase, lowercase, digits, and special characters
 * - [password] property to get/set the current password
 * - [strength] enum property reflecting the current strength level
 * - [onStrengthChange] callback when strength changes
 *
 * ### XML Usage
 * ```xml
 * <com.answufeng.ui.widget.AwPasswordInputView
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:password_hint="Enter password"
 *     app:password_showToggle="true"
 *     app:password_showStrength="true" />
 * ```
 *
 * ### Code Usage
 * ```kotlin
 * passwordInput.password = "MyP@ss1"
 * val strength = passwordInput.strength
 * passwordInput.onStrengthChange = { s -> updateStrengthLabel(s) }
 * ```
 *
 * | XML Attribute | Description | Default |
 * |---|---|---|
 * | `password_hint` | Hint text for the EditText | "Password" |
 * | `password_showToggle` | Show visibility toggle button | true |
 * | `password_showStrength` | Show strength indicator bar | true |
 */
class AwPasswordInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Password strength levels.
     */
    enum class Strength {
        /** Weak: score 0-1 */
        WEAK,
        /** Medium: score 2-3 */
        MEDIUM,
        /** Strong: score 4-5 */
        STRONG
    }

    private val density = resources.displayMetrics.density

    private val editText: EditText
    private val toggleButton: ImageView
    private lateinit var strengthBarView: StrengthBarView

    private var isPasswordVisible = false

    /**
     * Hint text displayed in the password EditText.
     */
    var hint: String = "Password"
        set(value) {
            field = value
            editText.hint = value
        }

    /**
     * Whether to show the visibility toggle button.
     */
    var showToggle: Boolean = true
        set(value) {
            field = value
            toggleButton.visibility = if (value) View.VISIBLE else View.GONE
        }

    /**
     * Whether to show the password strength indicator bar.
     */
    var showStrength: Boolean = true
        set(value) {
            field = value
            strengthBarView.visibility = if (value) View.VISIBLE else View.GONE
        }

    /**
     * The current password text.
     */
    var password: String
        get() = editText.text.toString()
        set(value) {
            editText.setText(value)
        }

    /**
     * The current password strength level.
     */
    val strength: Strength
        get() = calculateStrength(password)

    /**
     * Callback invoked when the password strength changes.
     */
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
            setImageDrawable(createEyeDrawable(false))
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
        toggleButton.setImageDrawable(createEyeDrawable(isPasswordVisible))
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
            putString("password", password)
            putBoolean("isPasswordVisible", isPasswordVisible)
            putString("strength", lastStrength.name)
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
            val savedPassword = state.getString("password") ?: ""
            val savedVisible = state.getBoolean("isPasswordVisible", false)
            val savedStrength = try {
                Strength.valueOf(state.getString("strength") ?: "WEAK")
            } catch (_: IllegalArgumentException) {
                Strength.WEAK
            }
            editText.removeTextChangedListener(textWatcher)
            editText.setText(savedPassword)
            editText.addTextChangedListener(textWatcher)
            isPasswordVisible = savedVisible
            if (isPasswordVisible) {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            lastStrength = savedStrength
            strengthBarView.currentStrength = savedStrength
            toggleButton.setImageDrawable(createEyeDrawable(isPasswordVisible))
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
