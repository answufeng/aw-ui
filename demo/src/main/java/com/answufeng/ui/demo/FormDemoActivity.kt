package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.form.AwFormValidator
import com.answufeng.ui.widget.AwCodeInputView
import com.answufeng.ui.widget.AwTitleBar
import com.google.android.material.textfield.TextInputEditText

class FormDemoActivity : AppCompatActivity() {

    private lateinit var validator: AwFormValidator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val codeInput = findViewById<AwCodeInputView>(R.id.codeInput)
        val tvResult = findViewById<android.widget.TextView>(R.id.tvResult)

        validator =
            AwFormValidator()
                .addField(etEmail, AwFormValidator.required("请输入邮箱"), AwFormValidator.email())
                .addField(etPhone, AwFormValidator.required("请输入手机号"), AwFormValidator.phone())
                .addCustomField(
                    codeInput,
                    getter = { codeInput.code },
                    AwFormValidator.required("请输入验证码"),
                    AwFormValidator.minLength(6, "请输入 6 位验证码"),
                )

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnValidate).setOnClickListener {
            validator.clearErrors()
            if (validator.validate()) {
                tvResult.text = "校验通过"
                Toast.makeText(this, "提交成功", Toast.LENGTH_SHORT).show()
            } else {
                val msg = validator.getErrors().values.firstOrNull() ?: "校验失败"
                tvResult.text = msg
            }
        }
    }
}
