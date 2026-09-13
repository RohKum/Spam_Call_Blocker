package com.dev2drop.cleanring.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PrefixRepository(
    private val context: Context,
    private val prefixDao: PrefixDao
) {
    private val sharedPrefs = context.getSharedPreferences("spam_prefs", Context.MODE_PRIVATE)

    val allPrefixesFlow: Flow<List<String>> = prefixDao.getAllPrefixesFlow().map { entities ->
        val list = entities.map { it.prefix }
        syncToPrefs(list)
        list
    }

    suspend fun addPrefix(prefix: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = prefix.trim()
        if (trimmed.isEmpty()) return@withContext false
        val rowId = prefixDao.insertPrefix(PrefixEntity(prefix = trimmed))
        if (rowId > 0) {
            val updated = prefixDao.getAllPrefixesSync()
            syncToPrefs(updated)
            true
        } else {
            false
        }
    }

    suspend fun removePrefix(prefix: String): Boolean = withContext(Dispatchers.IO) {
        val rowsDeleted = prefixDao.deletePrefix(prefix.trim())
        if (rowsDeleted > 0) {
            val updated = prefixDao.getAllPrefixesSync()
            syncToPrefs(updated)
            true
        } else {
            false
        }
    }

    fun getBlockedPrefixesFast(): List<String> {
        val raw = sharedPrefs.getString("blocked_prefixes", "") ?: ""
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun syncToPrefs(prefixes: List<String>) {
        val joined = prefixes.joinToString(",")
        sharedPrefs.edit().putString("blocked_prefixes", joined).apply()
    }

    suspend fun initializeCache() = withContext(Dispatchers.IO) {
        val prefixes = prefixDao.getAllPrefixesSync()
        syncToPrefs(prefixes)
    }
}
