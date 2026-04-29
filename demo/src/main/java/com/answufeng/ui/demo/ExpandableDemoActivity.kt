package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwExpandableLayout
import com.answufeng.ui.widget.AwTitleBar

class ExpandableDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val expandable1 = findViewById<AwExpandableLayout>(R.id.expandable_1)
        val expandable2 = findViewById<AwExpandableLayout>(R.id.expandable_2)
        val expandable3 = findViewById<AwExpandableLayout>(R.id.expandable_3)

        expandable1.onExpandChange = { isExpanded -> log("基础: ${if (isExpanded) "展开" else "收起"}") }
        expandable2.onExpandChange = { isExpanded -> log("慢速: ${if (isExpanded) "展开" else "收起"}") }
        expandable3.onExpandChange = { isExpanded -> log("默认展开: ${if (isExpanded) "展开" else "收起"}") }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_toggle_1).setOnClickListener {
            expandable1.toggle()
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_toggle_2).setOnClickListener {
            expandable2.toggle()
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_toggle_3).setOnClickListener {
            expandable3.toggle()
        }
    }

    private fun log(msg: String) {
        val tv = findViewById<TextView>(R.id.tv_log)
        tv.append("• $msg\n")
    }
}
