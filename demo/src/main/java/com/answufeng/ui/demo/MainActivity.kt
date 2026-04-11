package com.answufeng.ui.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.ui.statelayout.StateLayout

class MainActivity : AppCompatActivity() {

    private lateinit var stateLayout: StateLayout
    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = TextView(this).apply { textSize = 14f }
        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(tvLog)

        container.addView(button("Show Loading") {
            stateLayout.showLoading()
            log("State: LOADING")
        })

        container.addView(button("Show Content") {
            stateLayout.showContent()
            log("State: CONTENT")
        })

        container.addView(button("Show Empty") {
            stateLayout.showEmpty()
            log("State: EMPTY")
        })

        container.addView(button("Show Error") {
            stateLayout.showError { 
                log("Retry clicked!")
                stateLayout.showLoading()
            }
            log("State: ERROR")
        })
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply { this.text = text; setOnClickListener { onClick() } }
    }

    private fun log(msg: String) { tvLog.append("$msg\n") }
}
