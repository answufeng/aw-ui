package com.answufeng.ui.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.R
import com.answufeng.ui.dp

class AwTitleBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val density = resources.displayMetrics.density

        private val leftContainer: FrameLayout
        private val centerContainer: LinearLayout
        private val rightContainer: LinearLayout
        private val dividerView: View

        private val ivBack: ImageView
        private val tvTitle: TextView
        private val tvSubtitle: TextView
        private val tvRight: TextView
        private val ivRight: ImageView

        private var immersivePaddingApplied = false

        var title: CharSequence
            get() = tvTitle.text
            set(value) {
                tvTitle.text = value
            }

        var subtitle: CharSequence
            get() = tvSubtitle.text
            set(value) {
                tvSubtitle.text = value
                tvSubtitle.visibility = if (value.isBlank()) GONE else VISIBLE
            }

        var showBackButton: Boolean = true
            set(value) {
                if (field == value) return
                field = value
                ivBack.visibility = if (value) VISIBLE else GONE
            }

        var barHeightPx: Int = 56.dp
            set(value) {
                val safe = value.coerceAtLeast(48.dp)
                if (field == safe) return
                field = safe
                minimumHeight = safe
                updateChildHeights()
                requestLayout()
            }

        var titleColor: Int = Color.BLACK
            set(value) {
                field = value
                tvTitle.setTextColor(value)
            }

        var subtitleColor: Int = 0x99000000.toInt()
            set(value) {
                field = value
                tvSubtitle.setTextColor(value)
            }

        var rightTextColor: Int = Color.BLACK
            set(value) {
                field = value
                tvRight.setTextColor(value)
            }

        var iconTintColor: Int? = null
            set(value) {
                field = value
                applyIconTint(ivBack.drawable, value)?.let(ivBack::setImageDrawable)
                applyIconTint(ivRight.drawable, value)?.let(ivRight::setImageDrawable)
            }

        var showDivider: Boolean = false
            set(value) {
                if (field == value) return
                field = value
                dividerView.visibility = if (value) VISIBLE else GONE
            }

        var dividerColor: Int = 0x14000000.toInt()
            set(value) {
                field = value
                dividerView.setBackgroundColor(value)
            }

        init {
            clipChildren = false
            clipToPadding = false
            minimumHeight = barHeightPx

            leftContainer =
                FrameLayout(context).apply {
                    layoutParams =
                        LayoutParams(48.dp, barHeightPx).apply {
                            gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        }
                }
            addView(leftContainer)

            ivBack =
                ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    scaleType = ImageView.ScaleType.CENTER
                    setPadding(12.dp, 0, 12.dp, 0)
                    setImageResource(R.drawable.aw_ic_back)
                    contentDescription = "Back"
                }
            leftContainer.addView(ivBack)

            centerContainer =
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    layoutParams =
                        LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                            gravity = Gravity.CENTER
                            marginStart = 56.dp
                            marginEnd = 56.dp
                        }
                }
            addView(centerContainer)

            tvTitle =
                TextView(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    setTextColor(Color.BLACK)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    ViewCompat.setAccessibilityHeading(this, true)
                }
            centerContainer.addView(tvTitle)

            tvSubtitle =
                TextView(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(0x99000000.toInt())
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                    visibility = GONE
                }
            centerContainer.addView(tvSubtitle)

            rightContainer =
                LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams =
                        LayoutParams(LayoutParams.WRAP_CONTENT, barHeightPx).apply {
                            gravity = Gravity.END or Gravity.CENTER_VERTICAL
                            marginEnd = 4.dp
                        }
                }
            addView(rightContainer)

            tvRight =
                TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, barHeightPx)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    gravity = Gravity.CENTER
                    setPadding(10.dp, 0, 10.dp, 0)
                    visibility = GONE
                }
            rightContainer.addView(tvRight)

            ivRight =
                ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(48.dp, barHeightPx)
                    scaleType = ImageView.ScaleType.CENTER
                    setPadding(12.dp, 0, 12.dp, 0)
                    contentDescription = "Action"
                    visibility = GONE
                }
            rightContainer.addView(ivRight)

            dividerView =
                View(context).apply {
                    layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, 1.dp).apply {
                            gravity = Gravity.BOTTOM
                        }
                    visibility = GONE
                }
            addView(dividerView)

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwTitleBar)
            title = ta.getString(R.styleable.AwTitleBar_titleBar_title) ?: ""
            subtitle = ta.getString(R.styleable.AwTitleBar_titleBar_subtitle) ?: ""
            showBackButton = ta.getBoolean(R.styleable.AwTitleBar_titleBar_showBack, true)
            val leftIcon = ta.getResourceId(R.styleable.AwTitleBar_titleBar_leftIcon, R.drawable.aw_ic_back)
            val rightText = ta.getString(R.styleable.AwTitleBar_titleBar_rightText)
            val rightIcon = ta.getResourceId(R.styleable.AwTitleBar_titleBar_rightIcon, 0)
            titleColor =
                ta.getColor(
                    R.styleable.AwTitleBar_titleBar_titleColor,
                    resolveThemeColor(com.google.android.material.R.attr.colorOnSurface, Color.BLACK),
                )
            subtitleColor =
                ta.getColor(
                    R.styleable.AwTitleBar_titleBar_subtitleColor,
                    ContextCompat.getColor(context, R.color.aw_color_subtitle),
                )
            rightTextColor =
                ta.getColor(
                    R.styleable.AwTitleBar_titleBar_rightTextColor,
                    resolveThemeColor(com.google.android.material.R.attr.colorPrimary, Color.BLACK),
                )
            val bgColor =
                ta.getColor(
                    R.styleable.AwTitleBar_titleBar_bgColor,
                    resolveThemeColor(com.google.android.material.R.attr.colorSurface, Color.WHITE),
                )
            val immersive = ta.getBoolean(R.styleable.AwTitleBar_titleBar_immersive, false)
            showDivider = ta.getBoolean(R.styleable.AwTitleBar_titleBar_showDivider, false)
            dividerColor =
                ta.getColor(
                    R.styleable.AwTitleBar_titleBar_dividerColor,
                    ContextCompat.getColor(context, R.color.aw_color_title_divider),
                )
            barHeightPx = ta.getDimensionPixelSize(R.styleable.AwTitleBar_titleBar_height, 56.dp)
            val iconTint = ta.getColor(R.styleable.AwTitleBar_titleBar_iconTint, Int.MIN_VALUE)
            ta.recycle()

            setLeftIcon(leftIcon)
            setBackgroundColor(bgColor)
            if (!rightText.isNullOrBlank()) setRightText(rightText)
            if (rightIcon != 0) setRightIcon(rightIcon)
            if (iconTint != Int.MIN_VALUE) {
                iconTintColor = iconTint
            }

            if (immersive) applyImmersivePadding()

            ivBack.setOnClickListener {
                val activity = findActivity(context)
                if (activity is ComponentActivity) {
                    activity.onBackPressedDispatcher.onBackPressed()
                } else {
                    activity?.finish()
                }
            }
        }

        fun setLeftIcon(
            @DrawableRes iconRes: Int,
        ) {
            ivBack.setImageResource(iconRes)
            iconTintColor?.let { tint ->
                applyIconTint(ivBack.drawable, tint)?.let(ivBack::setImageDrawable)
            }
        }

        fun setRightText(
            text: CharSequence?,
            listener: OnClickListener? = null,
        ) {
            tvRight.text = text
            tvRight.visibility = if (text.isNullOrBlank()) GONE else VISIBLE
            listener?.let { tvRight.setOnClickListener(it) }
        }

        fun setRightIcon(
            @DrawableRes iconRes: Int,
            listener: OnClickListener? = null,
        ) {
            if (iconRes == 0) {
                ivRight.visibility = GONE
                return
            }
            ivRight.setImageResource(iconRes)
            iconTintColor?.let { tint ->
                applyIconTint(ivRight.drawable, tint)?.let(ivRight::setImageDrawable)
            }
            ivRight.visibility = VISIBLE
            listener?.let { ivRight.setOnClickListener(it) }
        }

        fun setOnBackClickListener(listener: OnClickListener) {
            ivBack.setOnClickListener(listener)
        }

        fun setOnRightTextClickListener(listener: OnClickListener) {
            tvRight.setOnClickListener(listener)
        }

        fun setOnRightIconClickListener(listener: OnClickListener) {
            ivRight.setOnClickListener(listener)
        }

        fun getRightTextView(): TextView = tvRight

        fun getRightImageView(): ImageView = ivRight

        fun getBackView(): View = ivBack

        fun applyImmersivePadding() {
            if (immersivePaddingApplied) return
            immersivePaddingApplied = true
            val originalTop = paddingTop
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val status = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.setPadding(view.paddingLeft, originalTop + status, view.paddingRight, view.paddingBottom)
                insets
            }
            ViewCompat.requestApplyInsets(this)
        }

        private fun updateChildHeights() {
            leftContainer.layoutParams = (leftContainer.layoutParams as LayoutParams).also { it.height = barHeightPx }
            rightContainer.layoutParams = (rightContainer.layoutParams as LayoutParams).also { it.height = barHeightPx }
            tvRight.layoutParams = (tvRight.layoutParams as LinearLayout.LayoutParams).also { it.height = barHeightPx }
            ivRight.layoutParams = (ivRight.layoutParams as LinearLayout.LayoutParams).also { it.height = barHeightPx }
        }

        private fun applyIconTint(
            drawable: Drawable?,
            color: Int?,
        ): Drawable? {
            drawable ?: return null
            color ?: return drawable
            return DrawableCompat.wrap(drawable.mutate()).also { DrawableCompat.setTint(it, color) }
        }

        private fun findActivity(ctx: Context): Activity? {
            var current = ctx
            while (current is ContextWrapper) {
                if (current is Activity) return current
                current = current.baseContext
            }
            return null
        }

        private fun resolveThemeColor(
            attr: Int,
            fallback: Int,
        ): Int {
            return try {
                val tv = TypedValue()
                if (context.theme.resolveAttribute(attr, tv, true)) {
                    if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        tv.data
                    } else {
                        ContextCompat.getColor(context, tv.resourceId)
                    }
                } else {
                    fallback
                }
            } catch (e: Exception) {
                fallback
            }
        }
    }
