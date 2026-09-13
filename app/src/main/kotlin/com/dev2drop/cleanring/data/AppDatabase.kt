package com.dev2drop.cleanring.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PrefixEntity::class, RuleEntity::class, CallLogEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prefixDao(): PrefixDao
    abstract fun ruleDao(): RuleDao
    abstract fun callLogDao(): CallLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `rules` (
                        `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pattern` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `is_enabled` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `call_logs` (
                        `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `phone_number` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `matched_rule_type` TEXT NOT NULL,
                        `matched_pattern` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `rules` (`pattern`, `type`, `action`, `is_enabled`, `label`, `created_at`)
                    SELECT `prefix`, 'PREFIX', 'BLOCK', 1, '', CAST(strftime('%s', 'now') AS INTEGER) * 1000
                    FROM `prefixes`
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `rules` ADD COLUMN `blocked_count` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `rules` ADD COLUMN `expiry_timestamp` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `rules` ADD COLUMN `schedule_days` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `rules` ADD COLUMN `schedule_start_hour` INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE `rules` ADD COLUMN `schedule_end_hour` INTEGER NOT NULL DEFAULT -1")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `rules` ADD COLUMN `country_code` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "CleanRing.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
