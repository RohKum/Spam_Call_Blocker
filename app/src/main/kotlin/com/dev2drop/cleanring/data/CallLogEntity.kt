package com.dev2drop.cleanring.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object LogAction {
    const val BLOCKED = "BLOCKED"
    const val ALLOWED = "ALLOWED"
}

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "action")
    val action: String = LogAction.BLOCKED,

    @ColumnInfo(name = "matched_rule_type")
    val matchedRuleType: String = "",

    @ColumnInfo(name = "matched_pattern")
    val matchedPattern: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
