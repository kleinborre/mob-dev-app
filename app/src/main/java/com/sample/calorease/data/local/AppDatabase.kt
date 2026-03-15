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
 * CalorEase Room Database — version 13
 *
 * Tables:
 *  - users            (UserEntity)      — authentication + admin flags
 *  - user_stats       (UserStats)       — onboarding profile & goals; 1-to-1 with users
 *  - daily_entries    (DailyEntryEntity)— food log per user per day
 *
 * Schema is 3NF compliant:
 *  - No transitive dependencies within each table
 *  - user_stats references users.userId (FK + CASCADE delete)
 *  - daily_entries references users.userId (FK + CASCADE delete)
 *  - Composite index (userId, date) on daily_entries for optimised range queries
 */
@Database(
    entities      = [UserEntity::class, DailyEntryEntity::class, UserStats::class],
    version       = 13,
    exportSchema  = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calorieDao(): CalorieDao

    companion object {
        /**
         * Migration 12 → 13
         * Changes:
         *  - daily_entries: FK onDelete changed from NO_ACTION to CASCADE
         *  - daily_entries: composite index (userId, date) added
         *
         * Room cannot alter FK constraints in SQLite; we recreate the table.
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with correct FK + indices
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

                // 2. Copy existing data
                db.execSQL("""
                    INSERT INTO `daily_entries_new`
                    SELECT entryId, userId, date, foodName, calories, mealType
                    FROM   `daily_entries`
                """.trimIndent())

                // 3. Drop old table
                db.execSQL("DROP TABLE `daily_entries`")

                // 4. Rename new table
                db.execSQL("ALTER TABLE `daily_entries_new` RENAME TO `daily_entries`")

                // 5. Recreate indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_entries_userId_date` ON `daily_entries` (`userId`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_entries_userId`      ON `daily_entries` (`userId`)")
            }
        }
    }
}
