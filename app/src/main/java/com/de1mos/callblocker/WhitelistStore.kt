package com.de1mos.callblocker

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import androidx.core.content.edit

internal class WhitelistStore(
    context: Context,
    private val defaultCountryIso: String = defaultCountryIso(context),
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    init {
        if (!preferences.getBoolean(KEY_E164_MIGRATED, false)) {
            val migratedNumbers = storedNumbers().mapNotNull(::normalize).toSet()
            preferences.edit {
                putStringSet(KEY_NUMBERS, migratedNumbers)
                putBoolean(KEY_E164_MIGRATED, true)
            }
        }
    }

    fun numbers(): List<String> =
        storedNumbers().sorted()

    fun add(rawNumber: String): Boolean {
        val normalized = normalize(rawNumber) ?: return false
        val updated = storedNumbers().toMutableSet()
        if (updated.add(normalized)) {
            preferences.edit { putStringSet(KEY_NUMBERS, updated) }
        }
        return true
    }

    fun remove(number: String) {
        val updated = storedNumbers().toMutableSet()
        if (updated.remove(number)) {
            preferences.edit { putStringSet(KEY_NUMBERS, updated) }
        }
    }

    fun isAllowed(rawNumber: String): Boolean {
        val normalized = normalize(rawNumber) ?: return false
        return normalized in storedNumbers()
    }

    private fun storedNumbers(): Set<String> =
        preferences.getStringSet(KEY_NUMBERS, emptySet()).orEmpty()

    private fun normalize(rawNumber: String): String? =
        PhoneNumberUtils.formatNumberToE164(rawNumber, defaultCountryIso)

    private companion object {
        const val PREFERENCES_NAME = "whitelist"
        const val KEY_NUMBERS = "allowed_numbers"
        const val KEY_E164_MIGRATED = "e164_migrated"

        fun defaultCountryIso(context: Context): String {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            return telephonyManager?.simCountryIso?.takeIf { it.length == 2 }
                ?: telephonyManager?.networkCountryIso?.takeIf { it.length == 2 }
                ?: context.resources.configuration.locales[0].country.takeIf { it.length == 2 }
                ?: "ZZ"
        }
    }
}
