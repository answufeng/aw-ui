package com.answufeng.ui.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.ui.demo.databinding.ItemDemoEntryBinding

class DemoEntryAdapter(
    private val onClick: (DemoEntry) -> Unit
) : RecyclerView.Adapter<DemoEntryAdapter.VH>() {

    private var items: List<DemoEntry> = emptyList()

    fun submitList(list: List<DemoEntry>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDemoEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        private val binding: ItemDemoEntryBinding,
        private val onClick: (DemoEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: DemoEntry) {
            binding.tvTitle.text = entry.title
            binding.tvDesc.text = entry.desc
            binding.root.setOnClickListener { onClick(entry) }
        }
    }
}

