package com.answufeng.ui.anim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation

object Anim {

    @Deprecated("Use View.fadeIn() extension instead", ReplaceWith("view.fadeIn(duration, onEnd)"))
    fun fadeIn(duration: Long = 300L): AlphaAnimation {
        return AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            fillAfter = true
        }
    }

    @Deprecated("Use View.fadeOut() extension instead", ReplaceWith("view.fadeOut(duration, onEnd)"))
    fun fadeOut(duration: Long = 300L): AlphaAnimation {
        return AlphaAnimation(1f, 0f).apply {
            this.duration = duration
            fillAfter = true
        }
    }

    @Deprecated("Use View.slideInFromBottom() extension instead")
    fun slideInFromBottom(duration: Long = 300L): TranslateAnimation {
        return TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f,
            Animation.RELATIVE_TO_SELF, 0f
        ).apply {
            this.duration = duration
            fillAfter = true
        }
    }

    @Deprecated("Use View.slideOutToBottom() extension instead")
    fun slideOutToBottom(duration: Long = 300L): TranslateAnimation {
        return TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f
        ).apply {
            this.duration = duration
            fillAfter = true
        }
    }

    @Deprecated("Use View.shake() extension instead")
    fun shake(duration: Long = 500L): Animation {
        return TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0.1f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f
        ).apply {
            this.duration = 100
            repeatCount = (duration / 100).toInt()
            repeatMode = Animation.REVERSE
        }
    }

    @Deprecated("Use View.pulse() extension instead")
    fun pulse(duration: Long = 200L): ScaleAnimation {
        return ScaleAnimation(
            1f, 1.1f, 1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            this.duration = duration
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
    }

    @Deprecated("Use View.bounce() extension instead")
    fun bounce(duration: Long = 400L): Animation {
        return ScaleAnimation(
            1f, 1.2f, 1f, 0.9f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            this.duration = duration / 2
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
    }

    @Deprecated("Use combination of fadeIn + slideInFromBottom extensions instead")
    fun fadeSlideIn(duration: Long = 300L): AnimationSet {
        return AnimationSet(true).apply {
            addAnimation(AlphaAnimation(0f, 1f).apply { this.duration = duration })
            addAnimation(TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f
            ).apply { this.duration = duration })
        }
    }

    fun View.fadeIn(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        animate().apply {
            alpha(1f)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    fun View.fadeOut(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        animate().apply {
            alpha(0f)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    fun View.slideInFromBottom(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        val targetY = translationY
        translationY = height.toFloat()
        animate().apply {
            translationY(targetY)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    fun View.slideOutToBottom(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        animate().apply {
            translationY(height.toFloat())
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    fun View.shake(duration: Long = 500L, onEnd: (() -> Unit)? = null) {
        val shakeCount = (duration / 100).coerceAtLeast(1)
        val shakeDistance = width * 0.1f
        val animator = ObjectAnimator.ofFloat(
            this, "translationX",
            *generateShakeValues(shakeDistance, shakeCount)
        ).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    private fun generateShakeValues(distance: Float, count: Int): FloatArray {
        val values = FloatArray(count * 2 + 1)
        values[0] = 0f
        for (i in 0 until count) {
            val sign = if (i % 2 == 0) 1f else -1f
            values[i * 2 + 1] = sign * distance
            values[i * 2 + 2] = 0f
        }
        return values
    }

    fun View.pulse(duration: Long = 200L, onEnd: (() -> Unit)? = null) {
        val scaleX = PropertyValuesHolder.ofKeyframe(
            android.animation.Keyframe.ofFloat(0f, 1f),
            android.animation.Keyframe.ofFloat(0.5f, 1.1f),
            android.animation.Keyframe.ofFloat(1f, 1f)
        )
        val scaleY = PropertyValuesHolder.ofKeyframe(
            android.animation.Keyframe.ofFloat(0f, 1f),
            android.animation.Keyframe.ofFloat(0.5f, 1.1f),
            android.animation.Keyframe.ofFloat(1f, 1f)
        )
        ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    fun View.bounce(duration: Long = 400L, onEnd: (() -> Unit)? = null) {
        val scaleX = PropertyValuesHolder.ofKeyframe(
            android.animation.Keyframe.ofFloat(0f, 1f),
            android.animation.Keyframe.ofFloat(0.5f, 1.2f),
            android.animation.Keyframe.ofFloat(1f, 1f)
        )
        val scaleY = PropertyValuesHolder.ofKeyframe(
            android.animation.Keyframe.ofFloat(0f, 1f),
            android.animation.Keyframe.ofFloat(0.5f, 0.9f),
            android.animation.Keyframe.ofFloat(1f, 1f)
        )
        ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }

    fun View.fadeSlideIn(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        alpha = 0f
        translationY = height.toFloat()
        animate().apply {
            alpha(1f)
            translationY(0f)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            onEnd?.let { callback ->
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        callback()
                    }
                })
            }
            start()
        }
    }
}
