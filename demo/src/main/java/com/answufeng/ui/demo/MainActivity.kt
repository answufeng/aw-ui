package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityMainBinding
import com.answufeng.ui.dialog.AwDialog
import com.answufeng.ui.dialog.LoadingDialog
import com.answufeng.ui.dialog.AwActionSheetDialog
import com.answufeng.ui.widget.AwBottomSheet
import com.answufeng.ui.widget.AwTagView

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
        setupFlowLayout()
        setupBadgeView()
        setupCircleProgress()
        setupFormValidation()
        setupTagView()
        setupTooltipView()

        log("✅ UI库初始化完成")
        log("📊 点击按钮测试各项功能")
    }

    private fun setupToolbar() {
        binding.titleBar.title = "🎨 UI库演示"
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
            AwDialog.Builder(this)
                .title("提示")
                .message("这是一个AwDialog对话框")
                .positiveButton("确定") {
                    log("✅ 点击了确定")
                }
                .negativeButton()
                .show()
        }

        binding.btnActionSheet.setOnClickListener {
            val items = arrayOf("选项1", "选项2", "选项3")
            AwActionSheetDialog(this)
                .setItems(items.toList())
                .setOnSelect { which ->
                    log("📋 选择了: ${items[which]}")
                }
                .setTitle("选择操作")
                .show()
        }

        binding.btnLoading.setOnClickListener {
            val dialog = LoadingDialog.show(this, "加载中...")
            binding.root.postDelayed({
                dialog?.dismiss()
            }, 2000)
            log("⏳ 加载对话框已显示")
        }

        binding.btnBottomSheet.setOnClickListener {
            val sheetView = TextView(this).apply {
                text = "这是一个BottomSheet内容"
                textSize = 16f
                setPadding(48, 48, 48, 48)
            }
            AwBottomSheet.Builder()
                .setContentView(sheetView)
                .setPeekHeight(400)
                .show(this)
            log("📋 BottomSheet已显示")
        }
    }

    private fun setupFlowLayout() {
        val tags = listOf("Kotlin", "Android", "UI", "ViewBinding", "Material", "RecyclerView", "Dialog")
        for (tag in tags) {
            val tv = TextView(this).apply {
                text = tag
                setPadding(24, 12, 24, 12)
                textSize = 14f
            }
            binding.flowLayout.addView(tv)
        }
    }

    private fun setupBadgeView() {
        binding.badgeView.count = 5
        binding.btnIncrementBadge.setOnClickListener {
            val newCount = binding.badgeView.count + 1
            binding.badgeView.count = newCount
            log("🔴 Badge count: $newCount")
        }
    }

    private fun setupCircleProgress() {
        binding.btnAnimateProgress.setOnClickListener {
            binding.circleProgressBar.setProgressWithAnimation(100f, 1500L)
            log("🔄 进度动画开始")
        }
    }

    private fun setupFormValidation() {
        binding.btnValidate.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val errors = mutableListOf<String>()
            if (username.length < 3) errors.add("用户名至少3个字符")
            if (!email.contains("@")) errors.add("邮箱格式不正确")
            if (errors.isEmpty()) {
                log("✅ 表单验证通过")
            } else {
                log("❌ ${errors.joinToString("; ")}")
            }
        }
    }

    private fun setupTagView() {
        binding.tagView.tags = listOf("标签1", "标签2", "标签3", "标签4", "标签5")
        binding.tagView.selectionMode = AwTagView.SelectionMode.MULTI
        binding.tagView.maxSelectCount = 3
        binding.tagView.onSelectionChange = { selected ->
            log("🏷️ 选中标签: $selected")
        }
    }

    private fun setupTooltipView() {
        binding.btnShowTooltip.setOnClickListener {
            binding.tooltipView.autoDismissDelay = 3000L
            binding.tooltipView.text = "这是一个Tooltip提示"
            binding.tooltipView.show(it)
            log("💬 Tooltip已显示")
        }
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
    }
}
