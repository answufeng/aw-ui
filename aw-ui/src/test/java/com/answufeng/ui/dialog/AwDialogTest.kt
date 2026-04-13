package com.answufeng.ui.dialog

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AwDialogTest {

    private lateinit var activity: AppCompatActivity

    @Before
    fun setup() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = controller.get()
        activity.setTheme(com.google.android.material.R.style.Theme_Material3_DayNight)
        controller.create().start().resume()
    }

    @Test
    fun `confirm shows a dialog`() {
        val dialog = AwDialog.confirm(activity, "Title", "Message") {}
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `alert shows a dialog`() {
        val dialog = AwDialog.alert(activity, "Alert", "Content")
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `input shows a dialog with text field`() {
        val dialog = AwDialog.input(activity, "Input", hint = "Enter text") {}
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `list shows a dialog`() {
        val dialog = AwDialog.list(activity, "Pick", listOf("A", "B", "C")) {}
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `bottomList shows a dialog`() {
        val dialog = AwDialog.bottomList(activity, "Choose", listOf("X", "Y")) {}
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `custom with view shows a dialog`() {
        val view = TextView(activity).apply { text = "Custom" }
        val dialog = AwDialog.custom(activity, title = "Custom", view = view)
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `confirm onConfirm callback fires`() {
        var confirmed = false
        AwDialog.confirm(activity, "T", "M") { confirmed = true }
        val dialog = ShadowDialog.getLatestDialog() as? androidx.appcompat.app.AlertDialog
        assertNotNull("Dialog should be AlertDialog", dialog)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val button = dialog!!.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
        assertNotNull("Positive button should exist", button)
        button.performClick()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertTrue(confirmed)
    }

    @Test
    fun `confirm onCancel callback fires`() {
        var cancelled = false
        AwDialog.confirm(activity, "T", "M", onCancel = { cancelled = true }) {}
        val dialog = ShadowDialog.getLatestDialog() as? androidx.appcompat.app.AlertDialog
        assertNotNull("Dialog should be AlertDialog", dialog)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val button = dialog!!.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
        assertNotNull("Negative button should exist", button)
        button.performClick()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertTrue(cancelled)
    }

    @Test
    fun `confirm returns dialog instance`() {
        val dialog = AwDialog.confirm(activity, "T", "M") {}
        assertNotNull(dialog)
    }

    @Test
    fun `alert returns dialog instance`() {
        val dialog = AwDialog.alert(activity, "T", "M")
        assertNotNull(dialog)
    }
}
