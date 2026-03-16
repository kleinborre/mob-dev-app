package com.sample.calorease.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily Entry Entity for Room Database
 * Stores individual food entries with calories for each user by date.
 *
 * Indices:
 *  - (userId, date) composite — optimises getDailyEntries / getDailyEntriesByDateRange queries
 *  - userId alone — kept for FK lookup performance
 */
@Entity(
    tableName = "daily_entries",
    foreignKeys = [
        ForeignKey(
            entity     = UserEntity::class,
            parentColumns = ["userId"],
            childColumns  = ["userId"],
            onDelete   = ForeignKey.CASCADE   // cascade: orphan entries auto-deleted with user
        )
    ],
    indices = [
        Index(value = ["userId", "date"]), // composite — primary query pattern
        Index(value = ["userId"])           // FK index
    ]
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val entryId: Int = 0,

    val userId: Int,      // Foreign key to users table
    val date: Long,       // Timestamp in milliseconds (start-of-day)
    val foodName: String,
    val calories: Int,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    
    // Remote Sync (Sprint 4 Phase 1)
    val lastUpdated: Long = System.currentTimeMillis() // Epoch timestamp for Last-Write-Wins sync
)
