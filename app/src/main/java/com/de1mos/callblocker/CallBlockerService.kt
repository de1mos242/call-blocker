package com.de1mos.callblocker

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.PhoneAccount
import android.telephony.TelephonyManager
import java.util.Locale

class CallBlockerService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val incomingNumber = callDetails.handle
            ?.takeIf { it.scheme == PhoneAccount.SCHEME_TEL }
            ?.schemeSpecificPart
        val allowed = incomingNumber != null && WhitelistStore(this).isAllowed(
            incomingNumber,
            defaultCountryIso(),
        )

        val response = if (allowed) {
            CallResponse.Builder()
                .setDisallowCall(false)
                .build()
        } else {
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }
        respondToCall(callDetails, response)
    }

    private fun defaultCountryIso(): String {
        val networkCountry = getSystemService(TelephonyManager::class.java)
            .networkCountryIso
            .takeIf { it.length == 2 }
        val localeCountry = resources.configuration.locales[0]
            .country
            .takeIf { it.length == 2 }
        return (networkCountry ?: localeCountry ?: "ZZ").uppercase(Locale.ROOT)
    }
}
