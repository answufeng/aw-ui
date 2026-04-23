package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.ui.demo.databinding.ActivitySegmentedControlDemoBinding
import com.answufeng.ui.widget.SegmentTab

class SegmentedControlDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySegmentedControlDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySegmentedControlDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleBar.setOnBackClickListener { finish() }

        binding.segIconText.tabs = listOf(
            SegmentTab("拍照", android.R.drawable.ic_menu_camera),
            SegmentTab("相册", android.R.drawable.ic_menu_gallery),
            SegmentTab("列表", android.R.drawable.ic_menu_view)
        )
        binding.segIconOnly.tabs = listOf(
            SegmentTab(label = "", iconRes = android.R.drawable.ic_menu_camera),
            SegmentTab(label = "", iconRes = android.R.drawable.ic_menu_gallery),
            SegmentTab(label = "", iconRes = android.R.drawable.ic_menu_view)
        )

        binding.vpWithPager.adapter = ColorPagerAdapter()
        binding.segWithPager.bindViewPager2(binding.vpWithPager)

        fun refreshNoPagerLabel(idx: Int) {
            binding.tvNoPagerState.text = getString(R.string.seg_demo_selected_only, idx)
        }
        refreshNoPagerLabel(binding.segNoPager.selectedIndex)
        binding.segNoPager.onSelectionChange = { refreshNoPagerLabel(it) }
    }

    private inner class ColorPagerAdapter : RecyclerView.Adapter<ColorPagerAdapter.Vh>() {

        private val colors = listOf(
            Color.parseColor("#E3F2FD"),
            Color.parseColor("#F3E5F5"),
            Color.parseColor("#E8F5E9")
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
            val v = TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                textSize = 18f
                setTextColor(Color.parseColor("#CC000000"))
            }
            return Vh(v)
        }

        override fun onBindViewHolder(holder: Vh, position: Int) {
            val tv = holder.itemView as TextView
            tv.setBackgroundColor(colors[position])
            tv.text = getString(R.string.seg_demo_page_n, position + 1)
        }

        override fun getItemCount(): Int = 3

        inner class Vh(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}
