package com.answufeng.ui.demo

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityBottomSheetDemoBinding
import com.answufeng.ui.dialog.AwBottomSheetDialog
import com.answufeng.ui.dpFloat
import com.answufeng.ui.viewBinding

class BottomSheetDemoActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityBottomSheetDemoBinding::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_sheet_demo)
        binding.topBar.setOnBackClickListener { finish() }

        binding.btnSimpleSheet.setOnClickListener {
            AwBottomSheetDialog(this).apply {
                setTitle("提示")
                setDialogContentView(createSimpleContent())
            }.show()
        }

        binding.btnCustomSheet.setOnClickListener {
            AwBottomSheetDialog.Builder(this)
                .setTitle("自定义面板")
                .setDialogContentView(createFormContent())
                .setDragHandleVisible(true)
                .setCornerRadius(16f.dpFloat)
                .setOnDismissListener { showToast("面板已关闭") }
                .show()
        }
    }

    private fun createSimpleContent(): TextView {
        return TextView(this).apply {
            text = "这是一个简单底部面板的内容区域。\n可以放置任意自定义 View。"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setTextColor(0xFF666666.toInt())
            minHeight = 120
        }
    }

    private fun createFormContent(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 16, 24, 24)

            val items = listOf("拍照", "从相册选择", "从文件选择")
            items.forEach { item ->
                addView(TextView(context).apply {
                    text = item
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setPadding(0, 16, 0, 16)
                    setTextColor(0xFF1976D2.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener { /* handle selection */ }
                })
                if (item != items.last()) {
                    addView(android.view.View(context).apply {
                        setBackgroundColor(0xFFE0E0E0.toInt())
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, (0.5f).toInt()
                        )
                    })
                }
            }
        }
    }

    private fun showToast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}



