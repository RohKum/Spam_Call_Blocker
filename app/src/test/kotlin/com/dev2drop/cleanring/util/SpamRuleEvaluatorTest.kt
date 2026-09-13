package com.dev2drop.cleanring.util

import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpamRuleEvaluatorTest {

    @Test
    fun testPrefixBlockRule() {
        val rules = listOf(
            RuleEntity(pattern = "140", type = RuleType.PREFIX, action = RuleAction.BLOCK)
        )
        val result = SpamRuleEvaluator.evaluate("+14085551234", rules, "US")

        assertTrue(result.isSpam)
        assertEquals("BLOCKED", result.action)
        assertEquals(RuleType.PREFIX, result.matchedRuleType)
        assertEquals("140", result.matchedPattern)
    }

    @Test
    fun testExactBlockRule() {
        val rules = listOf(
            RuleEntity(pattern = "+14085551234", type = RuleType.EXACT, action = RuleAction.BLOCK)
        )
        val matchResult = SpamRuleEvaluator.evaluate("+14085551234", rules, "US")
        assertTrue(matchResult.isSpam)

        val nonMatchResult = SpamRuleEvaluator.evaluate("+14085559999", rules, "US")
        assertFalse(nonMatchResult.isSpam)
    }

    @Test
    fun testAllowExceptionOverridesBlockRule() {
        val rules = listOf(
            RuleEntity(pattern = "140", type = RuleType.PREFIX, action = RuleAction.BLOCK),
            RuleEntity(pattern = "+14085551234", type = RuleType.EXACT, action = RuleAction.ALLOW)
        )

        // Exact allowed exception should NOT be blocked even if it matches prefix "140"
        val exceptionResult = SpamRuleEvaluator.evaluate("+14085551234", rules, "US")
        assertFalse(exceptionResult.isSpam)
        assertEquals("ALLOWED", exceptionResult.action)

        // Other numbers matching prefix "140" should still be blocked
        val blockedResult = SpamRuleEvaluator.evaluate("+14085559999", rules, "US")
        assertTrue(blockedResult.isSpam)
    }

    @Test
    fun testPausedRuleIsIgnored() {
        val rules = listOf(
            RuleEntity(pattern = "140", type = RuleType.PREFIX, action = RuleAction.BLOCK, isEnabled = false)
        )
        val result = SpamRuleEvaluator.evaluate("+14085551234", rules, "US")

        assertFalse(result.isSpam)
        assertEquals("ALLOWED", result.action)
    }
}
