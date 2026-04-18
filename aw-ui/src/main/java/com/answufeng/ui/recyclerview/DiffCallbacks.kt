@file:Suppress("unused")

package com.answufeng.ui.recyclerview

import androidx.recyclerview.widget.DiffUtil

fun stringDiffCallback(): DiffUtil.ItemCallback<String> = object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}

class IdDiffCallback<T : Any>(
    private val idSelector: (T) -> Any
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        idSelector(oldItem) == idSelector(newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        oldItem == newItem
}

class SimpleDiffCallback<T : Any>(
    private val itemId: (T) -> Any,
    private val contentSame: (T, T) -> Boolean = { old, new -> old == new }
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        itemId(oldItem) == itemId(newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        contentSame(oldItem, newItem)

    override fun getChangePayload(oldItem: T, newItem: T): Any? {
        return if (areItemsTheSame(oldItem, newItem) && !areContentsTheSame(oldItem, newItem)) {
            newItem
        } else null
    }
}

fun <T : Any> idDiffCallback(idSelector: (T) -> Any): DiffUtil.ItemCallback<T> =
    IdDiffCallback(idSelector)

fun <T : Any> simpleDiffCallback(
    itemId: (T) -> Any,
    contentSame: (T, T) -> Boolean = { old, new -> old == new }
): DiffUtil.ItemCallback<T> = SimpleDiffCallback(itemId, contentSame)
