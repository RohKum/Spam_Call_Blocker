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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.dev2drop.cleanring.util.AppTranslations
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

    fun tr(key: String): String = AppTranslations.getString(key, lang)

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

    val normalizedInfo = remember(sanitizedPattern, countryCode) {
        PhoneNumberNormalizer.getNormalizedPattern(sanitizedPattern, countryCode)
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
                text = if (initialRule != null) tr("edit_rule") else tr("create_rule"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Static Country Display
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("country_region"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$countryFlagEmoji $countryCode (+$callingCodeInt)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Rule Scope Selector
                Text(
                    text = tr("rule_scope"),
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
                        label = { Text(tr("prefix_series")) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = ruleType == RuleType.EXACT,
                        onClick = { ruleType = RuleType.EXACT },
                        label = { Text(tr("exact_number")) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rule Action Selector
                Text(
                    text = tr("rule_action"),
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
                        label = { Text(tr("block_calls")) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = ruleAction == RuleAction.ALLOW,
                        onClick = { ruleAction = RuleAction.ALLOW },
                        label = { Text(tr("allow_exception")) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pattern Input with Hard-Coded Country Code Prefix
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
                    label = { Text(if (ruleType == RuleType.PREFIX) tr("prefix_pattern") else tr("phone_number")) },
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
                        text = tr("duplicate_error"),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (!validationResult.isValid && rawPatternText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = validationResult.errorMessage ?: "Invalid pattern format",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Broadness Warning Alert
                if (validationResult.warningMessage != null && !isAlreadyExists) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = validationResult.warningMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Normalized Rule Form Preview Box
                if (sanitizedPattern.isNotBlank() && normalizedInfo.e164Form.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = tr("normalized_form"),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "E.164: ${normalizedInfo.e164Form} • National: ${normalizedInfo.nationalForm}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Example Numbers Preview
                if (exampleMatches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("example_matches"),
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
                Text(if (initialRule != null) tr("save") else tr("add_rule"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(tr("cancel"))
            }
        }
    )
}
