package com.answufeng.ui.demo

import android.app.Activity

data class DemoEntry(
    val title: String,
    val desc: String,
    val activity: Class<out Activity>
)

