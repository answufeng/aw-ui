package com.answufeng.ui.demo

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwFlowLayout
import com.answufeng.ui.widget.AwTagView
import com.answufeng.ui.widget.AwTitleBar

class FlowTagDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_tag_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        setupFlowLayout()
        setupTagViewSingle()
        setupTagViewMulti()
        setupTagViewNone()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_clear_selection).setOnClickListener {
            findViewById<AwTagView>(R.id.tag_view_single).clearSelection()
            findViewById<AwTagView>(R.id.tag_view_multi).clearSelection()
            log("已清除所有选中")
        }
    }

    private fun setupFlowLayout() {
        val flowLayout = findViewById<AwFlowLayout>(R.id.flow_layout)
        val tags = listOf("Kotlin", "Android", "Material3", "ViewBinding", "Dialog", "StateLayout", "RecyclerView")
        for (tag in tags) {
            val tv = TextView(this).apply {
                text = tag
                setPadding(24, 12, 24, 12)
                textSize = 14f
                setTextColor(getColor(R.color.on_surface))
                background = GradientDrawable().apply {
                    cornerRadius = 16f
                    setColor(getColor(R.color.surface_variant))
                }
            }
            flowLayout.addView(tv)
        }
    }

    private fun setupTagViewSingle() {
        val tagView = findViewById<AwTagView>(R.id.tag_view_single)
        tagView.tags = listOf("全部", "最新", "热门", "推荐", "精选")
        tagView.onSelectionChange = { selected -> log("单选选中: $selected") }
    }

    private fun setupTagViewMulti() {
        val tagView = findViewById<AwTagView>(R.id.tag_view_multi)
        tagView.tags = listOf("科技", "体育", "娱乐", "财经", "健康", "教育")
        tagView.onSelectionChange = { selected -> log("多选选中: $selected") }
    }

    private fun setupTagViewNone() {
        val tagView = findViewById<AwTagView>(R.id.tag_view_none)
        tagView.tags = listOf("已售罄", "新品", "限时", "热卖", "包邮")
        tagView.onTagClick = { tag, _ -> log("点击标签: $tag") }
    }

    private fun log(msg: String) {
        val tv = findViewById<TextView>(R.id.tv_log)
        tv.append("• $msg\n")
    }
}
