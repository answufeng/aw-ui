package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.answufeng.ui.anim.AwItemAnimator
import com.answufeng.ui.anim.bounce
import com.answufeng.ui.anim.fadeIn
import com.answufeng.ui.anim.fadeOut
import com.answufeng.ui.anim.fadeSlideIn
import com.answufeng.ui.anim.pulse
import com.answufeng.ui.anim.shake
import com.answufeng.ui.anim.slideInFromBottom
import com.answufeng.ui.dialog.AwDialog
import com.answufeng.ui.recyclerview.DividerDecoration
import com.answufeng.ui.recyclerview.LoadMoreAdapter
import com.answufeng.ui.recyclerview.SimpleAdapter
import com.answufeng.ui.statelayout.StateLayout
import com.answufeng.ui.titlebar.TitleBar
import com.answufeng.ui.widget.BadgeView
import com.answufeng.ui.widget.FlowLayout
import com.answufeng.ui.widget.LoadingDialog
import com.answufeng.ui.widget.RoundLayout

class MainActivity : AppCompatActivity() {

    private class TextItemBinding private constructor(private val container: FrameLayout, val textView: TextView) : ViewBinding {
        override fun getRoot(): FrameLayout = container
        companion object {
            fun inflate(inflater: LayoutInflater, parent: ViewGroup?, attach: Boolean): TextItemBinding {
                val density = inflater.context.resources.displayMetrics.density
                val tv = TextView(inflater.context).apply {
                    textSize = 14f
                    val dp12 = (12 * density).toInt()
                    setPadding(dp12, dp12, dp12, dp12)
                }
                val container = FrameLayout(inflater.context).apply { addView(tv) }
                if (attach) parent?.addView(container)
                return TextItemBinding(container, tv)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val dp16 = (16 * resources.displayMetrics.density).toInt()
            setPadding(dp16, 0, dp16, 0)
        }

        val titleBar = TitleBar(this).apply {
            title = "aw-ui Demo"
            setOnBackClickListener { finish() }
        }
        root.addView(titleBar)

        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(container)
        root.addView(scrollView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)

        addSection(container, "StateLayout") { stateLayoutDemo(it) }
        addSection(container, "TitleBar") { titleBarDemo(it) }
        addSection(container, "AwDialog") { dialogDemo(it) }
        addSection(container, "LoadingDialog") { loadingDialogDemo(it) }
        addSection(container, "BadgeView") { badgeViewDemo(it) }
        addSection(container, "FlowLayout") { flowLayoutDemo(it) }
        addSection(container, "RoundLayout") { roundLayoutDemo(it) }
        addSection(container, "SimpleAdapter") { simpleAdapterDemo(it) }
        addSection(container, "LoadMoreAdapter") { loadMoreAdapterDemo(it) }
        addSection(container, "AwAnim") { animDemo(it) }
        addSection(container, "AwItemAnimator") { itemAnimatorDemo(it) }
        addSection(container, "DividerDecoration") { dividerDemo(it) }
    }

    private fun addSection(container: LinearLayout, title: String, content: (LinearLayout) -> Unit) {
        val dp8 = (8 * resources.displayMetrics.density).toInt()
        val dp16 = (16 * resources.displayMetrics.density).toInt()

        container.addView(TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, dp16, 0, dp8)
        })

        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content(section)
        container.addView(section)
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }
    }

    // ==================== StateLayout ====================

    private fun stateLayoutDemo(container: LinearLayout) {
        val stateLayout = StateLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (200 * resources.displayMetrics.density).toInt()
            )
        }
        val content = TextView(this).apply {
            text = "这是内容视图"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }
        stateLayout.addView(content)
        container.addView(stateLayout)

        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnRow.addView(button("Loading") { stateLayout.showLoading() })
        btnRow.addView(button("Content") { stateLayout.showContent() })
        btnRow.addView(button("Empty") { stateLayout.showEmpty() })
        btnRow.addView(button("Error") {
            stateLayout.showError {
                stateLayout.showLoading()
            }
        })
        container.addView(btnRow)
    }

    // ==================== TitleBar ====================

    private fun titleBarDemo(container: LinearLayout) {
        val bar = TitleBar(this).apply {
            title = "自定义标题"
            setRightText("保存") { AwDialog.alert(context, "提示", "保存成功") }
        }
        container.addView(bar)

        container.addView(button("修改标题") {
            (bar).title = "新标题 ${System.currentTimeMillis() % 1000}"
        })
    }

    // ==================== AwDialog ====================

    private fun dialogDemo(container: LinearLayout) {
        container.addView(button("确认对话框") {
            AwDialog.confirm(this, "提示", "确定删除吗？") {
                AwDialog.alert(this, "结果", "已删除")
            }
        })
        container.addView(button("输入对话框") {
            AwDialog.input(this, "备注", hint = "请输入备注") { text ->
                AwDialog.alert(this, "输入内容", text)
            }
        })
        container.addView(button("列表选择") {
            AwDialog.list(this, "选择颜色", listOf("红色", "绿色", "蓝色")) { index ->
                AwDialog.alert(this, "选择结果", "你选择了第 ${index + 1} 项")
            }
        })
        container.addView(button("底部列表") {
            AwDialog.bottomList(this, "操作", listOf("拍照", "从相册选择", "取消")) { index ->
                AwDialog.alert(this, "选择结果", "你选择了: ${listOf("拍照", "从相册选择", "取消")[index]}")
            }
        })
    }

    // ==================== LoadingDialog ====================

    private fun loadingDialogDemo(container: LinearLayout) {
        container.addView(button("显示 Loading") {
            LoadingDialog.show(this, "提交中…")
            window?.decorView?.postDelayed({ LoadingDialog.dismiss() }, 2000)
        })
        container.addView(button("可取消 Loading") {
            LoadingDialog.show(this, "加载中…", cancelable = true) {
                AwDialog.alert(this, "提示", "已取消")
            }
        })
    }

    // ==================== BadgeView ====================

    private fun badgeViewDemo(container: LinearLayout) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val dp8 = (8 * resources.displayMetrics.density).toInt()

        val frame1 = FrameLayout(this)
        val tv1 = TextView(this).apply { text = "消息"; setPadding(dp8, dp8, dp8, dp8) }
        val badge1 = BadgeView(this).apply {
            count = 5
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.TOP
            )
        }
        frame1.addView(tv1)
        frame1.addView(badge1)
        row.addView(frame1)

        val frame2 = FrameLayout(this)
        val tv2 = TextView(this).apply { text = "通知"; setPadding(dp8, dp8, dp8, dp8) }
        val badge2 = BadgeView(this).apply {
            count = 0
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.TOP
            )
        }
        frame2.addView(tv2)
        frame2.addView(badge2)
        row.addView(frame2)

        container.addView(row)

        container.addView(button("切换角标数量") {
            badge1.count = if (badge1.count < 0) 5 else -1
        })
    }

    // ==================== FlowLayout ====================

    private fun flowLayoutDemo(container: LinearLayout) {
        val flowLayout = FlowLayout(this).apply {
            val dp8 = (8 * resources.displayMetrics.density).toInt()
            horizontalSpacing = dp8
            verticalSpacing = dp8
        }
        val tags = listOf("Kotlin", "Android", "UI", "RecyclerView", "ViewBinding", "Material Design", "DiffUtil")
        for (tag in tags) {
            flowLayout.addView(TextView(this).apply {
                text = tag
                setPadding(
                    (12 * resources.displayMetrics.density).toInt(),
                    (6 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (6 * resources.displayMetrics.density).toInt()
                )
                setBackgroundResource(android.R.drawable.btn_default_small)
            })
        }
        container.addView(flowLayout)
    }

    // ==================== RoundLayout ====================

    private fun roundLayoutDemo(container: LinearLayout) {
        val roundLayout = RoundLayout(this).apply {
            setRadius(16f * resources.displayMetrics.density)
            setBackgroundColor(Color.parseColor("#E3F2FD"))
            val dp16 = (16 * resources.displayMetrics.density).toInt()
            setPadding(dp16, dp16, dp16, dp16)
        }
        roundLayout.addView(TextView(this).apply {
            text = "圆角容器内的内容"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        })
        container.addView(roundLayout)
    }

    // ==================== SimpleAdapter ====================

    private fun simpleAdapterDemo(container: LinearLayout) {
        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (150 * resources.displayMetrics.density).toInt()
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(old: String, new: String) = old == new
            override fun areContentsTheSame(old: String, new: String) = old == new
        }

        val adapter = SimpleAdapter(
            inflate = TextItemBinding::inflate,
            diffCallback = diffCallback
        ) { binding, item, _ -> binding.textView.text = item }

        recyclerView.adapter = adapter
        adapter.submitList((1..5).map { "Item $it" })
        container.addView(recyclerView)
    }

    // ==================== LoadMoreAdapter ====================

    private fun loadMoreAdapterDemo(container: LinearLayout) {
        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (200 * resources.displayMetrics.density).toInt()
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(old: String, new: String) = old == new
            override fun areContentsTheSame(old: String, new: String) = old == new
        }

        val adapter = LoadMoreAdapter(
            inflate = TextItemBinding::inflate,
            diffCallback = diffCallback
        ) { binding, item, _ -> binding.textView.text = item }

        adapter.setOnLoadMoreListener {
            window?.decorView?.postDelayed({
                val currentSize = adapter.currentList().size
                val nextPage = (currentSize + 1..currentSize + 5).map { "Page Item $it" }
                if (currentSize >= 30) {
                    adapter.noMore()
                } else {
                    adapter.loadMore(nextPage)
                }
            }, 1000)
        }

        recyclerView.adapter = adapter
        adapter.submitList((1..10).map { "Item $it" })
        container.addView(recyclerView)
    }

    // ==================== AwAnim ====================

    private fun animDemo(container: LinearLayout) {
        val target = TextView(this).apply {
            text = "动画目标"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#E8F5E9"))
            val dp16 = (16 * resources.displayMetrics.density).toInt()
            setPadding(dp16, dp16, dp16, dp16)
        }
        container.addView(target)

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(button("fadeIn") { target.fadeIn() })
        row1.addView(button("fadeOut") { target.fadeOut(goneOnEnd = false) })
        row1.addView(button("slideIn") { target.slideInFromBottom() })
        row1.addView(button("shake") { target.shake() })
        container.addView(row1)

        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(button("pulse") { target.pulse() })
        row2.addView(button("bounce") { target.bounce() })
        row2.addView(button("fadeSlideIn") { target.fadeSlideIn() })
        container.addView(row2)
    }

    // ==================== AwItemAnimator ====================

    private fun itemAnimatorDemo(container: LinearLayout) {
        container.addView(button("入场动画 (FADE_SLIDE_UP)") {
            val recyclerView = findRecyclerView(container.parent as? ViewGroup)
            recyclerView?.let { rv ->
                val lm = rv.layoutManager as? LinearLayoutManager ?: return@let
                for (i in 0 until lm.childCount) {
                    val child = lm.getChildAt(i) ?: continue
                    val pos = rv.getChildAdapterPosition(child)
                    if (pos != RecyclerView.NO_POSITION) {
                        AwItemAnimator.animateItem(child, pos, AwItemAnimator.AnimType.FADE_SLIDE_UP)
                    }
                }
            }
        })
    }

    // ==================== DividerDecoration ====================

    private fun dividerDemo(container: LinearLayout) {
        val recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (120 * resources.displayMetrics.density).toInt()
            )
            layoutManager = LinearLayoutManager(this@MainActivity)
            val density = resources.displayMetrics.density
            addItemDecoration(DividerDecoration(height = (1 * density).toInt(), color = Color.parseColor("#E0E0E0")))
        }

        val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(old: String, new: String) = old == new
            override fun areContentsTheSame(old: String, new: String) = old == new
        }

        val adapter = SimpleAdapter(
            inflate = TextItemBinding::inflate,
            diffCallback = diffCallback
        ) { binding, item, _ -> binding.textView.text = item }

        recyclerView.adapter = adapter
        adapter.submitList((1..4).map { "分割线 Item $it" })
        container.addView(recyclerView)
    }

    private fun findRecyclerView(viewGroup: ViewGroup?): RecyclerView? {
        if (viewGroup == null) return null
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is RecyclerView) return child
            if (child is ViewGroup) {
                findRecyclerView(child)?.let { return it }
            }
        }
        return null
    }
}
