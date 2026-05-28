package com.answufeng.ui.demo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.recyclerview.awSimpleAdapter
import com.answufeng.ui.recyclerview.idDiffCallback
import com.answufeng.ui.statelayout.AwStateLayout
import com.answufeng.ui.widget.AwSkeletonView
import com.answufeng.ui.widget.AwTitleBar
import com.answufeng.ui.widget.skeleton.AwSkeleton
import com.answufeng.ui.widget.skeleton.AwSkeletonLayout
import com.answufeng.ui.widget.skeleton.applyAwSkeleton
import com.answufeng.ui.widget.skeleton.setContentAdapter

private data class FeedItem(val id: Long, val title: String, val subtitle: String)

class SkeletonDemoActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var cardLoadToken = 0
    private var listLoadToken = 0
    private var stateLoadToken = 0
    private var listSkeleton: AwSkeleton? = null
    private lateinit var listAdapter: com.answufeng.ui.recyclerview.AwSimpleAdapter<
        com.answufeng.ui.demo.databinding.ItemSkeletonFeedBinding,
        FeedItem,
        >

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeleton_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        setupCardSkeleton()
        setupListSkeleton()
        setupStateLayoutSkeleton()
        setupManualBlock()
    }

    private fun setupCardSkeleton() {
        val skeletonLayout = findViewById<AwSkeletonLayout>(R.id.skeletonLayoutCard)
        loadCardData(skeletonLayout)
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReloadCard)
            .setOnClickListener {
                skeletonLayout.showSkeleton()
                loadCardData(skeletonLayout)
            }
    }

    private fun loadCardData(skeletonLayout: AwSkeletonLayout) {
        val token = ++cardLoadToken
        mainHandler.postDelayed({
            if (token != cardLoadToken) return@postDelayed
            skeletonLayout.bindContent { root ->
                root.findViewById<TextView>(R.id.tvTitle).text = "AwSkeletonLayout 已加载"
                root.findViewById<TextView>(R.id.tvSubtitle).text = "真实数据绑定后 showContent()"
            }
            skeletonLayout.showContent()
        }, 1500L)
    }

    private fun setupListSkeleton() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSkeletonList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        listAdapter =
            awSimpleAdapter(
                diffCallback = idDiffCallback<FeedItem> { it.id },
                inflate = com.answufeng.ui.demo.databinding.ItemSkeletonFeedBinding::inflate,
            ) { binding, item, _ ->
                binding.tvTitle.text = item.title
                binding.tvSubtitle.text = item.subtitle
            }

        listSkeleton =
            recyclerView.applyAwSkeleton(
                itemLayout = R.layout.item_skeleton_feed,
                itemCount = 6,
            )
        listSkeleton?.setContentAdapter(listAdapter)
        listSkeleton?.showSkeleton()

        loadListData()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReloadList)
            .setOnClickListener {
                listSkeleton?.showSkeleton()
                loadListData()
            }
    }

    private fun loadListData() {
        val token = ++listLoadToken
        mainHandler.postDelayed({
            if (token != listLoadToken) return@postDelayed
            listAdapter.submitList(
                listOf(
                    FeedItem(1, "列表项 1", "applyAwSkeleton 占位"),
                    FeedItem(2, "列表项 2", "数据到达后 showContent"),
                    FeedItem(3, "列表项 3", "无需手写 skeleton item layout"),
                    FeedItem(4, "列表项 4", "AwSimpleAdapter 绑定"),
                ),
            )
            listSkeleton?.showContent()
        }, 1500L)
    }

    private fun setupStateLayoutSkeleton() {
        val stateLayout = findViewById<AwStateLayout>(R.id.stateLayoutSkeleton)
        stateLayout.loadingStyle = AwStateLayout.LoadingStyle.SKELETON
        stateLayout.showLoading()

        mainHandler.postDelayed({
            stateLayout.findViewById<TextView>(R.id.tvStateTitle).text = "StateLayout 内容"
            stateLayout.findViewById<TextView>(R.id.tvStateBody).text =
                "loadingStyle=skeleton 时 showLoading 对 content 加遮罩"
            stateLayout.showContent()
        }, 1500L)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStateLoading)
            .setOnClickListener {
                stateLayout.showLoading()
                val token = ++stateLoadToken
                mainHandler.postDelayed({
                    if (token != stateLoadToken) return@postDelayed
                    stateLayout.findViewById<TextView>(R.id.tvStateTitle).text = "重新加载完成"
                    stateLayout.findViewById<TextView>(R.id.tvStateBody).text = "骨架 → 内容"
                    stateLayout.showContent()
                }, 1200L)
            }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnStateContent)
            .setOnClickListener { stateLayout.showContent() }
    }

    private fun setupManualBlock() {
        val block = findViewById<AwSkeletonView>(R.id.skeletonBlock)
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnToggleBlock)
            .setOnClickListener {
                if (block.isShimmering) block.stopShimmer() else block.startShimmer()
            }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
