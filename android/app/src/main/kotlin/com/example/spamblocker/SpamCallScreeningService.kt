package com.example.spamblocker

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class SpamCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "SpamBlockerService"
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val rawPhoneNumber = extractPhoneNumber(callDetails)
        val handleStr = callDetails.handle?.toString() ?: "null"
        val schemeSpecific = callDetails.handle?.schemeSpecificPart ?: "null"

        Log.e(TAG, "=== ON SCREEN CALL FIRED ===")
        Log.e(TAG, "Raw extracted number: '$rawPhoneNumber'")
        Log.e(TAG, "Handle string: '$handleStr', SchemeSpecific: '$schemeSpecific'")
        
        val sharedPref = getSharedPreferences("spam_prefs", Context.MODE_PRIVATE)
        val prefixesString = sharedPref.getString("blocked_prefixes", "") ?: ""
        val prefixes = prefixesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        Log.e(TAG, "Active blocked prefixes: $prefixes")

        var isSpam = false
        var matchedPrefix = ""
        for (prefix in prefixes) {
            if (isNumberMatchingPrefix(rawPhoneNumber, prefix)) {
                isSpam = true
                matchedPrefix = prefix
                break
            }
        }

        if (isSpam) {
            Log.e(TAG, "🚫 BLOCKING CALL: '$rawPhoneNumber' matched prefix '$matchedPrefix'")
            showToast("🚫 Spam call blocked: $rawPhoneNumber (matched $matchedPrefix)")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val response = CallResponse.Builder()
                    .setRejectCall(true)
                    .setDisallowCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build()
                respondToCall(callDetails, response)
            }
        } else {
            Log.e(TAG, "✅ ALLOWING CALL: '$rawPhoneNumber'")
            showToast("✅ Screened call allowed: $rawPhoneNumber")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val response = CallResponse.Builder().build()
                respondToCall(callDetails, response)
            }
        }
    }

    private fun extractPhoneNumber(callDetails: Call.Details): String {
        val handle = callDetails.handle ?: return ""
        val schemeSpecific = handle.schemeSpecificPart
        if (!schemeSpecific.isNullOrEmpty()) {
            return schemeSpecific
        }
        val fullUri = handle.toString()
        if (fullUri.startsWith("tel:")) {
            return fullUri.substring(4)
        }
        return fullUri
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun isNumberMatchingPrefix(rawNumber: String, prefix: String): Boolean {
        if (rawNumber.isEmpty() || prefix.isEmpty()) return false

        val cleanNumber = rawNumber.replace(Regex("[^0-9+]"), "")
        val cleanPrefix = prefix.replace(Regex("[^0-9+]"), "")

        if (cleanNumber.isEmpty() || cleanPrefix.isEmpty()) return false

        // 1. Direct match (e.g. +9179... matches +9179...)
        if (cleanNumber.startsWith(cleanPrefix)) return true

        // 2. Direct match with '+' added to prefix (e.g. +9179... matches 9179...)
        if (cleanNumber.startsWith("+$cleanPrefix")) return true

        val digitsOnlyNumber = cleanNumber.replace("+", "")
        val digitsOnlyPrefix = cleanPrefix.replace("+", "")

        // 3. Digits match directly
        if (digitsOnlyNumber.startsWith(digitsOnlyPrefix)) return true

        // 4. Strip Indian country code (+91 or 91)
        if (digitsOnlyNumber.startsWith("91") && digitsOnlyNumber.length > 10) {
            val nationalNumber = digitsOnlyNumber.substring(2)
            if (nationalNumber.startsWith(digitsOnlyPrefix)) return true
        }

        // 5. Strip leading 0 (national trunk code)
        if (digitsOnlyNumber.startsWith("0")) {
            val nationalNumber = digitsOnlyNumber.substring(1)
            if (nationalNumber.startsWith(digitsOnlyPrefix)) return true
        }

        return false
    }
}
