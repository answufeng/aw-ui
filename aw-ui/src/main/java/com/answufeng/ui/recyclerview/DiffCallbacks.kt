@file:Suppress("unused")

package com.answufeng.ui.recyclerview

import androidx.recyclerview.widget.DiffUtil

/**
 * 适用于基于 String 内容比较的 DiffUtil 回调工厂函数。
 *
 * @return DiffUtil.ItemCallback<String> 实例，直接比较字符串内容
 */
fun stringDiffCallback(): DiffUtil.ItemCallback<String> =
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String,
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String,
        ): Boolean = oldItem == newItem
    }

/**
 * 适用于基于数据对象 id 属性比较的 DiffUtil 回调类。
 *
 * @param T 数据类型
 * @param idSelector 获取数据唯一标识的函数
 */
@Deprecated("Use SimpleDiffCallback instead: IdDiffCallback(id) ≡ SimpleDiffCallback(id)", ReplaceWith("SimpleDiffCallback(idSelector)"))
class IdDiffCallback<T : Any>(
    private val idSelector: (T) -> Any,
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(
        oldItem: T,
        newItem: T,
    ): Boolean = idSelector(oldItem) == idSelector(newItem)

    override fun areContentsTheSame(
        oldItem: T,
        newItem: T,
    ): Boolean = oldItem == newItem
}

/**
 * 适用于自定义比较逻辑的 DiffUtil 回调类。
 *
 * @param T 数据类型
 * @param itemId 获取数据唯一标识的函数
 * @param contentSame 自定义内容比较函数（默认使用 equals）
 */
class SimpleDiffCallback<T : Any>(
    private val itemId: (T) -> Any,
    private val contentSame: (T, T) -> Boolean = { old, new -> old == new },
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(
        oldItem: T,
        newItem: T,
    ): Boolean = itemId(oldItem) == itemId(newItem)

    override fun areContentsTheSame(
        oldItem: T,
        newItem: T,
    ): Boolean = contentSame(oldItem, newItem)

    override fun getChangePayload(
        oldItem: T,
        newItem: T,
    ): Any? {
        return if (areItemsTheSame(oldItem, newItem) && !areContentsTheSame(oldItem, newItem)) {
            newItem
        } else {
            null
        }
    }
}

/**
 * 创建基于 id 选择器的 DiffUtil 回调。
 *
 * @param T 数据类型
 * @param idSelector 获取数据唯一标识的函数
 * @return DiffUtil.ItemCallback<T> 实例
 */
@Deprecated("Use simpleDiffCallback instead", ReplaceWith("simpleDiffCallback(idSelector)"))
fun <T : Any> idDiffCallback(idSelector: (T) -> Any): DiffUtil.ItemCallback<T> = IdDiffCallback(idSelector)

/**
 * 创建自定义比较逻辑的 DiffUtil 回调。
 *
 * @param T 数据类型
 * @param itemId 获取数据唯一标识的函数
 * @param contentSame 自定义内容比较函数（默认使用 equals）
 * @return DiffUtil.ItemCallback<T> 实例
 */
fun <T : Any> simpleDiffCallback(
    itemId: (T) -> Any,
    contentSame: (T, T) -> Boolean = { old, new -> old == new },
): DiffUtil.ItemCallback<T> = SimpleDiffCallback(itemId, contentSame)
