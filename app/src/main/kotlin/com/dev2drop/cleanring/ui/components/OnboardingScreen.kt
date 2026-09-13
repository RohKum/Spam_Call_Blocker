package com.dev2drop.cleanring.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev2drop.cleanring.util.AppTranslations
import com.dev2drop.cleanring.util.PhoneNumberNormalizer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    isDefaultRole: Boolean,
    onRequestRole: () -> Unit,
    onCompleteOnboarding: (firstRulePattern: String, countryCode: String) -> Unit,
    isRevisit: Boolean = false,
    modifier: Modifier = Modifier,
    lang: String = "en"
) {
    var step by remember { mutableIntStateOf(1) }
    var firstPattern by remember { mutableStateOf("140") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Dots
            Row(modifier = Modifier.padding(bottom = 32.dp)) {
                repeat(4) { index ->
                    val active = (index + 1) <= step
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(height = 8.dp, width = if (index + 1 == step) 24.dp else 8.dp)
                            .background(
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            AnimatedContent(targetState = step, label = "OnboardingStep") { currentStep ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (currentStep) {
                        1 -> WelcomeStep(lang = lang, onNext = { step = 2 })
                        2 -> PrivacyStep(lang = lang, onNext = { step = 3 })
                        3 -> PermissionStep(
                            isDefaultRole = isDefaultRole,
                            onRequestRole = onRequestRole,
                            lang = lang,
                            onNext = { step = 4 }
                        )
                        4 -> FirstRuleStep(
                            firstPattern = firstPattern,
                            onPatternChange = { firstPattern = it },
                            isRevisit = isRevisit,
                            lang = lang,
                            onComplete = { countryIso -> onCompleteOnboarding(firstPattern, countryIso) },
                            onSkip = { onCompleteOnboarding("", "") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(lang: String, onNext: () -> Unit) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp).size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = tr("welcome_title"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = tr("welcome_desc"),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(tr("get_started"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PrivacyStep(lang: String, onNext: () -> Unit) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(16.dp).size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = tr("privacy_title"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                PrivacyItem(title = tr("no_contacts_upload"), desc = tr("no_contacts_desc"))
                Spacer(modifier = Modifier.height(16.dp))
                PrivacyItem(title = tr("no_cloud_tracking"), desc = tr("no_cloud_desc"))
                Spacer(modifier = Modifier.height(16.dp))
                PrivacyItem(title = tr("no_account"), desc = tr("no_account_desc"))
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(tr("continue_btn"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PrivacyItem(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionStep(
    isDefaultRole: Boolean,
    onRequestRole: () -> Unit,
    lang: String,
    onNext: () -> Unit
) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.PhoneInTalk,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(16.dp).size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = tr("setup_title"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = tr("setup_desc"),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isDefaultRole) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(tr("role_active"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        } else {
            Button(
                onClick = onRequestRole,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(tr("enable_screening"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        TextButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isDefaultRole) tr("next_step") else tr("skip_for_now"), fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirstRuleStep(
    firstPattern: String,
    onPatternChange: (String) -> Unit,
    isRevisit: Boolean,
    lang: String,
    onComplete: (countryIso: String) -> Unit,
    onSkip: () -> Unit
) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    val context = LocalContext.current
    val countryIso = remember { PhoneNumberNormalizer.detectCountryIso(context) }
    val callingCodeInt = remember(countryIso) { PhoneNumberNormalizer.getCountryCallingCode(countryIso) }
    val selectedCallingCode = remember(callingCodeInt) { if (callingCodeInt > 0) "+$callingCodeInt" else "" }

    val sanitizedPattern = remember(firstPattern, countryIso) {
        PhoneNumberNormalizer.sanitizePatternForCountry(firstPattern, countryIso)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp).size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = tr("first_rule_title"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = tr("first_rule_desc"),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        FlowRow(modifier = Modifier.fillMaxWidth()) {
            val presets = listOf("140" to "Telemarketers", "7970" to "Series", "4085" to "Series")
            presets.forEach { (pattern, desc) ->
                SuggestionChip(
                    onClick = { onPatternChange(pattern) },
                    label = { Text("$pattern ($desc)") },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = firstPattern,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it == '+' }) {
                    onPatternChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            prefix = {
                if (selectedCallingCode.isNotBlank()) {
                    Text(
                        text = "$selectedCallingCode ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            label = { Text(tr("prefix_pattern")) },
            placeholder = { Text("e.g. 140 or 7970") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                onComplete(countryIso)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = sanitizedPattern.isNotBlank()
        ) {
            Text(tr("start_protection"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (isRevisit) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tr("skip_for_now"), fontSize = 15.sp)
            }
        }
    }
}
