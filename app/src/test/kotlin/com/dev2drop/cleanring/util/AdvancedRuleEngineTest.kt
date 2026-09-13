package com.dev2drop.cleanring.util

import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AdvancedRuleEngineTest {

    @Test
    fun testEmergencyMasterSwitch() {
        val rules = listOf(
            RuleEntity(pattern = "140", type = RuleType.PREFIX, action = RuleAction.BLOCK)
        )

        // When emergency master switch is ON, blocking is disabled
        val emergencyResult = SpamRuleEvaluator.evaluate(
            rawIncomingNumber = "+14085551234",
            activeRules = rules,
            userCountryIso = "US",
            isEmergencyDisabled = true
        )

        assertFalse(emergencyResult.isSpam)
        assertEquals("ALLOWED_EMERGENCY", emergencyResult.action)
    }

    @Test
    fun testTemporaryRuleExpiry() {
        val now = System.currentTimeMillis()
        val expiredTime = now - 1000 // 1 second ago
        val futureTime = now + 3600000 // 1 hour from now

        val expiredRule = RuleEntity(
            pattern = "140",
            type = RuleType.PREFIX,
            action = RuleAction.BLOCK,
            expiryTimestamp = expiredTime
        )

        val activeRule = RuleEntity(
            pattern = "140",
            type = RuleType.PREFIX,
            action = RuleAction.BLOCK,
            expiryTimestamp = futureTime
        )

        assertFalse(SpamRuleEvaluator.isRuleActiveAtTime(expiredRule, now))
        assertTrue(SpamRuleEvaluator.isRuleActiveAtTime(activeRule, now))
    }

    @Test
    fun testScheduledHours() {
        val calendar = Calendar.getInstance()
        val currentDayCode = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> "MON"
        }

        val matchingDayRule = RuleEntity(
            pattern = "140",
            scheduleDays = currentDayCode
        )

        val nonMatchingDayRule = RuleEntity(
            pattern = "140",
            scheduleDays = "UNKNOWN_DAY"
        )

        assertTrue(SpamRuleEvaluator.isRuleActiveAtTime(matchingDayRule, System.currentTimeMillis()))
        assertFalse(SpamRuleEvaluator.isRuleActiveAtTime(nonMatchingDayRule, System.currentTimeMillis()))
    }
}
