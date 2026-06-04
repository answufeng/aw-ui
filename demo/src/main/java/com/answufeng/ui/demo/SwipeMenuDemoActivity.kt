package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwSwipeMenuLayout

class SwipeMenuDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_menu_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        // 编程方式添加滑动菜单项
        val container = findViewById<LinearLayout>(R.id.swipe_menu_container)
        val items = listOf("消息列表项 1", "消息列表项 2", "消息列表项 3")
        items.forEach { text ->
            val swipeLayout = AwSwipeMenuLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                onMenuOpenListener = { Toast.makeText(this@SwipeMenuDemoActivity, "菜单已打开", Toast.LENGTH_SHORT).show() }
                onMenuCloseListener = { Toast.makeText(this@SwipeMenuDemoActivity, "菜单已关闭", Toast.LENGTH_SHORT).show() }
            }

            // 内容视图
            val content = TextView(this).apply {
                this.text = text
                textSize = 16f
                setPadding(48, 48, 48, 48)
                setBackgroundColor(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // 菜单视图
            val menuLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            val editBtn = TextView(this).apply {
                this.text = "编辑"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#FF9800"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener {
                    Toast.makeText(this@SwipeMenuDemoActivity, "编辑: $text", Toast.LENGTH_SHORT).show()
                    swipeLayout.closeMenu()
                }
            }

            val deleteBtn = TextView(this).apply {
                this.text = "删除"
                setTextColor(Color.WHITE)
                textSize = 14f
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#F44336"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener {
                    Toast.makeText(this@SwipeMenuDemoActivity, "删除: $text", Toast.LENGTH_SHORT).show()
                    swipeLayout.closeMenu()
                }
            }

            menuLayout.addView(editBtn)
            menuLayout.addView(deleteBtn)
            swipeLayout.addView(content)
            swipeLayout.addView(menuLayout)
            container.addView(swipeLayout)
        }

        // 打开/关闭按钮
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_open_menu).setOnClickListener {
            if (container.childCount > 0) {
                (container.getChildAt(0) as AwSwipeMenuLayout).openMenu()
            }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_close_menu).setOnClickListener {
            if (container.childCount > 0) {
                (container.getChildAt(0) as AwSwipeMenuLayout).closeMenu()
            }
        }
    }
}
