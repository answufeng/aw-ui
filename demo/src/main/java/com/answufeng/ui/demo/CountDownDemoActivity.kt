package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwCountDownView
import com.answufeng.ui.widget.AwTitleBar

class CountDownDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val countDownSms = findViewById<AwCountDownView>(R.id.countdown_sms)
        val countDownSkip = findViewById<AwCountDownView>(R.id.countdown_skip)
        val countDownSeconds = findViewById<AwCountDownView>(R.id.countdown_seconds)
        val countDownMmss = findViewById<AwCountDownView>(R.id.countdown_mmss)

        countDownSms.onStartClick = {
            log("模拟发送验证码...")
            true
        }

        countDownSms.setCountDownListener(object : AwCountDownView.CountDownListener {
            override fun onFinish() {
                log("验证码倒计时完成，可重新获取")
            }
        })

        countDownSkip.setCountDownListener(object : AwCountDownView.CountDownListener {
            override fun onFinish() { log("广告倒计时完成，可点击跳过") }
            override fun onSkip() {
                log("已跳过广告")
                Toast.makeText(this@CountDownDemoActivity, "跳过广告", Toast.LENGTH_SHORT).show()
            }
        })

        countDownSeconds.setCountDownListener(object : AwCountDownView.CountDownListener {
            override fun onFinish() { log("秒数模式倒计时完成") }
            override fun onSkip() { log("秒数模式已跳过") }
        })

        countDownMmss.setCountDownListener(object : AwCountDownView.CountDownListener {
            override fun onFinish() { log("分秒模式倒计时完成") }
            override fun onSkip() { log("分秒模式已跳过") }
        })

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_start_sms).setOnClickListener {
            countDownSms.reset()
            log("已重置验证码倒计时，点击「获取验证码」重新开始")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_start_skip).setOnClickListener {
            countDownSkip.startSeconds(5)
            log("开始 5 秒广告倒计时")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_start_seconds).setOnClickListener {
            countDownSeconds.startSeconds(10)
            log("开始 10 秒倒计时")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_start_mmss).setOnClickListener {
            countDownMmss.startSeconds(120)
            log("开始 2 分钟倒计时")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_reset).setOnClickListener {
            countDownSms.reset()
            countDownSkip.reset()
            countDownSeconds.reset()
            countDownMmss.reset()
            log("已重置")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_skip).setOnClickListener {
            countDownSkip.skip()
            countDownSeconds.skip()
            countDownMmss.skip()
            log("已跳过")
        }
    }

    private fun log(msg: String) {
        val tv = findViewById<TextView>(R.id.tv_log)
        tv.append("• $msg\n")
    }
}
