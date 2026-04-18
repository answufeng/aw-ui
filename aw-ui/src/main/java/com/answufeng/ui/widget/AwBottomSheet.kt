package com.answufeng.ui.widget

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class AwBottomSheet private constructor(
    private val builder: Builder
) {

    private var dialog: BottomSheetDialog? = null

    fun show(context: Context) {
        dismiss()
        val view = builder.contentView ?: return
        dialog = BottomSheetDialog(context).apply {
            setContentView(view)
            setOnDismissListener {
                builder.onDismiss?.invoke()
            }
            behavior.peekHeight = builder.peekHeight
                ?: (context.resources.displayMetrics.heightPixels * 0.5).toInt()
            behavior.isDraggable = builder.draggable
            behavior.state = builder.state
            setCancelable(builder.cancelable)
        }
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true

    fun setPeekHeight(height: Int): AwBottomSheet {
        dialog?.behavior?.peekHeight = height
        return this
    }

    fun setCancelable(cancelable: Boolean): AwBottomSheet {
        dialog?.setCancelable(cancelable)
        return this
    }

    fun setDraggable(draggable: Boolean): AwBottomSheet {
        dialog?.behavior?.isDraggable = draggable
        return this
    }

    fun setState(state: Int): AwBottomSheet {
        dialog?.behavior?.state = state
        return this
    }

    fun setOnStateChangedListener(listener: (Int) -> Unit): AwBottomSheet {
        dialog?.behavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                listener(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return this
    }

    class Builder {
        internal var contentView: View? = null
        internal var peekHeight: Int? = null
        internal var draggable: Boolean = true
        internal var cancelable: Boolean = true
        internal var state: Int = BottomSheetBehavior.STATE_COLLAPSED
        internal var onDismiss: (() -> Unit)? = null

        fun setContentView(view: View): Builder {
            contentView = view
            return this
        }

        fun setPeekHeight(height: Int): Builder {
            peekHeight = height
            return this
        }

        fun setDraggable(draggable: Boolean): Builder {
            this.draggable = draggable
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setState(state: Int): Builder {
            this.state = state
            return this
        }

        fun setOnDismiss(listener: () -> Unit): Builder {
            onDismiss = listener
            return this
        }

        fun build(): AwBottomSheet = AwBottomSheet(this)

        fun show(context: Context): AwBottomSheet {
            val sheet = build()
            sheet.show(context)
            return sheet
        }
    }

    companion object {
        val STATE_COLLAPSED = BottomSheetBehavior.STATE_COLLAPSED
        val STATE_EXPANDED = BottomSheetBehavior.STATE_EXPANDED
        val STATE_HIDDEN = BottomSheetBehavior.STATE_HIDDEN
    }
}
