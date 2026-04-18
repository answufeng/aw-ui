@file:Suppress("unused")

package com.answufeng.ui

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : ViewBinding> Activity.viewBinding(
    noinline bind: (View) -> T
): ReadOnlyProperty<Activity, T> = ActivityViewBindingDelegate(bind)

inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline bind: (View) -> T
): ReadOnlyProperty<Fragment, T> = FragmentViewBindingDelegate(bind)

@PublishedApi
internal class ActivityViewBindingDelegate<T : ViewBinding>(
    private val bind: (View) -> T
) : ReadOnlyProperty<Activity, T> {

    private var binding: T? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): T {
        binding?.let { return it }
        val view = thisRef.findViewById<View>(android.R.id.content)
            ?: throw IllegalStateException("Activity contentView not found")
        return bind(view).also { binding = it }
    }
}

@PublishedApi
internal class FragmentViewBindingDelegate<T : ViewBinding>(
    private val bind: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }
        val view = thisRef.view
            ?: throw IllegalStateException("Fragment view not available. Use viewLifecycleOwner.lifecycle.")
        val bound = bind(view)
        binding = bound
        thisRef.viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding = null
            }
        })
        return bound
    }
}
