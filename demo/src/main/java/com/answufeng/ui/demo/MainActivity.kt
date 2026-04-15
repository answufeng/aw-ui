package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.dialog.AwActionSheetDialog
import com.answufeng.ui.dialog.LoadingDialog
import com.answufeng.ui.form.AwFormValidator
import com.answufeng.ui.widget.AwBadgeView
import com.answufeng.ui.widget.AwCircleProgressBar
import com.answufeng.ui.widget.AwCodeInputView
import com.answufeng.ui.widget.AwCountDownView
import com.answufeng.ui.widget.AwExpandableLayout
import com.answufeng.ui.widget.AwFlowLayout
import com.answufeng.ui.widget.AwMarqueeTextView
import com.answufeng.ui.widget.AwPasswordInputView
import com.answufeng.ui.widget.AwRoundImageView
import com.answufeng.ui.widget.AwRoundLayout
import com.answufeng.ui.widget.AwSegmentedControl
import com.answufeng.ui.widget.AwSkeletonView
import com.answufeng.ui.widget.AwSmartEditText
import com.answufeng.ui.widget.AwSwitchButton
import com.answufeng.ui.widget.AwTooltipView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private lateinit var logScrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 主布局
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)

        // 标题
        mainLayout.addView(TextView(this).apply {
            text = "🎨 aw-ui 功能演示"
            textSize = 20f
            setPadding(0, 0, 0, 20)
        })

        // 输入组件卡片
        val inputCard = createCard("输入组件")
        val inputLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inputLayout.addView(createButton("✏️ 智能输入框", ::testSmartEditText))
        inputLayout.addView(createButton("🔢 验证码输入", ::testCodeInputView))
        inputLayout.addView(createButton("🔒 密码输入", ::testPasswordInputView))
        inputCard.addView(inputLayout)
        mainLayout.addView(inputCard)

        // 选择组件卡片
        val selectCard = createCard("选择组件")
        val selectLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        selectLayout.addView(createButton("🎯 分段选择", ::testSegmentedControl))
        selectLayout.addView(createButton("🔄 开关按钮", ::testSwitchButton))
        selectCard.addView(selectLayout)
        mainLayout.addView(selectCard)

        // 进度组件卡片
        val progressCard = createCard("进度组件")
        val progressLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        progressLayout.addView(createButton("📊 圆形进度条", ::testCircleProgressBar))
        progressLayout.addView(createButton("⏳ 倒计时", ::testCountDownView))
        progressCard.addView(progressLayout)
        mainLayout.addView(progressCard)

        // 布局组件卡片
        val layoutCard = createCard("布局组件")
        val layoutLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layoutLayout.addView(createButton("📋 流式布局", ::testFlowLayout))
        layoutLayout.addView(createButton("🔄 可展开布局", ::testExpandableLayout))
        layoutLayout.addView(createButton("🔲 圆角布局", ::testRoundLayout))
        layoutCard.addView(layoutLayout)
        mainLayout.addView(layoutCard)

        // 特效组件卡片
        val effectCard = createCard("特效组件")
        val effectLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        effectLayout.addView(createButton("🏷️ 徽章视图", ::testBadgeView))
        effectLayout.addView(createButton("🏃 跑马灯文本", ::testMarqueeTextView))
        effectLayout.addView(createButton("💬 提示气泡", ::testTooltipView))
        effectLayout.addView(createButton("💀 骨架屏", ::testSkeletonView))
        effectLayout.addView(createButton("🖼️ 圆角图片", ::testRoundImageView))
        effectCard.addView(effectLayout)
        mainLayout.addView(effectCard)

        // 对话框卡片
        val dialogCard = createCard("对话框")
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        dialogLayout.addView(createButton("📢 提示对话框", ::testAlertDialog))
        dialogLayout.addView(createButton("📋 操作菜单", ::testActionSheet))
        dialogLayout.addView(createButton("⏳ 加载对话框", ::testLoadingDialog))
        dialogCard.addView(dialogLayout)
        mainLayout.addView(dialogCard)

        // 表单验证卡片
        val formCard = createCard("表单验证")
        val formLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        formLayout.addView(createButton("✅ 表单验证", ::testFormValidation))
        formCard.addView(formLayout)
        mainLayout.addView(formCard)

        // 管理功能卡片
        val manageCard = createCard("管理功能")
        val manageLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        manageLayout.addView(createButton("🗑️ 清除日志", ::clearLog))
        manageCard.addView(manageLayout)
        mainLayout.addView(manageCard)

        // 日志区域
        mainLayout.addView(TextView(this).apply {
            text = "操作日志："
            textSize = 16f
            setPadding(0, 20, 0, 10)
        })

        logScrollView = findViewById(R.id.logScrollView)
        tvLog = findViewById(R.id.tvLog)

        // 显示初始信息
        log("✅ UI 库初始化完成")
        log("📊 点击按钮测试各项功能")
    }

    private fun createCard(title: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            setPadding(20, 20, 20, 20)

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                setPadding(0, 0, 0, 12)
            })
        }
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 4)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun log(msg: String) {
        tvLog.append("$msg\n")
        logScrollView.post { logScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        android.util.Log.d("AwUIDemo", msg)
    }

    private fun clearLog() {
        tvLog.text = "日志已清除\n"
    }

    private fun testSmartEditText() {
        val smartEditText = findViewById<AwSmartEditText>(R.id.smartEditText)
        smartEditText.inputFilter = { it.isDigit() }
        smartEditText.addValidator("phone", { it.length == 11 }, "请输入11位手机号")
        smartEditText.setText("13812345678")
        log("✏️ 智能输入框: 已设置手机号过滤器")
    }

    private fun testCodeInputView() {
        val codeInputView = findViewById<AwCodeInputView>(R.id.codeInputView)
        codeInputView.onCodeComplete = { code ->
            log("🔢 验证码输入完成: $code")
            Toast.makeText(this, "验证码: $code", Toast.LENGTH_SHORT).show()
        }
        codeInputView.setText("")
        log("🔢 验证码输入: 已重置输入框")
    }

    private fun testPasswordInputView() {
        val passwordView = findViewById<AwPasswordInputView>(R.id.passwordInputView)
        passwordView.setText("123456")
        log("🔒 密码输入: 已设置密码")
    }

    private fun testSegmentedControl() {
        val segmentedControl = findViewById<AwSegmentedControl>(R.id.segmentedControl)
        segmentedControl.onSelectionChange = { index ->
            log("🎯 分段选择: 索引 $index")
        }
        segmentedControl.setSelected(1, true)
        log("🎯 分段选择: 已选择索引 1")
    }

    private fun testSwitchButton() {
        val switchButton = findViewById<AwSwitchButton>(R.id.switchButton)
        switchButton.onCheckedChangeListener = { checked ->
            log("🔄 开关: ${if (checked) "开启" else "关闭"}")
        }
        switchButton.isChecked = !switchButton.isChecked
        log("🔄 开关: 已切换状态")
    }

    private fun testCircleProgressBar() {
        val circleProgressBar = findViewById<AwCircleProgressBar>(R.id.circleProgressBar)
        val target = (0..100).random()
        circleProgressBar.setProgressWithAnimation(target.toFloat())
        log("📊 圆形进度: $target%")
    }

    private fun testCountDownView() {
        val countDownView = findViewById<AwCountDownView>(R.id.countDownView)
        countDownView.onFinish = {
            log("⏳ 倒计时结束")
            Toast.makeText(this, "倒计时结束", Toast.LENGTH_SHORT).show()
        }
        countDownView.start(5)
        log("⏳ 开始5秒倒计时")
    }

    private fun testFlowLayout() {
        val flowLayout = findViewById<AwFlowLayout>(R.id.flowLayout)
        flowLayout.removeAllViews()
        val labels = listOf("Kotlin", "Java", "Android", "Flutter", "Compose", "iOS")
        for (label in labels) {
            val chip = Button(this).apply {
                text = label
                setAllCaps(false)
                textSize = 12f
                setOnClickListener {
                    log("📋 FlowLayout 点击: $label")
                }
            }
            flowLayout.addView(chip)
        }
        log("📋 流式布局: 已添加标签")
    }

    private fun testExpandableLayout() {
        val expandableLayout = findViewById<AwExpandableLayout>(R.id.expandableLayout)
        expandableLayout.toggle()
        log("🔄 可展开布局: ${if (expandableLayout.expanded) "已展开" else "已收起"}")
    }

    private fun testRoundLayout() {
        val roundLayout = findViewById<AwRoundLayout>(R.id.roundLayout)
        roundLayout.cornerRadius = 20f
        log("🔲 圆角布局: 已设置圆角半径 20dp")
    }

    private fun testBadgeView() {
        val badgeView = findViewById<AwBadgeView>(R.id.badgeView)
        val count = (1..99).random()
        badgeView.count = count
        log("🏷️ 徽章视图: 已设置数量 $count")
    }

    private fun testMarqueeTextView() {
        val marqueeView = findViewById<AwMarqueeTextView>(R.id.marqueeTextView)
        marqueeView.text = "这是一个跑马灯文本效果，用于显示较长的文本内容..."
        marqueeView.startScroll()
        log("🏃 跑马灯文本: 已开始滚动")
    }

    private fun testTooltipView() {
        val tooltipView = findViewById<AwTooltipView>(R.id.tooltipView)
        tooltipView.show("这是一个提示气泡")
        log("💬 提示气泡: 已显示")
    }

    private fun testSkeletonView() {
        val skeletonView = findViewById<AwSkeletonView>(R.id.skeletonView)
        skeletonView.show()
        log("💀 骨架屏: 已显示")
        // 2秒后隐藏
        window?.decorView?.postDelayed({
            skeletonView.hide()
            log("💀 骨架屏: 已隐藏")
        }, 2000)
    }

    private fun testRoundImageView() {
        val roundImageView = findViewById<AwRoundImageView>(R.id.roundImageView)
        roundImageView.cornerRadius = 15f
        log("🖼️ 圆角图片: 已设置圆角")
    }

    private fun testAlertDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("提示")
            .setMessage("这是 aw-ui 的 AlertDialog 演示")
            .setPositiveButton("确定") { _, _ -> log("📢 提示对话框: 点击确定") }
            .setNegativeButton("取消") { _, _ -> log("📢 提示对话框: 点击取消") }
            .show()
        log("📢 提示对话框: 已显示")
    }

    private fun testActionSheet() {
        AwActionSheetDialog(this)
            .setTitle("选择操作")
            .setItems(listOf("拍照", "从相册选择", "删除"))
            .setDestructiveIndex(2)
            .setOnSelect { index ->
                val items = listOf("拍照", "从相册选择", "删除")
                log("📋 ActionSheet 选择: ${items[index]}")
                Toast.makeText(this, "选择了: ${items[index]}", Toast.LENGTH_SHORT).show()
            }
            .setOnCancel { log("📋 ActionSheet 取消") }
            .show()
        log("📋 ActionSheet: 已显示")
    }

    private fun testLoadingDialog() {
        LoadingDialog.show(this, "加载中...")
        log("⏳ LoadingDialog: 已显示")
        window?.decorView?.postDelayed({ LoadingDialog.dismiss() }, 2000)
    }

    private fun testFormValidation() {
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)

        etUsername.setText("user")
        etEmail.setText("user@example.com")

        val validator = AwFormValidator()
            .addField(etUsername, AwFormValidator.required(), AwFormValidator.minLength(3))
            .addField(etEmail, AwFormValidator.required(), AwFormValidator.email())

        if (validator.validate()) {
            log("✅ 表单验证通过")
            Toast.makeText(this, "验证通过", Toast.LENGTH_SHORT).show()
        } else {
            val errors = validator.getErrors()
            log("❌ 表单验证失败: ${errors.size} 个错误")
            errors.values.forEach { log("  - $it") }
        }
    }
}
