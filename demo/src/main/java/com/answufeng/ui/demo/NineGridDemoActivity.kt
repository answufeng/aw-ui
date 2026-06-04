package com.answufeng.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwNineGridImageView

class NineGridDemoActivity : AppCompatActivity() {

    private val allColors = listOf(
        "#E57373", "#F06292", "#BA68C8", "#9575CD",
        "#7986CB", "#64B5F6", "#4FC3F7", "#4DD0E1",
        "#4DB6AC", "#81C784", "#AED581", "#DCE775"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nine_grid_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }

        findViewById<com.answufeng.ui.widget.AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val nineGrid1 = findViewById<AwNineGridImageView>(R.id.nine_grid_1)
        val nineGrid4 = findViewById<AwNineGridImageView>(R.id.nine_grid_4)
        val nineGrid9 = findViewById<AwNineGridImageView>(R.id.nine_grid_9)
        val nineGridOverflow = findViewById<AwNineGridImageView>(R.id.nine_grid_overflow)

        // 使用简单的颜色方块代替真实图片
        val colorLoader: (android.widget.ImageView, String) -> Unit = { imageView, url ->
            imageView.setBackgroundColor(Color.parseColor(url))
            imageView.scaleType = android.widget.ImageView.ScaleType.CENTER
            // 在中间显示序号
            imageView.setImageDrawable(null)
        }

        setupNineGrid(nineGrid1, listOf(allColors[0]), colorLoader)
        setupNineGrid(nineGrid4, allColors.subList(0, 4), colorLoader)
        setupNineGrid(nineGrid9, allColors.subList(0, 9), colorLoader)
        setupNineGrid(nineGridOverflow, allColors, colorLoader, maxCount = 6)

        // 动态切换按钮
        val nineGridDynamic = findViewById<AwNineGridImageView>(R.id.nine_grid_dynamic)
        val tvCount = findViewById<TextView>(R.id.tv_image_count)
        var dynamicCount = 1

        nineGridDynamic.imageLoader = colorLoader
        nineGridDynamic.setOnImageClickListener { index ->
            Toast.makeText(this, "点击图片 $index", Toast.LENGTH_SHORT).show()
        }
        updateDynamicGrid(nineGridDynamic, dynamicCount, tvCount)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_add_image).setOnClickListener {
            if (dynamicCount < 9) {
                dynamicCount++
                updateDynamicGrid(nineGridDynamic, dynamicCount, tvCount)
            }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_remove_image).setOnClickListener {
            if (dynamicCount > 1) {
                dynamicCount--
                updateDynamicGrid(nineGridDynamic, dynamicCount, tvCount)
            }
        }
    }

    private fun setupNineGrid(
        nineGrid: AwNineGridImageView,
        colors: List<String>,
        loader: (android.widget.ImageView, String) -> Unit,
        maxCount: Int = 9
    ) {
        nineGrid.imageLoader = loader
        nineGrid.setOnImageClickListener { index ->
            Toast.makeText(this, "点击图片 $index", Toast.LENGTH_SHORT).show()
        }
        // AwNineGridImageView doesn't have setMaxCount public API, so we just use the default
        nineGrid.setImageUrls(colors)
    }

    private fun updateDynamicGrid(nineGrid: AwNineGridImageView, count: Int, tvCount: TextView) {
        tvCount.text = "当前图片数：$count"
        nineGrid.setImageUrls(allColors.subList(0, count))
    }
}
