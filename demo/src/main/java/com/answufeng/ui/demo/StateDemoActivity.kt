package com.answufeng.ui.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.demo.databinding.ActivityStateDemoBinding
import com.answufeng.ui.statelayout.StateTransition

class StateDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStateDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStateDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.setOnBackClickListener { finish() }

        val transModes = listOf(
            "无" to StateTransition.NONE,
            "淡入" to StateTransition.FADE,
            "交叉" to StateTransition.CROSS_FADE,
            "自底" to StateTransition.slideFromBottom()
        )
        var transIndex = 2
        fun applyTransition() {
            val (label, tr) = transModes[transIndex]
            binding.stateLayout.transition = tr
            binding.btnTransition.text = getString(R.string.state_demo_transition_cycling, label)
        }
        applyTransition()
        binding.btnTransition.setOnClickListener {
            transIndex = (transIndex + 1) % transModes.size
            applyTransition()
        }

        binding.btnContent.setOnClickListener { binding.stateLayout.showContent() }
        binding.btnLoading.setOnClickListener { binding.stateLayout.showLoading() }
        binding.btnEmpty.setOnClickListener { binding.stateLayout.showEmpty() }
        binding.btnError.setOnClickListener {
            binding.stateLayout.showError {
                binding.stateLayout.showLoading()
                binding.stateLayout.postDelayed({ binding.stateLayout.showContent() }, 900)
            }
        }

        binding.stateLayout.showContent()
    }
}

