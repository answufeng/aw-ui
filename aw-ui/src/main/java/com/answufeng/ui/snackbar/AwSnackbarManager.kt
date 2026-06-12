package com.answufeng.ui.snackbar

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View

/**
 * 通过 [Application.ActivityLifecycleCallbacks] 追踪当前前台 Activity，
 * 为无 View 引用场景（如 ViewModel / 工具类）提供自动查找锚点 View 的能力。
 *
 * ### 初始化
 * 在 Application 中调用一次：
 * ```kotlin
 * AwSnackbarManager.install(this)
 * ```
 */
object AwSnackbarManager {

    private var currentActivity: Activity? = null

    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity === activity) {
                    currentActivity = null
                }
            }

            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity === activity) {
                    currentActivity = null
                }
            }
        })
    }

    fun getCurrentActivity(): Activity? = currentActivity

    fun getContentView(activity: Activity): View {
        return activity.findViewById(android.R.id.content)
            ?: throw IllegalStateException("Cannot find content view for ${activity.javaClass.simpleName}")
    }
}
