package com.dev2drop.cleanring.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberNormalizerTest {

    @Test
    fun testCountryCallingCodes() {
        assertEquals(1, PhoneNumberNormalizer.getCountryCallingCode("US"))
        assertEquals(91, PhoneNumberNormalizer.getCountryCallingCode("IN"))
        assertEquals(44, PhoneNumberNormalizer.getCountryCallingCode("GB"))
        assertEquals(49, PhoneNumberNormalizer.getCountryCallingCode("DE"))
    }

    @Test
    fun testUSPhoneNumberMatching() {
        val region = "US"
        val incomingE164 = "+14085551234"

        // Direct national area code prefix
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "408", region))
        // Direct E.164 prefix
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "+1408", region))
        // Digits with country code
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "1408", region))
        // Mismatched prefix
        assertFalse(PhoneNumberNormalizer.isMatch(incomingE164, "650", region))
    }

    @Test
    fun testIndiaPhoneNumberMatching() {
        val region = "IN"
        val incomingE164 = "+917912345678"
        val incomingNational = "07912345678"

        // Local trunk code prefix "079"
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "079", region))
        // National prefix "79"
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "79", region))
        // E.164 prefix "+9179"
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "+9179", region))
        // Digits with country code "9179"
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "9179", region))

        // Local national caller matching international prefix "+9179"
        assertTrue(PhoneNumberNormalizer.isMatch(incomingNational, "+9179", region))
        assertTrue(PhoneNumberNormalizer.isMatch(incomingNational, "079", region))

        // Mismatched prefix
        assertFalse(PhoneNumberNormalizer.isMatch(incomingE164, "987", region))
    }

    @Test
    fun testUKPhoneNumberMatching() {
        val region = "GB"
        val incomingE164 = "+442079460912"
        val incomingNational = "02079460912"

        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "020", region))
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "20", region))
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "+4420", region))

        assertTrue(PhoneNumberNormalizer.isMatch(incomingNational, "+4420", region))
        assertFalse(PhoneNumberNormalizer.isMatch(incomingE164, "0161", region))
    }

    @Test
    fun testGermanyPhoneNumberMatching() {
        val region = "DE"
        val incomingE164 = "+49301234567"

        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "030", region))
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "30", region))
        assertTrue(PhoneNumberNormalizer.isMatch(incomingE164, "+4930", region))
        assertFalse(PhoneNumberNormalizer.isMatch(incomingE164, "089", region))
    }

    @Test
    fun testFormatForDisplay() {
        val usFormatted = PhoneNumberNormalizer.formatForDisplay("+14085551234", "US")
        assertTrue(usFormatted.contains("408"))

        val inFormatted = PhoneNumberNormalizer.formatForDisplay("+917912345678", "IN")
        assertTrue(inFormatted.contains("79"))

        val ukFormatted = PhoneNumberNormalizer.formatForDisplay("+442079460912", "GB")
        assertTrue(ukFormatted.contains("20"))
    }
}
