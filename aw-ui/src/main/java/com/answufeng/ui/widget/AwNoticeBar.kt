package com.answufeng.ui.widget

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.answufeng.ui.R
import com.answufeng.ui.dpToPx

/**
 * 顶部通知/公告条，支持关闭按钮与点击整栏回调。
 */
class AwNoticeBar
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val messageView: TextView
        private val closeView: ImageView

        var noticeText: CharSequence
            get() = messageView.text
            set(value) {
                messageView.text = value
            }

        var closable: Boolean = true
            set(value) {
                field = value
                closeView.visibility = if (value) VISIBLE else GONE
            }

        var onCloseClick: (() -> Unit)? = null

        var onBarClick: (() -> Unit)? = null

        init {
            val paddingH = context.resources.dpToPx(12)
            val paddingV = context.resources.dpToPx(10)
            setPadding(paddingH, paddingV, paddingH, paddingV)

            messageView =
                TextView(context).apply {
                    layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                            gravity = Gravity.CENTER_VERTICAL
                            marginEnd = context.resources.dpToPx(32)
                        }
                    textSize = 14f
                    setTextColor(0xFF333333.toInt())
                }
            closeView =
                ImageView(context).apply {
                    layoutParams =
                        LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                            gravity = Gravity.CENTER_VERTICAL or Gravity.END
                        }
                    setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    contentDescription = context.getString(R.string.aw_notice_close)
                }
            addView(messageView)
            addView(closeView)

            val ta = context.obtainStyledAttributes(attrs, R.styleable.AwNoticeBar)
            noticeText = ta.getString(R.styleable.AwNoticeBar_notice_text).orEmpty()
            closable = ta.getBoolean(R.styleable.AwNoticeBar_notice_closable, true)
            val bgColor = ta.getColor(R.styleable.AwNoticeBar_notice_backgroundColor, 0xFFFFF8E1.toInt())
            ta.recycle()

            background =
                GradientDrawable().apply {
                    cornerRadius = context.resources.dpToPx(8).toFloat()
                    setColor(bgColor)
                }

            setOnClickListener { onBarClick?.invoke() }

            // 关闭按钮点击时阻止事件冒泡到整栏的 onBarClick
            closeView.setOnClickListener {
                visibility = GONE
                onCloseClick?.invoke()
            }
        }

        fun showMessage(text: CharSequence) {
            noticeText = text
            visibility = VISIBLE
        }

        fun hide() {
            visibility = GONE
        }
    }
