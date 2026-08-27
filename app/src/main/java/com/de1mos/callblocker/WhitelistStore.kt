package com.de1mos.callblocker

import android.content.Context
import android.telephony.PhoneNumberUtils
import androidx.core.content.edit

class WhitelistStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun numbers(): List<String> =
        preferences.getStringSet(KEY_NUMBERS, emptySet()).orEmpty().sorted()

    fun add(rawNumber: String): Boolean {
        val normalized = normalize(rawNumber) ?: return false
        val updated = numbers().toMutableSet()
        updated.add(normalized)
        preferences.edit { putStringSet(KEY_NUMBERS, updated) }
        return true
    }

    fun remove(number: String) {
        val updated = numbers().toMutableSet()
        updated.remove(number)
        preferences.edit { putStringSet(KEY_NUMBERS, updated) }
    }

    fun isAllowed(rawNumber: String, countryIso: String): Boolean {
        val normalized = normalize(rawNumber) ?: return false
        return numbers().any { allowedNumber ->
            allowedNumber == normalized || PhoneNumberUtils.areSamePhoneNumber(
                allowedNumber,
                normalized,
                countryIso,
            )
        }
    }

    private fun normalize(rawNumber: String): String? {
        val normalized = PhoneNumberUtils.normalizeNumber(rawNumber)
        return normalized.takeIf(PhoneNumberUtils::isGlobalPhoneNumber)
    }

    private companion object {
        const val PREFERENCES_NAME = "whitelist"
        const val KEY_NUMBERS = "allowed_numbers"
    }
}
