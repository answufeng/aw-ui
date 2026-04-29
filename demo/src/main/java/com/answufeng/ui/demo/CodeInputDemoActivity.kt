package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwCodeInputView
import com.answufeng.ui.widget.AwTitleBar

class CodeInputDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_input_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val codeInput6 = findViewById<AwCodeInputView>(R.id.code_input_6)
        val codeInput4 = findViewById<AwCodeInputView>(R.id.code_input_4)
        val codeInputCustom = findViewById<AwCodeInputView>(R.id.code_input_custom)

        codeInput6.onCodeComplete = { code -> log("6位验证码完成: $code") }
        codeInput4.onCodeComplete = { code -> log("4位验证码完成: $code") }
        codeInputCustom.onCodeComplete = { code -> log("自定义验证码完成: $code") }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_fill_code).setOnClickListener {
            codeInput6.code = "123456"
            log("程序化填充: 123456")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_clear_code).setOnClickListener {
            codeInput6.code = ""
            codeInput4.code = ""
            codeInputCustom.code = ""
            log("已清空所有验证码")
        }
    }

    private fun log(msg: String) {
        val tv = findViewById<TextView>(R.id.tv_log)
        tv.append("• $msg\n")
    }
}
