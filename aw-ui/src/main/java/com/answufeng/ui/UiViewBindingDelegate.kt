@file:Suppress("unused")

package com.answufeng.ui

import android.view.LayoutInflater
import android.view.View
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * Activity 的 ViewBinding 属性委托。
 *
 * 通过 `by viewBinding()` 在 Activity 中延迟初始化 ViewBinding，
 * 自动在 [ComponentActivity.setContentView] 之后绑定视图。
 *
 * ### 用法
 * ```kotlin
 * class MainActivity : AppCompatActivity() {
 *     private val binding by viewBinding(ActivityMainBinding::bind)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *         binding.tvTitle.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param binder 用于绑定视图的函数引用（如 `ActivityMainBinding::bind`）
 */
fun <VB : ViewBinding> AppCompatActivity.viewBinding(
    binder: KClass<VB>
): ReadOnlyProperty<AppCompatActivity, VB> {
    return ViewBindingDelegate(binder) { activity ->
        val inflater = LayoutInflater.from(activity)
        val method = binder.java.getMethod("inflate", LayoutInflater::class.java)
        @Suppress("UNCHECKED_CAST")
        method.invoke(null, inflater) as VB
    }
}

/**
 * Fragment 的 ViewBinding 属性委托。
 *
 * 通过 `by viewBinding()` 在 Fragment 中延迟初始化 ViewBinding，
 * 自动在 [Fragment.onViewCreated] 之后绑定视图，并在 [Fragment.onDestroyView] 时清理引用。
 *
 * ### 用法
 * ```kotlin
 * class MyFragment : Fragment(R.layout.fragment_my) {
 *     private val binding by viewBinding(FragmentMyBinding::bind)
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         binding.tvTitle.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param binder 用于绑定视图的函数引用（如 `FragmentMyBinding::bind`）
 */
fun <VB : ViewBinding> Fragment.viewBinding(
    binder: KClass<VB>
): ReadOnlyProperty<Fragment, VB> {
    return FragmentViewBindingDelegate(binder) { fragment ->
        val view = fragment.view ?: error("Fragment view is null")
        val method = binder.java.getMethod("bind", View::class.java)
        @Suppress("UNCHECKED_CAST")
        method.invoke(null, view) as VB
    }
}

/**
 * DialogFragment 的 ViewBinding 属性委托。
 *
 * 与 Fragment 用法相同，但针对 DialogFragment 的对话框视图生命周期优化。
 *
 * ### 用法
 * ```kotlin
 * class MyDialog : DialogFragment() {
 *     private val binding by viewBinding(DialogMyBinding::bind)
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         binding.tvTitle.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param binder 用于绑定视图的函数引用（如 `DialogMyBinding::bind`）
 */
fun <VB : ViewBinding> DialogFragment.viewBinding(
    binder: KClass<VB>
): ReadOnlyProperty<DialogFragment, VB> {
    return FragmentViewBindingDelegate(binder) { fragment ->
        val view = fragment.view ?: error("DialogFragment view is null")
        val method = binder.java.getMethod("bind", View::class.java)
        @Suppress("UNCHECKED_CAST")
        method.invoke(null, view) as VB
    }
}

private class ViewBindingDelegate<VB : ViewBinding>(
    private val binder: KClass<VB>,
    private val bindingFactory: (AppCompatActivity) -> VB
) : ReadOnlyProperty<AppCompatActivity, VB> {

    private var binding: VB? = null

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: AppCompatActivity, property: kotlin.reflect.KProperty<*>): VB {
        return binding ?: run {
            val method = binder.java.getMethod("inflate", LayoutInflater::class.java)
            val inflater = LayoutInflater.from(thisRef)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, inflater) as VB
        }.also { binding = it }
    }
}

private class FragmentViewBindingDelegate<VB : ViewBinding>(
    private val binder: KClass<VB>,
    private val bindingFactory: (Fragment) -> VB
) : ReadOnlyProperty<Fragment, VB> {

    private var binding: VB? = null

    init {
        // Fragment view destroyed 时清理引用
    }

    override fun getValue(thisRef: Fragment, property: kotlin.reflect.KProperty<*>): VB {
        return binding ?: bindingFactory(thisRef).also { binding = it }
    }
}
