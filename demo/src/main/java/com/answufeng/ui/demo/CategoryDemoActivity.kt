package com.answufeng.ui.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.demo.databinding.ActivityCategoryDemoBinding

class CategoryDemoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY_TITLE = "category_title"
    }

    private lateinit var binding: ActivityCategoryDemoBinding
    private val adapter = DemoEntryAdapter { entry ->
        startActivity(Intent(this, entry.activity))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        val categoryTitle = intent.getStringExtra(EXTRA_CATEGORY_TITLE) ?: ""
        binding.titleBar.title = categoryTitle
        binding.titleBar.setOnBackClickListener { finish() }

        val entries = DemoData.getEntriesForCategory(categoryTitle)
        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = adapter
        adapter.submitList(entries)
    }
}
