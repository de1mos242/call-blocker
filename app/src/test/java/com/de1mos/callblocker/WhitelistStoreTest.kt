package com.de1mos.callblocker

import android.content.Context
import org.junit.Assert.assertEquals
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
class WhitelistStoreTest {
    private lateinit var context: Context
    private lateinit var store: WhitelistStore

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.deleteSharedPreferences("whitelist")
        store = WhitelistStore(context, "DE")
    }

    @Test
    fun `stores and matches equivalent numbers in E164 format`() {
        assertTrue(store.add("030 3030-8080"))

        assertEquals(listOf("+493030308080"), store.numbers())
        assertTrue(store.isAllowed("+49 (30) 3030 8080"))
    }

    @Test
    fun `does not store equivalent numbers twice`() {
        assertTrue(store.add("030 3030 8080"))
        assertTrue(store.add("+49 30 3030 8080"))

        assertEquals(listOf("+493030308080"), store.numbers())
    }

    @Test
    fun `migrates numbers stored by the previous format`() {
        context.deleteSharedPreferences("whitelist")
        context.getSharedPreferences("whitelist", Context.MODE_PRIVATE).edit()
            .putStringSet("allowed_numbers", setOf("03030308080"))
            .apply()

        val migratedStore = WhitelistStore(context, "DE")

        assertEquals(listOf("+493030308080"), migratedStore.numbers())
        assertTrue(migratedStore.isAllowed("+49 30 3030 8080"))
    }

    @Test
    fun `rejects invalid input`() {
        assertFalse(store.add("abc"))
        assertTrue(store.numbers().isEmpty())
        assertFalse(store.isAllowed("not a number"))
    }

    @Test
    fun `persists and removes numbers`() {
        store.add("+49 30 3030 8080")

        val restoredStore = WhitelistStore(context, "DE")
        assertTrue(restoredStore.isAllowed("030 3030 8080"))

        restoredStore.remove("+493030308080")
        assertTrue(store.numbers().isEmpty())
    }
}
