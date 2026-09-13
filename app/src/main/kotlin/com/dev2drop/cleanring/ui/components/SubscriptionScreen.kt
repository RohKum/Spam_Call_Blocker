package com.dev2drop.cleanring.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dev2drop.cleanring.util.AppTranslations
import com.dev2drop.cleanring.util.PhoneNumberNormalizer

@Composable
fun SubscriptionScreen(
    trialDaysRemaining: Int,
    isTrialActive: Boolean,
    monthlyPriceFormatted: String = "",
    yearlyPriceFormatted: String = "",
    onStartTrialOrSubscribe: (isYearly: Boolean) -> Unit,
    onRestorePurchases: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    lang: String = "en"
) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    val context = LocalContext.current
    val countryIso = remember { PhoneNumberNormalizer.detectCountryIso(context) }
    val isIndia = remember(countryIso) { countryIso.equals("IN", ignoreCase = true) }

    var selectedPlanIsYearly by remember { mutableStateOf(true) }

    val monthlyPriceText = if (monthlyPriceFormatted.isNotBlank()) monthlyPriceFormatted else (if (isIndia) "₹29" else "$1.99")
    val yearlyPriceText = if (yearlyPriceFormatted.isNotBlank()) yearlyPriceFormatted else (if (isIndia) "₹149" else "$9.99")

    Dialog(
        onDismissRequest = { if (isTrialActive) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = isTrialActive,
            dismissOnClickOutside = isTrialActive
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button top right (Only visible while trial is active)
                if (isTrialActive) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Header Shield Icon
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(14.dp)
                            .size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tr("upgrade_premium"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isTrialActive) {
                        tr("trial_days_left").replace("%d", "$trialDaysRemaining")
                    } else {
                        tr("trial_expired")
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isTrialActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Highlights
                Column(modifier = Modifier.fillMaxWidth()) {
                    PaywallFeatureItem("🔒 100% On-Device Privacy Guarantee")
                    Spacer(modifier = Modifier.height(8.dp))
                    PaywallFeatureItem("🚫 Unlimited Prefix & Exact Number Blocking")
                    Spacer(modifier = Modifier.height(8.dp))
                    PaywallFeatureItem("🚨 Instant Emergency Master Pause Controls")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Yearly Plan Box (Best Value)
                PlanCard(
                    title = tr("subscribe_yearly"),
                    price = if (yearlyPriceText.contains("/") || yearlyPriceText.contains("per")) yearlyPriceText else "$yearlyPriceText ${tr("per_year")}",
                    subtitle = if (isTrialActive) "Renews after 14-day trial" else "Cancel anytime",
                    isSelected = selectedPlanIsYearly,
                    isBestValue = true,
                    onClick = { selectedPlanIsYearly = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Monthly Plan Box
                PlanCard(
                    title = tr("subscribe_monthly"),
                    price = if (monthlyPriceText.contains("/") || monthlyPriceText.contains("per")) monthlyPriceText else "$monthlyPriceText ${tr("per_month")}",
                    subtitle = if (isTrialActive) "Renews after 14-day trial" else "Cancel anytime",
                    isSelected = !selectedPlanIsYearly,
                    isBestValue = false,
                    onClick = { selectedPlanIsYearly = false }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onStartTrialOrSubscribe(selectedPlanIsYearly) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isTrialActive) tr("start_free_trial") else tr("subscribe_now"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tr("cancel_anytime"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onRestorePurchases) {
                    Text(
                        text = tr("restore_purchases"),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallFeatureItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    subtitle: String,
    isSelected: Boolean,
    isBestValue: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (isBestValue) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "★ BEST VALUE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}
