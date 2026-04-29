package com.answufeng.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.answufeng.ui.R

abstract class AwBaseDialog(context: Context) : Dialog(context, R.style.AwBaseDialog) {
    protected open val dimAmount: Float = 0.6f

    protected open val cancelableOnTouchOutside: Boolean = true

    protected open val gravity: Int = Gravity.CENTER

    var customContentView: View? = null
        protected set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(cancelableOnTouchOutside)

        customContentView?.let { setContentView(it) }

        window?.apply {
            setDimAmount(dimAmount)
            setGravity(this@AwBaseDialog.gravity)
            attributes =
                attributes?.apply {
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                }
        }
    }

    override fun show() {
        super.show()
        window?.setDimAmount(dimAmount)
    }
}
