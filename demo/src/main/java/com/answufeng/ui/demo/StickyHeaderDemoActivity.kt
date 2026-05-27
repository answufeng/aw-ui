package com.answufeng.ui.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.ui.recyclerview.AwStickyHeaderDecoration
import com.answufeng.ui.widget.AwIndexBar
import com.answufeng.ui.widget.AwTitleBar

private data class StickyListItem(
    val section: String,
    val title: String,
    val isHeader: Boolean,
)

class StickyHeaderDemoActivity : AppCompatActivity() {

    private val items = buildList {
        for (letter in 'A'..'F') {
            val section = letter.toString()
            add(StickyListItem(section, section, isHeader = true))
            repeat(4) { i ->
                add(StickyListItem(section, "$section - 联系人 ${i + 1}", isHeader = false))
            }
        }
    }

    private val sectionPositions: Map<String, Int> =
        items.mapIndexedNotNull { index, item ->
            if (item.isHeader) item.section to index else null
        }.toMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticky_header_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = StickyAdapter(items)
        recyclerView.addItemDecoration(
            AwStickyHeaderDecoration(
                isHeader = { pos -> items[pos].isHeader },
                headerLayoutRes = R.layout.item_sticky_section_header,
            ) { headerView, position ->
                headerView.findViewById<TextView>(R.id.tvSectionTitle).text = items[position].section
            },
        )

        val indexBar = findViewById<AwIndexBar>(R.id.indexBar)
        indexBar.letters = sectionPositions.keys.toList()
        indexBar.onLetterSelected = { letter, _ ->
            val pos = sectionPositions[letter]
            if (pos != null) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(pos, 0)
            }
        }
    }

    private class StickyAdapter(
        private val data: List<StickyListItem>,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int = data.size

        override fun getItemViewType(position: Int): Int = if (data[position].isHeader) 0 else 1

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerView.ViewHolder {
            val layout = if (viewType == 0) R.layout.item_sticky_section_header else R.layout.item_sticky_row
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
        ) {
            val item = data[position]
            if (item.isHeader) {
                holder.itemView.findViewById<TextView>(R.id.tvSectionTitle).text = item.section
            } else {
                holder.itemView.findViewById<TextView>(R.id.tvRowTitle).text = item.title
            }
        }
    }
}
