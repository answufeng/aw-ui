package com.answufeng.ui.anim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

fun View.fadeIn(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
    animate().apply {
        alpha(1f)
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { callback ->
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { callback() }
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
                override fun onAnimationEnd(animation: Animator) { callback() }
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
                override fun onAnimationEnd(animation: Animator) { callback() }
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
                override fun onAnimationEnd(animation: Animator) { callback() }
            })
        }
        start()
    }
}

fun View.shake(duration: Long = 500L, onEnd: (() -> Unit)? = null) {
    val shakeCount = (duration / 100).coerceAtLeast(1).toInt()
    val shakeDistance = width * 0.1f
    ObjectAnimator.ofFloat(this, "translationX", *generateShakeValues(shakeDistance, shakeCount)).apply {
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { callback ->
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { callback() }
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
    val scaleX = PropertyValuesHolder.ofKeyframe("scaleX",
        android.animation.Keyframe.ofFloat(0f, 1f),
        android.animation.Keyframe.ofFloat(0.5f, 1.1f),
        android.animation.Keyframe.ofFloat(1f, 1f))
    val scaleY = PropertyValuesHolder.ofKeyframe("scaleY",
        android.animation.Keyframe.ofFloat(0f, 1f),
        android.animation.Keyframe.ofFloat(0.5f, 1.1f),
        android.animation.Keyframe.ofFloat(1f, 1f))
    ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY).apply {
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { callback ->
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { callback() }
            })
        }
        start()
    }
}

fun View.bounce(duration: Long = 400L, onEnd: (() -> Unit)? = null) {
    val scaleX = PropertyValuesHolder.ofKeyframe("scaleX",
        android.animation.Keyframe.ofFloat(0f, 1f),
        android.animation.Keyframe.ofFloat(0.5f, 1.2f),
        android.animation.Keyframe.ofFloat(1f, 1f))
    val scaleY = PropertyValuesHolder.ofKeyframe("scaleY",
        android.animation.Keyframe.ofFloat(0f, 1f),
        android.animation.Keyframe.ofFloat(0.5f, 1.2f),
        android.animation.Keyframe.ofFloat(1f, 1f))
    ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY).apply {
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { callback ->
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { callback() }
            })
        }
        start()
    }
}

fun View.fadeSlideIn(duration: Long = 400L, onEnd: (() -> Unit)? = null) {
    alpha = 0f
    translationY = height * 0.1f
    animate().apply {
        alpha(1f)
        translationY(0f)
        this.duration = duration
        interpolator = AccelerateDecelerateInterpolator()
        onEnd?.let { callback ->
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { callback() }
            })
        }
        start()
    }
}