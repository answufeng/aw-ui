@file:Suppress("unused")

package com.answufeng.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Activity 的 ViewBinding 属性委托。
 *
 * - 若已在 [AppCompatActivity.setContentView] 之后访问：对内容根 View 执行 `bind`
 * - 若尚未 setContentView：执行 `inflate`，需自行 `setContentView(binding.root)`
 *
 * ### 用法（推荐：先 setContentView）
 * ```kotlin
 * class MainActivity : AppCompatActivity() {
 *     private val binding by viewBinding(ActivityMainBinding::class)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *         binding.tvTitle.text = "Hello"
 *     }
 * }
 * ```
 *
 * ### 用法（inflate 路径）
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     setContentView(binding.root)
 * }
 * ```
 */
fun <VB : ViewBinding> AppCompatActivity.viewBinding(binder: KClass<VB>): ReadOnlyProperty<AppCompatActivity, VB> {
    return ActivityViewBindingDelegate(binder)
}

/**
 * Fragment 的 ViewBinding 属性委托。
 *
 * 在 [Fragment.onViewCreated] 之后绑定视图，并在视图销毁时清理引用。
 */
fun <VB : ViewBinding> Fragment.viewBinding(binder: KClass<VB>): ReadOnlyProperty<Fragment, VB> {
    return FragmentViewBindingDelegate(binder) { fragment ->
        val view = fragment.view ?: error("Fragment view is null")
        val method = binder.java.getMethod("bind", View::class.java)
        @Suppress("UNCHECKED_CAST")
        method.invoke(null, view) as VB
    }
}

/**
 * DialogFragment 的 ViewBinding 属性委托。
 */
fun <VB : ViewBinding> DialogFragment.viewBinding(binder: KClass<VB>): ReadOnlyProperty<DialogFragment, VB> {
    return FragmentViewBindingDelegate(binder) { fragment ->
        val view = fragment.view ?: error("DialogFragment view is null")
        val method = binder.java.getMethod("bind", View::class.java)
        @Suppress("UNCHECKED_CAST")
        method.invoke(null, view) as VB
    }
}

private class ActivityViewBindingDelegate<VB : ViewBinding>(
    private val binder: KClass<VB>,
) : ReadOnlyProperty<AppCompatActivity, VB> {
    private var binding: VB? = null
    private var lifecycleObserverRegistered = false

    @Suppress("UNCHECKED_CAST")
    override fun getValue(
        thisRef: AppCompatActivity,
        property: KProperty<*>,
    ): VB {
        binding?.let { return it }

        val content = thisRef.findViewById<ViewGroup>(android.R.id.content)
        val existingRoot = if (content.childCount > 0) content.getChildAt(0) else null

        val newBinding =
            if (existingRoot != null) {
                val bindMethod = binder.java.getMethod("bind", View::class.java)
                bindMethod.invoke(null, existingRoot) as VB
            } else {
                val inflateMethod = binder.java.getMethod("inflate", LayoutInflater::class.java)
                inflateMethod.invoke(null, LayoutInflater.from(thisRef)) as VB
            }

        binding = newBinding
        registerClearOnDestroy(thisRef)
        return newBinding
    }

    private fun registerClearOnDestroy(activity: AppCompatActivity) {
        if (lifecycleObserverRegistered) return
        lifecycleObserverRegistered = true
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    binding = null
                    lifecycleObserverRegistered = false
                    owner.lifecycle.removeObserver(this)
                }
            },
        )
    }
}

private class FragmentViewBindingDelegate<VB : ViewBinding>(
    private val binder: KClass<VB>,
    private val bindingFactory: (Fragment) -> VB,
) : ReadOnlyProperty<Fragment, VB> {
    private var binding: VB? = null

    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>,
    ): VB {
        binding?.let { return it }
        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        if (lifecycle.currentState == androidx.lifecycle.Lifecycle.State.DESTROYED) {
            error("Cannot access ViewBinding after Fragment view has been destroyed")
        }
        val newBinding = bindingFactory(thisRef)
        binding = newBinding
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    binding = null
                    lifecycle.removeObserver(this)
                }
            },
        )
        return newBinding
    }
}
