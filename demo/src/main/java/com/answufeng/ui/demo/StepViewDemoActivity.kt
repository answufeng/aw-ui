package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.answufeng.ui.widget.AwStepView
import com.answufeng.ui.widget.AwTitleBar

class StepViewDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_view_demo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.bottom)
            insets
        }

        findViewById<AwTitleBar>(R.id.top_bar).setOnBackClickListener { finish() }

        val stepView = findViewById<AwStepView>(R.id.stepView)
        stepView.labelTexts = listOf("填写", "确认", "支付", "完成")

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPrev).setOnClickListener {
            if (stepView.currentStep > 0) {
                stepView.currentStep -= 1
            }
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnNext).setOnClickListener {
            if (stepView.currentStep < stepView.stepCount - 1) {
                stepView.currentStep += 1
            }
        }
    }
}
