package com.answufeng.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

@PublishedApi
internal data class TypeRegistration(
    val clazz: KClass<*>,
    val create: (ViewGroup) -> ViewBinding,
    val bind: (ViewBinding, Any, Int) -> Unit,
    val bindWithPayload: ((ViewBinding, Any, Int, List<Any>) -> Unit)? = null
)

/**
 * 多类型 RecyclerView 适配器。
 *
 * 基于 ViewBinding 实现，通过 [register] 注册每种数据类型的创建与绑定逻辑，
 * 调用 [submitList] 更新数据时自动进行异步 DiffUtil 差量计算。
 *
 * ### 线程要求
 * [submitList] 和 [register] 必须在**主线程**调用（RecyclerView 适配器约束）。
 * [register] 应在 [submitList] 之前完成，否则可能出现未注册类型异常。
 * DiffUtil 计算在后台线程异步执行，结果自动分发到主线程。
 *
 * ### 基本用法
 * ```kotlin
 * val adapter = AwMultiTypeAdapter(
 *     itemDiff = { old, new -> old.id == new.id },
 *     contentDiff = { old, new -> old == new }
 * )
 *
 * adapter.register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) { binding, item, _ ->
 *     binding.tvText.text = item.text
 * }
 * ```
 *
 * ### DSL 用法
 * ```kotlin
 * val adapter = multiTypeAdapter {
 *     itemDiff { old, new -> old.id == new.id }
 *     contentDiff { old, new -> old == new }
 *
 *     register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) { binding, item, _ ->
 *         binding.tvText.text = item.text
 *     }
 *     register<ImageItem, ItemImageBinding>(ItemImageBinding::inflate) { binding, item, _ ->
 *         binding.ivImage.load(item.url)
 *     }
 * }
 * ```
 *
 * ### Payload 局部更新
 * ```kotlin
 * adapter.register<TextItem, ItemTextBinding>(
 *     inflate = ItemTextBinding::inflate,
 *     bind = { binding, item, _ -> binding.tvText.text = item.text },
 *     bindWithPayload = { binding, item, _, payloads ->
 *         payloads.forEach { payload ->
 *             if (payload is String) binding.tvText.text = payload
 *         }
 *     }
 * )
 * ```
 *
 * @param itemDiff    用于判断两个 item 是否为同一条目（如比较 id），
 *                    如未提供则使用引用相等（===）
 * @param contentDiff 用于判断两个相同 item 的内容是否发生变化（如比较全部字段），
 *                    如未提供则退化为使用 [itemDiff] 判断
 */
class AwMultiTypeAdapter(
    private val itemDiff: ((old: Any, new: Any) -> Boolean)? = null,
    private val contentDiff: ((old: Any, new: Any) -> Boolean)? = null
) : RecyclerView.Adapter<AwMultiTypeAdapter.BindingHolder>() {

    @PublishedApi
    internal val registrations = mutableListOf<TypeRegistration>()

    private val differ: AsyncListDiffer<Any> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (itemDiff == null) return oldItem === newItem
            return oldItem::class == newItem::class && itemDiff.invoke(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return (contentDiff ?: itemDiff)?.invoke(oldItem, newItem) ?: (oldItem == newItem)
        }

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            return if (contentDiff != null && itemDiff != null) {
                if (!contentDiff.invoke(oldItem, newItem) && itemDiff.invoke(oldItem, newItem)) {
                    newItem
                } else null
            } else null
        }
    })

    /** ViewBinding 持有的 ViewHolder */
    class BindingHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 注册一种数据类型的布局创建器与数据绑定器。
     *
     * 使用 reified 泛型保证类型安全，无需手动转型。
     *
     * @param T              数据类型
     * @param VB             ViewBinding 类型
     * @param inflate        ViewBinding 的 inflate 函数引用（如 `ItemTextBinding::inflate`）
     * @param bind           数据绑定回调，参数已自动转换为具体类型
     * @param bindWithPayload 局部更新绑定回调（可选），用于 DiffUtil payload 场景
     */
    inline fun <reified T : Any, reified VB : ViewBinding> register(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        noinline bind: (VB, T, Int) -> Unit,
        noinline bindWithPayload: ((VB, T, Int, List<Any>) -> Unit)? = null
    ) {
        registrations.add(TypeRegistration(
            clazz = T::class,
            create = { parent -> inflate(LayoutInflater.from(parent.context), parent, false) },
            bind = { binding, item, pos ->
                @Suppress("UNCHECKED_CAST")
                bind(binding as VB, item as T, pos)
            },
            bindWithPayload = bindWithPayload?.let { payloadBind ->
                { binding, item, pos, payloads ->
                    @Suppress("UNCHECKED_CAST")
                    payloadBind(binding as VB, item as T, pos, payloads)
                }
            }
        ))
    }

    /**
     * 提交新数据列表。
     *
     * 使用 [AsyncListDiffer] 在后台线程进行 DiffUtil 差量计算，
     * 结果自动分发到主线程，不会阻塞 UI。
     *
     * @param newItems        新的数据列表
     * @param commitCallback  Diff 完成后的回调（可选）
     */
    fun submitList(newItems: List<Any>, commitCallback: Runnable? = null) {
        differ.submitList(newItems, commitCallback)
    }

    /** 获取指定位置的数据项 */
    fun getItem(position: Int): Any = differ.currentList[position]

    /** 获取当前数据列表（不可变快照） */
    fun currentList(): List<Any> = differ.currentList

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        val item = differ.currentList[position]
        val index = registrations.indexOfFirst { it.clazz.java == item::class.java }
        if (index == -1) throw IllegalStateException("未注册类型: ${item::class.java.simpleName}")
        return index
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        return BindingHolder(registrations[viewType].create(parent))
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        registrations[holder.itemViewType].bind(holder.binding, differ.currentList[position], position)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val reg = registrations[holder.itemViewType]
            if (reg.bindWithPayload != null) {
                reg.bindWithPayload.invoke(holder.binding, differ.currentList[position], position, payloads)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}

/**
 * MultiTypeAdapter 的 DSL 构建器。
 *
 * ```kotlin
 * val adapter = multiTypeAdapter {
 *     itemDiff { old, new -> (old as? HasId)?.id == (new as? HasId)?.id }
 *     contentDiff { old, new -> old == new }
 *
 *     register<TextItem, ItemTextBinding>(ItemTextBinding::inflate) { binding, item, _ ->
 *         binding.tvText.text = item.text
 *     }
 * }
 * ```
 */
class AwMultiTypeAdapterBuilder {
    private var itemDiff: ((Any, Any) -> Boolean)? = null
    private var contentDiff: ((Any, Any) -> Boolean)? = null

    @PublishedApi
    internal val registrations = mutableListOf<TypeRegistration>()

    /** 设置 item 相同性判断 */
    fun itemDiff(block: (old: Any, new: Any) -> Boolean) { itemDiff = block }

    /** 设置内容相同性判断 */
    fun contentDiff(block: (old: Any, new: Any) -> Boolean) { contentDiff = block }

    /**
     * 注册一种数据类型，使用 ViewBinding inflate 方法引用。
     *
     * @param VB              ViewBinding 类型
     * @param T               数据类型
     * @param inflate         ViewBinding 的 inflate 函数引用（如 `ItemTextBinding::inflate`）
     * @param bind            数据绑定回调，参数已自动转换为具体类型
     * @param bindWithPayload 局部更新绑定回调（可选）
     */
    inline fun <reified T : Any, reified VB : ViewBinding> register(
        noinline inflate: (LayoutInflater, ViewGroup, Boolean) -> VB,
        noinline bind: (VB, T, Int) -> Unit,
        noinline bindWithPayload: ((VB, T, Int, List<Any>) -> Unit)? = null
    ) {
        registrations.add(TypeRegistration(
            clazz = T::class,
            create = { parent -> inflate(LayoutInflater.from(parent.context), parent, false) },
            bind = { binding, item, pos ->
                @Suppress("UNCHECKED_CAST")
                bind(binding as VB, item as T, pos)
            },
            bindWithPayload = bindWithPayload?.let { payloadBind ->
                { binding, item, pos, payloads ->
                    @Suppress("UNCHECKED_CAST")
                    payloadBind(binding as VB, item as T, pos, payloads)
                }
            }
        ))
    }

    fun build(): AwMultiTypeAdapter {
        val adapter = AwMultiTypeAdapter(itemDiff, contentDiff)
        adapter.registrations.addAll(registrations)
        return adapter
    }
}

/**
 * 使用 DSL 构建 [AwMultiTypeAdapter]。
 *
 * @see AwMultiTypeAdapterBuilder
 */
fun awMultiTypeAdapter(block: AwMultiTypeAdapterBuilder.() -> Unit): AwMultiTypeAdapter {
    return AwMultiTypeAdapterBuilder().apply(block).build()
}
