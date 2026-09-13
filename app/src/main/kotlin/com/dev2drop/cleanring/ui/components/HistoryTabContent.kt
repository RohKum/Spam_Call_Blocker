package com.dev2drop.cleanring.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev2drop.cleanring.data.CallLogEntity
import com.dev2drop.cleanring.data.LogAction
import com.dev2drop.cleanring.data.RuleType
import com.dev2drop.cleanring.util.AppTranslations
import com.dev2drop.cleanring.util.PhoneNumberNormalizer

@Composable
fun HistoryTabContent(
    logs: List<CallLogEntity>,
    onOneTapBlock: (phoneNumber: String, type: String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    lang: String = "en"
) {
    val context = LocalContext.current
    val countryIso = remember { PhoneNumberNormalizer.detectCountryIso(context) }
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${tr("call_history")} (${logs.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            if (logs.isNotEmpty()) {
                IconButton(onClick = onClearHistory) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = tr("clear_history"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = tr("no_history"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr("no_history_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp)
            ) {
                items(
                    items = logs,
                    key = { it.id }
                ) { log ->
                    val formatted = remember(log.phoneNumber, countryIso) {
                        PhoneNumberNormalizer.formatForDisplay(log.phoneNumber, countryIso)
                    }
                    val timeAgo = remember(log.timestamp) {
                        DateUtils.getRelativeTimeSpanString(
                            log.timestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = CircleShape,
                                    color = if (log.action == LogAction.BLOCKED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = if (log.action == LogAction.BLOCKED) Icons.Default.Block else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (log.action == LogAction.BLOCKED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (formatted.isNotBlank()) formatted else log.phoneNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Text(
                                        text = if (log.matchedPattern.isNotBlank()) {
                                            "${log.action} • Matched ${log.matchedPattern} ($timeAgo)"
                                        } else {
                                            "${log.action} • $timeAgo"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // One-Tap Block buttons
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onOneTapBlock(log.phoneNumber, RuleType.EXACT) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("block_number"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        val sanitized = PhoneNumberNormalizer.sanitizePatternForCountry(log.phoneNumber, countryIso)
                                        val cleanDigits = sanitized.replace(Regex("[^0-9]"), "").trimStart('0')
                                        val prefix4Digits = if (cleanDigits.length >= 4) cleanDigits.substring(0, 4) else cleanDigits
                                        onOneTapBlock(prefix4Digits, RuleType.PREFIX)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(tr("block_prefix"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
