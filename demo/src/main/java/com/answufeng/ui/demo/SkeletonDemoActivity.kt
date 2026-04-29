package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwSkeletonView
import com.answufeng.ui.widget.AwTitleBar

class SkeletonDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeleton_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val skeletonControllable = findViewById<AwSkeletonView>(R.id.skeleton_controllable)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_toggle_skeleton).setOnClickListener {
            if (skeletonControllable.isShimmering) {
                skeletonControllable.stopShimmer()
            } else {
                skeletonControllable.startShimmer()
            }
        }
    }
}
