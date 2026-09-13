package com.dev2drop.cleanring.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY _id DESC")
    fun getAllRulesFlow(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules")
    suspend fun getAllRulesSync(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE is_enabled = 1")
    suspend fun getActiveRulesSync(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE _id = :id")
    suspend fun getRuleById(id: Long): RuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity): Long

    @Update
    suspend fun updateRule(rule: RuleEntity): Int

    @Query("DELETE FROM rules WHERE _id = :id")
    suspend fun deleteRule(id: Long): Int

    @Query("UPDATE rules SET is_enabled = :isEnabled WHERE _id = :id")
    suspend fun toggleRuleStatus(id: Long, isEnabled: Boolean): Int

    @Query("UPDATE rules SET blocked_count = blocked_count + 1 WHERE _id = :id")
    suspend fun incrementBlockCount(id: Long): Int

    @Query("UPDATE rules SET blocked_count = blocked_count + 1 WHERE pattern = :pattern")
    suspend fun incrementBlockCountByPattern(pattern: String): Int
}
