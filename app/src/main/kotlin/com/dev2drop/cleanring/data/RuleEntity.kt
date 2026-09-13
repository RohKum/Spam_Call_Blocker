package com.dev2drop.cleanring.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object RuleType {
    const val PREFIX = "PREFIX"
    const val EXACT = "EXACT"
}

object RuleAction {
    const val BLOCK = "BLOCK"
    const val ALLOW = "ALLOW"
}

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "pattern")
    val pattern: String,

    @ColumnInfo(name = "type")
    val type: String = RuleType.PREFIX,

    @ColumnInfo(name = "action")
    val action: String = RuleAction.BLOCK,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "label")
    val label: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "blocked_count")
    val blockedCount: Int = 0,

    @ColumnInfo(name = "expiry_timestamp")
    val expiryTimestamp: Long = 0,

    @ColumnInfo(name = "schedule_days")
    val scheduleDays: String = "",

    @ColumnInfo(name = "schedule_start_hour")
    val scheduleStartHour: Int = -1,

    @ColumnInfo(name = "schedule_end_hour")
    val scheduleEndHour: Int = -1,

    @ColumnInfo(name = "country_code")
    val countryCode: String = ""
)
