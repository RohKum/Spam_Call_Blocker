package com.dev2drop.cleanring.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int = 100): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CallLogEntity): Long

    @Query("DELETE FROM call_logs")
    suspend fun clearAllLogs(): Int
}
