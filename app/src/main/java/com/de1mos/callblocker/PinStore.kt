package com.de1mos.callblocker

import android.content.Context
import androidx.core.content.edit

internal class PinStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isConfigured(): Boolean = preferences.contains(KEY_PIN)

    fun setPin(pin: String) {
        preferences.edit { putString(KEY_PIN, pin) }
    }

    fun verify(pin: String): Boolean = preferences.getString(KEY_PIN, null) == pin

    private companion object {
        const val PREFERENCES_NAME = "pin"
        const val KEY_PIN = "value"
    }
}
