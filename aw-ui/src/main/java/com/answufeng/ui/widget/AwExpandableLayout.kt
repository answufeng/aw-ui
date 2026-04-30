package com.answufeng.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import com.answufeng.ui.R

/**
 * Expandable / collapsible layout with smooth height animation.
 *
 * Measures the child at full height once, then animates [LayoutParams.height]
 * between 0 and the measured height using [ValueAnimator].
 *
 * ### XML usage
 * ```xml
 * <com.answufeng.ui.widget.AwExpandableLayout
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:expandable_expanded="false"
 *     app:expandable_duration="300">
 *     <TextView ... />
 * </com.answufeng.ui.widget.AwExpandableLayout>
 * ```
 *
 * ### Programmatic usage
 * ```kotlin
 * expandableLayout.expand()
 * expandableLayout.collapse()
 * expandableLayout.toggle()
 * expandableLayout.onExpandChange = { isExpanded -> ... }
 * ```
 *
 * | XML attribute | Description | Default |
 * |---|---|---|
 * | `expandable_expanded` | Whether the layout starts expanded | false |
 * | `expandable_duration` | Animation duration in milliseconds | 300 |
 */
class AwExpandableLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        /** Whether the layout is currently expanded. */
        var expanded: Boolean = false
            set(value) {
                if (field != value) {
                    field = value
                    if (value) expand() else collapse()
                }
            }

        /** Animation duration in milliseconds. */
        var duration: Long = 300L
            set(value) {
                field = value
            }

        /** Callback invoked when the expanded state changes. */
        var onExpandChange: ((Boolean) -> Unit)? = null

        private var measuredChildHeight: Int = 0

        private var animator: ValueAnimator? = null

        private var isAnimating: Boolean = false

        init {
            orientation = VERTICAL
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwExpandableLayout)
            expanded = ta.getBoolean(R.styleable.AwExpandableLayout_expandable_expanded, false)
            duration = ta.getInt(R.styleable.AwExpandableLayout_expandable_duration, 300).toLong()
            ta.recycle()
        }

        override fun onSaveInstanceState(): Parcelable {
            return Bundle().apply {
                putParcelable("superState", super.onSaveInstanceState())
                putBoolean("expanded", expanded)
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
                val wasExpanded = state.getBoolean("expanded", false)
                if (wasExpanded != expanded) {
                    expanded = wasExpanded
                }
            } else {
                super.onRestoreInstanceState(state)
            }
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            if (childCount > 0) {
                var totalHeight = 0
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    if (child.visibility == GONE) continue
                    measureChildWithMargins(child, widthMeasureSpec, 0, MeasureSpec.UNSPECIFIED, 0)
                    val lp = child.layoutParams as MarginLayoutParams
                    totalHeight += child.measuredHeight + lp.topMargin + lp.bottomMargin
                }
                measuredChildHeight = totalHeight
            }

            if (!expanded && !isAnimating) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
                return
            }

            if (isAnimating) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                return
            }

            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(measuredChildHeight + paddingTop + paddingBottom, MeasureSpec.EXACTLY),
            )
        }

        /**
         * Toggles between expanded and collapsed states.
         */
        fun toggle() {
            if (expanded) collapse() else expand()
        }

        /**
         * Expands the layout with animation.
         */
        fun expand() {
            if (expanded && !isAnimating) return
            expanded = true
            animateHeight(layoutParams?.height ?: 0, measuredChildHeight)
            onExpandChange?.invoke(true)
        }

        /**
         * Collapses the layout with animation.
         */
        fun collapse() {
            if (!expanded && !isAnimating) return
            expanded = false
            animateHeight(measuredChildHeight, 0)
            onExpandChange?.invoke(false)
        }

        private fun animateHeight(
            from: Int,
            to: Int,
        ) {
            animator?.cancel()

            if (from == to) {
                isAnimating = false
                layoutParams = layoutParams?.apply { height = to }
                requestLayout()
                return
            }

            isAnimating = true

            if (from == 0) {
                layoutParams?.height = 0
            }

            animator =
                ValueAnimator.ofInt(from.coerceAtLeast(0), to).apply {
                    duration = this@AwExpandableLayout.duration
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        val value = animation.animatedValue as Int
                        layoutParams = layoutParams?.apply { height = value }
                        requestLayout()
                    }
                    addListener(
                        object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                isAnimating = false
                                requestLayout()
                            }
                        },
                    )
                    start()
                }
        }
    }
