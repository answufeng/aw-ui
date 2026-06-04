package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.answufeng.ui.widget.AwDotIndicator

class DotIndicatorDemoActivity : AppCompatActivity() {

    private val pages = listOf("页面 A", "页面 B", "页面 C", "页面 D")
    private val colors = listOf("#E57373", "#81C784", "#64B5F6", "#FFD54F")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dot_indicator_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val dotIndicator = findViewById<AwDotIndicator>(R.id.dot_indicator)

        viewPager.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val textView = TextView(parent.context).apply {
                    textSize = 24f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(textView) {}
            }

            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).apply {
                    text = pages[position]
                    setBackgroundColor(Color.parseColor(colors[position]))
                }
            }

            override fun getItemCount() = pages.size
        }

        dotIndicator.bindViewPager2(viewPager)
    }
}
