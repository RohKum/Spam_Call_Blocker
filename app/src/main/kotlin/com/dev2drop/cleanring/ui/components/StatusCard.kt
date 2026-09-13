package com.dev2drop.cleanring.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev2drop.cleanring.ui.theme.GreenCardDark
import com.dev2drop.cleanring.ui.theme.GreenCardLight
import com.dev2drop.cleanring.ui.theme.GreenTextDark
import com.dev2drop.cleanring.ui.theme.GreenTextLight
import com.dev2drop.cleanring.ui.theme.RedCardDark
import com.dev2drop.cleanring.ui.theme.RedCardLight
import com.dev2drop.cleanring.ui.theme.RedTextDark
import com.dev2drop.cleanring.ui.theme.RedTextLight
import com.dev2drop.cleanring.util.AppTranslations

@Composable
fun StatusCard(
    isDefaultRole: Boolean,
    activeRulesCount: Int = 0,
    blockedCallsCount: Int = 0,
    screenedCallsCount: Int = 0,
    modifier: Modifier = Modifier,
    lang: String = "en",
    onEnableProtectionClick: () -> Unit
) {
    if (isDefaultRole) {
        ActiveProtectionCard(
            activeRulesCount = activeRulesCount,
            blockedCallsCount = blockedCallsCount,
            screenedCallsCount = screenedCallsCount,
            lang = lang,
            modifier = modifier
        )
    } else {
        ActionRequiredCard(
            lang = lang,
            onEnableProtectionClick = onEnableProtectionClick,
            modifier = modifier
        )
    }
}

@Composable
private fun ActiveProtectionCard(
    activeRulesCount: Int,
    blockedCallsCount: Int,
    screenedCallsCount: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val containerColor = if (isDark) GreenCardDark else GreenCardLight
    val contentColor = if (isDark) GreenTextDark else GreenTextLight

    fun tr(key: String): String = AppTranslations.getString(key, lang)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = contentColor.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = tr("status_protected"),
                        tint = contentColor,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr("status_protected"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tr("status_protected_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = contentColor.copy(alpha = 0.14f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$blockedCallsCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Spam Blocked",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = contentColor.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = contentColor.copy(alpha = 0.14f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$screenedCallsCount",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tr("calls_screened"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = contentColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRequiredCard(
    lang: String,
    onEnableProtectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val containerColor = if (isDark) RedCardDark else RedCardLight
    val contentColor = if (isDark) RedTextDark else RedTextLight

    fun tr(key: String): String = AppTranslations.getString(key, lang)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = tr("action_required"),
                    tint = contentColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tr("action_required"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tr("action_required_desc"),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.92f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            FilledTonalButton(
                onClick = onEnableProtectionClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = contentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("enable_protection"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
