package com.dev2drop.cleanring.util

import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryScopedRuleTest {

    @Test
    fun testCountryFlagEmoji() {
        assertEquals("🇺🇸", PhoneNumberNormalizer.countryIsoToFlagEmoji("US"))
        assertEquals("🇮🇳", PhoneNumberNormalizer.countryIsoToFlagEmoji("IN"))
        assertEquals("🇬🇧", PhoneNumberNormalizer.countryIsoToFlagEmoji("GB"))
        assertEquals("🇩🇪", PhoneNumberNormalizer.countryIsoToFlagEmoji("DE"))
        assertEquals("🇯🇵", PhoneNumberNormalizer.countryIsoToFlagEmoji("JP"))
    }

    @Test
    fun testPatternSanitizerForSelectedCountry() {
        // When region IN (+91) is selected:
        assertEquals("7912", PhoneNumberNormalizer.sanitizePatternForCountry("+917912", "IN"))
        assertEquals("7912", PhoneNumberNormalizer.sanitizePatternForCountry("917912", "IN"))
        assertEquals("7912", PhoneNumberNormalizer.sanitizePatternForCountry("7912", "IN"))

        // When region US (+1) is selected:
        assertEquals("408555", PhoneNumberNormalizer.sanitizePatternForCountry("+1408555", "US"))
        assertEquals("408555", PhoneNumberNormalizer.sanitizePatternForCountry("1408555", "US"))
        assertEquals("408555", PhoneNumberNormalizer.sanitizePatternForCountry("408555", "US"))
    }

    @Test
    fun testCountryScopedRuleMatching() {
        val usRule = RuleEntity(
            pattern = "408",
            type = RuleType.PREFIX,
            action = RuleAction.BLOCK,
            countryCode = "US"
        )

        val inRule = RuleEntity(
            pattern = "79",
            type = RuleType.PREFIX,
            action = RuleAction.BLOCK,
            countryCode = "IN"
        )

        val rules = listOf(usRule, inRule)

        // US caller matching US rule
        val usResult = SpamRuleEvaluator.evaluate("+14085551234", rules, "US")
        assertTrue(usResult.isSpam)
        assertEquals("408", usResult.matchedPattern)

        // India caller matching IN rule
        val inResult = SpamRuleEvaluator.evaluate("+917912345678", rules, "IN")
        assertTrue(inResult.isSpam)
        assertEquals("79", inResult.matchedPattern)

        // India caller should NOT match US rule even if digits overlap
        val crossResult = SpamRuleEvaluator.evaluate("+914085551234", rules, "IN")
        assertFalse(crossResult.isSpam)
    }

    @Test
    fun testPatternNormalizationAndValidation() {
        val usValidation = PhoneNumberNormalizer.validateForCountry("14085551234", "US", RuleType.EXACT)
        assertTrue(usValidation.isValid)

        val shortValidation = PhoneNumberNormalizer.validateForCountry("12", "US", RuleType.EXACT)
        assertFalse(shortValidation.isValid)
        assertTrue(shortValidation.errorMessage?.contains("short") == true)

        val normalized = PhoneNumberNormalizer.getNormalizedPattern("+14085551234", "US")
        assertEquals("+14085551234", normalized.e164Form)
        assertEquals("4085551234", normalized.nationalForm)
    }
}
