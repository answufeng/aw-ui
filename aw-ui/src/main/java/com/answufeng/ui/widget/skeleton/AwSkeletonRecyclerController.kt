package com.answufeng.ui.widget.skeleton

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

internal class AwSkeletonListAdapter(
    @LayoutRes private val itemLayout: Int,
    private val itemCount: Int,
) : RecyclerView.Adapter<AwSkeletonListAdapter.VH>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VH {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
    ) {
        // 占位 item，不绑定数据
    }

    override fun getItemCount(): Int = itemCount

    class VH(view: View) : RecyclerView.ViewHolder(view)
}

internal class AwSkeletonRecyclerController(
    private val recyclerView: RecyclerView,
    @LayoutRes private val itemLayout: Int,
    private var placeholderCount: Int,
    override var config: AwSkeletonConfig,
) : AwSkeleton {
    private var originalAdapter: RecyclerView.Adapter<*>? = null
    private var showingSkeleton = false

    override val isShowingSkeleton: Boolean get() = showingSkeleton

    override fun showSkeleton() {
        if (showingSkeleton) return
        showingSkeleton = true
        if (originalAdapter == null) {
            originalAdapter = recyclerView.adapter
        }
        recyclerView.adapter = AwSkeletonListAdapter(itemLayout, placeholderCount)
    }

    override fun showContent(animate: Boolean) {
        if (!showingSkeleton && originalAdapter == null) return
        showingSkeleton = false
        val restore = originalAdapter
        recyclerView.adapter = restore
        if (animate && restore != null) {
            fadeInContent(recyclerView, 200L)
        }
    }

    /** 在 showContent 前设置将要恢复的真实 adapter */
    fun setContentAdapter(adapter: RecyclerView.Adapter<*>) {
        originalAdapter = adapter
    }
}
