package com.sample.calorease.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Daily Entry Entity for Room Database
 * Stores individual food entries with calories for each user by date
 */
@Entity(
    tableName = "daily_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val entryId: Int = 0,
    
    val userId: Int, // Foreign key to users table
    val date: Long, // Timestamp in milliseconds
    val foodName: String,
    val calories: Int,
    val mealType: String // "Breakfast", "Lunch", "Dinner", "Snack"
)
