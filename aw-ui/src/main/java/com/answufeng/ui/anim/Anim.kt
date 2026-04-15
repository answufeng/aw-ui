package com.answufeng.ui.anim

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation

object Anim {
    fun fadeIn(duration: Long = 300L): AlphaAnimation {
        return AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            fillAfter = true
        }
    }

    fun fadeOut(duration: Long = 300L): AlphaAnimation {
        return AlphaAnimation(1f, 0f).apply {
            this.duration = duration
            fillAfter = true
        }
    }

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

    fun fadeSlideIn(duration: Long = 300L): AnimationSet {
        return AnimationSet(true).apply {
            addAnimation(fadeIn(duration))
            addAnimation(slideInFromBottom(duration))
        }
    }

    fun View.fadeIn(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        startAnimation(Anim.fadeIn(duration).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { onEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        })
    }

    fun View.fadeOut(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        startAnimation(Anim.fadeOut(duration).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { onEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        })
    }

    fun View.slideInFromBottom(duration: Long = 300L, onEnd: (() -> Unit)? = null) {
        startAnimation(Anim.slideInFromBottom(duration).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) { onEnd?.invoke() }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        })
    }

    fun View.shake(duration: Long = 500L) {
        startAnimation(Anim.shake(duration))
    }

    fun View.pulse(duration: Long = 200L) {
        startAnimation(Anim.pulse(duration))
    }

    fun View.bounce(duration: Long = 400L) {
        startAnimation(Anim.bounce(duration))
    }
}