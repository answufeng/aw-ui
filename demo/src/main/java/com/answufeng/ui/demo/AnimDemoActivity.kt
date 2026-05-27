package com.answufeng.ui.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.anim.fadeIn
import com.answufeng.ui.anim.fadeOut
import com.answufeng.ui.anim.fadeSlideIn
import com.answufeng.ui.anim.slideInFromBottom
import com.answufeng.ui.anim.slideOutToBottom
import com.answufeng.ui.widget.AwTitleBar

class AnimDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val card = findViewById<View>(R.id.demoCard)
        val status = findViewById<android.widget.TextView>(R.id.tvAnimStatus)

        fun notify(name: String) {
            status.text = getString(R.string.anim_demo_status, name)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFadeIn).setOnClickListener {
            resetCard(card)
            card.fadeIn { notify("fadeIn") }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFadeOut).setOnClickListener {
            card.fadeOut { notify("fadeOut") }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSlideIn).setOnClickListener {
            resetCard(card)
            card.slideInFromBottom { notify("slideInFromBottom") }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSlideOut).setOnClickListener {
            card.slideOutToBottom { notify("slideOutToBottom") }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFadeSlideIn).setOnClickListener {
            resetCard(card)
            card.fadeSlideIn { notify("fadeSlideIn") }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReset).setOnClickListener {
            resetCard(card)
            status.text = "点击下方按钮触发动画"
        }
    }

    private fun resetCard(card: View) {
        card.animate().cancel()
        card.alpha = 1f
        card.translationY = 0f
        card.visibility = View.VISIBLE
    }
}
