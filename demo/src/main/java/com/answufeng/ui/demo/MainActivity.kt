package com.answufeng.ui.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.demo.databinding.ActivityHomeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val categoryAdapter = CategoryAdapter { category ->
        startActivity(
            Intent(this, CategoryDemoActivity::class.java)
                .putExtra(CategoryDemoActivity.EXTRA_CATEGORY_TITLE, category.title),
        )
    }
    private val searchAdapter = DemoEntryAdapter(
        onClick = { entry -> startActivity(Intent(this, entry.activity)) },
        showCategory = true,
    )
    private var isSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        setupToolbar()
        setupHero()
        setupList()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.titleBar.title = getString(R.string.demo_home_title)
        binding.titleBar.showBackButton = false
        binding.titleBar.setOnRightTextClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.demo_playbook_title)
                .setMessage(R.string.demo_playbook_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun setupHero() {
        val categoryCount = DemoData.categories.size
        val componentCount = DemoData.totalComponentCount
        binding.tvStats.text = getString(R.string.demo_stats_format, categoryCount, componentCount)
    }

    private fun setupList() {
        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = categoryAdapter
        categoryAdapter.submitList(DemoData.categories)
    }

    private fun setupSearch() {
        binding.etSearch.onQueryChange = { query ->
            if (query.isEmpty()) {
                binding.emptySearch.isVisible = false
                binding.rvEntries.isVisible = true
                if (isSearchMode) {
                    binding.rvEntries.adapter = categoryAdapter
                    isSearchMode = false
                }
            } else {
                if (!isSearchMode) {
                    binding.rvEntries.adapter = searchAdapter
                    isSearchMode = true
                }
                val results = DemoData.allEntries.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.desc.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                }
                searchAdapter.submitList(results)
                binding.emptySearch.isVisible = results.isEmpty()
                binding.rvEntries.isVisible = results.isNotEmpty()
            }
        }
    }
}
