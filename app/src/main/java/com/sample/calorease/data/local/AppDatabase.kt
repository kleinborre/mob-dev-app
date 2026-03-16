package com.sample.calorease.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats

/**
 * CalorEase Room Database — version 14
 *
 * Tables:
 *  - users            (UserEntity)      — authentication + admin flags + optional googleId
 *  - user_stats       (UserStats)       — onboarding profile & goals; 1-to-1 with users
 *  - daily_entries    (DailyEntryEntity)— food log per user per day
 *
 * Schema is 3NF compliant.
 */
@Database(
    entities      = [UserEntity::class, DailyEntryEntity::class, UserStats::class],
    version       = 16,
    exportSchema  = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calorieDao(): CalorieDao

    companion object {

        /**
         * Migration 12 → 13
         * - daily_entries: FK onDelete changed to CASCADE
         * - daily_entries: composite index (userId, date) added
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_entries_new` (
                        `entryId`   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId`    INTEGER NOT NULL,
                        `date`      INTEGER NOT NULL,
                        `foodName`  TEXT    NOT NULL,
                        `calories`  INTEGER NOT NULL,
                        `mealType`  TEXT    NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`userId`) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `daily_entries_new`
                    SELECT entryId, userId, date, foodName, calories, mealType
                    FROM   `daily_entries`
                """.trimIndent())

                db.execSQL("DROP TABLE `daily_entries`")
                db.execSQL("ALTER TABLE `daily_entries_new` RENAME TO `daily_entries`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_entries_userId_date` ON `daily_entries` (`userId`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_entries_userId`      ON `daily_entries` (`userId`)")
            }
        }

        /**
         * Migration 13 → 14
         * - users: add nullable `googleId` TEXT column for Google OAuth linking
         */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Simple ALTER TABLE — adding a nullable column is always backward safe in SQLite
                db.execSQL("ALTER TABLE `users` ADD COLUMN `googleId` TEXT")
                android.util.Log.d("AppDatabase", "Migration 13→14: added googleId column to users")
            }
        }
        /**
         * Migration 14 → 15
         * - users: add `isEmailVerified` INTEGER default 0 (false)
         */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `users` ADD COLUMN `isEmailVerified` INTEGER NOT NULL DEFAULT 0")
                android.util.Log.d("AppDatabase", "Migration 14→15: added isEmailVerified column to users")
            }
        }
        /**
         * Migration 15 → 16
         * - users: add `lastUpdated` INTEGER default current millis
         * - daily_entries: add `lastUpdated` INTEGER default current millis
         * Required for Sprint 4 Phase 1 remote sync synchronization mappings.
         */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val currentMillis = System.currentTimeMillis()
                db.execSQL("ALTER TABLE `users` ADD COLUMN `lastUpdated` INTEGER NOT NULL DEFAULT $currentMillis")
                db.execSQL("ALTER TABLE `daily_entries` ADD COLUMN `lastUpdated` INTEGER NOT NULL DEFAULT $currentMillis")
                android.util.Log.d("AppDatabase", "Migration 15→16: added lastUpdated columns")
            }
        }
    }
}
