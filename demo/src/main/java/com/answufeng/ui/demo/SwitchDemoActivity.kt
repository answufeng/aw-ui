package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwSwitchButton
import com.answufeng.ui.widget.AwTitleBar

class SwitchDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val switchDefault = findViewById<AwSwitchButton>(R.id.switch_default)
        val tvSwitchStatus = findViewById<TextView>(R.id.tv_switch_status)
        val tvLog = findViewById<TextView>(R.id.tv_log)

        switchDefault.onCheckedChangeListener = { checked ->
            tvSwitchStatus.text = if (checked) "ON" else "OFF"
            log(tvLog, "默认开关: ${if (checked) "ON" else "OFF"}")
        }

        findViewById<AwSwitchButton>(R.id.switch_green).onCheckedChangeListener = { checked ->
            log(tvLog, "绿色开关: $checked")
        }
        findViewById<AwSwitchButton>(R.id.switch_red).onCheckedChangeListener = { checked ->
            log(tvLog, "红色开关: $checked")
        }
        findViewById<AwSwitchButton>(R.id.switch_orange).onCheckedChangeListener = { checked ->
            log(tvLog, "橙色开关: $checked")
        }
        findViewById<AwSwitchButton>(R.id.switch_checked).onCheckedChangeListener = { checked ->
            log(tvLog, "默认选中开关: $checked")
        }
    }

    private fun log(tv: TextView, msg: String) {
        tv.append("• $msg\n")
    }
}
