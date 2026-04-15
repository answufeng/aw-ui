package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.dialog.AwActionSheetDialog
import com.answufeng.ui.dialog.LoadingDialog
import com.answufeng.ui.form.AwFormValidator
import com.answufeng.ui.widget.AwBadgeView
import com.answufeng.ui.widget.AwCircleProgressBar
import com.answufeng.ui.widget.AwCodeInputView
import com.answufeng.ui.widget.AwExpandableLayout
import com.answufeng.ui.widget.AwFlowLayout
import com.answufeng.ui.widget.AwSegmentedControl
import com.answufeng.ui.widget.AwSmartEditText
import com.answufeng.ui.widget.AwSwitchButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)

        setupInputComponents()
        setupSelectComponents()
        setupProgressComponents()
        setupToolComponents()
        setupEffectComponents()
        setupDialogComponents()
        setupFormValidation()
        setupLayoutComponents()
    }

    private fun setupInputComponents() {
        val smartEditText = findViewById<AwSmartEditText>(R.id.smartEditText)
        smartEditText.inputFilter = { it.isDigit() }
        smartEditText.addValidator("phone", { it.length == 11 }, "请输入11位手机号")

        val codeInputView = findViewById<AwCodeInputView>(R.id.codeInputView)
        codeInputView.onCodeComplete = { code ->
            addLog("验证码输入完成: $code")
        }
    }

    private fun setupSelectComponents() {
        val segmentedControl = findViewById<AwSegmentedControl>(R.id.segmentedControl)
        segmentedControl.onSelectionChange = { index ->
            addLog("分段选择: 索引 $index")
        }
    }

    private fun setupProgressComponents() {
        val circleProgressBar = findViewById<AwCircleProgressBar>(R.id.circleProgressBar)
        circleProgressBar.setOnClickListener {
            val target = (0..100).random()
            circleProgressBar.setProgressWithAnimation(target.toFloat())
            addLog("圆形进度: $target%")
        }
    }

    private fun setupToolComponents() {
        val switchButton = findViewById<AwSwitchButton>(R.id.switchButton)
        switchButton.onCheckedChangeListener = { checked ->
            addLog("开关: ${if (checked) "开启" else "关闭"}")
        }

        val countDownView = findViewById<com.answufeng.ui.widget.AwCountDownView>(R.id.countDownView)
        countDownView.onFinish = {
            addLog("倒计时结束")
        }

        findViewById<Button>(R.id.btnStartCountDown).setOnClickListener {
            countDownView.start(10)
            addLog("开始10秒倒计时")
        }
    }

    private fun setupEffectComponents() {
        val expandableLayout = findViewById<AwExpandableLayout>(R.id.expandableLayout)
        findViewById<Button>(R.id.btnToggleExpand).setOnClickListener {
            expandableLayout.toggle()
            addLog("展开/收起: ${if (expandableLayout.expanded) "已展开" else "已收起"}")
        }
    }

    private fun setupDialogComponents() {
        findViewById<Button>(R.id.btnAlertDialog).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("提示")
                .setMessage("这是 aw-ui 的 AlertDialog 演示")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .show()
        }

        findViewById<Button>(R.id.btnActionSheet).setOnClickListener {
            AwActionSheetDialog(this)
                .setTitle("选择操作")
                .setItems(listOf("拍照", "从相册选择", "删除"))
                .setDestructiveIndex(2)
                .setOnSelect { index ->
                    addLog("ActionSheet 选择: 索引 $index")
                    Toast.makeText(this, "选择了: ${listOf("拍照", "从相册选择", "删除")[index]}", Toast.LENGTH_SHORT).show()
                }
                .setOnCancel { addLog("ActionSheet 取消") }
                .show()
        }

        findViewById<Button>(R.id.btnLoading).setOnClickListener {
            LoadingDialog.show(this, "加载中...")
            addLog("显示 LoadingDialog")
            window?.decorView?.postDelayed({
                LoadingDialog::class.java.getDeclaredMethod("dismiss").apply {
                    isAccessible = true
                }
                addLog("LoadingDialog 已关闭")
            }, 2000)
        }
    }

    private fun setupFormValidation() {
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)

        val validator = AwFormValidator()
            .addField(etUsername, AwFormValidator.required(), AwFormValidator.minLength(3))
            .addField(etEmail, AwFormValidator.required(), AwFormValidator.email())

        findViewById<Button>(R.id.btnValidate).setOnClickListener {
            if (validator.validate()) {
                addLog("表单验证通过")
                Toast.makeText(this, "验证通过", Toast.LENGTH_SHORT).show()
            } else {
                val errors = validator.getErrors()
                addLog("表单验证失败: ${errors.size} 个错误")
                errors.values.forEach { addLog("  - $it") }
            }
        }
    }

    private fun setupLayoutComponents() {
        val flowLayout = findViewById<AwFlowLayout>(R.id.flowLayout)
        val labels = listOf("Kotlin", "Java", "Android", "Flutter", "Compose", "iOS", "React Native", "Swift")
        for (label in labels) {
            val chip = Button(this).apply {
                text = label
                setAllCaps(false)
                textSize = 12f
                setOnClickListener {
                    addLog("FlowLayout 点击: $label")
                }
            }
            flowLayout.addView(chip)
        }

        val badgeView = findViewById<AwBadgeView>(R.id.badgeView)
        badgeView.count = 5
    }

    private fun addLog(message: String) {
        val currentLog = tvLog.text.toString()
        val prefix = if (currentLog == "操作日志将显示在这里...") "" else "$currentLog\n"
        tvLog.text = "$prefix[$message]"
    }
}
