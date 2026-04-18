package com.answufeng.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AwBottomSheet {

    private var dialog: BottomSheetDialog? = null
    private var contentView: View? = null
    private var onDismiss: (() -> Unit)? = null

    fun setContentView(view: View): AwBottomSheet {
        contentView = view
        return this
    }

    fun setOnDismiss(listener: () -> Unit): AwBottomSheet {
        onDismiss = listener
        return this
    }

    fun show(context: Context) {
        dismiss()
        val view = contentView ?: return
        dialog = BottomSheetDialog(context).apply {
            setContentView(view)
            setOnDismissListener {
                onDismiss?.invoke()
            }
            behavior.peekHeight = (context.resources.displayMetrics.heightPixels * 0.5).toInt()
            show()
        }
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

    companion object {
        val STATE_COLLAPSED = BottomSheetBehavior.STATE_COLLAPSED
        val STATE_EXPANDED = BottomSheetBehavior.STATE_EXPANDED
        val STATE_HIDDEN = BottomSheetBehavior.STATE_HIDDEN
    }
}
