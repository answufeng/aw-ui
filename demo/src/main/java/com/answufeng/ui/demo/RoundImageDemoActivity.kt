package com.answufeng.ui.demo

import android.os.Bundle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwRoundImageView
import com.answufeng.ui.widget.AwTitleBar

class RoundImageDemoActivity : AppCompatActivity() {

    private var isCircle = false
    private var radiusIndex = 0
    private var borderIndex = 0
    private val radii = floatArrayOf(12f, 24f, 36f, 48f)
    private val borders = listOf(
        0 to Color.TRANSPARENT,
        2 to Color.WHITE,
        3 to Color.parseColor("#3B82F6"),
        4 to Color.parseColor("#EF4444")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round_image_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val roundImageDynamic = findViewById<AwRoundImageView>(R.id.round_image_dynamic)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_toggle_circle).setOnClickListener {
            isCircle = !isCircle
            roundImageDynamic.isCircle = isCircle
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_radius).setOnClickListener {
            radiusIndex = (radiusIndex + 1) % radii.size
            roundImageDynamic.radius = radii[radiusIndex] * resources.displayMetrics.density
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_change_border).setOnClickListener {
            borderIndex = (borderIndex + 1) % borders.size
            val (widthDp, color) = borders[borderIndex]
            roundImageDynamic.borderWidth = widthDp * resources.displayMetrics.density
            roundImageDynamic.borderColor = color
        }
    }
}
