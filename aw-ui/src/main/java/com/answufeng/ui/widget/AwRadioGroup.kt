package com.answufeng.ui.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.view.children
import com.answufeng.ui.R
import com.answufeng.ui.dpFloat

/**
 * 单选按钮组，管理一组 [AwRadioButton] 的互斥选中状态。
 *
 * 支持水平和垂直两种排列方向，可通过 XML 或代码设置默认选中项。
 *
 * XML 用法：
 * ```xml
 * <com.answufeng.ui.widget.AwRadioGroup
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:radioGroup_orientation="vertical"
 *     app:radioGroup_spacing="12dp">
 *
 *     <com.answufeng.ui.widget.AwRadioButton
 *         android:layout_width="wrap_content"
 *         android:layout_height="wrap_content"
 *         app:radio_text="选项 A" />
 * </com.answufeng.ui.widget.AwRadioGroup>
 * ```
 */
class AwRadioGroup
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {

        var onCheckedChange: ((index: Int, label: String?) -> Unit)? = null

        private var checkedIndex: Int = -1
        private var itemSpacing: Int = 0

        init {
            orientation = VERTICAL
            gravity = Gravity.CENTER_VERTICAL

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwRadioGroup)
            val orientationAttr = ta.getInt(R.styleable.AwRadioGroup_radioGroup_orientation, 1)
            orientation = if (orientationAttr == 0) HORIZONTAL else VERTICAL
            checkedIndex = ta.getInt(R.styleable.AwRadioGroup_radioGroup_checkedIndex, -1)
            itemSpacing = ta.getDimensionPixelSize(R.styleable.AwRadioGroup_radioGroup_spacing, 8f.dpFloat.toInt())
            ta.recycle()

            // Use a transparent divider to create spacing between children
            if (itemSpacing > 0) {
                showDividers = SHOW_DIVIDER_MIDDLE
                val spaceDrawable = object : android.graphics.drawable.Drawable() {
                    override fun draw(canvas: android.graphics.Canvas) {}
                    override fun setAlpha(alpha: Int) {}
                    override fun setColorFilter(cf: android.graphics.ColorFilter?) {}
                    override fun getOpacity() = android.graphics.PixelFormat.TRANSPARENT
                    override fun getIntrinsicWidth() = itemSpacing
                    override fun getIntrinsicHeight() = itemSpacing
                }
                setDividerDrawable(spaceDrawable)
            }
        }

        override fun onFinishInflate() {
            super.onFinishInflate()
            setupButtons()
        }

        private fun setupButtons() {
            var index = 0
            for (child in children) {
                if (child is AwRadioButton) {
                    val btnIndex = index
                    child.isChecked = (btnIndex == checkedIndex)
                    child.onCheckedChangeInternal = { checked ->
                        if (checked) {
                            updateCheckedIndex(btnIndex)
                        }
                    }
                    index++
                }
            }
        }

        private fun updateCheckedIndex(index: Int) {
            checkedIndex = index
            var i = 0
            var checkedLabel: String? = null
            for (child in children) {
                if (child is AwRadioButton) {
                    child.isChecked = (i == index)
                    if (i == index) checkedLabel = child.label
                    i++
                }
            }
            onCheckedChange?.invoke(index, checkedLabel)
        }

        fun getCheckedIndex(): Int = checkedIndex

        fun setCheckedIndex(index: Int) {
            updateCheckedIndex(index)
        }

        fun getRadioButtonAt(index: Int): AwRadioButton? {
            var i = 0
            for (child in children) {
                if (child is AwRadioButton) {
                    if (i == index) return child
                    i++
                }
            }
            return null
        }
    }

