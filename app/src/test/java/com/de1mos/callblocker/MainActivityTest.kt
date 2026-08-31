package com.de1mos.callblocker

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.EditText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class MainActivityTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteSharedPreferences("pin")
        context.getSharedPreferences("setup", Context.MODE_PRIVATE).edit()
            .putBoolean("prompted", true)
            .commit()
    }

    @Test
    fun `keeps configuration locked until a PIN is created and confirmed`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val root = activity.findViewById<View>(R.id.root)
        val dialog = latestDialog()
        val pinInput = dialog.findViewById<EditText>(R.id.pin_input)
        val confirmationInput = dialog.findViewById<EditText>(R.id.pin_confirmation)

        assertEquals(View.INVISIBLE, root.visibility)
        pinInput.setText("1234")
        confirmationInput.setText("4321")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        assertTrue(dialog.isShowing)
        assertNotNull(confirmationInput.error)
        assertEquals(View.INVISIBLE, root.visibility)

        confirmationInput.setText("1234")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        assertFalse(dialog.isShowing)
        assertTrue(PinStore(context).isConfigured())
        assertEquals(View.VISIBLE, root.visibility)
    }

    @Test
    fun `requires the configured PIN and rejects an incorrect PIN`() {
        PinStore(context).setPin("1234")
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val root = activity.findViewById<View>(R.id.root)
        val dialog = latestDialog()
        val pinInput = dialog.findViewById<EditText>(R.id.pin_input)

        pinInput.setText("4321")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        assertTrue(dialog.isShowing)
        assertNotNull(pinInput.error)
        assertEquals(View.INVISIBLE, root.visibility)

        pinInput.setText("1234")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        assertFalse(dialog.isShowing)
        assertEquals(View.VISIBLE, root.visibility)
    }

    @Test
    fun `locks configuration when the app leaves the foreground`() {
        PinStore(context).setPin("1234")
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val root = activity.findViewById<View>(R.id.root)
        val dialog = latestDialog()
        dialog.findViewById<EditText>(R.id.pin_input).setText("1234")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        assertEquals(View.VISIBLE, root.visibility)

        controller.pause().stop()

        assertEquals(View.INVISIBLE, root.visibility)
    }

    private fun latestDialog(): AlertDialog =
        ShadowAlertDialog.getLatestAlertDialog()
}
