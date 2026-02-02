package com.sample.calorease.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity

/**
 * CalorEase Room Database
 * CRITICAL: Uses persistent storage (NOT inMemoryDatabaseBuilder)
 * Database name: "calorease_db"
 */
@Database(
    entities = [UserEntity::class, DailyEntryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calorieDao(): CalorieDao
}
