package com.dev2drop.cleanring.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrefixDao {
    @Query("SELECT * FROM prefixes ORDER BY _id ASC")
    fun getAllPrefixesFlow(): Flow<List<PrefixEntity>>

    @Query("SELECT prefix FROM prefixes")
    suspend fun getAllPrefixesSync(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrefix(entity: PrefixEntity): Long

    @Query("DELETE FROM prefixes WHERE prefix = :prefix")
    suspend fun deletePrefix(prefix: String): Int

    @Query("SELECT COUNT(*) FROM prefixes WHERE prefix = :prefix")
    suspend fun exists(prefix: String): Int
}
