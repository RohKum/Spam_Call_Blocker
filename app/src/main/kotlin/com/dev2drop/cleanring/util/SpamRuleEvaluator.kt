package com.dev2drop.cleanring.util

import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import java.util.Calendar
import java.util.Locale

data class RuleEvaluationResult(
    val isSpam: Boolean,
    val action: String,
    val matchedRuleType: String,
    val matchedPattern: String
)

object SpamRuleEvaluator {

    /**
     * Evaluates an incoming phone number against active rules, considering:
     * - Emergency Master Switch
     * - Rule Expiry (Temporary Blocking)
     * - Scheduled Active Hours & Days
     * - Country-Code Rule Scoping
     * - ALLOW rules (Exceptions take precedence over BLOCK rules)
     */
    fun evaluate(
        rawIncomingNumber: String,
        activeRules: List<RuleEntity>,
        userCountryIso: String = "US",
        isEmergencyDisabled: Boolean = false,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): RuleEvaluationResult {
        // 1. Emergency Master Switch override
        if (isEmergencyDisabled || rawIncomingNumber.isBlank() || activeRules.isEmpty()) {
            return RuleEvaluationResult(
                isSpam = false,
                action = if (isEmergencyDisabled) "ALLOWED_EMERGENCY" else "ALLOWED",
                matchedRuleType = "NONE",
                matchedPattern = ""
            )
        }

        // Filter rules that are enabled, not expired, and active according to schedule
        val validRules = activeRules.filter { rule ->
            rule.isEnabled && isRuleActiveAtTime(rule, currentTimeMillis)
        }

        // 2. Check ALLOW rules (Exceptions) first
        val allowRules = validRules.filter { it.action == RuleAction.ALLOW }
        for (rule in allowRules) {
            if (matches(rawIncomingNumber, rule, userCountryIso)) {
                return RuleEvaluationResult(
                    isSpam = false,
                    action = "ALLOWED",
                    matchedRuleType = "ALLOWLIST_${rule.type}",
                    matchedPattern = rule.pattern
                )
            }
        }

        // 3. Check BLOCK rules second
        val blockRules = validRules.filter { it.action == RuleAction.BLOCK }
        for (rule in blockRules) {
            if (matches(rawIncomingNumber, rule, userCountryIso)) {
                return RuleEvaluationResult(
                    isSpam = true,
                    action = "BLOCKED",
                    matchedRuleType = rule.type,
                    matchedPattern = rule.pattern
                )
            }
        }

        return RuleEvaluationResult(
            isSpam = false,
            action = "ALLOWED",
            matchedRuleType = "NONE",
            matchedPattern = ""
        )
    }

    /**
     * Checks if a rule is currently active based on temporary expiry and schedule settings.
     */
    fun isRuleActiveAtTime(rule: RuleEntity, currentTimeMillis: Long): Boolean {
        if (!rule.isEnabled) return false

        // Temporary Expiry check
        if (rule.expiryTimestamp > 0 && currentTimeMillis > rule.expiryTimestamp) {
            return false
        }

        // Schedule Days and Hours check
        if (rule.scheduleDays.isNotBlank() || (rule.scheduleStartHour >= 0 && rule.scheduleEndHour >= 0)) {
            val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
            
            // Check day of week
            if (rule.scheduleDays.isNotBlank()) {
                val currentDayCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "MON"
                    Calendar.TUESDAY -> "TUE"
                    Calendar.WEDNESDAY -> "WED"
                    Calendar.THURSDAY -> "THU"
                    Calendar.FRIDAY -> "FRI"
                    Calendar.SATURDAY -> "SAT"
                    Calendar.SUNDAY -> "SUN"
                    else -> ""
                }
                if (!rule.scheduleDays.uppercase(Locale.ROOT).contains(currentDayCode)) {
                    return false
                }
            }

            // Check active hours
            if (rule.scheduleStartHour in 0..23 && rule.scheduleEndHour in 0..23) {
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                if (rule.scheduleStartHour <= rule.scheduleEndHour) {
                    if (currentHour < rule.scheduleStartHour || currentHour > rule.scheduleEndHour) {
                        return false
                    }
                } else {
                    // Overnight window (e.g., 22:00 to 07:00)
                    if (currentHour < rule.scheduleStartHour && currentHour > rule.scheduleEndHour) {
                        return false
                    }
                }
            }
        }

        return true
    }

    private fun matches(
        rawIncomingNumber: String,
        rule: RuleEntity,
        userCountryIso: String
    ): Boolean {
        if (rule.pattern.isBlank()) return false

        val ruleCountry = rule.countryCode.uppercase(Locale.ROOT)
        val targetCountryIso = if (ruleCountry.isNotBlank()) ruleCountry else userCountryIso

        // If rule is scoped to a specific country code, verify caller region or calling code
        if (ruleCountry.isNotBlank()) {
            val ruleCallingCode = PhoneNumberNormalizer.getCountryCallingCode(ruleCountry)
            val cleanIncoming = rawIncomingNumber.replace(Regex("[^0-9+]"), "")

            if (cleanIncoming.startsWith("+")) {
                if (!cleanIncoming.startsWith("+$ruleCallingCode")) {
                    return false
                }
            } else if (userCountryIso.uppercase(Locale.ROOT) != ruleCountry) {
                // National incoming caller in a different region does not match rule scoped to ruleCountry
                return false
            }
        }

        return if (rule.type == RuleType.EXACT) {
            val cleanIncoming = rawIncomingNumber.replace(Regex("[^0-9+]"), "").replace("+", "")
            val cleanPattern = rule.pattern.replace(Regex("[^0-9+]"), "").replace("+", "")
            cleanIncoming == cleanPattern || PhoneNumberNormalizer.isMatch(rawIncomingNumber, rule.pattern, targetCountryIso)
        } else {
            PhoneNumberNormalizer.isMatch(rawIncomingNumber, rule.pattern, targetCountryIso)
        }
    }
}
