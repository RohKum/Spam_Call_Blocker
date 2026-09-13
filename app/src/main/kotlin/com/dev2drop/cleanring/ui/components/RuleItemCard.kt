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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import com.dev2drop.cleanring.util.PhoneNumberNormalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleItemCard(
    rule: RuleEntity,
    onToggleStatus: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val countryIso = remember { PhoneNumberNormalizer.detectCountryIso(context) }
    val formattedDisplay = remember(rule.pattern, countryIso) {
        PhoneNumberNormalizer.formatForDisplay(rule.pattern, countryIso)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Badge (Block vs Allow)
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (rule.action == RuleAction.BLOCK) {
                        if (rule.isEnabled) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    } else {
                        if (rule.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    }
                ) {
                    Icon(
                        imageVector = if (rule.action == RuleAction.BLOCK) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (rule.action == RuleAction.BLOCK) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (formattedDisplay.isNotBlank()) formattedDisplay else rule.pattern,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = if (rule.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Type Badge (Prefix vs Exact)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = if (rule.type == RuleType.PREFIX) "PREFIX" else "EXACT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        // Action Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (rule.action == RuleAction.BLOCK) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = if (rule.action == RuleAction.BLOCK) "BLOCK" else "ALLOW EXCEPTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (rule.action == RuleAction.BLOCK) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        // Block Counter Badge
                        if (rule.blockedCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = "🛡️ ${rule.blockedCount} Blocked",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (rule.label.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = rule.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Pause / Resume Toggle Switch
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggleStatus,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Toolbar (Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Rule",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Rule",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
