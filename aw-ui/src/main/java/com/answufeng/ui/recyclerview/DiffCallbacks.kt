@file:Suppress("unused")

package com.answufeng.ui.recyclerview

import androidx.recyclerview.widget.DiffUtil

fun stringDiffCallback(): DiffUtil.ItemCallback<String> = object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}

fun <T> idDiffCallback(idSelector: (T) -> Any): DiffUtil.ItemCallback<T> =
    object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            idSelector(oldItem) == idSelector(newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem == newItem
    }
