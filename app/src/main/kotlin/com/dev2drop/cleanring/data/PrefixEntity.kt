package com.dev2drop.cleanring.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prefixes")
data class PrefixEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "prefix")
    val prefix: String
)
