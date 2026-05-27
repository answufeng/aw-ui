package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.dialog.AwActionSheetDialog
import com.answufeng.ui.dialog.AwDialog
import com.answufeng.ui.dialog.AwLoadingDialog
import com.answufeng.ui.dialog.showAwConfirm
import com.answufeng.ui.dialog.showAwMessage
import com.answufeng.ui.widget.AwTitleBar

class ShowcaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        setupDialogs()
    }

    private fun setupDialogs() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_alert).setOnClickListener {
            AwDialog.Builder(this)
                .title("提示")
                .message("这是一个 AwDialog 对话框示例，支持标题、消息和按钮自定义。")
                .positiveButton("确定") { toast("点击了确定") }
                .negativeButton("取消") { toast("点击了取消") }
                .show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_alert_single).setOnClickListener {
            AwDialog.Builder(this)
                .title("通知")
                .message("仅一个按钮的对话框。")
                .positiveButton("知道了") { toast("点击了确定") }
                .cancelable(false)
                .show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_action_sheet).setOnClickListener {
            val items = listOf("分享到微信", "分享到微博", "复制链接", "收藏")
            AwActionSheetDialog(this)
                .setItems(items)
                .setOnSelect { which -> toast("选择了: ${items[which]}") }
                .setTitle("分享")
                .show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_loading).setOnClickListener {
            val dialog = AwLoadingDialog.show(this, "加载中…")
            findViewById<android.view.View>(R.id.main).postDelayed({ dialog?.dismiss() }, 2000)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_loading_msg).setOnClickListener {
            val dialog = AwLoadingDialog.show(this, "正在提交数据…")
            findViewById<android.view.View>(R.id.main).postDelayed({ dialog?.dismiss() }, 2000)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_ext_message).setOnClickListener {
            showAwMessage("快捷提示", "Context.showAwMessage() 一行代码弹出消息框。")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_ext_confirm).setOnClickListener {
            showAwConfirm("确认操作", "Context.showAwConfirm() 确认后执行回调。") {
                toast("已确认")
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
