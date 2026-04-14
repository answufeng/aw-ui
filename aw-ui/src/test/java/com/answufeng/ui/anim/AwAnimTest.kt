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
class AwAnimTest {

    private lateinit var activity: Activity
    private lateinit var testView: View

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        testView = View(activity).apply {
            val parent = FrameLayout(activity)
            parent.addView(this, 100, 100)
            parent.measure(
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY)
            )
            parent.layout(0, 0, 200, 200)
        }
    }

    // ==================== 便捷型 API（自动 start）====================

    @Test
    fun `fadeIn returns animator and sets visible`() {
        testView.visibility = View.GONE
        val anim = testView.fadeIn()
        assertNotNull(anim)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `fadeOut returns animator`() {
        testView.visibility = View.VISIBLE
        testView.alpha = 1f
        val anim = testView.fadeOut()
        assertNotNull(anim)
    }

    @Test
    fun `slideInFromBottom returns animator`() {
        val anim = testView.slideInFromBottom()
        assertNotNull(anim)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `slideInFromTop returns animator`() {
        val anim = testView.slideInFromTop()
        assertNotNull(anim)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `slideInFromLeft returns animator`() {
        val anim = testView.slideInFromLeft()
        assertNotNull(anim)
    }

    @Test
    fun `slideInFromRight returns animator`() {
        val anim = testView.slideInFromRight()
        assertNotNull(anim)
    }

    @Test
    fun `slideOutToTop returns animator`() {
        val anim = testView.slideOutToTop()
        assertNotNull(anim)
    }

    @Test
    fun `slideOutToBottom returns animator`() {
        val anim = testView.slideOutToBottom()
        assertNotNull(anim)
    }

    @Test
    fun `scaleIn returns animator`() {
        val anim = testView.scaleIn()
        assertNotNull(anim)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `scaleOut returns animator`() {
        val anim = testView.scaleOut()
        assertNotNull(anim)
    }

    @Test
    fun `pulse returns animator`() {
        val anim = testView.pulse()
        assertNotNull(anim)
    }

    @Test
    fun `shake returns animator`() {
        val anim = testView.shake()
        assertNotNull(anim)
    }

    @Test
    fun `bounce returns animator`() {
        val anim = testView.bounce()
        assertNotNull(anim)
    }

    @Test
    fun `fadeSlideIn returns animator`() {
        val anim = testView.fadeSlideIn()
        assertNotNull(anim)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `fadeSlideOut returns animator`() {
        val anim = testView.fadeSlideOut()
        assertNotNull(anim)
    }

    @Test
    fun `rotate returns animator`() {
        val anim = testView.rotate()
        assertNotNull(anim)
    }

    @Test
    fun `fadeIn with custom duration`() {
        val anim = testView.fadeIn(duration = 500L)
        assertNotNull(anim)
    }

    @Test
    fun `fadeOut with goneOnEnd false`() {
        val anim = testView.fadeOut(goneOnEnd = false)
        assertNotNull(anim)
    }

    @Test
    fun `onEnd callback is accepted`() {
        var ended = false
        val anim = testView.fadeIn(onEnd = { ended = true })
        assertNotNull(anim)
    }

    // ==================== 创建型 API（不自动 start）====================

    @Test
    fun `createFadeIn returns animator but does not start it`() {
        testView.visibility = View.GONE
        val anim = testView.createFadeIn()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
        // createFadeIn sets visibility to VISIBLE and alpha to 0 as preparation
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `createFadeOut returns animator but does not start it`() {
        testView.alpha = 1f
        val anim = testView.createFadeOut()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createShake returns animator but does not start it`() {
        val anim = testView.createShake()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createBounce returns animator but does not start it`() {
        val anim = testView.createBounce()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createScaleIn returns animator but does not start it`() {
        val anim = testView.createScaleIn()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `createScaleOut returns animator but does not start it`() {
        val anim = testView.createScaleOut()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createPulse returns animator but does not start it`() {
        val anim = testView.createPulse()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createFadeSlideIn returns animator but does not start it`() {
        val anim = testView.createFadeSlideIn()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `createFadeSlideOut returns animator but does not start it`() {
        val anim = testView.createFadeSlideOut()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createRotate returns animator but does not start it`() {
        val anim = testView.createRotate()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createSlideInFromBottom returns animator but does not start it`() {
        val anim = testView.createSlideInFromBottom()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
        assertEquals(View.VISIBLE, testView.visibility)
    }

    @Test
    fun `createSlideOutToTop returns animator but does not start it`() {
        val anim = testView.createSlideOutToTop()
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createFadeIn with custom duration`() {
        val anim = testView.createFadeIn(duration = 500L)
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createFadeOut with goneOnEnd false`() {
        val anim = testView.createFadeOut(goneOnEnd = false)
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }

    @Test
    fun `createShake with custom amplitude`() {
        val anim = testView.createShake(amplitude = 20f, duration = 600L)
        assertNotNull(anim)
        assertFalse(anim.isRunning)
    }
}
