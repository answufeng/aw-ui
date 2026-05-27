package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwRatingBar
import com.answufeng.ui.widget.AwTitleBar

class RatingBarDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating_bar_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val ratingBar = findViewById<AwRatingBar>(R.id.ratingBar)
        val tvRating = findViewById<android.widget.TextView>(R.id.tvRatingValue)
        ratingBar.onRatingChange = { score ->
            tvRating.text = "当前评分：$score"
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnResetRating).setOnClickListener {
            ratingBar.rating = 0f
            tvRating.text = "当前评分：0.0"
        }
    }
}
