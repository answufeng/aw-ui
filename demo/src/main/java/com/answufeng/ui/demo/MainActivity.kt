package com.answufeng.ui.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.answufeng.ui.demo.databinding.ActivityHomeBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val categoryAdapter = CategoryAdapter { category ->
        val intent = Intent(this, CategoryDemoActivity::class.java)
        intent.putExtra(CategoryDemoActivity.EXTRA_CATEGORY_TITLE, category.title)
        startActivity(intent)
    }
    private val searchAdapter = DemoEntryAdapter { entry ->
        startActivity(Intent(this, entry.activity))
    }
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
        setupList()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.titleBar.title = getString(R.string.demo_home_title)
        binding.titleBar.showBackButton = false
    }

    private fun setupList() {
        binding.rvEntries.layoutManager = LinearLayoutManager(this)
        binding.rvEntries.adapter = categoryAdapter
        categoryAdapter.submitList(DemoData.categories)
    }

    private fun setupSearch() {
        binding.etSearch.onQueryChange = { q ->
            if (q.isEmpty()) {
                if (isSearchMode) {
                    binding.rvEntries.adapter = categoryAdapter
                    isSearchMode = false
                }
            } else {
                if (!isSearchMode) {
                    binding.rvEntries.adapter = searchAdapter
                    isSearchMode = true
                }
                searchAdapter.submitList(
                    DemoData.allEntries.filter {
                        it.title.contains(q, ignoreCase = true) || it.desc.contains(
                            q,
                            ignoreCase = true
                        )
                    }
                )
            }
        }
    }
}
