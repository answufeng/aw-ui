package com.answufeng.ui.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwNoticeBar
import com.answufeng.ui.widget.AwTitleBar

class NoticeBarDemoActivity : AppCompatActivity() {

    private var messageIndex = 0
    private val messages =
        listOf(
            "欢迎使用 aw-ui，点击通知条或下方按钮切换文案",
            "新版本已发布，点击查看更新说明",
            "限时活动进行中，满减优惠等你来",
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_bar_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val noticeBar = findViewById<AwNoticeBar>(R.id.noticeBar)
        noticeBar.onBarClick = {
            noticeBar.showMessage("你点击了通知条")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnUpdateMessage).setOnClickListener {
            messageIndex = (messageIndex + 1) % messages.size
            noticeBar.showMessage(messages[messageIndex])
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnShowNotice).setOnClickListener {
            noticeBar.visibility = View.VISIBLE
            noticeBar.showMessage(messages[messageIndex])
        }
    }
}
