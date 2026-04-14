package com.answufeng.ui.anim

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AwItemAnimatorTest {

    private lateinit var activity: Activity
    private lateinit var itemView: View

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        itemView = View(activity).apply {
            val parent = FrameLayout(activity)
            parent.addView(this, 100, 50)
            parent.measure(
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY)
            )
            parent.layout(0, 0, 200, 200)
        }
    }

    @Test
    fun `animateItem FADE_SLIDE_UP does not crash`() {
        AwItemAnimator.animateItem(itemView, 0, type = AwItemAnimator.AnimType.FADE_SLIDE_UP)
    }

    @Test
    fun `animateItem FADE_SLIDE_LEFT does not crash`() {
        AwItemAnimator.animateItem(itemView, 1, type = AwItemAnimator.AnimType.FADE_SLIDE_LEFT)
    }

    @Test
    fun `animateItem FADE_SLIDE_RIGHT does not crash`() {
        AwItemAnimator.animateItem(itemView, 2, type = AwItemAnimator.AnimType.FADE_SLIDE_RIGHT)
    }

    @Test
    fun `animateItem FADE_IN does not crash`() {
        AwItemAnimator.animateItem(itemView, 0, type = AwItemAnimator.AnimType.FADE_IN)
    }

    @Test
    fun `animateItem SCALE_IN does not crash`() {
        AwItemAnimator.animateItem(itemView, 0, type = AwItemAnimator.AnimType.SCALE_IN)
    }

    @Test
    fun `custom duration and delay`() {
        AwItemAnimator.animateItem(itemView, 3, duration = 500L, delayPerItem = 100L)
    }

    @Test
    fun `resetItem restores default state`() {
        AwItemAnimator.animateItem(itemView, 0, type = AwItemAnimator.AnimType.SCALE_IN)
        AwItemAnimator.resetItem(itemView)
        assertEquals(1f, itemView.alpha)
        assertEquals(0f, itemView.translationX)
        assertEquals(0f, itemView.translationY)
        assertEquals(1f, itemView.scaleX)
        assertEquals(1f, itemView.scaleY)
    }

    @Test
    fun `all AnimType values are handled`() {
        for (type in AwItemAnimator.AnimType.entries) {
            AwItemAnimator.animateItem(itemView, 0, type = type)
            AwItemAnimator.resetItem(itemView)
        }
    }

    @Test
    fun `delay is capped at MAX_DELAY`() {
        AwItemAnimator.animateItem(itemView, 100, delayPerItem = 100L)
        AwItemAnimator.resetItem(itemView)
    }

    @Test
    fun `firstVisiblePosition reduces delay`() {
        AwItemAnimator.animateItem(itemView, 100, firstVisiblePosition = 95, delayPerItem = 50L)
        AwItemAnimator.resetItem(itemView)
    }

    @Test
    fun `firstVisiblePosition zero is default`() {
        AwItemAnimator.animateItem(itemView, 5, delayPerItem = 50L)
        AwItemAnimator.resetItem(itemView)
    }
}
