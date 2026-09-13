package com.dev2drop.cleanring.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class SpamBlockerRepository(
    private val context: Context,
    private val ruleDao: RuleDao,
    private val callLogDao: CallLogDao,
    private val prefixDao: PrefixDao
) {
    private val sharedPrefs = context.getSharedPreferences("spam_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    val allRulesFlow: Flow<List<RuleEntity>> = ruleDao.getAllRulesFlow().map { rules ->
        syncActiveRulesToPrefs(rules.filter { it.isEnabled })
        rules
    }

    val recentCallLogsFlow: Flow<List<CallLogEntity>> = callLogDao.getRecentLogsFlow(100)

    fun getAppLanguage(): String {
        return sharedPrefs.getString("app_language", "en") ?: "en"
    }

    fun setAppLanguage(langCode: String) {
        sharedPrefs.edit().putString("app_language", langCode).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean("onboarding_completed", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun isEmergencyDisabled(): Boolean {
        return sharedPrefs.getBoolean("emergency_disabled", false)
    }

    fun setEmergencyDisabled(disabled: Boolean) {
        sharedPrefs.edit().putBoolean("emergency_disabled", disabled).apply()
    }

    fun isAllowContactsEnabled(): Boolean {
        return sharedPrefs.getBoolean("allow_contacts", true)
    }

    fun setAllowContactsEnabled(allow: Boolean) {
        sharedPrefs.edit().putBoolean("allow_contacts", allow).apply()
    }

    fun getFirstInstallTime(): Long {
        if (!sharedPrefs.contains("first_install_time")) {
            sharedPrefs.edit().putLong("first_install_time", System.currentTimeMillis()).apply()
        }
        return sharedPrefs.getLong("first_install_time", System.currentTimeMillis())
    }

    fun isTrialActive(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val installTime = getFirstInstallTime()
        return currentTimeMs < (installTime + (14L * 24 * 3600 * 1000))
    }

    fun getTrialDaysRemaining(currentTimeMs: Long = System.currentTimeMillis()): Int {
        val installTime = getFirstInstallTime()
        val totalMs = 14L * 24 * 3600 * 1000
        val remainingMs = totalMs - (currentTimeMs - installTime)
        if (remainingMs <= 0) return 0
        return ((remainingMs / (24L * 3600 * 1000)) + 1).toInt().coerceAtMost(14)
    }

    fun isSubscribed(): Boolean {
        return sharedPrefs.getBoolean("is_subscribed", false)
    }

    fun setSubscribed(subscribed: Boolean) {
        sharedPrefs.edit().putBoolean("is_subscribed", subscribed).apply()
    }

    suspend fun exportAllRulesToJson(): String = withContext(Dispatchers.IO) {
        val allRules = ruleDao.getAllRulesSync()
        gson.toJson(allRules)
    }

    suspend fun importRulesFromJson(jsonContent: String): Int = withContext(Dispatchers.IO) {
        if (jsonContent.isBlank()) return@withContext 0
        try {
            val type = object : TypeToken<List<RuleEntity>>() {}.type
            val importedRules: List<RuleEntity> = gson.fromJson(jsonContent, type) ?: emptyList()
            var count = 0
            val existingRules = ruleDao.getAllRulesSync()
            val existingPatterns = existingRules.map { it.pattern.replace(Regex("[^0-9+]"), "") }.toSet()

            for (rule in importedRules) {
                val cleanPattern = rule.pattern.replace(Regex("[^0-9+]"), "")
                if (cleanPattern.isNotBlank() && !existingPatterns.contains(cleanPattern)) {
                    ruleDao.insertRule(
                        RuleEntity(
                            pattern = rule.pattern,
                            type = rule.type,
                            action = rule.action,
                            isEnabled = rule.isEnabled,
                            label = rule.label,
                            countryCode = rule.countryCode
                        )
                    )
                    count++
                }
            }
            if (count > 0) {
                refreshCache()
            }
            count
        } catch (_: Exception) {
            0
        }
    }

    suspend fun addRule(
        pattern: String,
        type: String = RuleType.PREFIX,
        action: String = RuleAction.BLOCK,
        label: String = "",
        countryCode: String = "",
        expiryTimestamp: Long = 0,
        scheduleDays: String = "",
        scheduleStartHour: Int = -1,
        scheduleEndHour: Int = -1
    ): Boolean = withContext(Dispatchers.IO) {
        val trimmed = pattern.trim()
        if (trimmed.isEmpty()) return@withContext false

        val entity = RuleEntity(
            pattern = trimmed,
            type = type,
            action = action,
            isEnabled = true,
            label = label.trim(),
            countryCode = countryCode.trim(),
            expiryTimestamp = expiryTimestamp,
            scheduleDays = scheduleDays,
            scheduleStartHour = scheduleStartHour,
            scheduleEndHour = scheduleEndHour
        )
        val rowId = ruleDao.insertRule(entity)
        if (rowId > 0) {
            refreshCache()
            true
        } else {
            false
        }
    }

    suspend fun updateRule(rule: RuleEntity): Boolean = withContext(Dispatchers.IO) {
        val rows = ruleDao.updateRule(rule)
        if (rows > 0) {
            refreshCache()
            true
        } else {
            false
        }
    }

    suspend fun toggleRuleStatus(id: Long, isEnabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val rows = ruleDao.toggleRuleStatus(id, isEnabled)
        if (rows > 0) {
            refreshCache()
            true
        } else {
            false
        }
    }

    suspend fun duplicateRule(rule: RuleEntity): Boolean = withContext(Dispatchers.IO) {
        val duplicate = rule.copy(
            id = 0,
            label = if (rule.label.isNotBlank()) "${rule.label} (Copy)" else "Copy",
            createdAt = System.currentTimeMillis()
        )
        val rowId = ruleDao.insertRule(duplicate)
        if (rowId > 0) {
            refreshCache()
            true
        } else {
            false
        }
    }

    suspend fun deleteRule(id: Long): Boolean = withContext(Dispatchers.IO) {
        val rows = ruleDao.deleteRule(id)
        if (rows > 0) {
            refreshCache()
            true
        } else {
            false
        }
    }

    suspend fun incrementRuleBlockCount(pattern: String) = withContext(Dispatchers.IO) {
        if (pattern.isNotBlank()) {
            ruleDao.incrementBlockCountByPattern(pattern)
            refreshCache()
        }
    }

    suspend fun logCallEvent(
        phoneNumber: String,
        action: String,
        matchedRuleType: String = "",
        matchedPattern: String = ""
    ) = withContext(Dispatchers.IO) {
        val log = CallLogEntity(
            phoneNumber = phoneNumber,
            action = action,
            matchedRuleType = matchedRuleType,
            matchedPattern = matchedPattern,
            timestamp = System.currentTimeMillis()
        )
        callLogDao.insertLog(log)
    }

    suspend fun clearCallLogs() = withContext(Dispatchers.IO) {
        callLogDao.clearAllLogs()
    }

    fun getActiveRulesFast(): List<RuleEntity> {
        val json = sharedPrefs.getString("active_rules_json", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<RuleEntity>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getWeeklyBlockedCount(logs: List<CallLogEntity>): Int {
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000)
        return logs.count { it.action == LogAction.BLOCKED && it.timestamp >= sevenDaysAgo }
    }

    fun getTodayBlockedCount(logs: List<CallLogEntity>): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis
        return logs.count { it.action == LogAction.BLOCKED && it.timestamp >= startOfToday }
    }

    fun getTodayScreenedCount(logs: List<CallLogEntity>): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis
        return logs.count { it.timestamp >= startOfToday }
    }

    fun getTotalBlockedCount(logs: List<CallLogEntity>, rules: List<RuleEntity>): Int {
        val countFromLogs = logs.count { it.action == LogAction.BLOCKED }
        val countFromRules = rules.sumOf { it.blockedCount }
        return maxOf(countFromLogs, countFromRules)
    }

    private suspend fun refreshCache() {
        val activeRules = ruleDao.getActiveRulesSync()
        syncActiveRulesToPrefs(activeRules)
    }

    private fun syncActiveRulesToPrefs(activeRules: List<RuleEntity>) {
        val json = gson.toJson(activeRules)
        val legacyPrefixes = activeRules.filter { it.action == RuleAction.BLOCK }.joinToString(",") { it.pattern }
        sharedPrefs.edit()
            .putString("active_rules_json", json)
            .putString("blocked_prefixes", legacyPrefixes)
            .apply()
    }

    suspend fun initializeCache() = withContext(Dispatchers.IO) {
        refreshCache()
    }
}
