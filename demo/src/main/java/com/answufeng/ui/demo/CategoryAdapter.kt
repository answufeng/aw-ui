package com.answufeng.ui.demo

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.ui.demo.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onClick: (DemoCategory) -> Unit,
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var items: List<DemoCategory> = emptyList()

    fun submitList(list: List<DemoCategory>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        private val binding: ItemCategoryBinding,
        private val onClick: (DemoCategory) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: DemoCategory) {
            binding.tvTitle.text = category.title
            binding.tvCount.text = category.desc
            binding.tvIcon.text = category.icon
            binding.tvBadge.text = "${category.count} 项"
            try {
                val color = Color.parseColor(category.colorHex)
                binding.accentBar.setBackgroundColor(color)
                val bg = binding.tvIcon.background
                if (bg is GradientDrawable) {
                    bg.setColor(adjustAlpha(color, 0.14f))
                }
            } catch (_: Exception) {
            }
            binding.root.setOnClickListener { onClick(category) }
        }

        private fun adjustAlpha(color: Int, factor: Float): Int {
            val alpha = (255 * factor).toInt().coerceIn(0, 255)
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }
    }
}
