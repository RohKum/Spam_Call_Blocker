package com.dev2drop.cleanring.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dev2drop.cleanring.SpamBlockerApp
import com.dev2drop.cleanring.billing.BillingManager
import com.dev2drop.cleanring.data.CallLogEntity
import com.dev2drop.cleanring.data.RuleAction
import com.dev2drop.cleanring.data.RuleEntity
import com.dev2drop.cleanring.data.RuleType
import com.dev2drop.cleanring.data.SpamBlockerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class DialogState(
    val isOpen: Boolean = false,
    val rule: RuleEntity? = null,
    val pattern: String = "",
    val type: String = RuleType.PREFIX
)

private data class SubscriptionState(
    val isSubscribed: Boolean = false,
    val isPaywallOpen: Boolean = false,
    val monthlyPriceText: String = "",
    val yearlyPriceText: String = ""
)

private data class SettingsState(
    val isDefaultRole: Boolean = false,
    val isOnboardingCompleted: Boolean = true,
    val isEmergencyDisabled: Boolean = false,
    val isAllowContactsEnabled: Boolean = true,
    val selectedLanguage: String = "en"
)

private data class CombinedState(
    val settings: SettingsState,
    val subscription: SubscriptionState,
    val lastDeletedRule: RuleEntity?,
    val dialog: DialogState
)

data class SpamBlockerUiState(
    val rules: List<RuleEntity> = emptyList(),
    val callLogs: List<CallLogEntity> = emptyList(),
    val weeklyBlockedCount: Int = 0,
    val todayBlockedCount: Int = 0,
    val todayScreenedCount: Int = 0,
    val totalBlockedCount: Int = 0,
    val totalScreenedCount: Int = 0,
    val selectedTab: Int = 0,
    val selectedRuleFilter: String = "ALL",
    val isDefaultRole: Boolean = false,
    val isOnboardingCompleted: Boolean = true,
    val isEmergencyDisabled: Boolean = false,
    val isAllowContactsEnabled: Boolean = true,
    val selectedLanguage: String = "en",
    val isSubscribed: Boolean = false,
    val isTrialActive: Boolean = true,
    val trialDaysRemaining: Int = 14,
    val isPaywallOpen: Boolean = false,
    val monthlyPriceFormatted: String = "",
    val yearlyPriceFormatted: String = "",
    val isAddEditDialogOpen: Boolean = false,
    val editingRule: RuleEntity? = null,
    val prefillPattern: String = "",
    val prefillType: String = RuleType.PREFIX,
    val lastDeletedRule: RuleEntity? = null
)

class SpamBlockerViewModel(
    private val repository: SpamBlockerRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _selectedRuleFilter = MutableStateFlow("ALL")
    private val _isDefaultRole = MutableStateFlow(false)
    private val _isOnboardingCompleted = MutableStateFlow(repository.isOnboardingCompleted())
    private val _isEmergencyDisabled = MutableStateFlow(repository.isEmergencyDisabled())
    private val _isAllowContactsEnabled = MutableStateFlow(repository.isAllowContactsEnabled())
    private val _selectedLanguage = MutableStateFlow(repository.getAppLanguage())
    private val _isSubscribed = MutableStateFlow(repository.isSubscribed())
    private val _isPaywallOpen = MutableStateFlow(false)
    private val _isAddEditDialogOpen = MutableStateFlow(false)
    private val _editingRule = MutableStateFlow<RuleEntity?>(null)
    private val _prefillPattern = MutableStateFlow("")
    private val _prefillType = MutableStateFlow(RuleType.PREFIX)
    private val _lastDeletedRule = MutableStateFlow<RuleEntity?>(null)

    private val billingManager by lazy { SpamBlockerApp.instance.billingManager }

    private val _dialogState = combine(
        _isAddEditDialogOpen,
        _editingRule,
        _prefillPattern,
        _prefillType
    ) { isOpen, rule, pattern, type ->
        DialogState(isOpen, rule, pattern, type)
    }

    private val _subscriptionState = combine(
        _isSubscribed,
        _isPaywallOpen,
        SpamBlockerApp.instance.billingManager.availableSubscriptions
    ) { isSub, isPaywall, subList ->
        val monthly = subList.firstOrNull { it.productId == BillingManager.PRODUCT_MONTHLY }?.formattedPrice ?: ""
        val yearly = subList.firstOrNull { it.productId == BillingManager.PRODUCT_YEARLY }?.formattedPrice ?: ""
        SubscriptionState(isSub, isPaywall, monthly, yearly)
    }

    private val _settingsState = combine(
        _isDefaultRole,
        _isOnboardingCompleted,
        _isEmergencyDisabled,
        _isAllowContactsEnabled,
        _selectedLanguage
    ) { isRole, isOnboarding, isEmergency, isAllow, lang ->
        SettingsState(isRole, isOnboarding, isEmergency, isAllow, lang)
    }

    private val _combinedState = combine(
        _settingsState,
        _subscriptionState,
        _lastDeletedRule,
        _dialogState
    ) { settings, subscription, lastDeleted, dialog ->
        CombinedState(settings, subscription, lastDeleted, dialog)
    }

    val uiState: StateFlow<SpamBlockerUiState> = combine(
        repository.allRulesFlow,
        repository.recentCallLogsFlow,
        _selectedTab,
        _selectedRuleFilter,
        _combinedState
    ) { rules, logs, tab, filter, combined ->
        val settings = combined.settings
        val subscription = combined.subscription
        val lastDeleted = combined.lastDeletedRule
        val dialog = combined.dialog

        val filteredRules = when (filter) {
            "BLOCK" -> rules.filter { it.action == RuleAction.BLOCK }
            "ALLOW" -> rules.filter { it.action == RuleAction.ALLOW }
            "PAUSED" -> rules.filter { !it.isEnabled }
            else -> rules
        }

        val weeklyCount = repository.getWeeklyBlockedCount(logs)
        val todayBlocked = repository.getTodayBlockedCount(logs)
        val todayScreened = repository.getTodayScreenedCount(logs)
        val totalBlocked = repository.getTotalBlockedCount(logs, rules)
        val totalScreened = logs.size

        val trialActive = repository.isTrialActive()
        val trialDays = repository.getTrialDaysRemaining()
        val isSubscribed = subscription.isSubscribed
        val autoPaywallOpen = subscription.isPaywallOpen || (!trialActive && !isSubscribed)

        SpamBlockerUiState(
            rules = filteredRules,
            callLogs = logs,
            weeklyBlockedCount = weeklyCount,
            todayBlockedCount = todayBlocked,
            todayScreenedCount = todayScreened,
            totalBlockedCount = totalBlocked,
            totalScreenedCount = totalScreened,
            selectedTab = tab,
            selectedRuleFilter = filter,
            isDefaultRole = settings.isDefaultRole,
            isOnboardingCompleted = settings.isOnboardingCompleted,
            isEmergencyDisabled = settings.isEmergencyDisabled,
            isAllowContactsEnabled = settings.isAllowContactsEnabled,
            selectedLanguage = settings.selectedLanguage,
            isSubscribed = isSubscribed,
            isTrialActive = trialActive,
            trialDaysRemaining = trialDays,
            isPaywallOpen = autoPaywallOpen,
            monthlyPriceFormatted = subscription.monthlyPriceText,
            yearlyPriceFormatted = subscription.yearlyPriceText,
            isAddEditDialogOpen = dialog.isOpen,
            editingRule = dialog.rule,
            prefillPattern = dialog.pattern,
            prefillType = dialog.type,
            lastDeletedRule = lastDeleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpamBlockerUiState(
            isOnboardingCompleted = repository.isOnboardingCompleted(),
            isSubscribed = repository.isSubscribed(),
            isTrialActive = repository.isTrialActive(),
            trialDaysRemaining = repository.getTrialDaysRemaining()
        )
    )

    fun setAppLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        repository.setAppLanguage(langCode)
        try {
            val localeList = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(localeList)
        } catch (_: Exception) {}
    }

    suspend fun exportRulesJson(): String {
        return repository.exportAllRulesToJson()
    }

    fun importRulesJson(jsonContent: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importRulesFromJson(jsonContent)
            onComplete(count)
        }
    }

    fun completeOnboarding(firstRulePattern: String = "", countryCode: String = "") {
        _isOnboardingCompleted.value = true
        repository.setOnboardingCompleted(true)
        if (firstRulePattern.isNotBlank()) {
            val cleanPattern = firstRulePattern.replace(Regex("[^0-9+]"), "")
            viewModelScope.launch {
                val currentRules = repository.getActiveRulesFast()
                val exists = currentRules.any { rule ->
                    rule.pattern.replace(Regex("[^0-9+]"), "") == cleanPattern
                }
                if (!exists) {
                    repository.addRule(
                        pattern = firstRulePattern,
                        label = "First Rule",
                        countryCode = countryCode
                    )
                }
            }
        }
    }

    fun reopenOnboarding() {
        _isOnboardingCompleted.value = false
    }

    fun openPaywall() {
        _isPaywallOpen.value = true
    }

    fun closePaywall() {
        _isPaywallOpen.value = false
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setRuleFilter(filter: String) {
        _selectedRuleFilter.value = filter
    }

    fun updateRoleStatus(isHeld: Boolean) {
        _isDefaultRole.value = isHeld
    }

    fun toggleEmergencyDisable() {
        val newStatus = !_isEmergencyDisabled.value
        _isEmergencyDisabled.value = newStatus
        repository.setEmergencyDisabled(newStatus)
    }

    fun toggleAllowContacts() {
        val newStatus = !_isAllowContactsEnabled.value
        _isAllowContactsEnabled.value = newStatus
        repository.setAllowContactsEnabled(newStatus)
    }

    fun openAddDialog(prefillPattern: String = "", prefillType: String = RuleType.PREFIX) {
        _editingRule.value = null
        _prefillPattern.value = prefillPattern
        _prefillType.value = prefillType
        _isAddEditDialogOpen.value = true
    }

    fun openEditDialog(rule: RuleEntity) {
        _editingRule.value = rule
        _prefillPattern.value = rule.pattern
        _prefillType.value = rule.type
        _isAddEditDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditDialogOpen.value = false
        _editingRule.value = null
        _prefillPattern.value = ""
    }

    fun addOrUpdateRule(pattern: String, type: String, action: String, label: String, countryCode: String = "") {
        val currentEdit = _editingRule.value
        viewModelScope.launch {
            if (currentEdit != null) {
                val updated = currentEdit.copy(
                    pattern = pattern.trim(),
                    type = type,
                    action = action,
                    label = label.trim(),
                    countryCode = countryCode
                )
                repository.updateRule(updated)
            } else {
                repository.addRule(pattern, type, action, label, countryCode = countryCode)
            }
        }
        closeAddEditDialog()
    }

    fun toggleRuleStatus(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRuleStatus(id, isEnabled)
        }
    }

    fun duplicateRule(rule: RuleEntity) {
        viewModelScope.launch {
            repository.duplicateRule(rule)
        }
    }

    fun deleteRule(rule: RuleEntity) {
        _lastDeletedRule.value = rule
        viewModelScope.launch {
            repository.deleteRule(rule.id)
        }
    }

    fun undoLastDelete() {
        val lastDeleted = _lastDeletedRule.value ?: return
        viewModelScope.launch {
            repository.addRule(
                pattern = lastDeleted.pattern,
                type = lastDeleted.type,
                action = lastDeleted.action,
                label = lastDeleted.label,
                countryCode = lastDeleted.countryCode
            )
            _lastDeletedRule.value = null
        }
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            repository.clearCallLogs()
        }
    }

    class Factory(private val repository: SpamBlockerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SpamBlockerViewModel(repository) as T
        }
    }
}
