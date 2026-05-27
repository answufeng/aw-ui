package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.dpToPx
import com.answufeng.ui.demo.databinding.ActivityRecyclerDemoBinding
import com.answufeng.ui.demo.databinding.ItemRecyclerDemoBannerBinding
import com.answufeng.ui.demo.databinding.ItemRecyclerDemoRowBinding
import com.answufeng.ui.recyclerview.AwDividerDecoration
import com.answufeng.ui.recyclerview.AwItemAnimator
import com.answufeng.ui.recyclerview.awMultiTypeAdapter
import com.answufeng.ui.recyclerview.awSimpleAdapter
import com.answufeng.ui.recyclerview.idDiffCallback
import com.answufeng.ui.widget.AwTitleBar

private data class SimpleRow(val id: Long, val title: String)

private sealed class MultiItem {
    abstract val id: Long

    data class TextRow(override val id: Long, val title: String) : MultiItem()

    data class BannerRow(override val id: Long, val title: String, val desc: String) : MultiItem()
}

class RecyclerViewDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecyclerDemoBinding
    private var simpleSeq = 4L
    private var multiTypeMode = 0

    private val simpleAdapter by lazy {
        awSimpleAdapter(
            diffCallback = idDiffCallback<SimpleRow> { it.id },
            inflate = ItemRecyclerDemoRowBinding::inflate,
        ) { rowBinding, item, _ ->
            rowBinding.tvTitle.text = item.title
        }.also { adapter ->
            adapter.setOnItemClickListener { item, _ ->
                toast("点击：${item.title}")
            }
        }
    }

    private val multiTypeAdapter by lazy {
        awMultiTypeAdapter {
            itemDiff { old, new -> (old as MultiItem).id == (new as MultiItem).id }
            contentDiff { old, new -> old == new }

            register<MultiItem.TextRow, ItemRecyclerDemoRowBinding>(
                inflate = ItemRecyclerDemoRowBinding::inflate,
                bind = { rowBinding, item, _ ->
                    rowBinding.tvTitle.text = item.title
                },
            )
            register<MultiItem.BannerRow, ItemRecyclerDemoBannerBinding>(
                inflate = ItemRecyclerDemoBannerBinding::inflate,
                bind = { bannerBinding, item, _ ->
                    bannerBinding.tvBannerTitle.text = item.title
                    bannerBinding.tvBannerDesc.text = item.desc
                },
            )
        }
    }

    private val dividerAdapter by lazy {
        awSimpleAdapter(
            diffCallback = idDiffCallback<SimpleRow> { it.id },
            inflate = ItemRecyclerDemoRowBinding::inflate,
        ) { rowBinding, item, _ ->
            rowBinding.tvTitle.text = item.title
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        setupSimpleList()
        setupMultiTypeList()
        setupDividerList()
    }

    private fun setupSimpleList() {
        binding.rvSimple.layoutManager = LinearLayoutManager(this)
        binding.rvSimple.adapter = simpleAdapter
        simpleAdapter.submitList(
            listOf(
                SimpleRow(1, "DiffUtil 差量更新"),
                SimpleRow(2, "ViewBinding 绑定"),
                SimpleRow(3, "Item 点击回调"),
            ),
        )

        binding.btnSimpleAdd.setOnClickListener {
            val current = simpleAdapter.currentList.toMutableList()
            current.add(SimpleRow(++simpleSeq, "新增条目 #$simpleSeq"))
            simpleAdapter.submitList(current)
        }
        binding.btnSimpleRemove.setOnClickListener {
            val current = simpleAdapter.currentList.toMutableList()
            if (current.isNotEmpty()) {
                current.removeAt(current.lastIndex)
                simpleAdapter.submitList(current)
            }
        }
    }

    private fun setupMultiTypeList() {
        binding.rvMultiType.layoutManager = LinearLayoutManager(this)
        binding.rvMultiType.itemAnimator = AwItemAnimator()
        binding.rvMultiType.adapter = multiTypeAdapter
        submitMultiTypeSample()

        binding.btnToggleMultiType.setOnClickListener {
            multiTypeMode = 1 - multiTypeMode
            submitMultiTypeSample()
        }
    }

    private fun submitMultiTypeSample() {
        val list =
            if (multiTypeMode == 0) {
                listOf(
                    MultiItem.BannerRow(100, "多类型列表", "Banner 与 Text 两种 ViewHolder"),
                    MultiItem.TextRow(101, "普通文本条目 A"),
                    MultiItem.TextRow(102, "普通文本条目 B"),
                )
            } else {
                listOf(
                    MultiItem.TextRow(201, "切换后：仅文本"),
                    MultiItem.TextRow(202, "AwItemAnimator 增删动画"),
                    MultiItem.BannerRow(203, "再次切换可恢复 Banner", "DiffUtil 自动计算差异"),
                )
            }
        multiTypeAdapter.submitList(list)
    }

    private fun setupDividerList() {
        val decoration =
            AwDividerDecoration(
                height = resources.dpToPx(1),
                color = Color.parseColor("#E2E8F0"),
                paddingStart = resources.dpToPx(16),
                paddingEnd = resources.dpToPx(16),
            )
        binding.rvDivider.layoutManager = LinearLayoutManager(this)
        binding.rvDivider.addItemDecoration(decoration)
        binding.rvDivider.adapter = dividerAdapter
        dividerAdapter.submitList(
            listOf(
                SimpleRow(10, "分割线 · 第一项"),
                SimpleRow(11, "分割线 · 第二项"),
                SimpleRow(12, "分割线 · 第三项"),
                SimpleRow(13, "分割线 · 第四项"),
            ),
        )
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
