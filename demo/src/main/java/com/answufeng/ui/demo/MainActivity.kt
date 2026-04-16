package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvLog = binding.tvLog

        setupToolbar()
        setupSegmentedControl()
        setupSwitchButton()
        setupCountDownView()
        setupMarqueeText()
        setupExpandableLayout()
        setupDialogs()

        log("✅ UI库初始化完成")
        log("📊 点击按钮测试各项功能")
    }

    private fun setupToolbar() {
        binding.toolbar.title = "🎨 UI库演示"
        binding.toolbar.setTitleTextColor(0xFFFFFFFF.toInt())
    }

    private fun setupSegmentedControl() {
        binding.segmentedControl.items = listOf("选项1", "选项2", "选项3")
        binding.segmentedControl.onSelectionChange = { index ->
            log("📋 分段控制选择: 索引=$index")
        }
    }

    private fun setupSwitchButton() {
        binding.switchButton.onCheckedChangeListener = { isChecked ->
            log("🔘 开关状态: $isChecked")
        }
    }

    private fun setupCountDownView() {
        binding.btnStartCountDown.setOnClickListener {
            binding.countDownView.start(10)
            log("⏱️ 倒计时开始: 10秒")
        }
    }

    private fun setupMarqueeText() {
        binding.marqueeTextView.setText("这是一条滚动的文字公告内容！")
    }

    private fun setupExpandableLayout() {
        binding.btnToggleExpand.setOnClickListener {
            binding.expandableLayout.toggle()
        }
    }

    private fun setupDialogs() {
        binding.btnAlertDialog.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("提示")
                .setMessage("这是一个AlertDialog对话框")
                .setPositiveButton("确定", null)
                .show()
        }

        binding.btnActionSheet.setOnClickListener {
            val items = arrayOf("选项1", "选项2", "选项3")
            MaterialAlertDialogBuilder(this)
                .setTitle("选择操作")
                .setItems(items) { _, which ->
                    log("📋 选择了: ${items[which]}")
                }
                .show()
        }

        binding.btnLoading.setOnClickListener {
            log("⏳ 加载对话框已显示")
        }
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
    }
}
