package com.dev2drop.cleanring.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import com.dev2drop.cleanring.util.PhoneNumberNormalizer

@Composable
fun AddEditRuleDialog(
    initialRule: RuleEntity? = null,
    prefillPattern: String = "",
    prefillType: String = RuleType.PREFIX,
    existingRules: List<RuleEntity> = emptyList(),
    onDismissRequest: () -> Unit,
    onConfirm: (pattern: String, type: String, action: String, label: String, countryCode: String) -> Unit,
    lang: String = "en"
) {
    val context = LocalContext.current
    val detectedCountryIso = remember { PhoneNumberNormalizer.detectCountryIso(context) }
    val countryCode = remember(initialRule, detectedCountryIso) { initialRule?.countryCode ?: detectedCountryIso }

    val countryFlagEmoji = remember(countryCode) { PhoneNumberNormalizer.countryIsoToFlagEmoji(countryCode) }
    val callingCodeInt = remember(countryCode) { PhoneNumberNormalizer.getCountryCallingCode(countryCode) }
    val selectedCallingCode = remember(callingCodeInt) { if (callingCodeInt > 0) "+$callingCodeInt" else "" }

    var rawPatternText by remember { mutableStateOf(initialRule?.pattern ?: prefillPattern) }
    var ruleType by remember { mutableStateOf(initialRule?.type ?: prefillType) }
    var ruleAction by remember { mutableStateOf(initialRule?.action ?: RuleAction.BLOCK) }

    val focusRequester = remember { FocusRequester() }

    val sanitizedPattern = remember(rawPatternText, countryCode) {
        PhoneNumberNormalizer.sanitizePatternForCountry(rawPatternText, countryCode)
    }

    val cleanPatternText = remember(sanitizedPattern) { sanitizedPattern.replace(Regex("[^0-9+]"), "") }
    val isAlreadyExists = remember(cleanPatternText, existingRules, initialRule) {
        if (cleanPatternText.isBlank()) false
        else {
            existingRules.any { rule ->
                rule.id != (initialRule?.id ?: -1L) &&
                        rule.pattern.replace(Regex("[^0-9+]"), "") == cleanPatternText
            }
        }
    }

    val validationResult = remember(sanitizedPattern, countryCode, ruleType) {
        PhoneNumberNormalizer.validateForCountry(sanitizedPattern, countryCode, ruleType)
    }

    val exampleMatches = remember(sanitizedPattern, countryCode, ruleType) {
        if (sanitizedPattern.isBlank()) emptyList()
        else if (ruleType == RuleType.EXACT) {
            listOf(PhoneNumberNormalizer.formatForDisplay(sanitizedPattern, countryCode))
        } else {
            val clean = sanitizedPattern.trim()
            listOf(
                PhoneNumberNormalizer.formatForDisplay("${clean}0123", countryCode),
                PhoneNumberNormalizer.formatForDisplay("${clean}4567", countryCode)
            ).filter { it.isNotBlank() }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (initialRule != null) "Edit Blocking Rule" else "Block Unwanted Calls",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Country Display
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Country:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$countryFlagEmoji $countryCode (+$callingCodeInt)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Rule Scope Selector
                Text(
                    text = "How to Block:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                ) {
                    FilterChip(
                        selected = ruleType == RuleType.PREFIX,
                        onClick = { ruleType = RuleType.PREFIX },
                        label = { Text("Starting Digits") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = ruleType == RuleType.EXACT,
                        onClick = { ruleType = RuleType.EXACT },
                        label = { Text("Full Number") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rule Action Selector
                Text(
                    text = "Action:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                ) {
                    FilterChip(
                        selected = ruleAction == RuleAction.BLOCK,
                        onClick = { ruleAction = RuleAction.BLOCK },
                        label = { Text("🚫 Block") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = ruleAction == RuleAction.ALLOW,
                        onClick = { ruleAction = RuleAction.ALLOW },
                        label = { Text("✅ Always Allow") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pattern Input with Calling Code Prefix
                OutlinedTextField(
                    value = rawPatternText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() || it == '+' }) {
                            rawPatternText = newValue
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    prefix = {
                        if (selectedCallingCode.isNotBlank()) {
                            Text(
                                text = "$selectedCallingCode ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    label = { Text(if (ruleType == RuleType.PREFIX) "Starting Digits" else "Full Phone Number") },
                    placeholder = { Text(if (ruleType == RuleType.PREFIX) "e.g. 140 or 7970" else "e.g. 7912345678") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null
                        )
                    },
                    isError = (!validationResult.isValid || isAlreadyExists) && rawPatternText.isNotBlank(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (sanitizedPattern.isNotBlank() && validationResult.isValid && !isAlreadyExists) {
                                onConfirm(sanitizedPattern, ruleType, ruleAction, "", countryCode)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Already Exists Error Message
                if (isAlreadyExists && rawPatternText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ This number or pattern is already in your blocklist.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (!validationResult.isValid && rawPatternText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = validationResult.errorMessage ?: "Please enter valid digits",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Example Numbers Preview
                if (exampleMatches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Example Blocked Callers:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    exampleMatches.forEach { ex ->
                        Text(
                            text = "• $ex",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sanitizedPattern.isNotBlank() && validationResult.isValid && !isAlreadyExists) {
                        onConfirm(sanitizedPattern, ruleType, ruleAction, "", countryCode)
                    }
                },
                enabled = sanitizedPattern.isNotBlank() && validationResult.isValid && !isAlreadyExists,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialRule != null) "Save Rule" else "Block Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
