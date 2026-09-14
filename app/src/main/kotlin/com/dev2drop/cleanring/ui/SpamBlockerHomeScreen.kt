package com.dev2drop.cleanring.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.ui.components.AddEditRuleDialog
import com.dev2drop.cleanring.ui.components.HistoryTabContent
import com.dev2drop.cleanring.ui.components.OnboardingScreen
import com.dev2drop.cleanring.ui.components.PremiumStatusDialog
import com.dev2drop.cleanring.ui.components.RuleItemCard
import com.dev2drop.cleanring.ui.components.StatusCard
import com.dev2drop.cleanring.ui.components.SubscriptionScreen
import com.dev2drop.cleanring.ui.components.WeeklySummaryCard
import com.dev2drop.cleanring.ui.theme.CyberBackground
import com.dev2drop.cleanring.ui.theme.CyberBorder
import com.dev2drop.cleanring.ui.theme.CyberSurface
import com.dev2drop.cleanring.ui.theme.CyberSurfaceVariant
import com.dev2drop.cleanring.ui.theme.ElectricBlue
import com.dev2drop.cleanring.ui.theme.ElectricBlueLight
import com.dev2drop.cleanring.ui.theme.IconBlueContainer
import com.dev2drop.cleanring.ui.theme.IconOrange
import com.dev2drop.cleanring.ui.theme.IconOrangeContainer
import com.dev2drop.cleanring.ui.theme.IconPurple
import com.dev2drop.cleanring.ui.theme.IconPurpleContainer
import com.dev2drop.cleanring.ui.theme.NeonEmerald
import com.dev2drop.cleanring.ui.theme.NeonEmeraldDark
import com.dev2drop.cleanring.ui.theme.StatGreen
import com.dev2drop.cleanring.ui.theme.StatGreenContainer
import com.dev2drop.cleanring.ui.theme.StatRed
import com.dev2drop.cleanring.ui.theme.StatRedContainer
import com.dev2drop.cleanring.util.AppTranslations
import com.dev2drop.cleanring.util.PhoneNumberNormalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpamBlockerHomeScreen(
    uiState: SpamBlockerUiState,
    onRequestRole: () -> Unit,
    onCompleteOnboarding: (firstRulePattern: String, countryCode: String) -> Unit,
    onReopenOnboarding: () -> Unit,
    onOpenPaywall: () -> Unit,
    onClosePaywall: () -> Unit,
    onStartSubscriptionFlow: (isYearly: Boolean) -> Unit,
    onSelectTab: (Int) -> Unit,
    onSetRuleFilter: (String) -> Unit,
    onToggleEmergencyDisable: () -> Unit,
    onToggleAllowContacts: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onExportRules: () -> Unit,
    onImportRules: () -> Unit,
    onOpenAddDialog: (prefillPattern: String, prefillType: String) -> Unit,
    onOpenEditDialog: (RuleEntity) -> Unit,
    onCloseAddEditDialog: () -> Unit,
    onAddOrUpdateRule: (pattern: String, type: String, action: String, label: String, countryCode: String) -> Unit,
    onToggleRuleStatus: (id: Long, isEnabled: Boolean) -> Unit,
    onDeleteRule: (RuleEntity) -> Unit,
    onClearCallLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val countryIso = remember { PhoneNumberNormalizer.detectCountryIso(context) }
    val countryFlagEmoji = remember(countryIso) { PhoneNumberNormalizer.countryIsoToFlagEmoji(countryIso) }
    val lang = uiState.selectedLanguage

    var isPremiumStatusDialogOpen by remember { mutableStateOf(false) }

    fun tr(key: String): String = AppTranslations.getString(key, lang)

    if (!uiState.isOnboardingCompleted) {
        OnboardingScreen(
            isDefaultRole = uiState.isDefaultRole,
            onRequestRole = onRequestRole,
            onCompleteOnboarding = onCompleteOnboarding,
            isRevisit = uiState.rules.isNotEmpty(),
            lang = lang,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MainDashboardHeader(
                countryFlagEmoji = countryFlagEmoji,
                isTrialActive = uiState.isTrialActive,
                isSubscribed = uiState.isSubscribed,
                trialDaysRemaining = uiState.trialDaysRemaining,
                onOpenPaywall = onOpenPaywall,
                onOpenPremiumStatus = { isPremiumStatusDialogOpen = true },
                onOpenSettings = { onSelectTab(3) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    icon = { Icon(Icons.Outlined.Shield, contentDescription = "Blocklist") },
                    label = { Text("Blocklist") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 3,
                    onClick = { onSelectTab(3) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "More") },
                    label = { Text("More") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        floatingActionButton = {
            if (uiState.selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { onOpenAddDialog("", "PREFIX") },
                    icon = { Icon(Icons.Default.Add, contentDescription = tr("add_rule")) },
                    text = { Text(tr("add_rule"), fontWeight = FontWeight.SemiBold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Emergency Disable Banner
                if (uiState.isEmergencyDisabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tr("emergency_pause"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = tr("emergency_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Button(
                                onClick = onToggleEmergencyDisable,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(tr("resume"), fontSize = 12.sp)
                            }
                        }
                    }
                }

                when (uiState.selectedTab) {
                    0 -> MainHomeDashboard(
                        uiState = uiState,
                        onRequestRole = onRequestRole,
                        onOpenAddDialog = { onOpenAddDialog("", "PREFIX") },
                        onSelectTab = onSelectTab
                    )
                    1 -> RulesTabContent(
                        uiState = uiState,
                        lang = lang,
                        onRequestRole = onRequestRole,
                        onSetRuleFilter = onSetRuleFilter,
                        onToggleRuleStatus = onToggleRuleStatus,
                        onOpenEditDialog = onOpenEditDialog,
                        onDeleteRule = onDeleteRule
                    )
                    2 -> HistoryTabContent(
                        logs = uiState.callLogs,
                        lang = lang,
                        onOneTapBlock = { phoneNumber, type ->
                            onOpenAddDialog(phoneNumber, type)
                        },
                        onClearHistory = onClearCallLogs
                    )
                    3 -> SettingsTabContent(
                        uiState = uiState,
                        lang = lang,
                        onRequestRole = onRequestRole,
                        onToggleEmergencyDisable = onToggleEmergencyDisable,
                        onReopenOnboarding = onReopenOnboarding,
                        onOpenPaywall = onOpenPaywall,
                        onOpenPremiumStatus = { isPremiumStatusDialogOpen = true },
                        onSelectLanguage = onSelectLanguage,
                        onExportRules = onExportRules,
                        onImportRules = onImportRules
                    )
                }
            }
        }
    }

    if (uiState.isAddEditDialogOpen) {
        AddEditRuleDialog(
            initialRule = uiState.editingRule,
            prefillPattern = uiState.prefillPattern,
            prefillType = uiState.prefillType,
            existingRules = uiState.rules,
            lang = lang,
            onDismissRequest = onCloseAddEditDialog,
            onConfirm = { pattern, type, action, label, countryCode ->
                onAddOrUpdateRule(pattern, type, action, label, countryCode)
            }
        )
    }

    if (uiState.isPaywallOpen) {
        SubscriptionScreen(
            trialDaysRemaining = uiState.trialDaysRemaining,
            isTrialActive = uiState.isTrialActive,
            monthlyPriceFormatted = uiState.monthlyPriceFormatted,
            yearlyPriceFormatted = uiState.yearlyPriceFormatted,
            onStartTrialOrSubscribe = { isYearly ->
                onStartSubscriptionFlow(isYearly)
            },
            onRestorePurchases = {
                onClosePaywall()
            },
            onDismiss = onClosePaywall,
            lang = lang
        )
    }

    if (isPremiumStatusDialogOpen) {
        PremiumStatusDialog(
            onDismissRequest = { isPremiumStatusDialogOpen = false }
        )
    }
}

@Composable
private fun MainDashboardHeader(
    countryFlagEmoji: String,
    isTrialActive: Boolean,
    isSubscribed: Boolean,
    trialDaysRemaining: Int,
    onOpenPaywall: () -> Unit,
    onOpenPremiumStatus: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Shield-Phone Logo
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Clean ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ring",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "No Spam. Just Calls You Want.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subscription Status Chip
        if (isSubscribed) {
            Surface(
                onClick = onOpenPremiumStatus,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑 PREMIUM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Surface(
                onClick = onOpenPaywall,
                shape = RoundedCornerShape(12.dp),
                color = if (isTrialActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.dp, if (isTrialActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isTrialActive) Icons.Default.Star else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isTrialActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTrialActive) "${trialDaysRemaining}d Trial" else "🚨 Trial Expired",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MainHomeDashboard(
    uiState: SpamBlockerUiState,
    onRequestRole: () -> Unit,
    onOpenAddDialog: () -> Unit,
    onSelectTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item(key = "status_ring") {
            CentralShieldStatusRing(
                isProtected = uiState.isDefaultRole && !uiState.isEmergencyDisabled,
                blockedCountTotal = uiState.totalBlockedCount,
                blockedCountToday = uiState.todayBlockedCount,
                onRingClick = onRequestRole
            )
        }

        item(key = "featured_prefix") {
            FeaturedPrefixBanner(onClick = onOpenAddDialog)
        }

        item(key = "quick_actions") {
            QuickActionGrid(
                onSpamPrefixesClick = { onSelectTab(1) },
                onRecentCallsClick = { onSelectTab(2) },
                onCallerIdClick = onRequestRole,
                onSettingsClick = { onSelectTab(3) }
            )
        }

        item(key = "bottom_wave") {
            BottomDecorativeWave()
        }
    }
}

@Composable
private fun CentralShieldStatusRing(
    isProtected: Boolean,
    blockedCountTotal: Int,
    blockedCountToday: Int,
    onRingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Flank Stat (Spam Calls Blocked)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = StatRedContainer,
                border = BorderStroke(1.dp, StatRed.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = StatRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$blockedCountTotal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Spam Calls\nBlocked",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Center Ring Shield Dashboard
        Box(
            modifier = Modifier
                .size(190.dp)
                .clickable { onRingClick() },
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor,
                            secondaryColor,
                            primaryColor
                        )
                    ),
                    radius = radius,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = if (isProtected) StatGreenContainer else StatRedContainer,
                    border = BorderStroke(1.5.dp, if (isProtected) MaterialTheme.colorScheme.primary else StatRed)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = if (isProtected) MaterialTheme.colorScheme.primary else StatRed,
                            modifier = Modifier.size(42.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isProtected) "Protected" else "Disabled",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Spam Calls Blocked",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Right Flank Stat (Calls Blocked Today)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = StatGreenContainer,
                border = BorderStroke(1.dp, StatGreen.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$blockedCountToday",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Calls\nBlocked Today",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeaturedPrefixBanner(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = StatGreenContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Blocking Spam by Prefix",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "You add the prefixes, we block the rest.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionGrid(
    onSpamPrefixesClick: () -> Unit,
    onRecentCallsClick: () -> Unit,
    onCallerIdClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Card 1: Spam Prefixes (Red Icon)
            QuickActionCard(
                title = "Spam Prefixes",
                subtitle = "Manage blocked\nnumber prefixes",
                icon = Icons.Default.PhoneDisabled,
                iconColor = StatRed,
                iconContainerColor = StatRedContainer,
                modifier = Modifier.weight(1f),
                onClick = onSpamPrefixesClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Card 2: Recent Calls (Blue Icon)
            QuickActionCard(
                title = "Recent Calls",
                subtitle = "View blocked\nspam calls",
                icon = Icons.Default.FormatListBulleted,
                iconColor = ElectricBlueLight,
                iconContainerColor = IconBlueContainer,
                modifier = Modifier.weight(1f),
                onClick = onRecentCallsClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Card 3: Caller ID (Purple Icon)
            QuickActionCard(
                title = "Caller ID",
                subtitle = "Default caller ID\nfor unknown calls",
                icon = Icons.Default.Security,
                iconColor = IconPurple,
                iconContainerColor = IconPurpleContainer,
                modifier = Modifier.weight(1f),
                onClick = onCallerIdClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Card 4: Settings (Orange Icon)
            QuickActionCard(
                title = "Settings",
                subtitle = "Customize your\nprotection",
                icon = Icons.Default.Settings,
                iconColor = IconOrange,
                iconContainerColor = IconOrangeContainer,
                modifier = Modifier.weight(1f),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconContainerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = iconContainerColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BottomDecorativeWave() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Back Dark Mountain Wave Path
            val backWave = Path().apply {
                moveTo(0f, height * 0.5f)
                cubicTo(
                    width * 0.25f, height * 0.2f,
                    width * 0.6f, height * 0.8f,
                    width, height * 0.4f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = backWave,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00382B).copy(alpha = 0.6f),
                        Color(0xFF071019)
                    )
                )
            )

            // Front Neon Mountain Wave Path
            val frontWave = Path().apply {
                moveTo(0f, height * 0.7f)
                cubicTo(
                    width * 0.35f, height * 0.4f,
                    width * 0.75f, height * 0.9f,
                    width, height * 0.6f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = frontWave,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00FF88).copy(alpha = 0.25f),
                        Color(0xFF00BFA5).copy(alpha = 0.05f)
                    )
                )
            )

            val frontWaveStroke = Path().apply {
                moveTo(0f, height * 0.7f)
                cubicTo(
                    width * 0.35f, height * 0.4f,
                    width * 0.75f, height * 0.9f,
                    width, height * 0.6f
                )
            }
            drawPath(
                path = frontWaveStroke,
                color = Color(0xFF00FF88).copy(alpha = 0.7f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Less Spam.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = Color(0xFF00FF88)
            )
            Text(
                text = "More Peace.",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = Color(0xFF00FF88)
            )
        }
    }
}

@Composable
private fun RulesTabContent(
    uiState: SpamBlockerUiState,
    lang: String,
    onRequestRole: () -> Unit,
    onSetRuleFilter: (String) -> Unit,
    onToggleRuleStatus: (id: Long, isEnabled: Boolean) -> Unit,
    onOpenEditDialog: (RuleEntity) -> Unit,
    onDeleteRule: (RuleEntity) -> Unit
) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
    ) {
        item(key = "status_card") {
            StatusCard(
                isDefaultRole = uiState.isDefaultRole,
                activeRulesCount = uiState.rules.count { it.isEnabled },
                blockedCallsCount = uiState.totalBlockedCount,
                screenedCallsCount = uiState.totalScreenedCount,
                modifier = Modifier.padding(bottom = 12.dp),
                lang = lang,
                onEnableProtectionClick = onRequestRole
            )
        }

        item(key = "weekly_summary") {
            WeeklySummaryCard(
                weeklyBlockedCount = uiState.weeklyBlockedCount,
                lang = lang,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        item(key = "rule_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedRuleFilter == "ALL",
                    onClick = { onSetRuleFilter("ALL") },
                    label = { Text("${tr("filter_all")} (${uiState.rules.size})") },
                    modifier = Modifier.padding(end = 6.dp)
                )
                FilterChip(
                    selected = uiState.selectedRuleFilter == "BLOCK",
                    onClick = { onSetRuleFilter("BLOCK") },
                    label = { Text(tr("filter_block")) },
                    modifier = Modifier.padding(end = 6.dp)
                )
                FilterChip(
                    selected = uiState.selectedRuleFilter == "ALLOW",
                    onClick = { onSetRuleFilter("ALLOW") },
                    label = { Text(tr("filter_allow")) },
                    modifier = Modifier.padding(end = 6.dp)
                )
                FilterChip(
                    selected = uiState.selectedRuleFilter == "PAUSED",
                    onClick = { onSetRuleFilter("PAUSED") },
                    label = { Text(tr("filter_paused")) }
                )
            }
        }

        if (uiState.rules.isEmpty()) {
            item(key = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = tr("no_rules"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tr("no_rules_desc"),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(
                uiState.rules,
                key = { it.id }
            ) { rule ->
                RuleItemCard(
                    rule = rule,
                    onToggleStatus = { isEnabled -> onToggleRuleStatus(rule.id, isEnabled) },
                    onEdit = { onOpenEditDialog(rule) },
                    onDelete = { onDeleteRule(rule) }
                )
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    uiState: SpamBlockerUiState,
    lang: String,
    onRequestRole: () -> Unit,
    onToggleEmergencyDisable: () -> Unit,
    onReopenOnboarding: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenPremiumStatus: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onExportRules: () -> Unit,
    onImportRules: () -> Unit
) {
    fun tr(key: String): String = AppTranslations.getString(key, lang)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Exclusive Active Premium Member Status Card
        if (uiState.isSubscribed) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onOpenPremiumStatus() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👑", fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Clean Ring Premium Active",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Thank you for supporting Clean Ring! Unlimited protection is active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            StatusCard(
                isDefaultRole = uiState.isDefaultRole,
                activeRulesCount = uiState.rules.count { it.isEnabled },
                blockedCallsCount = uiState.totalBlockedCount,
                screenedCallsCount = uiState.totalScreenedCount,
                modifier = Modifier.padding(bottom = 20.dp),
                lang = lang,
                onEnableProtectionClick = onRequestRole
            )
        }

        // Language Preference Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Language",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select your preferred language",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        var langMenuExpanded by remember { mutableStateOf(false) }
                        val languages = listOf(
                            "en" to "🇺🇸 English",
                            "hi" to "🇮🇳 हिन्दी",
                            "es" to "🇪🇸 Español",
                            "fr" to "🇫🇷 Français",
                            "de" to "🇩🇪 Deutsch",
                            "ja" to "🇯🇵 日本語",
                            "zh" to "🇨🇳 中文",
                            "ar" to "🇸🇦 العربية",
                            "pt" to "🇧🇷 Português",
                            "ru" to "🇷🇺 Русский"
                        )
                        val activeLangName = languages.firstOrNull { it.first == uiState.selectedLanguage }?.second ?: "🇺🇸 English"

                        Box {
                            OutlinedButton(
                                onClick = { langMenuExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(activeLangName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            DropdownMenu(
                                expanded = langMenuExpanded,
                                onDismissRequest = { langMenuExpanded = false }
                            ) {
                                languages.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = { Text(name, fontSize = 13.sp) },
                                        onClick = {
                                            langMenuExpanded = false
                                            onSelectLanguage(code)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔒 Backup & Restore Rules Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Local Backup & Restore Rules",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Save your blocked numbers/rules list to a local file or restore them if you reinstall the app. 100% private.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onExportRules,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Backup", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedButton(
                            onClick = onImportRules,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Rules", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = tr("emergency_controls"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("emergency_master"), fontWeight = FontWeight.SemiBold)
                            Text(tr("emergency_master_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.isEmergencyDisabled,
                            onCheckedChange = { onToggleEmergencyDisable() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.error,
                                checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = tr("privacy_guarantee"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr("privacy_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onReopenOnboarding,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("revisit_guide"))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tr("features_title"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val features = listOf(
                        tr("features_bullet_prefix"),
//                        tr("features_bullet_country"),
//                        tr("features_bullet_normalization"),
                        tr("features_bullet_validation"),
                        tr("features_bullet_neighbor"),
                        tr("features_bullet_false_positive")
                    )

                    features.forEachIndexed { index, feature ->
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (index < features.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}
