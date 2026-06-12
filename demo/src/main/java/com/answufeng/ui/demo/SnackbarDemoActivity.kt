package com.answufeng.ui.demo

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.snackbar.AwSnackbar
import com.answufeng.ui.snackbar.AwSnackbarManager
import com.answufeng.ui.snackbar.showAwError
import com.answufeng.ui.snackbar.showAwInfo
import com.answufeng.ui.snackbar.showAwSnackbar
import com.answufeng.ui.snackbar.showAwSuccess
import com.answufeng.ui.snackbar.showAwWarning
import com.answufeng.ui.widget.AwTitleBar
import com.google.android.material.snackbar.Snackbar

class SnackbarDemoActivity : AppCompatActivity() {

    private var indefiniteSnackbar: AwSnackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snackbar_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        setupPresetStyles()
        setupActionButtons()
        setupBuilderCustom()
        setupCustomView()
        setupSnackbarManager()
        setupExtensions()
    }

    // ── 预设样式 ──────────────────────────────────────────

    private fun setupPresetStyles() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_default).setOnClickListener {
            showAwSnackbar("默认 Toast 风格提示")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_success).setOnClickListener {
            showAwSuccess("保存成功")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_warning).setOnClickListener {
            showAwWarning("存储空间不足")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_error).setOnClickListener {
            showAwError("网络连接失败")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_info).setOnClickListener {
            showAwInfo("新版本 v2.0 可用")
        }
    }

    // ── 带操作按钮 ────────────────────────────────────────

    private fun setupActionButtons() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_action).setOnClickListener {
            showAwSnackbar("文件已删除", "撤销") {
                showAwInfo("已撤销删除")
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_action_retry).setOnClickListener {
            AwSnackbar.make(findViewById(R.id.main))
                .error()
                .text("请求失败，请检查网络")
                .action("重试") { showAwSuccess("重试成功") }
                .show()
        }
    }

    // ── Builder 自定义 ────────────────────────────────────

    private fun setupBuilderCustom() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_long_text).setOnClickListener {
            AwSnackbar.make(findViewById(R.id.main))
                .info()
                .text("这是一条很长的提示信息，用于演示 maxLines 的效果。当文本内容超过最大行数时，超出部分将以省略号显示。")
                .maxLines(3)
                .duration(Snackbar.LENGTH_LONG)
                .show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_indefinite).setOnClickListener {
            indefiniteSnackbar?.dismiss()
            indefiniteSnackbar = AwSnackbar.make(findViewById(R.id.main))
                .warning()
                .text("持久显示，点击关闭按钮消除")
                .duration(Snackbar.LENGTH_INDEFINITE)
                .action("关闭") { indefiniteSnackbar = null }
                .show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_anchor).setOnClickListener {
            AwSnackbar.make(findViewById(R.id.main))
                .success()
                .text("锚定在按钮上方显示")
                .anchorView(it)
                .show()
        }
    }

    // ── 自定义 View ───────────────────────────────────────

    private fun setupCustomView() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_custom_view).setOnClickListener {
            val customLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val dp16 = (16 * resources.displayMetrics.density).toInt()
                val dp8 = (8 * resources.displayMetrics.density).toInt()
                setPadding(dp16, dp8, dp16, dp8)

                addView(ImageView(this.context).apply {
                    setImageDrawable(androidx.core.content.ContextCompat.getDrawable(
                        context, android.R.drawable.ic_dialog_info
                    ))
                    val dp20 = (20 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(dp20, dp20)
                })

                addView(TextView(this.context).apply {
                    text = "自定义布局：图标 + 文字"
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 14f
                    val dp8inner = (8 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { marginStart = dp8inner }
                })
            }

            AwSnackbar.make(findViewById(R.id.main))
                .customView(customLayout)
                .duration(Snackbar.LENGTH_LONG)
                .show()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_custom_progress).setOnClickListener {
            val customLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val dp16 = (16 * resources.displayMetrics.density).toInt()
                val dp8 = (8 * resources.displayMetrics.density).toInt()
                setPadding(dp16, dp8, dp16, dp8)

                addView(ProgressBar(this.context).apply {
                    isIndeterminate = true
                    val dp20 = (20 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(dp20, dp20)
                    indeterminateTintList = android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.WHITE
                    )
                })

                addView(TextView(this.context).apply {
                    text = "正在同步数据…"
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 14f
                    val dp8inner = (8 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { marginStart = dp8inner }
                })
            }

            AwSnackbar.make(findViewById(R.id.main))
                .customView(customLayout)
                .duration(Snackbar.LENGTH_INDEFINITE)
                .action("完成") { /* 手动关闭 */ }
                .show()
        }
    }

    // ── AwSnackbarManager ─────────────────────────────────

    private fun setupSnackbarManager() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_context_snackbar).setOnClickListener {
            // 通过 Context 直接调用，无需 View 引用
            // AwSnackbarManager.install() 已在 DemoApplication 中调用
            this.showAwSnackbar("通过 Context 自动查找锚点 View")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_context_success).setOnClickListener {
            this.showAwSuccess("Context.showAwSuccess() 无需传 View")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_manager_status).setOnClickListener {
            val activity = AwSnackbarManager.getCurrentActivity()
            val status = if (activity != null) {
                "已 install\n当前 Activity: ${activity.javaClass.simpleName}"
            } else {
                "未 install 或当前无前台 Activity"
            }
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        }
    }

    // ── 扩展函数对比 ──────────────────────────────────────

    private fun setupExtensions() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_view_ext).setOnClickListener {
            // View 扩展：直接以该按钮为锚点
            it.showAwError("View 扩展：以按钮为锚点")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_fragment_ext).setOnClickListener {
            // 模拟 Fragment 扩展：在 Activity 中等价于 Context 扩展
            supportFragmentManager.findFragmentById(android.R.id.content)?.let { fragment ->
                fragment.showAwInfo("Fragment 扩展调用")
            } ?: run {
                // 当前无 Fragment，用 Activity 的 Fragment 扩展等价演示
                showAwInfo("Fragment.showAwInfo() 等价效果")
            }
        }
    }
}
