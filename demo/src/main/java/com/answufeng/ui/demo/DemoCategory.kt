package com.answufeng.ui.demo

import android.app.Activity

data class DemoCategory(
    val title: String,
    val desc: String,
    val icon: String,
    val colorHex: String,
    val entries: List<DemoEntry>,
) {
    val count: Int get() = entries.size
}
