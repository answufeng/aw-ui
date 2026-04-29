package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwSearchView
import com.answufeng.ui.widget.AwTitleBar

class SearchDemoActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private var colorToggle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }
        tvLog = findViewById(R.id.tv_log)

        setupDefaultSearch()
        setupCustomSearch()
        setupDarkSearch()
        setupDynamicSearch()
    }

    private fun setupDefaultSearch() {
        val search = findViewById<AwSearchView>(R.id.search_default)
        search.onQueryChange = { log("默认搜索-文字变化: $it") }
        search.onQuerySubmit = { log("默认搜索-提交: $it") }
        search.onClearClick = { log("默认搜索-清除") }
        search.onSearchFocusChange = { hasFocus -> log("默认搜索-焦点: $hasFocus") }
    }

    private fun setupCustomSearch() {
        val search = findViewById<AwSearchView>(R.id.search_custom)
        search.onQueryChange = { log("自定义搜索-文字变化: $it") }
        search.onQuerySubmit = { log("自定义搜索-提交: $it") }
    }

    private fun setupDarkSearch() {
        val search = findViewById<AwSearchView>(R.id.search_dark)
        search.onQueryChange = { log("深色搜索-文字变化: $it") }
        search.onQuerySubmit = { log("深色搜索-提交: $it") }
    }

    private fun setupDynamicSearch() {
        val search = findViewById<AwSearchView>(R.id.search_dynamic)
        search.onQueryChange = { log("动态搜索-文字变化: $it") }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_hint).setOnClickListener {
            search.hint = if (search.hint == "搜索") "请输入商品名称" else "搜索"
            log("动态修改hint: ${search.hint}")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_color).setOnClickListener {
            colorToggle = !colorToggle
            if (colorToggle) {
                search.searchBackgroundColor = Color.parseColor("#FFF3E0")
                search.searchIconColor = Color.parseColor("#FF9800")
                search.searchTextColor = Color.parseColor("#E65100")
                search.searchHintColor = Color.parseColor("#FFB74D")
            } else {
                search.searchBackgroundColor = Color.parseColor("#F5F5F5")
                search.searchIconColor = Color.GRAY
                search.searchTextColor = Color.BLACK
                search.searchHintColor = Color.GRAY
            }
            log("动态修改颜色: ${if (colorToggle) "橙色主题" else "默认主题"}")
        }
    }

    private fun log(msg: String) {
        tvLog.append("• $msg\n")
    }
}
