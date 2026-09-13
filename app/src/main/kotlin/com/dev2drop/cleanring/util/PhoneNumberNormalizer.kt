package com.dev2drop.cleanring.util

import android.content.Context
import android.telephony.TelephonyManager
import com.dev2drop.cleanring.data.RuleType
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import java.util.Locale

data class NormalizedPattern(
    val e164Form: String,
    val nationalForm: String,
    val formattedDisplay: String,
    val callingCode: Int
)

data class PatternValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val warningMessage: String? = null
)

object PhoneNumberNormalizer {

    private val phoneUtil: PhoneNumberUtil by lazy {
        PhoneNumberUtil.getInstance()
    }

    /**
     * Converts a 2-letter country ISO (e.g. "US", "IN", "GB", "DE") to its Unicode flag emoji.
     * Works universally for all 200+ countries around the world!
     */
    fun countryIsoToFlagEmoji(countryIso: String): String {
        val iso = countryIso.trim().uppercase(Locale.ROOT)
        if (iso.length != 2) return "🌐"
        val firstChar = Character.codePointAt(iso, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(iso, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    /**
     * Strips leading '+' and redundant country calling code if the user selects a Country Region Scope
     * and manually types the country code (e.g., enters "+917912" or "917912" when IN (+91) is selected -> returns "7912").
     */
    fun sanitizePatternForCountry(rawPattern: String, countryIso: String): String {
        val trimmed = rawPattern.trim()
        if (countryIso.isBlank() || trimmed.isBlank()) return trimmed
        val callingCode = getCountryCallingCode(countryIso).toString()
        var clean = trimmed
        if (clean.startsWith("+")) {
            clean = clean.substring(1)
        }
        if (callingCode.isNotEmpty() && clean.startsWith(callingCode) && clean.length > callingCode.length) {
            clean = clean.substring(callingCode.length)
        }
        return clean
    }

    /**
     * Returns a list of all supported countries with their flag emoji, ISO code, and calling code.
     * Keeps the detected user country as the first country item right after Global!
     */
    fun getAllSupportedCountries(detectedCountryIso: String = ""): List<Pair<String, String>> {
        val list = mutableListOf("" to "🌍 Global (All Countries)")
        val detectedUpper = detectedCountryIso.trim().uppercase(Locale.ROOT)

        if (detectedUpper.length == 2 && phoneUtil.supportedRegions.contains(detectedUpper)) {
            val callingCode = getCountryCallingCode(detectedUpper)
            val flag = countryIsoToFlagEmoji(detectedUpper)
            list.add(detectedUpper to "$flag $detectedUpper (+$callingCode) - Your Region")
        }

        val regions = phoneUtil.supportedRegions.sorted()
        for (region in regions) {
            if (region != detectedUpper) {
                val callingCode = getCountryCallingCode(region)
                val flag = countryIsoToFlagEmoji(region)
                list.add(region to "$flag $region (+$callingCode)")
            }
        }
        return list
    }

    /**
     * Dynamically detects the user's country ISO code (2-letter ISO, e.g. "US", "IN", "GB").
     * Uses TelephonyManager network country ISO -> SIM country ISO -> System Locale -> Default "IN" / "US".
     */
    fun detectCountryIso(context: Context): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val networkCountry = telephonyManager?.networkCountryIso?.trim()?.uppercase(Locale.ROOT)
            if (!networkCountry.isNullOrEmpty() && (networkCountry.length == 2)) {
                return networkCountry
            }

            val simCountry = telephonyManager?.simCountryIso?.trim()?.uppercase(Locale.ROOT)
            if (!simCountry.isNullOrEmpty() && (simCountry.length == 2)) {
                return simCountry
            }

            val localeCountry = Locale.getDefault().country.trim().uppercase(Locale.ROOT)
            if (localeCountry.isNotEmpty() && (localeCountry.length == 2)) {
                return localeCountry
            }

            val localeLang = Locale.getDefault().language.trim().lowercase(Locale.ROOT)
            if (localeLang == "hi" || localeCountry.contains("IN")) {
                return "IN"
            }

            "US"
        } catch (_: Exception) {
            "US"
        }
    }

    /**
     * Gets country calling code for a given ISO region (e.g. "US" -> 1, "IN" -> 91, "GB" -> 44).
     */
    fun getCountryCallingCode(countryIso: String): Int {
        val iso = if (countryIso.isBlank()) "US" else countryIso.uppercase(Locale.ROOT)
        return phoneUtil.getCountryCodeForRegion(iso)
    }

    /**
     * Generates normalized E.164 and National forms for a phone pattern or exact number.
     */
    fun getNormalizedPattern(rawInput: String, countryIso: String = "US"): NormalizedPattern {
        val trimmed = rawInput.trim()
        val iso = if (countryIso.isBlank()) "US" else countryIso.uppercase(Locale.ROOT)
        val callingCode = getCountryCallingCode(iso)

        if (trimmed.isEmpty()) {
            return NormalizedPattern(e164Form = "", nationalForm = "", formattedDisplay = "", callingCode = callingCode)
        }

        return try {
            val parsed: PhoneNumber = phoneUtil.parse(trimmed, iso)
            val e164 = phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
            val national = phoneUtil.getNationalSignificantNumber(parsed)
            val formatted = phoneUtil.format(parsed, if (trimmed.startsWith("+")) PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL else PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            val code = parsed.countryCode

            NormalizedPattern(
                e164Form = e164,
                nationalForm = national,
                formattedDisplay = formatted,
                callingCode = if (code > 0) code else callingCode
            )
        } catch (_: Exception) {
            val digits = trimmed.replace(Regex("[^0-9+]"), "")
            val cleanDigits = digits.replace("+", "")
            NormalizedPattern(
                e164Form = if (digits.startsWith("+")) digits else "+$callingCode$cleanDigits",
                nationalForm = cleanDigits,
                formattedDisplay = formatPrefixFallback(trimmed, iso),
                callingCode = callingCode
            )
        }
    }

    /**
     * Validates a pattern or phone number for a specific region using libphonenumber constraints.
     */
    fun validateForCountry(
        rawInput: String,
        countryIso: String = "US",
        ruleType: String = RuleType.PREFIX
    ): PatternValidationResult {
        val trimmed = rawInput.trim()
        val iso = if (countryIso.isBlank()) "US" else countryIso.uppercase(Locale.ROOT)
        val callingCode = getCountryCallingCode(iso)

        if (trimmed.isEmpty()) {
            return PatternValidationResult(isValid = false, errorMessage = "Pattern cannot be empty")
        }

        val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")
        if (digitsOnly.isEmpty()) {
            return PatternValidationResult(isValid = false, errorMessage = "Pattern must contain digits")
        }

        if (ruleType == RuleType.EXACT) {
            return try {
                val parsed = phoneUtil.parse(trimmed, iso)
                val validationReason = phoneUtil.isPossibleNumberWithReason(parsed)
                when (validationReason) {
                    PhoneNumberUtil.ValidationResult.TOO_SHORT -> PatternValidationResult(isValid = false, errorMessage = "Number is too short for region $iso (+$callingCode)")
                    PhoneNumberUtil.ValidationResult.TOO_LONG -> PatternValidationResult(isValid = false, errorMessage = "Number is too long for region $iso (+$callingCode)")
                    PhoneNumberUtil.ValidationResult.INVALID_COUNTRY_CODE -> PatternValidationResult(isValid = false, errorMessage = "Invalid country code in pattern")
                    else -> {
                        if (phoneUtil.isValidNumber(parsed) || phoneUtil.isPossibleNumber(parsed)) {
                            PatternValidationResult(isValid = true)
                        } else {
                            PatternValidationResult(isValid = false, errorMessage = "Invalid phone number format for region $iso (+$callingCode)")
                        }
                    }
                }
            } catch (_: Exception) {
                if (digitsOnly.length < 7) {
                    PatternValidationResult(isValid = false, errorMessage = "Exact number is too short for region $iso (expected 7-15 digits)")
                } else if (digitsOnly.length > 15) {
                    PatternValidationResult(isValid = false, errorMessage = "Exact number exceeds maximum international length (15 digits)")
                } else {
                    PatternValidationResult(isValid = true)
                }
            }
        } else { // PREFIX Rule
            if (digitsOnly.length < 2) {
                return PatternValidationResult(isValid = false, errorMessage = "Prefix pattern must contain at least 2 digits")
            }
            if (digitsOnly.length > 15) {
                return PatternValidationResult(isValid = false, errorMessage = "Prefix pattern exceeds maximum length (15 digits)")
            }

            var warning: String? = null
            if (digitsOnly.length <= 3) {
                warning = "⚠️ Broad Rule Warning: Prefix '$trimmed' will match millions of numbers in region $iso (+$callingCode)"
            }
            return PatternValidationResult(isValid = true, warningMessage = warning)
        }
    }

    /**
     * Formats a raw number or prefix for display in international or national presentation.
     */
    fun formatForDisplay(rawInput: String, userCountryIso: String = "US"): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return ""

        return try {
            val iso = if (userCountryIso.isBlank()) "US" else userCountryIso.uppercase(Locale.ROOT)
            val parsedNumber: PhoneNumber = phoneUtil.parse(trimmed, iso)
            if (phoneUtil.isValidNumber(parsedNumber) || phoneUtil.isPossibleNumber(parsedNumber)) {
                if (trimmed.startsWith("+")) {
                    phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
                } else {
                    phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                }
            } else {
                formatPrefixFallback(trimmed, iso)
            }
        } catch (_: Exception) {
            formatPrefixFallback(trimmed, userCountryIso)
        }
    }

    private fun formatPrefixFallback(rawInput: String, userCountryIso: String): String {
        val clean = rawInput.replace(Regex("[^0-9+]"), "")
        if (clean.startsWith("+")) {
            val callingCode = getCountryCallingCode(userCountryIso)
            if (clean.startsWith("+$callingCode")) {
                val rest = clean.substring(1 + callingCode.toString().length)
                return "+$callingCode $rest".trim()
            }
            return clean
        }
        return clean
    }

    /**
     * Checks whether an incoming phone number matches a blocked prefix in a country-aware manner.
     */
    fun isMatch(
        rawIncomingNumber: String,
        blockedPrefix: String,
        userCountryIso: String = "US",
    ): Boolean {
        if (rawIncomingNumber.isBlank() || blockedPrefix.isBlank()) return false

        val cleanIncoming = rawIncomingNumber.replace(Regex("[^0-9+]"), "")
        val cleanPrefix = blockedPrefix.replace(Regex("[^0-9+]"), "")

        if (cleanIncoming.isEmpty() || cleanPrefix.isEmpty()) return false

        // 1. Direct string prefix match
        if (cleanIncoming.startsWith(cleanPrefix)) return true
        if (cleanIncoming.startsWith("+$cleanPrefix")) return true

        val digitsIncoming = cleanIncoming.replace("+", "")
        val digitsPrefix = cleanPrefix.replace("+", "")

        // 2. Direct digits prefix match
        if (digitsIncoming.startsWith(digitsPrefix)) return true

        // 3. Trunk code / leading zero handling (e.g., prefix "079" vs incoming "+9179..." or "79...")
        val prefixWithoutZero = digitsPrefix.trimStart('0')
        if (prefixWithoutZero.isNotEmpty()) {
            if (digitsIncoming.startsWith(prefixWithoutZero)) return true
        }

        // 4. Libphonenumber structural parsing and E.164 / National normalization
        val iso = if (userCountryIso.isBlank()) "US" else userCountryIso.uppercase(Locale.ROOT)
        try {
            val parsedIncoming = phoneUtil.parse(cleanIncoming, iso)
            val e164Incoming = phoneUtil.format(parsedIncoming, PhoneNumberUtil.PhoneNumberFormat.E164)
            val nationalIncoming = phoneUtil.getNationalSignificantNumber(parsedIncoming)
            val callingCode = parsedIncoming.countryCode

            // Check E.164 incoming against prefix
            if (e164Incoming.startsWith(cleanPrefix) || e164Incoming.startsWith("+$digitsPrefix")) return true

            // Check national number against prefix or prefix without leading zero
            if (nationalIncoming.startsWith(digitsPrefix)) return true
            if (prefixWithoutZero.isNotEmpty() && nationalIncoming.startsWith(prefixWithoutZero)) return true

            // If prefix has calling code (e.g. "9179" or "+9179"), check against e164 digits
            val e164Digits = e164Incoming.replace("+", "")
            if (e164Digits.startsWith(digitsPrefix)) return true

            // If digitsIncoming starts with caller's country code, strip country code and check national digits
            val callingCodeStr = callingCode.toString()
            if (digitsIncoming.startsWith(callingCodeStr) && (digitsIncoming.length > callingCodeStr.length)) {
                val nationalPart = digitsIncoming.substring(callingCodeStr.length)
                if (nationalPart.startsWith(digitsPrefix)) return true
                if (prefixWithoutZero.isNotEmpty() && nationalPart.startsWith(prefixWithoutZero)) return true
            }
        } catch (_: Exception) {
            // Parsing fallback: strip country code if digits begin with user's region calling code
            val countryCode = getCountryCallingCode(iso).toString()
            if (countryCode.isNotEmpty() && digitsIncoming.startsWith(countryCode) && (digitsIncoming.length > countryCode.length)) {
                val nationalPart = digitsIncoming.substring(countryCode.length)
                if (nationalPart.startsWith(digitsPrefix)) return true
                if (prefixWithoutZero.isNotEmpty() && nationalPart.startsWith(prefixWithoutZero)) return true
            }
        }

        return false
    }
}
