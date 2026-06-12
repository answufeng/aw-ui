package com.answufeng.ui.demo

import android.app.Application
import com.answufeng.ui.snackbar.AwSnackbarManager

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AwSnackbarManager.install(this)
    }
}
