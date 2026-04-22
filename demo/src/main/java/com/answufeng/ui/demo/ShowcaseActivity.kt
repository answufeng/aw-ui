package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityMainBinding
import com.answufeng.ui.dialog.AwActionSheetDialog
import com.answufeng.ui.dialog.AwDialog
import com.answufeng.ui.dialog.LoadingDialog
import com.answufeng.ui.widget.AwBottomSheet
import com.answufeng.ui.widget.AwTagView

class ShowcaseActivity : AppCompatActivity() {

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

        log("UI 组件初始化完成")
        log("点击按钮体验交互与回调")
    }

    private fun setupToolbar() {
        binding.titleBar.title = "组件总览"
        binding.titleBar.setOnBackClickListener { finish() }
    }

    private fun setupSegmentedControl() {
        binding.segmentedControl.items = listOf("选项1", "选项2", "选项3")
        binding.segmentedControl.onSelectionChange = { index ->
            log("分段控制选择: index=$index")
        }
    }

    private fun setupSwitchButton() {
        binding.switchButton.onCheckedChangeListener = { isChecked ->
            log("开关状态: $isChecked")
        }
    }

    private fun setupCountDownView() {
        binding.btnStartCountDown.setOnClickListener {
            binding.countDownView.startSeconds(10)
            log("倒计时开始: 10s")
        }
    }

    private fun setupMarqueeText() {
        binding.marqueeTextView.setText("这是一条滚动公告：支持超长文本自动滚动展示。")
    }

    private fun setupExpandableLayout() {
        binding.btnToggleExpand.setOnClickListener {
            binding.expandableLayout.toggle()
            log("切换展开状态")
        }
    }

    private fun setupDialogs() {
        binding.btnAlertDialog.setOnClickListener {
            AwDialog.Builder(this)
                .title("提示")
                .message("这是一个 AwDialog 对话框示例")
                .positiveButton("确定") { log("点击了确定") }
                .negativeButton("取消") { log("点击了取消") }
                .show()
        }

        binding.btnActionSheet.setOnClickListener {
            val items = arrayOf("分享", "收藏", "删除")
            AwActionSheetDialog(this)
                .setItems(items.toList())
                .setOnSelect { which -> log("选择了: ${items[which]}") }
                .setTitle("选择操作")
                .show()
        }

        binding.btnLoading.setOnClickListener {
            val dialog = LoadingDialog.show(this, "加载中…")
            binding.root.postDelayed({ dialog?.dismiss() }, 1500)
            log("显示 LoadingDialog")
        }

        binding.btnBottomSheet.setOnClickListener {
            val sheetView = TextView(this).apply {
                text = "这是一个 BottomSheet 内容区域\n\n你可以在这里放表单、列表或操作项。"
                textSize = 16f
                setPadding(48, 48, 48, 48)
            }
            AwBottomSheet.Builder()
                .setContentView(sheetView)
                .setPeekHeight(520)
                .show(this)
            log("显示 BottomSheet")
        }
    }

    private fun setupFlowLayout() {
        val tags = listOf("Kotlin", "Android", "Material3", "ViewBinding", "Dialog", "StateLayout", "RecyclerView")
        binding.flowLayout.removeAllViews()
        for (tag in tags) {
            val tv = TextView(this).apply {
                text = tag
                setPadding(24, 12, 24, 12)
                textSize = 14f
                setTextColor(getColor(R.color.on_surface))
                setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            }
            binding.flowLayout.addView(tv)
        }
    }

    private fun setupBadgeView() {
        binding.badgeView.count = 5
        binding.btnIncrementBadge.setOnClickListener {
            val newCount = binding.badgeView.count + 1
            binding.badgeView.count = newCount
            log("Badge count: $newCount")
        }
    }

    private fun setupCircleProgress() {
        binding.btnAnimateProgress.setOnClickListener {
            binding.circleProgressBar.setProgressWithAnimation(100f, 1500L)
            log("进度动画开始")
        }
    }

    private fun setupFormValidation() {
        binding.btnValidate.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val errors = mutableListOf<String>()
            if (username.length < 3) errors.add("用户名至少 3 个字符")
            if (!email.contains("@")) errors.add("邮箱格式不正确")
            if (errors.isEmpty()) {
                log("表单验证通过")
            } else {
                log(errors.joinToString("；"))
            }
        }
    }

    private fun setupTagView() {
        binding.tagView.tags = listOf("标签1", "标签2", "标签3", "标签4", "标签5")
        binding.tagView.selectionMode = AwTagView.SelectionMode.MULTI
        binding.tagView.maxSelectCount = 3
        binding.tagView.onSelectionChange = { selected ->
            log("选中标签: $selected")
        }
    }

    private fun setupTooltipView() {
        binding.btnShowTooltip.setOnClickListener {
            binding.tooltipView.autoDismissDelay = 2500L
            binding.tooltipView.text = "这是一个 Tooltip 提示"
            binding.tooltipView.show(it)
            log("显示 Tooltip")
        }
    }

    private fun log(msg: String) {
        tvLog.append("• $msg\n")
    }
}

