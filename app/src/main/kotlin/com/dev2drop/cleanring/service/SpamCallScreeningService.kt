package com.dev2drop.cleanring.service

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.dev2drop.cleanring.SpamBlockerApp
import com.dev2drop.cleanring.util.PhoneNumberNormalizer
import com.dev2drop.cleanring.util.SpamRuleEvaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class SpamCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "CleanRingService"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawPhoneNumber = extractPhoneNumber(callDetails)
        val handleStr = callDetails.handle?.toString() ?: "null"
        val schemeSpecific = callDetails.handle?.schemeSpecificPart ?: "null"
        val userCountryIso = PhoneNumberNormalizer.detectCountryIso(this)

        Log.d(TAG, "=== ON SCREEN CALL FIRED ===")
        Log.d(TAG, "Raw extracted number: '$rawPhoneNumber', User country ISO: '$userCountryIso'")
        Log.d(TAG, "Handle string: '$handleStr', SchemeSpecific: '$schemeSpecific'")

        val repository = SpamBlockerApp.instance.spamRepository
        val isEmergencyDisabled = repository.isEmergencyDisabled()
        val isSubscribed = repository.isSubscribed()
        val isTrialActive = repository.isTrialActive()

        // 🔒 If trial has ended AND user is not subscribed, pause call screening and allow all calls!
        if (!isSubscribed && !isTrialActive) {
            Log.w(TAG, "🔒 Free Trial expired and user is not subscribed. Call screening paused (allowing call).")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val response = CallResponse.Builder().build()
                respondToCall(callDetails, response)
            }
            return
        }

        val activeRules = try {
            repository.getActiveRulesFast()
        } catch (_: Exception) {
            emptyList()
        }

        val eval = SpamRuleEvaluator.evaluate(
            rawIncomingNumber = rawPhoneNumber,
            activeRules = activeRules,
            userCountryIso = userCountryIso,
            isEmergencyDisabled = isEmergencyDisabled
        )
        Log.d(TAG, "Rule evaluation result: $eval")

        // Record call screening decision into history database & increment block counter if blocked
        serviceScope.launch {
            try {
                repository.logCallEvent(
                    phoneNumber = rawPhoneNumber,
                    action = eval.action,
                    matchedRuleType = eval.matchedRuleType,
                    matchedPattern = eval.matchedPattern
                )
                if (eval.isSpam && eval.matchedPattern.isNotBlank()) {
                    repository.incrementRuleBlockCount(eval.matchedPattern)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log call screening event", e)
            }
        }

        if (eval.isSpam) {
            Log.w(TAG, "🚫 BLOCKING CALL: '$rawPhoneNumber' matched rule pattern '${eval.matchedPattern}' (${eval.matchedRuleType})")
            showToast("🚫 Spam call blocked: $rawPhoneNumber (matched ${eval.matchedPattern})")

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
            Log.d(TAG, "✅ ALLOWING CALL: '$rawPhoneNumber'")
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
}
