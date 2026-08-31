package com.de1mos.callblocker

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class PinStoreTest {
    private lateinit var context: Context
    private lateinit var store: PinStore

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteSharedPreferences("pin")
        store = PinStore(context)
    }

    @Test
    fun `starts without a configured PIN`() {
        assertFalse(store.isConfigured())
        assertFalse(store.verify("1234"))
    }

    @Test
    fun `persists and verifies the PIN`() {
        store.setPin("1234")

        val restoredStore = PinStore(context)
        assertTrue(restoredStore.isConfigured())
        assertTrue(restoredStore.verify("1234"))
        assertFalse(restoredStore.verify("4321"))
    }

    @Test
    fun `clearing app preferences returns to setup state`() {
        store.setPin("1234")

        context.deleteSharedPreferences("pin")

        assertFalse(PinStore(context).isConfigured())
    }
}
