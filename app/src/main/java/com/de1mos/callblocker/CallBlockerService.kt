package com.de1mos.callblocker

import android.telecom.Call
import android.telecom.CallScreeningService

class CallBlockerService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val incomingNumber = callDetails.handle?.schemeSpecificPart
        val shouldBlock = incomingNumber == null || !WhitelistStore(this).isAllowed(incomingNumber)

        val response = CallResponse.Builder()
            .setDisallowCall(shouldBlock)
            .setRejectCall(shouldBlock)
            .build()
        respondToCall(callDetails, response)
    }
}
