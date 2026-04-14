package com.answufeng.ui.binding

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Creates a [ViewBinding] property delegate for [AppCompatActivity].
 *
 * The binding is lazily initialized from the activity's content view
 * using the specified [bind] function.
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
 * @param T    ViewBinding 类型
 * @param bind Function that takes a [View] and returns the binding instance,
 *             typically a method reference like `ActivityMainBinding::bind`.
 */
inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bind: (View) -> T
): ReadOnlyProperty<AppCompatActivity, T> {
    return object : ReadOnlyProperty<AppCompatActivity, T> {
        override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
            val contentView = thisRef.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            return bind(contentView)
        }
    }
}

/**
 * Creates a [ViewBinding] property delegate for [Fragment].
 *
 * The binding is lazily initialized from the fragment's view and is automatically
 * set to null when the fragment's view is destroyed, preventing memory leaks.
 *
 * ### 用法
 * ```kotlin
 * class MyFragment : Fragment(R.layout.fragment_main) {
 *     private val binding by viewBinding(FragmentMainBinding::bind)
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         binding.tvTitle.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param T    ViewBinding 类型
 * @param bind Function that takes a [View] and returns the binding instance,
 *             typically a method reference like `FragmentMainBinding::bind`.
 */
inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline bind: (View) -> T
): ReadOnlyProperty<Fragment, T> {
    return FragmentViewBindingDelegate(bind)
}

/**
 * A [ReadOnlyProperty] delegate for [Fragment] that manages the [ViewBinding] lifecycle.
 *
 * The binding is lazily created from the fragment's view via the [bind] function.
 * It automatically observes the fragment's view lifecycle and sets the binding reference
 * to null when the [Lifecycle.Event.ON_DESTROY] event occurs, ensuring the binding
 * does not outlive the fragment's view and preventing memory leaks.
 *
 * @param T The type of [ViewBinding].
 * @property bind Function that takes a [View] and returns the binding instance.
 */
class FragmentViewBindingDelegate<T : ViewBinding>(
    private val bind: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val existing = binding
        if (existing != null) return existing

        val view = thisRef.requireView()
        val lifecycle = thisRef.viewLifecycleOwner.lifecycle

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding = null
            }
        })

        return bind(view).also { binding = it }
    }
}
