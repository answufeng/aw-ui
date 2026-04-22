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

        binding.stateLayout.transition = StateTransition.CROSS_FADE

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

