package com.answufeng.ui.recyclerview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 列表项动画器，在项目变更时添加淡入淡出效果。
 *
 * 使用方式：`recyclerView.itemAnimator = AwItemAnimator()`
 */
class AwItemAnimator : DefaultItemAnimator() {
    private val pendingAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val runningAnimations = mutableListOf<RecyclerView.ViewHolder>()

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        holder.itemView.scaleX = 0.8f
        holder.itemView.scaleY = 0.8f
        pendingAnimations.add(holder)
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val animatorSet = AnimatorSet()
        val alphaAnim = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 1f, 0f)
        val scaleXAnim = ObjectAnimator.ofFloat(holder.itemView, View.SCALE_X, 1f, 0.8f)
        val scaleYAnim = ObjectAnimator.ofFloat(holder.itemView, View.SCALE_Y, 1f, 0.8f)

        animatorSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim)
        animatorSet.duration = getRemoveDuration()
        animatorSet.interpolator = DecelerateInterpolator()

        animatorSet.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    runningAnimations.add(holder)
                }

                override fun onAnimationEnd(animation: Animator) {
                    animatorSet.removeAllListeners()
                    runningAnimations.remove(holder)
                    dispatchRemoveFinished(holder)
                }

                override fun onAnimationCancel(animation: Animator) {
                    runningAnimations.remove(holder)
                    dispatchRemoveFinished(holder)
                }
            },
        )

        animatorSet.start()
        return true
    }

    override fun runPendingAnimations() {
        val addHolders = pendingAnimations.toList()
        pendingAnimations.clear()

        for (holder in addHolders) {
            val animatorSet = AnimatorSet()
            val alphaAnim = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 0f, 1f)
            val scaleXAnim = ObjectAnimator.ofFloat(holder.itemView, View.SCALE_X, 0.8f, 1f)
            val scaleYAnim = ObjectAnimator.ofFloat(holder.itemView, View.SCALE_Y, 0.8f, 1f)

            animatorSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim)
            animatorSet.duration = getAddDuration()
            animatorSet.interpolator = OvershootInterpolator(1f)

            animatorSet.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        dispatchAddStarting(holder)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        animatorSet.removeAllListeners()
                        dispatchAddFinished(holder)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        holder.itemView.alpha = 1f
                        holder.itemView.scaleX = 1f
                        holder.itemView.scaleY = 1f
                        dispatchAddFinished(holder)
                    }
                },
            )

            animatorSet.start()
        }

        super.runPendingAnimations()
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        item.itemView.animate().cancel()
        if (pendingAnimations.contains(item)) {
            pendingAnimations.remove(item)
            // 恢复 alpha，避免 item 不可见
            item.itemView.alpha = 1f
            item.itemView.scaleX = 1f
            item.itemView.scaleY = 1f
            dispatchAddFinished(item)
        }
        if (runningAnimations.contains(item)) {
            runningAnimations.remove(item)
            dispatchRemoveFinished(item)
        }
        super.endAnimation(item)
    }

    override fun endAnimations() {
        for (holder in pendingAnimations.toList()) {
            pendingAnimations.remove(holder)
            holder.itemView.alpha = 1f
            holder.itemView.scaleX = 1f
            holder.itemView.scaleY = 1f
            dispatchAddFinished(holder)
        }
        for (holder in runningAnimations.toList()) {
            runningAnimations.remove(holder)
            dispatchRemoveFinished(holder)
        }
        super.endAnimations()
    }

    override fun isRunning(): Boolean {
        return pendingAnimations.isNotEmpty() || runningAnimations.isNotEmpty() || super.isRunning()
    }
}
